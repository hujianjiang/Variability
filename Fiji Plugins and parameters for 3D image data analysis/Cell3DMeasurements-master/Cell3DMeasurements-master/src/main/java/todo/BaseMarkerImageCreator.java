package todo;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

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
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import utils.MaximaFinder3D;
import utils.Measurer;
import utils.MyMath;
import utils.Nucleus3DFileUtils;

/**
 * The plugin Create_Marker_Image_3D is able to create marker images with different point detection methods in 2D and 3D. The plugin recognize the type of image and switch to 2D or 3D. For the 3D part the options are: Manual points, Manual points
 * translate and Laplacian of Gaussian. For the 2D part the options are: Manual points, LoG, distance transform and watershed points. - LoG: Fiji -> Plugin -> LoG3D (Daniel Sage) - Maxima finder: Fiji -> Plugin -> 3D -> 3D Maxima finder (code is
 * adapted)
 *
 * @author Esther Markus
 * @author Merijn van Erp
 *
 */
abstract public class BaseMarkerImageCreator
{
	public final static String MANUALPOINTS = "Manual points", LAPLACIAN_OF_GAUSSIAN = "Laplacian Of Gaussian", DISTANCETRANSFORM = "Distance transform", WATERSHEDPOINTS = "Watershed points";
	public static final String MARKER_FILE_INFIX = "_Markers_";


