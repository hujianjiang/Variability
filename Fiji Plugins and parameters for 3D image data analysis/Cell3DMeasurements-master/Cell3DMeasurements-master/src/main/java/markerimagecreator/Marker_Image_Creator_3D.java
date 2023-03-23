package markerimagecreator;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import data.Coordinates;
import data.NucleiSegmentationParameters;
import data.PointValue;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.plugin.Duplicator;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import utils.AttenuationAdjuster;
import utils.MaximaFinder3D;
import utils.Nucleus3DFileUtils;

/**
 * The plugin Marker_Image_Creator_3D is able to create marker images with different point detection methods in 3D. The options are: Manual points and Laplacian of Gaussian.
 *
 * @author Esther Markus
 * @author Merijn van Erp
 *
 */
public class Marker_Image_Creator_3D implements PlugIn
{
	private static final String NONE = "None";
	public static final String MANUALPOINTS = "Manual points", LOG = "Laplacian of Gaussian";

	private static final String MARKERFILEPATH_PREF = "Marker_Image_Creator_3D.MarkerFilePath";

	private static final boolean DEBUG = false;


	public static String createMarkerFileName(final ImagePlus aImage, final String aMarkerMethod)
	{
		return aImage.getShortTitle() + "_Markers_" + aMarkerMethod.replace(' ', '_');
	}

	private double[] parameters;

	private String pointDetectionMethod;


	/**
	 * Combine the LoG images based on different sigma sizes into an aggregate image getting the maximum value from the set of images for each pixel.
	 *
	 * @param aListOfLoGImages,
	 *            A list of the LoG images for each different sigma value used
	 *
	 * @return The combined image of the different LoG images
	 */
	private ImagePlus combineLoGIterations(final ArrayList<ImagePlus> aListOfLoGImages)
	{
		final ImagePlus result = aListOfLoGImages.get(0).duplicate();
		final ImageStack resultStack = result.getImageStack();

		// Create an arrayList of imageStacks
		final ArrayList<ImageStack> listOfLoGImagesStack = new ArrayList<>();
		for (int i = 0; i < aListOfLoGImages.size(); i++)
		{
			listOfLoGImagesStack.add(aListOfLoGImages.get(i).getImageStack());
		}

		// Loop through all the pixels and add the highest pixel to the combined image
		for (int z = 0; z < result.getNSlices(); z++)
		{
			for (int x = 0; x < result.getWidth(); x++)
			{
				for (int y = 0; y < result.getHeight(); y++)
				{
					double value = resultStack.getVoxel(x, y, z); // == image 0
					for (int i = 1; i < listOfLoGImagesStack.size(); i++)
					{
						final double altValue = listOfLoGImagesStack.get(i).getVoxel(x, y, z);
						if (value < altValue)
						{
							value = altValue;
						}
					}
					resultStack.setVoxel(x, y, z, value);
				}
			}
		}
		result.show();
		return result;
	}


	/**
	 * Create a string which can be added to name of the marker image and marker file for identification purposes. The identification part is in the sigmas used in the X/Y and Z directions and in a letter identification for the method used: S (sigma)
	 * for the Laplacian of Gaussian and MAX_R (radius) for the Maximafinder.
	 *
	 * @param aSigmaXY
	 *            The sigma/radius used for the X and Y dimensions
	 * @param aSigmaZ
	 *            The sigma/radius used in the Z dimension
	 * @param aAddLoGSizes
	 *            Should the filename contain information on the LoG size?
	 *
	 * @return A string with the additional name of the file
	 */
	private String createAdditionOfFileName(final double aSigmaXY, final double aSigmaZ, final boolean aAddLoGSizes)
	{
		// Create a string for the name the total LoG image
		final String stringSigmaXY = new BigDecimal(aSigmaXY).setScale(2, RoundingMode.HALF_UP).toString();
		final String stringSigmaZ = new BigDecimal(aSigmaZ).setScale(2, RoundingMode.HALF_UP).toString();

		if (aAddLoGSizes)
		{
			return "_SXY" + stringSigmaXY + "-SZ" + stringSigmaZ;
		}

		// Method = MAX
		return "_Max-RXY" + stringSigmaXY + "-RZ" + stringSigmaZ;
	}


