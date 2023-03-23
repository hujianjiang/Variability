package watershedsegmentation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.NucleiSegmentationParameters;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.CurveFitter;
import ij.plugin.Duplicator;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.process.AutoThresholder;
import ij.process.ImageProcessor;
import ij.process.StackStatistics;
import utils.AttenuationAdjuster;
import utils.GenUtils;

//TODO Deze plugin kan zowel 2D als 3D images aan
// Hierbij wordt er alleen onderscheid gemaakt in de threshold
// Moet ik de Mediaan filter ook aanpassen naar een 2D mogelijkheid?
// Of moet ik 2D en 3D helemaal loskoppelen?

/**
 * Create a segmented image based on a set of markers points. The input is the image to be segmented and an image in which for each segment a colour-labelled point has been added (most often the output of the Marker_Image_Creator_3D). Starting from
 * the marker points, regions are grown in a thresholded version of the input image until all of the foreground that contains a marker has been claimed by one of the segments. This specific plugin assumes the input image to contain at least a channel
 * showing a nucleus-identifying signal (e.g. DAPI) and is also capable of using the markers on a complete cell-based signal (with the exception of the nucleus if need be). The latter is an option for the user to decide.
 *
 * This plugins use the following plugins: - Median filter: Fiji -> Process -> Median(3D) - Threshold: Fiji -> Image -> Adjust -> Threshold - Exact Euclidean Distance Transform: Fiji -> Process -> Exact Euclidean Distance Transform(3D) - Marker
 * Controlled Watershed: Fiji --> Plugins -> MorpholibJ
 *
 * @author Esther
 */
public class Marker_Controlled_Watershed_3D implements PlugIn
{
	private static final String NONE = "None";
	private static final String FILTER = "Median";
	private static final String MANUAL = "Manual";
	private static final String MEAN = "Mean", MEDIAN = "Median";
	private static String[] FILTERS = { MEDIAN, MEAN };
	public static final String WATERSHED_INFIX = "_MCWatershed_";
	public static final String DAPI = "_DAPI_";
	public static final String ACTIN = "_Actin_";
	private String threshold;
	private String thresholdActin;
	private String filter;
	private String filterActin;
	private final boolean[] calculateDams = { false, false };
	private int[] manualThreshold = null; // 0 for lower threshold, 1 for upper threshold
	private ImagePlus originalImage = null;
	private int dapiChannel = -1;
	private int actinChannel = -1;
	private boolean segmentActinChannel = false;
	private List<Double> attenuationAdjustments;
	private boolean adjustAttenuation;
	private double attenuationPerc;
	private int attenuationStartIntensity;
	private int attenuationEndIntensity;
	private int attenuationStandardSlice;
	private int attenuationFitMethod;
	private boolean experimentalThreshold;
	private int thresholdWindow;