	/**
	 * Method createAdditionOfFileName creates a string which can be added to name of the marker image and marker file for identification purposes. The identification part is in the sigmas used in X/Y and Z directions and in a letter identification
	 * for the method used: S (sigma) for the mexican hat and MAX_R (radius) for the Maximafinder.
	 *
	 * @param aSigmaXY
	 *            The sigma/radius used for the X and Y dimensions
	 * @param aSigmaZ
	 *            The sigma/radius used in the Z dimension
	 * @param aAddLoGSizes
	 *            Should the filename contain information on the Mexican Hat size?
	 *
	 * @return A string with the additional name of the file
	 */
	static private String createAdditionOfFileName(final double aSigmaXY, final double aSigmaZ, final boolean aAddLoGSizes)
	{
		// Create a string for the name the total Mexican Hat image
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
	static protected void createMarkerImage(final ImagePlus aOriginalImage, final List<List<PointValue>> aListOfSlicesWithSeeds, final File aOutputDirectory, final String aTitle)
	{
		// Create a new image and results table
		final ImagePlus markerImage = createNewImage(aOriginalImage);
		final ResultsTable results = new ResultsTable();

		// For each slide of the image create a processor
		int count = 0;

		for (int i = 0; i < aListOfSlicesWithSeeds.size(); i++)
		{
			markerImage.setSlice(i + 1);
			final ImageProcessor markerImageProc = markerImage.getProcessor();
			final List<PointValue> listOfSeeds = aListOfSlicesWithSeeds.get(i);
			// For each seed on this slide, make a results table and draw it on the image
			for (final PointValue seed : listOfSeeds)
			{
				count = count + 1; // Start with count + 1 as the first one (0) is background
				if (count > 65536) // Max numbers of different colours possible
				{
					IJ.log("Error there are to many cells to label them individually");
				}
				else
				{
					final int xValue = (int) seed.getXcoordinate();
					final int yValue = (int) seed.getYcoordinate();
					final int zValue = (int) seed.getZcoordinate();
					final double value = seed.getValue();

					// Add point to results table for the marker file
					results.incrementCounter();
					results.addValue("Label", count);
					results.addValue("X", xValue);
					results.addValue("Y", yValue);
					results.addValue("Z", zValue);
					results.addValue("Value", value);

					// Draw on the image
					final Roi roi = new PointRoi(xValue, yValue);
					markerImageProc.setColor(count);
					markerImageProc.draw(roi);
				}
			}
		}

		if (count <= 65536)
		{
			markerImage.updateAndDraw();
			// Save the image and the table
			saveMarkerFileAndImage(markerImage, results, aOutputDirectory, aTitle);
		}
	}


	/**
	 * Method createNewImage creates an empty marker image which have similar size as the original image
	 *
	 * @param aOriginalImage,
	 *            The original image
	 * @return An empty marker image
	 */
	static private ImagePlus createNewImage(final ImagePlus aOriginalImage)
	{
		// Create a new image with the same size of the original image
		final int height = aOriginalImage.getHeight();
		final int width = aOriginalImage.getWidth();
		final ImagePlus markerImage = IJ.createImage("Markers", "16-bit black", width, height, aOriginalImage.getNSlices());
		markerImage.show();
		return markerImage;
	}


	/**
	 * Create a standardised name for the marker file, consisting of the image name and the point selection method.
	 *
	 * @param aImage
	 *            The image, for its name
	 * @param aDetectionMethod
	 *            The detection method: manual or mexican hat
	 *
	 * @return The standardised file name
	 */
	public static String createOutputFileName(final ImagePlus aImage, final String aDetectionMethod)
	{
		return aImage.getShortTitle() + MARKER_FILE_INFIX + aDetectionMethod.replace(' ', '_');
	}


	/**
	 * Dialog MexicanHatSettings asks the user to set the settings for the mexican hat algorithm and the subsequent maximum finder.
	 *
	 * @return The list of settings for the mexican hat and the maximum finder.
	 */
	static private double[] dialogMexicanHatSettings()
	{
		final double minSizePref = Prefs.get(NucleiSegmentationParameters.MI_MINIMUM_SIZE, 10);
		final double maxSizePref = Prefs.get(NucleiSegmentationParameters.MI_MAXIMUM_SIZE, 20);
		final double stepSizePref = Prefs.get(NucleiSegmentationParameters.MI_STEPSIZE, 5);
		final double noisePref = Prefs.get(NucleiSegmentationParameters.MI_NOISE, 1);
		final double minLoGPref = Prefs.get(NucleiSegmentationParameters.MI_MINIMUM_LOG_VALUE, 0);
		final double radiusPref = Prefs.get(NucleiSegmentationParameters.MI_XY_RADIUS, 0);

		final boolean perSlicePref = Prefs.get(NucleiSegmentationParameters.MI_PROCESS_PER_SLICE, false);

		final GenericDialog dia = new GenericDialog("Parameters Mexian Hat");
		dia.addMessage("Parameters for Laplacian of Gaussian");
		dia.addNumericField("Minimal size of a nucleus", minSizePref, 1);
		dia.addNumericField("Maximal size of nucleus", maxSizePref, 1);
		dia.addNumericField("Stepsize of hat", stepSizePref, 1);
		dia.addCheckbox("Display LoG Kernel", false);
		dia.addCheckbox("Process per slice", perSlicePref);
		dia.addCheckbox("Show intermediate images", false);

		dia.addMessage("Parameters for maxima filter");
		dia.addNumericField("Noise", noisePref, 3);
		dia.addNumericField("Minimum LoG value", minLoGPref, 3);
		dia.addNumericField("Radius XY", radiusPref, 3, 5, "pixel");

		dia.showDialog();

		final double min = dia.getNextNumber();
		final double max = dia.getNextNumber();
		final double step = dia.getNextNumber();
		final double kernel = dia.getNextBoolean() ? 1 : 0;
		final double perSlice = dia.getNextBoolean() ? 1 : 0;
		final double showImages = dia.getNextBoolean() ? 1 : 0;

		final double noise = dia.getNextNumber();
		final double minValue = dia.getNextNumber();
		final double radius = dia.getNextNumber();
		final double[] result = { min, max, step, kernel, noise, perSlice, minValue, radius, showImages };

		Prefs.set(NucleiSegmentationParameters.MI_MINIMUM_SIZE, min);
		Prefs.set(NucleiSegmentationParameters.MI_MAXIMUM_SIZE, max);
		Prefs.set(NucleiSegmentationParameters.MI_STEPSIZE, step);
		Prefs.set(NucleiSegmentationParameters.MI_PROCESS_PER_SLICE, perSlice == 1 ? true : false);
		Prefs.set(NucleiSegmentationParameters.MI_NOISE, noise);
		Prefs.set(NucleiSegmentationParameters.MI_MINIMUM_LOG_VALUE, minValue);
		Prefs.set(NucleiSegmentationParameters.MI_XY_RADIUS, radius);
		Prefs.savePreferences();

		return result;

	}


	/**
	 * Find the maxima in an image in an area of certain radius and noise.
	 *
	 * @param aImage,
	 *            The input image on which the maxima will be sought
	 * @param aMinimum,
	 *            The minimal value for a maximum
	 * @param aNoise,
	 *            The minimum pixel value which is no longer considered as noise
	 * @param aRadiusXY,
	 *            The XY radius in which just one maximum can be located
	 * @param aRadiusZ,
	 *            The Z radius in which just one maximum can be located
	 *
	 * @return A list which contains for each slice a list of seeds (maxima).
	 */
	static private List<List<PointValue>> findMaxima(final ImagePlus aImage, final float aMinimum, final float aNoise, final float aRadiusXY, final float aRadiusZ)
	{
		// Create a maximaFinder and find all maxima
		final MaximaFinder3D maximaFinder = new MaximaFinder3D(aImage, aNoise, aMinimum);
		maximaFinder.setRadii(aRadiusXY, aRadiusXY, Math.max(1, aRadiusZ));

		// Create a list which which have the length of the amount of slices
		// This list contains list of seeds which were annotated by a maximum
		final List<List<PointValue>> listOfSlicesWithSeeds = new ArrayList<>(aImage.getNSlices());
		for (int i = 0; i < aImage.getNSlices(); i++)
		{
			listOfSlicesWithSeeds.add(i, new ArrayList<PointValue>());
		}

		// Make a list with voxel3D and add the seed to the list of z position
		final ArrayList<PointValue> list = maximaFinder.getListPeaks();
		for (int i = 0; i < list.size(); i++)
		{
			final int x = (int) list.get(i).getXcoordinate();
			final int y = (int) list.get(i).getYcoordinate();
			final int z = (int) list.get(i).getZcoordinate();
			final int value = (int) list.get(i).getValue();
			listOfSlicesWithSeeds.get(z).add(new PointValue(x, y, z, value));
		}

		return listOfSlicesWithSeeds;
	}


	/**
	 * Method markerImage3D loops through all the selected point detection methods
	 *
	 * @param originalImage,
	 *            the 3D gray scale image
	 * @param title,
	 *            of the original image to give the marker image the same name
	 * @param slices,
	 *            the amount of slices of the original image
	 * @param directory,
	 *            where the manual point file is save and were the marker images need to be saved
	 * @param names,
	 *            to select the manual point file
	 */
	static public void markerImage3D(final ImagePlus aOriginalImage, final String aDetectionMethod, final File aInputDirectory, final String aInputFileName, final File aOutputDirectory)
			throws IOException
	{
		final int totalSlices = aOriginalImage.getNSlices();

		// For each point detection method that is chosen by the user
		// Determine the point detection method and create the marker image name
		final String titlemethod = createOutputFileName(aOriginalImage, aDetectionMethod);
		if (aDetectionMethod.equals(MANUALPOINTS))
		{
			// Create a list (n=slices) of list (n= point detection points per slide)
			final List<List<PointValue>> listofslices = readManualPoints(totalSlices, aInputDirectory, aInputFileName);
			// Create a new image and save the marker image and the marker file

			createMarkerImage(aOriginalImage, listofslices, aOutputDirectory, titlemethod);
		}
		else // Mexican hat
		{
			// Perform the mexican hat and save the marker image and marker file
			mexicanHatMaster(aOriginalImage, titlemethod, aOutputDirectory);
		}
	}


	/**
	 * Starts the mexican hat plugin of Daniel Sage (LoG3D), inverts the resulting image and multiplies it by the XYsigma to normalise its values a bit.
	 *
	 * @param aParameters
	 *            the input parameters of the user
	 * @param listOfLoGImages
	 *            a list with the results LOGImages
	 * @param aMexican
	 *            the original image
	 * @param aSigmaXY
	 *            The calculated XY sigma
	 * @param aSigmaZ
	 *            The calculated Z sigma
	 */
	static private ImagePlus mexicanHat(final ImagePlus aMexican, final int aDisplayKernel, final boolean aPerSlice, final double aSigmaXY, final double aSigmaZ)
	{
		// Start the Mexican Hat plugin
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

		IJ.log("    Mexican Hat input: " + LoG3DInput);
		// Plugin: LoG3D (Daniel Sage)
		IJ.run("LoG 3D", LoG3DInput);

		// Wait until the new image is displayed
		ImagePlus mexicanResults;
		do
		{
			mexicanResults = IJ.getImage();
		} while (mexicanResults.getTitle().equals(aMexican.getTitle()));

		// Invert the image and add to the list of LoGImages
		IJ.run(mexicanResults, "Divide...", "value=-1.000 stack");

		IJ.run(mexicanResults, "Multiply...", "value=" + aSigmaXY + " stack");

		return mexicanResults;
	}


	/**
	 * Method mexicanHatCombineIterations combines the different LoG images, based on the highest value of each pixel
	 *
	 * @param aListOfLoGImages,
	 *            A list of the LoG images for each different sigma value used
	 *
	 * @return The combined image of the different LoG images
	 */
	static private ImagePlus mexicanHatCombineIteration(final ArrayList<ImagePlus> aListOfLoGImages)
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
	 * Method mexicanHat performs the mexican hat and creates the marker image and marker file
	 *
	 * @param aOriginalImage,
	 *            The input for the mexican hat
	 * @param aTitlemethod,
	 *            To create the name of the marker file and marker image
	 * @param aOutputDirectory,
	 *            Where the marker file and marker image need to be saved
	 */
	static protected void mexicanHatMaster(final ImagePlus aOriginalImage, final String aTitlemethod, final File aOutputDirectory)
	{
		IJ.log("  Start Mexican Hat");
		final long timeStart = System.currentTimeMillis();

		// Dialog to select the parameters for the Mexican hat and maxima finder
		final double[] parameters = dialogMexicanHatSettings();

		// Extract the parameters which need to be use to determine the Sigma of the mexican hat and the iteration
		final double min = parameters[0];
		final double max = parameters[1];
		final double step = parameters[2];
		final double displayKernel = parameters[3];
		final double noise = parameters[4];
		final boolean perSlice = parameters[5] == 1;
		final double minValue = parameters[6];
		double xyRadius = parameters[7];
		final boolean showImages = parameters[8] == 1;
		final Calibration cal = aOriginalImage.getCalibration();

		// Variable to save all the different Mexican hat images
		final ArrayList<ImagePlus> listOfLoGImages = new ArrayList<>();

		// First do some filtering to get rid of noise
		IJ.log("   Start Median filter 3D nucleus image");
		IJ.run(aOriginalImage, "Median (3D)", "");
		ImagePlus originalImageMedian;
		do
		{
			originalImageMedian = IJ.getImage();
		} while (originalImageMedian.getTitle().equals(aOriginalImage.getTitle()));
		IJ.log("   End Median filter 3D");
		originalImageMedian.setTitle(aOriginalImage.getTitle());

		String mexicanHatName = "";
		int count = 0;
		final ArrayList<double[]> sigmas = new ArrayList<>();
		// For each iteration of the mexican hat filter
		for (double cellDiameter = min; cellDiameter <= max; cellDiameter = cellDiameter + step)
		{
			final ImagePlus mexican = originalImageMedian.duplicate();
			mexican.show();

			// Calculate the settings of the different mexican hat filters
			final double radius = (cellDiameter / 2);
			final double sigmaXY = (radius / 3) * 2;
			double sigmaZ = 0;
			if (aOriginalImage.getNSlices() > 1 && !perSlice) // Not per slice, but really 3D
			{
				final double factor = cal.pixelDepth / cal.pixelWidth;
				sigmaZ = (sigmaXY / factor);
			}
			count = count + 1;
			final double[] sigma = { sigmaXY, sigmaZ };
			sigmas.add(sigma);
			mexicanHatName = mexicanHatName + createAdditionOfFileName(sigmaXY, sigmaZ, true);
			listOfLoGImages.add(mexicanHat(mexican, (int) displayKernel, perSlice, sigmaXY, sigmaZ));
			mexican.close();
		}
		final ImagePlus mexicanHatCombine = mexicanHatCombineIteration(listOfLoGImages);

		// Start the maximum finder
		double minSigmaXY = 100000;
		double minSigmaZ = 100000;
		for (int i = 0; i < sigmas.size(); i++)
		{
			if (sigmas.get(i)[0] < minSigmaXY)
			{
				minSigmaXY = sigmas.get(i)[0];
			}
			if (sigmas.get(i)[1] < minSigmaZ)
			{
				minSigmaZ = sigmas.get(i)[1];
			}
		}

		xyRadius = Math.max(minSigmaXY, xyRadius); // Make sure that the default 0 isn't used
		final String maxFinderName = createAdditionOfFileName(xyRadius, minSigmaZ < 1 ? 1 : minSigmaZ, false);

		final String nametotal = aTitlemethod + "_n=" + count + mexicanHatName + maxFinderName;
		IJ.log("   End Mexican Hat");
		final long timeEndMexicanHat = System.currentTimeMillis();
		final List<List<PointValue>> listofslicesWithSeeds = findMaxima(mexicanHatCombine, (float) minValue, (float) noise, (float) xyRadius, (float) minSigmaZ);
		if (!showImages)
		{
			mexicanHatCombine.changes = false;
			mexicanHatCombine.close();
			for (final ImagePlus img : listOfLoGImages)
			{
				img.changes = false;
				img.close();
			}
			originalImageMedian.close();
		}

		final List<Coordinates> coords = new ArrayList<>();
		final List<Double> xVals = new ArrayList<>();
		final List<Double> yVals = new ArrayList<>();
		final List<Double> zVals = new ArrayList<>();
		for (final List<PointValue> pointList : listofslicesWithSeeds)
		{
			coords.addAll(pointList);
			for (final PointValue point : pointList)
			{
				xVals.add(point.getXcoordinate());
				yVals.add(point.getYcoordinate());
				zVals.add(point.getZcoordinate());
			}
		}

		Measurer.findProgressiveCentreOfMass(coords, 0.9, null);
		final double xMean = MyMath.getMean(xVals);
		final double yMean = MyMath.getMean(yVals);
		final double zMean = MyMath.getMean(zVals);
		IJ.log("xSkew : " + MyMath.getSkewness(xVals, xMean, MyMath.getStandardDeviation(xVals, xMean)));
		IJ.log("ySkew : " + MyMath.getSkewness(yVals, yMean, MyMath.getStandardDeviation(yVals, yMean)));
		IJ.log("zSkew : " + MyMath.getSkewness(zVals, zMean, MyMath.getStandardDeviation(zVals, zMean)));

		// Create a new marker image
		createMarkerImage(aOriginalImage, listofslicesWithSeeds, aOutputDirectory, nametotal);
		final long timeEndMaximaFinder = System.currentTimeMillis();

		// Do some time logging
		final long[] time = new long[3];
		time[0] = timeEndMexicanHat - timeStart;
		time[1] = timeEndMaximaFinder - timeEndMexicanHat;
		time[2] = timeEndMaximaFinder - timeStart;

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
				IJ.log("Time (" + timeName + ") for MexicanHat: " + timeTotal);
			}
			else if (t == 1)
			{
				IJ.log("Time (" + timeName + ") for MaximaFinder: " + timeTotal);
			}
			else if (t == 2)
			{
				IJ.log("Time (" + timeName + ") for MexicanHat and MaximaFinder: " + timeTotal);
			}
		}

	}


	static public List<List<PointValue>> readManualPoints(final int aNrOfSlices, final File aDirectory, final String aFileName) throws IOException
	{

		IJ.log("   Start Manual Points");
		// Create a list (n=slices) of list (n= point detection points per slide)
		final List<List<PointValue>> listOfSlices = new ArrayList<>(aNrOfSlices);
		for (int i = 0; i < aNrOfSlices; i++)
		{
			listOfSlices.add(i, new ArrayList<PointValue>());
		}

		final List<Coordinates> seeds = Nucleus3DFileUtils.readNucleusSeeds(aDirectory);
		for (final Coordinates seed : seeds)
		{
			listOfSlices.get((int) seed.getZcoordinate()).add(new PointValue(seed.getXcoordinate(), seed.getYcoordinate(), seed.getZcoordinate()));
		}

		return listOfSlices;
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
	static private void saveMarkerFileAndImage(final ImagePlus aMarkerImage, final ResultsTable aResults, final File aOutputDirectory, final String aTitle)
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

}