	/**
	 * Starts the Laplacian of Gaussian plugin of Daniel Sage (LoG3D), inverts the resulting image and multiplies it by the XYsigma to normalise its values a bit (as larger sigma will typically produce a lower image output).
	 *
	 * @param aOriginalImage
	 *            The original image
	 * @param aDisplayKernel
	 *            Should the kernel be shown
	 * @param aPerSlice
	 *            Calculate per slice (true) or 3D (false)
	 * @param aSigmaXY
	 *            The calculated XY sigma
	 * @param aSigmaZ
	 *            The calculated Z sigma
	 */
	private ImagePlus createLoGImage(final ImagePlus aOriginalImage, final int aDisplayKernel, final boolean aPerSlice, final double aSigmaXY, final double aSigmaZ)
	{
		String LoG3DInput = "";
		if (aSigmaZ > 0)
		{
			LoG3DInput = "sigmax=" + aSigmaXY + " sigmay=" + aSigmaXY + " sigmaz=" + aSigmaZ + " displaykernel=" + aDisplayKernel + " volume=1";
		}
		if (aSigmaZ == 0)
		{
			// Sigma Z should not be 0 unless explicitely set so by the 'per slice' parameter. Otherwise get a minimum of 1.
			LoG3DInput = "sigmax=" + aSigmaXY + " sigmay=" + aSigmaXY + " displaykernel=" + aDisplayKernel + " volume=" + (aPerSlice ? "0" : "1");
		}

		IJ.log("LoG input: " + LoG3DInput);
		// Plugin: LoG3D (Daniel Sage)
		IJ.run("LoG 3D", LoG3DInput);

		// Wait until the new image is displayed
		ImagePlus logImage;
		do
		{
			logImage = IJ.getImage();
		} while (logImage.getTitle().equals(aOriginalImage.getTitle()));

		// Invert the image and add to the list of LoGImages
		IJ.run(logImage, "Divide...", "value=-1.000 stack");

		IJ.run(logImage, "Multiply...", "value=" + aSigmaXY + " stack");

		return logImage;
	}


	/**
	 * Perform the LoG filter and creates the marker image and marker file.
	 *
	 * @param aOriginalImage
	 *            The input for the LoG
	 * @param aTitlemethod,
	 *            To create the name of the marker file and marker image
	 * @param aOutputDirectory,
	 *            Where the marker file and marker image need to be saved
	 */
	private void createLoGMarkers(final ImagePlus aOriginalImage, final String aTitleMethod, final File aOutputDirectory)
	{
		IJ.log("Start Laplacian of Gaussian");
		final long timeStart = System.currentTimeMillis();

		// Dialog to select the parameters for the LoG and maxima finder
		this.parameters = dialogNucleusIdentifierParameters();

		// Extract the parameters which need to be use to determine the Sigma of the Laplacian of Gaussian and the iteration
		final double min = this.parameters[0];
		final double max = this.parameters[1];
		final double step = this.parameters[2];
		final double displayKernel = this.parameters[3];
		final double noise = this.parameters[4];
		final boolean perSlice = this.parameters[5] == 1;
		final double minValue = this.parameters[6];
		double xyRadius = this.parameters[7];
		final boolean showImages = this.parameters[8] == 1;
		final boolean attenuationCorrection = this.parameters[9] == 1;

		if (attenuationCorrection)
		{
			AttenuationAdjuster.adjustAttenuation(aOriginalImage, 1, 90, 10, 3, 1, 255, CurveFitter.EXP_WITH_OFFSET, null);
		}

		// First do some filtering to get rid of noise
		IJ.log("   Start Median filter 3D nucleus image");
		IJ.run(aOriginalImage, "Median (3D)", "");

		ImagePlus originalImageMedian;
		do
		{
			originalImageMedian = IJ.getImage();
		} while (originalImageMedian.getTitle().equals(aOriginalImage.getTitle()));
		originalImageMedian.setTitle(aOriginalImage.getTitle());
		IJ.log("   End Median filter 3D");

		// Get factor for z dimension
		final Calibration cal = aOriginalImage.getCalibration();
		final double factor = cal.pixelDepth / cal.pixelWidth;

		// Variables to save all the different LoG images
		final ArrayList<ImagePlus> listOfLoGImages = new ArrayList<>();
		int count = 0;
		String logFileName = "";
		double minSigmaXY = 100000;
		double minSigmaZ = 100000;

		// For each iteration of the LoG filter
		for (double cellDiameter = min; cellDiameter <= max; cellDiameter = cellDiameter + step)
		{
			final ImagePlus logImage = originalImageMedian.duplicate();
			logImage.show();

			// Calculate the settings of the different LoG filters
			final double radius = (cellDiameter / 2);
			final double sigmaXY = (radius / 3) * 2;
			double sigmaZ = 0;
			if (aOriginalImage.getNSlices() > 1 && !perSlice) // Not per slice, but really 3D
			{
				sigmaZ = (sigmaXY / factor);
			}
			count = count + 1;
			if (sigmaXY < minSigmaXY)
			{
				minSigmaXY = sigmaXY;
			}
			if (sigmaZ < minSigmaZ)
			{
				minSigmaZ = sigmaZ;
			}
			logFileName = logFileName + createAdditionOfFileName(sigmaXY, sigmaZ, true);
			listOfLoGImages.add(createLoGImage(logImage, (int) displayKernel, perSlice, sigmaXY, sigmaZ));
			logImage.close();
		}
		final ImagePlus logCombine = combineLoGIterations(listOfLoGImages);
		IJ.log("   End Laplacian of Gaussian");
		final long timeEndLoG = System.currentTimeMillis();

		// Start the maximum finder
		xyRadius = Math.max(minSigmaXY, xyRadius); // Make sure that the default 0 isn't used
		final List<List<PointValue>> listofslicesWithSeeds = findMaximums(logCombine, (float) minValue, (float) noise, (float) xyRadius, (float) minSigmaZ);
		if (!showImages)
		{
			logCombine.changes = false;
			logCombine.close();
			for (final ImagePlus img : listOfLoGImages)
			{
				img.changes = false;
				img.close();
			}
			originalImageMedian.close();
		}

		// Create a new marker image
		final String nametotal = aTitleMethod + "_n=" + count + logFileName + createAdditionOfFileName(xyRadius, minSigmaZ < 1 ? 1 : minSigmaZ, false);
		createMarkerImage(aOriginalImage, listofslicesWithSeeds, aOutputDirectory, nametotal);
		final long timeEndMaximaFinder = System.currentTimeMillis();

		if (DEBUG)
		{
			debugProcessingTime(timeStart, timeEndLoG, timeEndMaximaFinder);
		}
	}