	/**
	 * Apply the threshold that has been set by the user on a (presumably) already filtered image.
	 *
	 * @param aFilteredImage
	 *            The channel image after filtering has been done already
	 * @param aThreshold
	 *            The thresholding method to apply
	 */
	private void applyThreshold(final ImagePlus aFilteredImage, final String aThreshold)
	{
		IJ.log("   Start threshold");
		final String stackIt = this.originalImage.getNSlices() > 1 ? " stack" : "";
		if (aThreshold.equals(MANUAL))
		{
			final String thresholdSettings = "Default dark" + stackIt;
			IJ.setAutoThreshold(aFilteredImage, thresholdSettings);
			IJ.setThreshold(aFilteredImage, this.manualThreshold[0], this.manualThreshold[1], null);
			IJ.log("      Threshold: " + this.manualThreshold[0] + " - " + this.manualThreshold[1]);
			IJ.run(aFilteredImage, "Convert to Mask", "method=Default background=Dark black");
		}
		else if (this.experimentalThreshold)
		{
			final int stackSize = aFilteredImage.getNSlices();
			final int[] originalThresholds = new int[stackSize + (2 * this.thresholdWindow)];
			final ImageStack stack = aFilteredImage.getImageStack();
			for (int slicenr = 1; slicenr <= stackSize; slicenr++)
			{
				final ImageProcessor proc = stack.getProcessor(slicenr);
				final int[] histogram = proc.getHistogram();

				final AutoThresholder autoT = new AutoThresholder();
				originalThresholds[slicenr - 1 + this.thresholdWindow] = autoT.getThreshold(aThreshold, histogram);
			}

			final int lowValue = originalThresholds[this.thresholdWindow];
			final int highValue = originalThresholds[stackSize - 1 + this.thresholdWindow];
			for (int i = 1; i <= this.thresholdWindow; i++)
			{
				originalThresholds[this.thresholdWindow - i] = lowValue;
				originalThresholds[stackSize - 1 + this.thresholdWindow + i] = highValue;
			}

			for (int slicenr = 0; slicenr < stackSize; slicenr++)
			{
				int currentThreshold = 0;
				for (int i = -this.thresholdWindow; i <= this.thresholdWindow; i++)
				{
					currentThreshold += originalThresholds[slicenr + this.thresholdWindow + i];
				}
				currentThreshold /= (this.thresholdWindow * 2) + 1;
				aFilteredImage.setSlice(slicenr + 1);
				IJ.setThreshold(aFilteredImage, currentThreshold, aFilteredImage.getBitDepth() == 8 ? 255 : 65335, null);
				IJ.log(slicenr + "\t" + currentThreshold);
				IJ.run(aFilteredImage, "Convert to Mask", "method=Default background=Dark only black");
			}
		}
		else
		{
			reportStackThresholds(aFilteredImage);

			final String thresholdInput1 = aThreshold + " dark" + stackIt;
			final String threhsoldInput2 = "method=" + aThreshold + " background=Dark black";
			IJ.setAutoThreshold(aFilteredImage, thresholdInput1);
			IJ.run(aFilteredImage, "Convert to Mask", threhsoldInput2);
			IJ.log("      Treshold: " + aThreshold);
		}
		IJ.log("   End threshold");
	}


	private boolean dialogAttenuationAdjustment()
	{
		final GenericDialog gd = new GenericDialog("Settings for the threshold");

		final boolean attenuationPref = Prefs.get(NucleiSegmentationParameters.WS_ADJUST_ATTENUATION, true);
		final double percentilePref = Prefs.get(NucleiSegmentationParameters.WS_ATN_PERCENTILE, 90.0);
		final int standardPref = (int) Prefs.get(NucleiSegmentationParameters.WS_ATN_STANDARD_SLICE, 10);
		final int startPref = (int) Prefs.get(NucleiSegmentationParameters.WS_ATN_START_INTENS, 3);
		final int endPref = (int) Prefs.get(NucleiSegmentationParameters.WS_ATN_END_INTENS, 255);
		final int fitMethodPref = (int) Prefs.get(NucleiSegmentationParameters.WS_ATN_FIT_METHOD, CurveFitter.EXP_WITH_OFFSET);

		gd.addCheckbox("Adjust for attenuation?", attenuationPref);
		gd.addNumericField("Foreground percentile", percentilePref, 1);
		gd.addNumericField("Start intensity", startPref, 0);
		gd.addNumericField("End intensity", endPref, 0);
		gd.addNumericField("Standard slice nr", standardPref, 0);
		gd.addChoice("Curve fit method", CurveFitter.fitList, CurveFitter.fitList[fitMethodPref]);

		gd.showDialog();

		if (gd.wasOKed())
		{
			this.adjustAttenuation = gd.getNextBoolean();
			this.attenuationPerc = gd.getNextNumber();
			this.attenuationStartIntensity = (int) gd.getNextNumber();
			this.attenuationEndIntensity = (int) gd.getNextNumber();
			this.attenuationStandardSlice = (int) gd.getNextNumber();
			final String fitMethod = gd.getNextChoice();
			this.attenuationFitMethod = CurveFitter.getFitCode(fitMethod);

			Prefs.set(NucleiSegmentationParameters.WS_ADJUST_ATTENUATION, this.adjustAttenuation);
			Prefs.set(NucleiSegmentationParameters.WS_ATN_PERCENTILE, this.attenuationPerc);
			Prefs.set(NucleiSegmentationParameters.WS_ATN_STANDARD_SLICE, this.attenuationStandardSlice);
			Prefs.set(NucleiSegmentationParameters.WS_ATN_START_INTENS, this.attenuationStartIntensity);
			Prefs.set(NucleiSegmentationParameters.WS_ATN_END_INTENS, this.attenuationEndIntensity);
			Prefs.set(NucleiSegmentationParameters.WS_ATN_FIT_METHOD, this.attenuationFitMethod);
			Prefs.savePreferences();

			return true;
		}

		return false;
	}


	/**
	 * Ask the user to select the marker image.
	 *
	 * @param aNames
	 *            The list of images available
	 *
	 * @return The image if one has been chosen, null otherwise (none chosen or cancelled)
	 */
	private ImagePlus dialogInputMarkerImage(final String[] aNames)
	{
		final GenericDialog gd = new GenericDialog("Select marker image");
		gd.addMessage("Select the marker image from open images");
		gd.addChoice("Marker Image", aNames, aNames[0]);
		gd.showDialog();

		if (!gd.wasCanceled())
		{
			final String imageIndex1 = gd.getNextChoice();

			if (imageIndex1 != NONE)
			{
				return WindowManager.getImage(imageIndex1);
			}
		}

		return null;
	}


	/**
	 * Ask the user to select the original image from the list of open images.
	 *
	 * @param aNames
	 *            The names of the available images
	 * @return True if the dialog was ok-ed, false if not
	 */
	private boolean dialogOriginalImage(final String[] aNames)
	{
		final GenericDialog gd = new GenericDialog("Original image selection");
		gd.addMessage("Select the original image from open images");
		gd.addChoice("Original image", aNames, aNames[0]);
		gd.showDialog();

		if (gd.wasOKed())
		{
			final String originalImageName = gd.getNextChoice();
			this.originalImage = (WindowManager.getImage(originalImageName));
			return true;
		}

		return false;
	}


	/**
	 * Ask the user to select the which threshold and filter method must be used to prepare the image for the segmentation. The user can select different options for the nucleus and cell images. Also allows the user to include 'dams' in the watershed
	 * image (choice made per channel). Dams are a minimal 1 zero value pixel division between all segments. Using dams really helps visibility at the cost of slightly small segments.
	 *
	 * @return True if the dialog has been OK-ed, false otherwise.
	 */
	private boolean dialogThresholdSetting()
	{
		final GenericDialog gd = new GenericDialog("Settings for the threshold");

		final String dapiFilterPref = Prefs.get(NucleiSegmentationParameters.WS_DAPI_FILTER, FILTERS[0]);
		final String dapiThreshPref = Prefs.get(NucleiSegmentationParameters.WS_DAPI_THRESHOLD, MANUAL);
		final String actinFilterPref = Prefs.get(NucleiSegmentationParameters.WS_ACTIN_FILTER, FILTERS[0]);
		final String actinThreshPref = Prefs.get(NucleiSegmentationParameters.WS_ACTIN_THRESHOLD, MANUAL);

		final boolean dapiDamsPref = Prefs.get(NucleiSegmentationParameters.WS_DAPI_DAMS, true);
		final boolean actinDamsPref = Prefs.get(NucleiSegmentationParameters.WS_ACTIN_DAMS, false);
		final boolean experimentalThresholdPref = Prefs.get(NucleiSegmentationParameters.WS_DO_EXP_THRESHOLD, false);

		final double expThreshold = Prefs.get(NucleiSegmentationParameters.WS_EXPERIMENTAL_THRESHOLD, 3);

		gd.addChoice("Select the filter for the nucleus channel", FILTERS, dapiFilterPref);
		final List<String> thresholds = new ArrayList<>();
		thresholds.add(MANUAL);
		thresholds.addAll(Arrays.asList(AutoThresholder.getMethods()));
		final String[] threshList = thresholds.toArray(new String[0]);
		gd.addChoice("Select the threshold for the nucleus segments", threshList, dapiThreshPref);
		gd.addCheckbox("Calculate dams on nucleus segments?", dapiDamsPref);
		if (this.segmentActinChannel) // Selected in an earlier dialog
		{
			gd.addChoice("Select the filter for the actin channel", FILTERS, actinFilterPref);
			gd.addChoice("Select the threshold for the actin segments", threshList, actinThreshPref);
			gd.addCheckbox("Calculate dams actin segments?", actinDamsPref);
		}
		gd.addCheckbox("Use experimental threshold?", experimentalThresholdPref);
		gd.addNumericField("Threshold window", expThreshold, 0);
		gd.showDialog();

		if (gd.wasOKed())
		{
			this.filter = gd.getNextChoice();
			this.threshold = gd.getNextChoice();
			this.calculateDams[0] = gd.getNextBoolean();
			if (this.segmentActinChannel)
			{
				this.filterActin = gd.getNextChoice();
				this.thresholdActin = gd.getNextChoice();
				this.calculateDams[1] = gd.getNextBoolean();
			}
			this.experimentalThreshold = gd.getNextBoolean();
			this.thresholdWindow = (int) gd.getNextNumber();

			Prefs.set(NucleiSegmentationParameters.WS_DAPI_FILTER, this.filter);
			Prefs.set(NucleiSegmentationParameters.WS_DAPI_THRESHOLD, this.threshold);
			Prefs.set(NucleiSegmentationParameters.WS_ACTIN_FILTER, this.filterActin);
			Prefs.set(NucleiSegmentationParameters.WS_ACTIN_THRESHOLD, this.thresholdActin);
			Prefs.set(NucleiSegmentationParameters.WS_DAPI_DAMS, this.calculateDams[0]);
			Prefs.set(NucleiSegmentationParameters.WS_ACTIN_DAMS, this.calculateDams[1]);
			Prefs.set(NucleiSegmentationParameters.WS_DO_EXP_THRESHOLD, this.experimentalThreshold);
			Prefs.set(NucleiSegmentationParameters.WS_EXPERIMENTAL_THRESHOLD, this.thresholdWindow);
			Prefs.savePreferences();
			return true;
		}

		return false;
	}