	/**
	 * This method creates a marker image with a different colour for each seed and a marker file (table) with the XYZ coordinates and colour of the seed.
	 *
	 * @param aOriginalImage
	 *            The original image on which the marker file will be based
	 * @param aListOfSlicesWithSeeds
	 *            List of seed coordinates organised per z-slice
	 * @param aOutputDirectory
	 *            The base output directory (each file will be in its own subdir)
	 * @param aTitle
	 *            The generic part of the file name for both output files
	 */
	private void createMarkerImage(final ImagePlus aOriginalImage, final List<List<PointValue>> aListOfSlicesWithSeeds, final File aOutputDirectory, final String aTitle)
	{
		// Create a new image and results table
		final ImagePlus markerImage = IJ.createImage("Markers", "16-bit black", aOriginalImage.getWidth(), aOriginalImage.getHeight(), aOriginalImage.getNSlices());
		markerImage.show();
		final ResultsTable results = new ResultsTable();

		// For each slide of the image create a processor
		int coloursUsed = 0;
		final ImageStack stack = markerImage.getStack();
		for (int i = 0; i < aListOfSlicesWithSeeds.size(); i++)
		{
			final ImageProcessor markerImageProc = stack.getProcessor(i + 1);

			// For each seed on this slide, make a results table and draw it on the image
			final List<PointValue> listOfSeeds = aListOfSlicesWithSeeds.get(i);
			for (final PointValue seed : listOfSeeds)
			{
				coloursUsed = coloursUsed + 1; // Start with count + 1 as the first one (0) is background
				if (coloursUsed > 65535) // Max numbers of different colours possible
				{
					IJ.log("Error there are too many cells to label them individually");
				}
				else
				{
					final int xValue = (int) seed.getXcoordinate();
					final int yValue = (int) seed.getYcoordinate();
					final int zValue = (int) seed.getZcoordinate();
					final double value = seed.getValue();

					// Add point to results table for the marker file
					results.incrementCounter();
					results.addValue("Label", coloursUsed);
					results.addValue("X", xValue);
					results.addValue("Y", yValue);
					results.addValue("Z", zValue);
					results.addValue("Value", value);

					// Draw on the image
					final Roi roi = new PointRoi(xValue, yValue);
					markerImageProc.setColor(coloursUsed);
					markerImageProc.draw(roi);
				}
			}
		}

		if (coloursUsed <= 65535)
		{
			markerImage.updateAndDraw();
			// Save the image and the table
			saveMarkerFileAndImage(markerImage, results, aOutputDirectory, aTitle);
		}
	}