	/**
	 * Ask the user the settings for the manual threshold.
	 *
	 * @return True if the dialog has been ok-ed, false otherwise.
	 */
	private boolean dialogThresholdSettingManual()
	{
		final GenericDialog gd = new GenericDialog("Settings for the manual threshold");

		gd.addNumericField("Lower threshold level", 0, 0);
		gd.addNumericField("Upper threshold level", 65535, 0);
		gd.showDialog();

		if (gd.wasOKed())
		{
			this.manualThreshold = new int[2];
			this.manualThreshold[0] = (int) gd.getNextNumber();
			this.manualThreshold[1] = (int) gd.getNextNumber();
			return true;
		}

		return false;
	}


	/**
	 * Do the preparation before the segmentation. Filter and threshold the input images to reduce noise and to get a starting point for the watershed.
	 *
	 * @param aChannel
	 *            The number of the channel that needs to be prepared
	 * @param aFilter
	 *            The name of the filter to apply
	 * @param aThreshold
	 *            The name of the threshold method to use
	 *
	 * @return A (duplicated) filtered and thresholded image of just the selected channel.
	 */
	private ImagePlus filterAndThresholdChannel(final int aChannel, final String aFilter, final String aThreshold)
	{
		// Get a duplicate of the chosen channel. This will be the new 'original'.
		final ImagePlus segmentImage = new Duplicator().run(this.originalImage, aChannel, aChannel, 1, this.originalImage.getNSlices(), 1, 1);
		segmentImage.setTitle(this.originalImage.getTitle());
		if (this.adjustAttenuation)
		{
			if (this.attenuationAdjustments == null)
			{
				this.attenuationAdjustments = AttenuationAdjuster.adjustAttenuation(segmentImage, 1, this.attenuationPerc, this.attenuationStandardSlice, this.attenuationStartIntensity, 1,
						this.attenuationEndIntensity, this.attenuationFitMethod, null);
			}
			else
			{
				AttenuationAdjuster.adjustAttenuation(segmentImage, 1, this.attenuationPerc, this.attenuationStandardSlice, this.attenuationStartIntensity, 1, this.attenuationEndIntensity,
						this.attenuationFitMethod, this.attenuationAdjustments);
			}
		}

		// Median Filter
		ImagePlus filteredImage = null;
		if (aFilter.equals(MEDIAN))
		{
			IJ.log("   Start Median filter 3D");
			IJ.run(segmentImage, "Median (3D)", ""); // TODO: make this into parameters?
			// Do until de median filter has been done. This is detected by seeing that the current (i.e. newest) image has a different title compared to the original
			do
			{
				filteredImage = IJ.getImage();
			} while (filteredImage.getTitle().equals(segmentImage.getTitle()));
			IJ.log("   End Median filter 3D");
		}
		else if (aFilter.equals(MEAN))
		{
			IJ.log("   Start Mean filter 3D");
			filteredImage = segmentImage.duplicate();
			IJ.run(filteredImage, "Mean 3D...", "x=3 y=3 z=3"); // TODO: make this into parameters?
			IJ.wait(10000); // Delay to wait for mean filter to end processing
			filteredImage.show();
			IJ.log("   End Mean filter 3D");
		}

		// Start of the threshold
		applyThreshold(filteredImage, aThreshold);

		// Get rid of the original duplicate.
		segmentImage.close();

		return filteredImage;
	}


	/**
	 *
	 * @param aImage
	 * @param aMarkerImage
	 * @param aOutputDirectory
	 * @param aChannelName
	 * @param aThreshold
	 * @param aCalculateDams
	 * @return
	 */
	private ImagePlus markerControlledWatershed(final ImagePlus aImage, final ImagePlus aMarkerImage, final File aOutputDirectory, final String aChannelName, final String aThreshold,
			final String aFilter, final boolean aCalculateDams)
	{
		IJ.log("   Start Distance Map");
		ImagePlus originalImageDistance;
		final String subTitleMedian = GenUtils.getTitleNoExtension(aImage);
		final String inputDistance = "map=EDT image=" + subTitleMedian + " mask=None threshold=1 inverse";
		IJ.run("3D Distance Map", inputDistance);
		do
		{
			originalImageDistance = IJ.getImage();
		} while (originalImageDistance.getTitle().equals(aImage.getTitle()));
		IJ.log("   End Distance Map");

		// Input strings for the marker-controlled watershed
		final String maskName = "mask=" + aImage.getShortTitle();
		final String inputName = "input=" + originalImageDistance.getShortTitle();

		// For each marker image a marker-controlled watershed is performed
		IJ.log("   Marker-Controlled Watershed " + aMarkerImage.getShortTitle());
		final String markerName = "marker=" + aMarkerImage.getShortTitle();
		String options = "use";
		if (aCalculateDams)
		{
			options = "calculate use";
		}
		final String input = inputName + " " + markerName + " " + maskName + " " + options;
		IJ.log("     " + input);
		// Marker-Controlled watershed
		IJ.run("Marker-controlled Watershed", input);

		final ImagePlus imageseg = IJ.getImage();

		// Create the name of the segmented Image
		final String title = aMarkerImage.getTitle();
		final String title2 = title.substring(title.indexOf("Markers_") + 8);
		String nameSegImage = "";
		if (aThreshold.equals(MANUAL))
		{
			nameSegImage = File.separator + this.originalImage.getShortTitle() + aChannelName + "_" + aFilter + "_" + aThreshold + "-" + this.manualThreshold[0] + "-" + this.manualThreshold[1]
					+ WATERSHED_INFIX + title2;
		}
		else
		{
			nameSegImage = File.separator + this.originalImage.getShortTitle() + aChannelName + "_" + aFilter + "_" + aThreshold + WATERSHED_INFIX + title2;
		}

		// Save the segmented image
		final String name = aOutputDirectory.getPath() + nameSegImage;
		IJ.saveAs(imageseg, "Tiff", name);
		IJ.log("Segmented image is save as: " + nameSegImage);
		IJ.log("   Segmented image is saved in " + name);
		originalImageDistance.close();
		return imageseg;
	}


	/**
	 * A small bit of code to show the threshold values of the different auto-threshold methods on an image stack.
	 *
	 * @param aImage
	 *            The ImagePlus to be thresholded
	 */
	private void reportStackThresholds(final ImagePlus aImage)
	{
		final StackStatistics ss = new StackStatistics(aImage);
		final long[] longHist = ss.getHistogram();
		final int[] histo = new int[longHist.length];
		for (int i = 0; i < longHist.length; i++)
		{
			histo[i] = Math.toIntExact(longHist[i]);
		}
		for (final AutoThresholder.Method method : AutoThresholder.Method.values())
		{
			final AutoThresholder autoT = new AutoThresholder();
			IJ.log("Threshold " + method.name() + " is " + autoT.getThreshold(method, histo));
		}
	}