	/**
	 * Log timing of procedural parts.
	 *
	 * @param aStart
	 *            Start time of the main processing part of the plugin
	 * @param aEndTimeOfLoG
	 *            End of the LoG part of the marker creation
	 * @param aEndTimeOfMaximaFinder
	 *            End time of the maximum finder
	 */
	private void debugProcessingTime(final long aStart, final long aEndTimeOfLoG, final long aEndTimeOfMaximaFinder)
	{
		// Do some time logging
		final long[] time = new long[3];
		time[0] = aEndTimeOfLoG - aStart;
		time[1] = aEndTimeOfMaximaFinder - aEndTimeOfLoG;
		time[2] = aEndTimeOfMaximaFinder - aStart;

		for (int t = 0; t <= 2; t++)
		{
			String timeName = "seconds";
			int timeTotal = (int) (time[t] / 1000) % 60;
			if (timeTotal > 60)
			{
				timeTotal = (int) ((time[t] / (1000 * 60)) % 60);
				timeName = "minuts";
				if (timeTotal > 60)
				{
					timeTotal = (int) ((time[t] / (1000 * 60 * 60)) % 24);
					timeName = "hours";
				}
			}
			if (t == 0)
			{
				IJ.log("Time (" + timeName + ") for LoG: " + timeTotal);
			}
			else if (t == 1)
			{
				IJ.log("Time (" + timeName + ") for maxima finder: " + timeTotal);
			}
			else if (t == 2)
			{
				IJ.log("Time (" + timeName + ") for LoG and maxima finder: " + timeTotal);
			}
		}
	}


	/**
	 * Ask to select different methods for point detection. Repeating asking until it is cancelled.
	 *
	 * @return 'Exit' if the cancel button was chosen, 'Add extra marker images' if yes was chosen and 'Next step' for a choice of no.
	 */
	private boolean dialogMethodSelection(final String[] aMethods) // Dialog for the original image
	{
		final GenericDialog gd = new GenericDialog("Selection point detection method");
		final String methodPref = Prefs.get(NucleiSegmentationParameters.MI_DETECTION_METHOD, aMethods[0]);

		// The input fields for each method to select the method and optional add a extra identity to the name
		gd.addMessage("Select the point detection method\n ");
		gd.addChoice("Method", aMethods, methodPref);

		gd.showDialog();

		final String methodName = gd.getNextChoice();
		Prefs.set(NucleiSegmentationParameters.MI_DETECTION_METHOD, methodName);
		Prefs.savePreferences();

		// When there is an input method, add name to order list and add name+identity to name list
		if (methodName != NONE)
		{
			this.pointDetectionMethod = methodName;

			return gd.wasOKed();
		}

		return false;
	}


	/**
	 * Ask the user to set the settings for the Laplacian of Gaussian algorithm and the subsequent maximum finder.
	 *
	 * @return The list of settings for the LoG and the maximum finder.
	 */
	private double[] dialogNucleusIdentifierParameters()
	{
		final double minSizePref = Prefs.get(NucleiSegmentationParameters.MI_MINIMUM_SIZE, 10);
		final double maxSizePref = Prefs.get(NucleiSegmentationParameters.MI_MAXIMUM_SIZE, 20);
		final double stepSizePref = Prefs.get(NucleiSegmentationParameters.MI_STEPSIZE, 5);
		final double noisePref = Prefs.get(NucleiSegmentationParameters.MI_NOISE, 1);
		final double minLoGPref = Prefs.get(NucleiSegmentationParameters.MI_MINIMUM_LOG_VALUE, 0);
		final double radiusPref = Prefs.get(NucleiSegmentationParameters.MI_XY_RADIUS, 0);

		final boolean perSlicePref = Prefs.get(NucleiSegmentationParameters.MI_PROCESS_PER_SLICE, false);
		final boolean attAdjustmentPref = Prefs.get(NucleiSegmentationParameters.MI_ATTENUATION_ADJUSTMENT, false);

		final GenericDialog dialog = new GenericDialog("Parameters nucleus identifier");
		dialog.addMessage("Parameters for Laplacian of Gaussian");
		dialog.addNumericField("Minimal size of a nucleus", minSizePref, 1);
		dialog.addNumericField("Maximal size of nucleus", maxSizePref, 1);
		dialog.addNumericField("Step size", stepSizePref, 1);
		dialog.addCheckbox("Display LoG Kernel", false);
		dialog.addCheckbox("Process per slice", perSlicePref);
		dialog.addCheckbox("Show intermediate images", false);
		dialog.addCheckbox("Adjust intesity for depth?", attAdjustmentPref);

		dialog.addMessage("Parameters for maxima filter");
		dialog.addNumericField("Noise", noisePref, 3);
		dialog.addNumericField("Minimum LoG value", minLoGPref, 3);
		dialog.addNumericField("Radius XY", radiusPref, 3, 5, "pixel");

		dialog.showDialog();

		final double min = dialog.getNextNumber();
		final double max = dialog.getNextNumber();
		final double step = dialog.getNextNumber();
		final double kernel = dialog.getNextBoolean() ? 1 : 0;
		final double perSlice = dialog.getNextBoolean() ? 1 : 0;
		final double showImages = dialog.getNextBoolean() ? 1 : 0;
		final double attenuationCorrection = dialog.getNextBoolean() ? 1 : 0;

		final double noise = dialog.getNextNumber();
		final double minValue = dialog.getNextNumber();
		final double radius = dialog.getNextNumber();
		final double[] result = { min, max, step, kernel, noise, perSlice, minValue, radius, showImages, attenuationCorrection };

		Prefs.set(NucleiSegmentationParameters.MI_MINIMUM_SIZE, min);
		Prefs.set(NucleiSegmentationParameters.MI_MAXIMUM_SIZE, max);
		Prefs.set(NucleiSegmentationParameters.MI_STEPSIZE, step);
		Prefs.set(NucleiSegmentationParameters.MI_PROCESS_PER_SLICE, perSlice == 1 ? true : false);
		Prefs.set(NucleiSegmentationParameters.MI_ATTENUATION_ADJUSTMENT, attenuationCorrection == 1 ? true : false);
		Prefs.set(NucleiSegmentationParameters.MI_NOISE, noise);
		Prefs.set(NucleiSegmentationParameters.MI_MINIMUM_LOG_VALUE, minValue);
		Prefs.set(NucleiSegmentationParameters.MI_XY_RADIUS, radius);
		Prefs.savePreferences();

		return result;

	}


	/**
	 * Find the maximums in an image in an area of certain radius and noise.
	 *
	 * @param aImage,
	 *            The input image on which the maximums will be sought
	 * @param aMinimum,
	 *            The minimal value for a maximum
	 * @param aNoise,
	 *            The minimum value difference at which a neighbouring pixel is no longer considered as part of the same maximum (ie. not just different by noise)
	 * @param aRadiusXY,
	 *            The XY radius in which just one maximum can be located
	 * @param aRadiusZ,
	 *            The Z radius in which just one maximum can be located
	 *
	 * @return A list which contains for each slice a list of seeds (maximums).
	 */
	private List<List<PointValue>> findMaximums(final ImagePlus aImage, final float aMinimum, final float aNoise, final float aRadiusXY, final float aRadiusZ)
	{
		// Create a maximaFinder and find all maximums
		final MaximaFinder3D maximaFinder = new MaximaFinder3D(aImage, aNoise, aMinimum);
		maximaFinder.setRadii(aRadiusXY, aRadiusXY, Math.max(1, aRadiusZ));

		// Create a list of maximums per slice
		final List<List<PointValue>> listOfSlicesWithSeeds = new ArrayList<>(aImage.getNSlices());
		for (int i = 0; i < aImage.getNSlices(); i++)
		{
			listOfSlicesWithSeeds.add(i, new ArrayList<PointValue>());
		}

		// Get the maximums and put them in the correct slice list
		final ArrayList<PointValue> list = maximaFinder.getListPeaks();
		for (final PointValue pv : list)
		{
			final int z = (int) pv.getZcoordinate();
			listOfSlicesWithSeeds.get(z).add(pv);
		}

		return listOfSlicesWithSeeds;
	}