	@Override
	public void run(final String aArg)
	{
		// For all images, the names are listed in the array ImagesNames to select the original image and the marker images
		final int amountWindows = WindowManager.getImageCount();
		final String[] imagesNames = new String[amountWindows + 1];
		imagesNames[0] = NONE;
		for (int i = 0; i < amountWindows; i++)
		{
			// Fill the array with the available images (there is no 0th image in the manager)
			imagesNames[i + 1] = WindowManager.getImage(i + 1).getTitle();
		}

		// Get the original image
		if (!dialogOriginalImage(imagesNames) || this.originalImage == null)
		{
			return;
		}

		// For multiple channels, select the correct nucleus channel and give the option for cell-based segmentation as well
		if (this.originalImage.getNChannels() > 1)
		{
			if (!selectChannels(this.originalImage))
			{
				return;
			}
		}

		// Get the marker image
		final ImagePlus markerImage = dialogInputMarkerImage(imagesNames);
		if (markerImage == null)
		{
			return;
		}

		// Get the threshold and filter choices
		if (!dialogThresholdSetting())
		{
			return;
		}
		// If threshold is Manual the dialogue 'DialogueThresholdSettingManual' is used to set the manual threshold
		if (this.threshold == MANUAL && !dialogThresholdSettingManual()) // i.e. the settings for dapi == settings for actin
		{
			return;
		}

		// See if the user wants an attenuation adjustment
		if (!dialogAttenuationAdjustment())
		{
			return;
		}

		// Select the working directory where the segmented images need to be saved
		final File workingDir = NucleiSegmentationParameters.getWorkingDirectory();
		if (workingDir == null)
		{
			return;
		}
		// Create the segments output directory name and create the directory if need be
		final File directoryOutputFile = NucleiSegmentationParameters.getSegmentsDir(workingDir);
		if (!directoryOutputFile.exists())
		{
			directoryOutputFile.mkdir();
		}

		// Start of the marker-controlled watershed
		IJ.log("Start Marker-controlled watershed");
		this.attenuationAdjustments = null;
		final ImagePlus originalImageFilter = filterAndThresholdChannel(this.dapiChannel, this.filter, this.threshold);
		final ImagePlus nucSegmentImage = markerControlledWatershed(originalImageFilter, markerImage, directoryOutputFile, DAPI, this.threshold, this.filter, this.calculateDams[0]);

		// Segment the cell channel if chosen to do so
		if (this.segmentActinChannel && this.actinChannel != 0)
		{
			final ImagePlus actinImageFilter = filterAndThresholdChannel(this.actinChannel, this.filterActin, this.thresholdActin);

			// Add the original filtered image to the actin filtered image to fill in the nucleus gaps (little actin there).
			final ImageCalculator ic = new ImageCalculator();
			final ImagePlus actinImageCombine = ic.run("Add create stack", originalImageFilter, actinImageFilter);
			actinImageFilter.close();
			actinImageCombine.show();

			markerControlledWatershed(actinImageCombine, nucSegmentImage, directoryOutputFile, ACTIN, this.thresholdActin, this.filterActin, this.calculateDams[1]);
			actinImageCombine.close();
		}
		originalImageFilter.close();

		// Save parameters
		storeUsedParameters(workingDir);

		IJ.log("End Marker-controlled watershed 3D");
	}