	/**
	 * Read the manual marker file and create a z-slice divided list of PointValues from the seeds.
	 *
	 * @param aNrOfSlices
	 *            The number of slices in the image
	 * @param aMarkerInputFile
	 *            The manual marker input file
	 *
	 * @return The List of PointValue Lists per slice
	 *
	 * @throws IOException
	 *             If the file gives problems
	 */
	private List<List<PointValue>> readManualPoints(final int aNrOfSlices, final File aMarkerInputFile) throws IOException
	{

		IJ.log("   Start Manual Points");
		// Create a list (n=slices) of list (n= point detection points per slide)
		final List<List<PointValue>> listOfSlices = new ArrayList<>(aNrOfSlices);
		for (int i = 0; i < aNrOfSlices; i++)
		{
			listOfSlices.add(i, new ArrayList<PointValue>());
		}

		final List<Coordinates> seeds = Nucleus3DFileUtils.readNucleusSeeds(aMarkerInputFile);
		for (final Coordinates seed : seeds)
		{
			listOfSlices.get((int) seed.getZcoordinate()).add(new PointValue(seed.getXcoordinate(), seed.getYcoordinate(), seed.getZcoordinate()));
		}

		return listOfSlices;
	}


	/**
	 * Create the marker image and file based on the selected manual markers file.
	 *
	 * @param aArg
	 *            The requisite PlugIn arguments
	 */
	@Override
	public void run(final String aArg)
	{
		// Get the original image and test if it is a 2D or 3D image
		final ImagePlus originalImage = IJ.getImage();

		if (originalImage.getNSlices() == 1)
		{
			IJ.showMessage("Prerequisite error", "The 3D marker images can only be created when the number of slices in the image > 1.");
			return;
		}

		int nucChannel = -1;
		if (originalImage.getNChannels() > 1)
		{
			final String prefNucleusChannel = Prefs.get(NucleiSegmentationParameters.NS_NUCLEUS_CHANNEL, "1");
			// With a multi-channel image, select the current channel to work on. Duplicate it.
			final int channels = originalImage.getNChannels();
			final GenericDialog dialog = new GenericDialog("Set channels");
			final String[] channelchooser = new String[channels];
			for (int i = 1; i <= channels; i++)
			{
				channelchooser[i - 1] = i + "";
			}

			dialog.addMessage("Please select the nucleus channel.");
			dialog.addChoice("Nucleus channel", channelchooser, prefNucleusChannel);
			dialog.showDialog();

			if (dialog.wasCanceled())
			{
				return;
			}
			nucChannel = Integer.parseInt(dialog.getNextChoice());
			Prefs.set(NucleiSegmentationParameters.NS_NUCLEUS_CHANNEL, nucChannel);
			Prefs.savePreferences();
		}

		// Dialog for input to select the different point detection methods 3D, choose from String[] methods.
		final String[] methods = { NONE, MANUALPOINTS, LOG };
		final boolean okayed = dialogMethodSelection(methods);
		if (!okayed || this.pointDetectionMethod == null)
		{
			// Nothing chosen, so ignore
			return;
		}

		// Select the directory where the marker files need to be saved
		final File workDirectory = NucleiSegmentationParameters.getWorkingDirectory();
		if (workDirectory != null)
		{
			final File outputDirectory = new File(workDirectory.getPath() + File.separator + NucleiSegmentationParameters.MI_MARKERS_MAIN_DIR);
			if (!outputDirectory.exists())
			{
				outputDirectory.mkdir();
			}

			// Start making the marker image and file
			IJ.log("Start Create Marker Image 3D");
			try
			{
				// Determine the point detection method and create the marker image name
				final String titlemethod = createMarkerFileName(originalImage, this.pointDetectionMethod);
				if (this.pointDetectionMethod.equals(MANUALPOINTS))
				{
					// Select the directory where the manual annotations of the PointPicker 3D is located
					final File markerInputFile = selectMarkerInputFile();

					if (markerInputFile != null)
					{
						// Create a list (n=slices) of list (n= point detection points per slide)
						final List<List<PointValue>> listofslices = readManualPoints(originalImage.getNSlices(), markerInputFile);
						// Create a new image and save the marker image and the marker file

						createMarkerImage(originalImage, listofslices, outputDirectory, titlemethod);
					}
				}
				else // Laplacian of Gaussian
				{
					// Perform the LoG and save the marker image and marker file
					if (nucChannel > 0)
					{
						final ImagePlus duplicate = new Duplicator().run(originalImage, nucChannel, nucChannel, 1, originalImage.getNSlices(), 1, 1);
						duplicate.show();
						createLoGMarkers(duplicate, titlemethod, outputDirectory);
						duplicate.changes = false;
						duplicate.close();
					}
					else
					{
						createLoGMarkers(originalImage, titlemethod, outputDirectory);
					}
				}
			}
			catch (final IOException ioe)
			{
				IJ.handleException(ioe);
			}
		}

		// Save parameters
		if (this.pointDetectionMethod.equals(LOG))
		{
			final String[] paramNames = { NucleiSegmentationParameters.MI_DETECTION_METHOD, NucleiSegmentationParameters.MI_MINIMUM_SIZE, NucleiSegmentationParameters.MI_MAXIMUM_SIZE,
					NucleiSegmentationParameters.MI_STEPSIZE, NucleiSegmentationParameters.MI_PROCESS_PER_SLICE, NucleiSegmentationParameters.MI_NOISE,
					NucleiSegmentationParameters.MI_MINIMUM_LOG_VALUE, NucleiSegmentationParameters.MI_XY_RADIUS };
			final String[] params = { this.pointDetectionMethod + "", this.parameters[0] + "", this.parameters[1] + "", this.parameters[2] + "", this.parameters[5] + "", this.parameters[4] + "",
					this.parameters[6] + "", this.parameters[7] + "" };
			NucleiSegmentationParameters.writeToParametersFile(paramNames, params, false, workDirectory);
		}

		IJ.log("End Create marker image 3D");
	}