	/**
	 * Select the channel of the input image in which the nucleus signal can be found. This is also the dialog in which the user can opt to segment a cell-wide signal. If the user chooses so, a channel for the cell signal must also be given.
	 *
	 * @param aInputImage
	 *            The image in which at least the nucleus signal is present. The cell signal should be in another channel of this image as well if it is to be used.
	 *
	 * @return Was the channel dialog ok-ed (true) or not (false)
	 */
	private boolean selectChannels(final ImagePlus aInputImage)
	{
		final String dapiChannelPref = Prefs.get(NucleiSegmentationParameters.NS_NUCLEUS_CHANNEL, "1");
		final String actinChannelPref = Prefs.get(NucleiSegmentationParameters.NS_ACTIN_CHANNEL, "2");
		final boolean segmentActinPref = Prefs.get(NucleiSegmentationParameters.WS_SEGMENT_ACTIN, false);

		final int channels = aInputImage.getNChannels();
		final GenericDialog gd = new GenericDialog("Set channels Original image");
		final String[] channelchooser = new String[channels + 1];
		for (int i = 0; i <= channels; i++)
		{
			channelchooser[i] = i + "";
		}
		gd.addChoice("Nucleus Channel", channelchooser, dapiChannelPref);
		gd.addCheckbox("Segment actinSignal", segmentActinPref);
		gd.addChoice("Actin Channel", channelchooser, actinChannelPref);
		gd.showDialog();

		if (gd.wasOKed())
		{
			this.dapiChannel = Integer.parseInt(gd.getNextChoice());
			this.segmentActinChannel = gd.getNextBoolean();
			this.actinChannel = Integer.parseInt(gd.getNextChoice());

			Prefs.set(NucleiSegmentationParameters.NS_NUCLEUS_CHANNEL, this.dapiChannel);
			Prefs.set(NucleiSegmentationParameters.NS_ACTIN_CHANNEL, this.actinChannel);
			Prefs.set(NucleiSegmentationParameters.WS_SEGMENT_ACTIN, this.segmentActinChannel);
			Prefs.savePreferences();
			return true;
		}

		return false;
	}


	/**
	 * Store the parameters of the Marker_Controlled_Watershed_3D in the working directory.
	 *
	 * @param aWorkingDir
	 *            The path to the working directory.
	 */
	private void storeUsedParameters(final File aWorkingDir)
	{
		final Map<String, String> params = new HashMap<>();
		params.put(NucleiSegmentationParameters.NS_NUCLEUS_CHANNEL, this.dapiChannel + "");
		params.put(NucleiSegmentationParameters.WS_DAPI_FILTER, this.filter + "");
		params.put(NucleiSegmentationParameters.WS_DAPI_THRESHOLD, this.threshold + "");
		params.put(NucleiSegmentationParameters.WS_DAPI_DAMS, this.calculateDams[0] + "");
		params.put(NucleiSegmentationParameters.WS_SEGMENT_ACTIN, this.segmentActinChannel + "");
		if (this.segmentActinChannel)
		{
			params.put(NucleiSegmentationParameters.NS_ACTIN_CHANNEL, this.actinChannel + "");
			params.put(NucleiSegmentationParameters.WS_ACTIN_FILTER, this.filterActin + "");
			params.put(NucleiSegmentationParameters.WS_ACTIN_THRESHOLD, this.thresholdActin + "");
			params.put(NucleiSegmentationParameters.WS_ACTIN_DAMS, this.calculateDams[1] + "");
		}
		params.put(NucleiSegmentationParameters.WS_DO_EXP_THRESHOLD, this.experimentalThreshold + "");
		if (this.experimentalThreshold)
		{
			params.put(NucleiSegmentationParameters.WS_EXPERIMENTAL_THRESHOLD, this.thresholdWindow + "");
		}
		params.put(NucleiSegmentationParameters.WS_ADJUST_ATTENUATION, this.adjustAttenuation + "");
		if (this.adjustAttenuation)
		{
			params.put(NucleiSegmentationParameters.WS_ATN_FIT_METHOD, this.attenuationFitMethod + "");
			params.put(NucleiSegmentationParameters.WS_ATN_PERCENTILE, this.attenuationPerc + "");
			params.put(NucleiSegmentationParameters.WS_ATN_STANDARD_SLICE, this.attenuationStandardSlice + "");
			params.put(NucleiSegmentationParameters.WS_ATN_START_INTENS, this.attenuationStartIntensity + "");
			params.put(NucleiSegmentationParameters.WS_ATN_END_INTENS, this.attenuationEndIntensity + "");
		}

		NucleiSegmentationParameters.writeToParametersFile(params, true, aWorkingDir);
	}

}