	/**
	 * Save the marker file in the chosen directory in a (newly created if need be) sub-directory named "Marker_File". Also save the Marker image in a (newly created if need be) sub-directory named "Marker_Images".
	 *
	 * @param aMarkerImage
	 *            The image with the markers in it
	 * @param aResults
	 *            The results table containing the marker info
	 * @param aOutputDirectory
	 *            The main output directory
	 * @param aTitle
	 *            The standard part of the output file names
	 */
	private void saveMarkerFileAndImage(final ImagePlus aMarkerImage, final ResultsTable aResults, final File aOutputDirectory, final String aTitle)
	{
		// Save marker file
		final File markerFileDir = new File(aOutputDirectory.getPath() + File.separator + NucleiSegmentationParameters.MI_MARKER_FILES_DIR);
		if (!markerFileDir.exists())
		{
			markerFileDir.mkdir();
		}
		final String markerfilename = File.separator + aTitle + ".txt";
		final String name = markerFileDir.getPath() + markerfilename;
		aResults.save(name);
		IJ.log("   Marker file is save as: " + markerfilename + " in " + name);

		// Save marker image
		final File markerImageDir = new File(aOutputDirectory.getPath() + File.separator + NucleiSegmentationParameters.MI_MARKERS_IMAGE_DIR);
		if (!markerImageDir.exists())
		{
			markerImageDir.mkdir();
		}
		final String imageName = File.separator + aTitle + ".tif";
		final String name1 = markerImageDir.getPath() + imageName;
		IJ.saveAs(aMarkerImage, "Tif", name1);
		IJ.log("   Marker image is save as: " + imageName + " in " + name1);
	}


	/**
	 * Get the input marker file from user selection.
	 *
	 * @return The selected marker input file
	 */
	private File selectMarkerInputFile()
	{
		final String prefInputFile = Prefs.get(MARKERFILEPATH_PREF, null);
		final JFileChooser fileChooser = new JFileChooser(prefInputFile);
		fileChooser.setDialogTitle("Select the file with the manual annotations of the PointPicker 3D");

		final int number = fileChooser.showOpenDialog(IJ.getInstance());
		if (number == JFileChooser.CANCEL_OPTION)
		{
			return null;
		}

		final File file = fileChooser.getSelectedFile();
		Prefs.set(MARKERFILEPATH_PREF, file.getPath());
		Prefs.savePreferences();

		return file;
	}

}