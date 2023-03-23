package featureextractor;

import java.awt.AWTEvent;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.font.TextAttribute;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import data.Cell3D;
import data.Cell3D_Group;
import data.Coordinates;
import data.NucleiSegmentationParameters;
import data.Nucleus3D;
import data.spheroid.SphereIO;
import data.spheroid.Spheroid;
import featureextractor.measurements.CellMeasurer;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.MessageDialog;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import ij.plugin.PlugIn;
import ij.process.ImageConverter;
import markerimagecreator.Marker_Image_Creator_3D;
import migrationmodeanalysis.MigrationModeAnalyser;

/**
 * This plugins use the following plugins:] - MorpholibJ - Measure 3D - Particle analyzer 3D - Region adjacency graph - 3D ImageJ Suite - 3D Intensity measure - 3D Geometric measure - 3D Shape Measure
 *
 * @author Esther
 *
 */
public class Feature_Extractor_3D implements PlugIn
{
	private static class TypedChannel
	{
		public int channelNr;
		public ImagePlus channelImage;
		public String channelType;


		public TypedChannel(final int aNr, final String aType)
		{
			this.channelNr = aNr;
			this.channelType = aType;
		}
	}

	// Positions of accuracy values in the marker accuracy array
	public static final int MARKER_OK = 0, MARKER_DOUBLE = 1, MARKER_NONUC = 2, MARKER_EX = 3, MARKER_DQ = 4;

	private static final String NONE = "None";
	private static final String NO_CHANNEL = "0";

	public static final String NUCLEAR = "Nuclear";
	public static final String NUCLEUS_SURROUND = "Nucleus surrounding";
	public static final String CELL = "Cell";
	public static final String CELL_NONUCLEUS = "Cell without the nucleus";
	public static final String NUCLEAR_CENTER = "Nuclear centre";
	private static final int NR_OF_ADDITIONAL_CHANNELS = 4;
	private final boolean[] calculateDams = new boolean[2];

	private List<TypedChannel> alternateChannels;
	private ImagePlus dapiImage;
	private ImagePlus actinImage;
	private ImagePlus resultImage;
	private ImagePlus dapiSegments;
	private ImagePlus actinSegments;
	private boolean saveImages;
	private boolean excludeBorderNuclei;
	private boolean excludeTooSmallNuclei;
	private boolean manualMarkers;
	private File originalDir;
	private File workingDir;
	private Integer smallNucleusSize;
	private Integer exclusionZone;


	/**
	 * Count the markers which are located: 0= in a nucleus, 1= multiple in a nucleus, 2= outside all the nuclei, 3 = in an excluded (border or too small) nucleus, 4 = in a disqualified nucleus. The counts are returned as an int array.
	 *
	 * @param aListOfMarkers
	 *            The List of Labeled_Coordinates to be counted (labelled with the migration mode)
	 * @param aCells
	 *            The list of cells to which the markers may belong
	 * @return An int[5] array containing the counts (see above for indexes).
	 */
	private int[] countMarkersAndAddToNucleus(final List<Labeled_Coordinate> aListOfMarkers, final Cell3D[] aCells)
	{
		int markersInExcludedNucleus = 0, markersInDisqualifiedNucleus = 0;
		int markersDoubleInNucleus = 0, markersInNucleus = 0, markersWithoutNucleus = 0;

		for (final Labeled_Coordinate marker : aListOfMarkers)
		{
			boolean markerInNucleus = false;
			final Coordinates markerCoords = marker.getCoordinates();
			for (final Cell3D cell : aCells)
			{
				final Nucleus3D nucleus = cell.getNucleus();
				// If the marker is located inside check if the seed is inside the nucleus
				if (nucleus.contains(markerCoords))
				{
					cell.addMarker(markerCoords, marker.getMigrationMode());
					markerInNucleus = true;
					if (nucleus.isBorderNucleus() || nucleus.isTooSmall())
					{
						markersInExcludedNucleus++;
					}
					else if (nucleus.isDisqualified())
					{
						markersInDisqualifiedNucleus++;
					}
					else
					{
						if (nucleus.getMarkersCount() > 1)
						{
							markersDoubleInNucleus++;
						}
						else
						{
							markersInNucleus++;
						}

					}

					break;
				}
			}
			if (!markerInNucleus)
			{
				markersWithoutNucleus++;
			}
		}
		final int[] result = new int[5];
		result[MARKER_OK] = markersInNucleus;
		result[MARKER_DOUBLE] = markersDoubleInNucleus;
		result[MARKER_NONUC] = markersWithoutNucleus;
		result[MARKER_EX] = markersInExcludedNucleus;
		result[MARKER_DQ] = markersInDisqualifiedNucleus;
		return result;
	}


	/**
	 * This method prepares two images for displaying the segmentation results. The input image is converted to a RGB image and a second blank RGB image (black background) is created with the same dimensions as the input image.
	 *
	 * @param aImage
	 *            The input image. This method will convert this image to an RGB image.
	 *
	 * @return A blank RGB image of the same dimensions as the input image.
	 */
	private ImagePlus createBackgroundResultImages(final ImagePlus aImage)
	{
		// Create results image with the original image as background
		final ImageConverter con = new ImageConverter(aImage);
		con.convertToRGB();

		// Create result image with black background
		final int height = aImage.getHeight();
		final int width = aImage.getWidth();
		final ImagePlus resultImageBlack = IJ.createImage("Nucleus segments", "16-bit Black", width, height, aImage.getNSlices());
		final ImageConverter con2 = new ImageConverter(resultImageBlack);
		con2.convertToRGB();

		aImage.show();
		resultImageBlack.show();

		return resultImageBlack;
	}


	/**
	 * Add any non-standard functionality that will be done after the standard measurements and results have been handled.
	 *
	 * @param aCells
	 *            The list of detected cells
	 * @param aSpheroid
	 *            The spheroid information, if any
	 */
	private void doAfterStandard(final Cell3D[] aCells, final Spheroid aSpheroid, final ImagePlus aResultsImage)
	{
		if (aSpheroid != null)
		{
			// if (this.alternateChannels != null && this.alternateChannels.size() == 4)
			// {
			// AfterExtractionReporter.doYAPReporting(aCells, 0, 1, this.saveImages ? this.workingDir : null);
			// }

			final ResultsTable karResults = ResultsTableGenerator.getKarolinskaResults(aCells);
			final File directoryOutputFile = NucleiSegmentationParameters.getResultsDir(this.workingDir);
			ResultsTableGenerator.saveResultsTable(getTitleWithoutExtension(this.dapiImage) + "_BriefOutput", directoryOutputFile, karResults);
			// AfterExtractionReporter.doModeMigrationHistogram(aCells, this.saveImages ? this.workingDir : null);
			final ImagePlus yapRatioImage = AfterExtractionReporter.drawYAPRatio(aCells, aResultsImage, 0, 1);
			IJ.saveAs(yapRatioImage, "Tif", directoryOutputFile + File.separator + getTitleWithoutExtension(this.dapiImage) + "_Nucleus_Surroundings");
		}
	}


	/**
	 * Get the title of an image and remove its file extension part (if any).
	 *
	 * @param aImage
	 *            The image for the shorter title
	 * @return The shortened image title
	 */
	private String getTitleWithoutExtension(final ImagePlus aImage)
	{
		String subTitle = aImage.getTitle();
		final int pointIndex = subTitle.lastIndexOf(".");
		if (pointIndex > 0)
		{
			subTitle = subTitle.substring(0, pointIndex);
		}
		return subTitle;
	}


	/**
	 * From a list of marker-file names, select the file which matches the given image title and read the markers from the file. This produces two lists: one containing the marker Coordinates and one containing the Coordinates and all other info
	 * contained in the marker file.
	 *
	 * @param aMarkerFile
	 *            The list of possible marker-file names
	 * @param aReadCoordinates
	 *            The list of coordinates that will be extended with the marker coordinates
	 *
	 * @return A list of Labeled_Coordinate containing all the marker coordinates of the marker file and their additional values such as the label and detection value.
	 */
	private List<Labeled_Coordinate> readMarkerFile(final File aMarkerFile, final List<Coordinates> aReadCoordinates)
	{
		final List<Labeled_Coordinate> listofSeeds = new ArrayList<>();
		try
		{
			final FileReader fileReader = new FileReader(aMarkerFile);
			final BufferedReader br = new BufferedReader(fileReader);
			String line;
			final String splitter = "\t";
			final boolean getZ = this.dapiImage.getNSlices() != 1;
			try
			{
				// Read lines until they run out
				while ((line = br.readLine()) != null)
				{
					// Split line into columns
					final String[] columns = line.split(splitter);
					// Skip any line that starts with a space
					if (!columns[0].contains("Label"))
					{
						final int label = Float.valueOf(columns[0]).intValue();
						final int xValue = Float.valueOf(columns[1]).intValue();
						final int yValue = Float.valueOf(columns[2]).intValue();

						int zValue = 0;
						if (getZ)
						{
							zValue = Float.valueOf(columns[3]).intValue();
						}

						final int value = Float.valueOf(columns[4]).intValue();
						String migrationMode = "";
						if (columns.length == 6)
						{
							migrationMode = columns[5];
						}
						final Coordinates seed = new Coordinates(xValue, yValue, zValue);
						aReadCoordinates.add(seed);
						listofSeeds.add(new Labeled_Coordinate(label, seed, value, migrationMode));
					}
				}

				br.close();
			}
			catch (final IOException ioe)
			{
				IJ.handleException(ioe);
			}
		}
		catch (final FileNotFoundException fnfe)
		{
			IJ.handleException(fnfe);
		}

		return listofSeeds;
	}


	@Override
	public void run(final String arg)
	{
		// Select all the input files and store the marker file names for later processing
		final File[] markerFileNames = selectInputFiles();
		if (markerFileNames == null)
		{
			// No marker files, so cancel
			return;
		}

		// Get the output directory if needed
		File directoryOutputFile = null;
		if (this.saveImages)
		{
			directoryOutputFile = NucleiSegmentationParameters.getResultsDir(this.workingDir);
			if (!directoryOutputFile.exists())
			{
				directoryOutputFile.mkdir();
			}
		}

		// Set the feature extraction parameters
		final Boolean runMigrationMode = selectFeatureExtractionParameters();

		if (runMigrationMode == null)
		{
			// Dialog was cancelled
			return;
		}

		IJ.log("Read markers");
		// Extract all the seeds that are created by the Point detection method and add them to the nucleus
		final List<Coordinates> seedCoords = new ArrayList<>();
		final List<Coordinates> markerCoords = new ArrayList<>();
		final String seedFileName = Marker_Image_Creator_3D.createMarkerFileName(this.dapiImage, Marker_Image_Creator_3D.LOG);
		File seedFile = null;
		for (final File markers : markerFileNames)
		{
			if (markers.getName().startsWith(seedFileName))
			{
				seedFile = markers;
				break;
			}
		}
		final List<Labeled_Coordinate> listOfSeeds = readMarkerFile(seedFile, seedCoords);

		// Measure all the features of the detected cells/nuclei
		final String segmentationTitle = getTitleWithoutExtension(this.dapiSegments);
		IJ.log("Analyze 3D: " + segmentationTitle);
		final Cell3D[] cells = CellMeasurer.getMeasuredCells(this.dapiImage, this.actinImage, this.dapiSegments, this.actinSegments, listOfSeeds, this.calculateDams);

		// Before measuring, detect any cells that fail to meet the desired standards.
		PostProcessor.postProcessCellList(cells, this.dapiImage, this.excludeTooSmallNuclei ? this.smallNucleusSize : null, this.excludeBorderNuclei ? this.exclusionZone : null);

		int[] markerResults = null;
		if (this.manualMarkers)
		{
			final String markerFileName = Marker_Image_Creator_3D.createMarkerFileName(this.dapiImage, Marker_Image_Creator_3D.MANUALPOINTS);
			File markerFile = null;
			for (final File markers : markerFileNames)
			{
				if (markers.getName().startsWith(markerFileName))
				{
					markerFile = markers;
					break;
				}
			}

			if (markerFile != null)
			{
				final List<Labeled_Coordinate> listOfMarkers = readMarkerFile(markerFile, markerCoords);
				markerResults = countMarkersAndAddToNucleus(listOfMarkers, cells);
			}
			else
			{
				final MessageDialog errorDialog = new MessageDialog(this.dapiImage.getWindow(), "Error: no manual markers!",
						"Error: No manual marker file has been found, while it was selected to be used!");
			}
		}

		File spheroidFile = null;
		for (final File spheroid : this.originalDir.listFiles())
		{
			if (spheroid.getName().endsWith(SphereIO.SPHERE_TXT))
			{
				spheroidFile = spheroid;
				break;
			}
		}
		final double zFactor = this.dapiImage.getCalibration().pixelDepth / this.dapiImage.getCalibration().pixelWidth;
		final Spheroid spheroid = SphereIO.readSpheroidFile(spheroidFile, zFactor);
		if (spheroid != null)
		{
			IJ.log("Read spheroid and calculate migration distances");
			setDistanceToSpheroid(cells, spheroid);
		}

		if (this.alternateChannels != null)
		{
			IJ.log("Measure additional channels:");
			int i = 0;
			for (final TypedChannel measureChannel : this.alternateChannels)
			{
				i = i + 1;
				IJ.log("\tMeasure additional channel" + i);
				CellMeasurer.measureCoordinatesIntensity(measureChannel.channelImage, this.actinSegments, measureChannel.channelType, cells);
			}
		}

		// Draw the nucleus and the coordinates of the markers and the seeds
		final ImagePlus blackImage = createBackgroundResultImages(this.resultImage);
		Visualiser.drawNucleusResults(cells, this.resultImage, blackImage, this.dapiSegments);
		Visualiser.drawMarkers(this.resultImage, seedCoords, Color.YELLOW, 3);
		Visualiser.drawMarkers(this.resultImage, markerCoords, Color.GREEN, 3);

		final Cell3D_Group nucleusGroup = new Cell3D_Group(Arrays.asList(cells));

		ResultsTable resultsPerGroup = null;
		double[][] migrationSetData = null;
		double[] migrationAccuracyData = null;
		if (runMigrationMode)
		{
			// TODO check if actin profiling needs to be used as an alternative
			final MigrationModeAnalyser analysis = new MigrationModeAnalyser(cells, this.dapiImage, segmentationTitle);
			migrationSetData = analysis.getMigrationSetData();
			migrationAccuracyData = analysis.getMigrationAccuracyData();
			resultsPerGroup = analysis.getResultsPerCellGroup();
			if (this.saveImages)
			{
				analysis.saveResultImages(directoryOutputFile);
			}
		}

		final ResultsTable mergedTable = ResultsTableGenerator.getResultsPerNucleus(cells);
		if (mergedTable == null)
		{
			IJ.log("ERROR: A nulceus segment can not be correlated to an automated nucleus marker");
			IJ.log("  Check if the Marker file correspond with the segmented image");
			return;
		}
		mergedTable.show("Results Per Nucleus");

		ResultsTable cellTable = null;
		if (this.actinImage != null)
		{
			cellTable = ResultsTableGenerator.getResultsPerCell(cells, runMigrationMode);
			cellTable.show("Results Per Cell");
		}

		final ResultsTable resultsSum = ResultsTableGenerator.getImageSummary(migrationSetData, migrationAccuracyData, markerResults, nucleusGroup, listOfSeeds.size(), segmentationTitle);
		resultsSum.show("Results summary");

		if (this.saveImages)
		{
			if (resultsPerGroup != null)
			{
				ResultsTableGenerator.saveResultsTable(segmentationTitle + "_ResultsPerGroup", directoryOutputFile, resultsPerGroup);
			}
			ResultsTableGenerator.saveResultsTable(segmentationTitle + "_NucleusFeatures", directoryOutputFile, mergedTable);
			if (cellTable != null)
			{
				ResultsTableGenerator.saveResultsTable(segmentationTitle + "_CellFeatures", directoryOutputFile, cellTable);
			}
			ResultsTableGenerator.saveResultsTable(getTitleWithoutExtension(this.dapiImage) + "_Summary", directoryOutputFile, resultsSum);
			saveOutputImage(this.resultImage, blackImage, segmentationTitle, directoryOutputFile);
		}

		// TODO: Handle this good/bad/not counted image
		blackImage.changes = false;
		blackImage.close();

		final int height = this.resultImage.getHeight();
		final int width = this.resultImage.getWidth();
		final ImagePlus yapImage = IJ.createImage("Nucleus segments", "8-bit Black", width, height, this.resultImage.getNSlices());
		doAfterStandard(cells, spheroid, yapImage);

		// Save parameters
		final Map<String, String> paramNames = new HashMap<>();
		paramNames.put(NucleiSegmentationParameters.FE_MANUAL_MARKERS, this.manualMarkers + "");
		paramNames.put(NucleiSegmentationParameters.FE_EXCLUDE_BORDER, this.excludeBorderNuclei + "");
		if (this.excludeBorderNuclei)
		{
			paramNames.put(NucleiSegmentationParameters.FE_BORDER_ZONE, this.exclusionZone + "");
		}
		paramNames.put(NucleiSegmentationParameters.FE_EXCLUDE_SIZE, this.excludeTooSmallNuclei + "");
		if (this.excludeTooSmallNuclei)
		{
			paramNames.put(NucleiSegmentationParameters.FE_EXCLUSION_SIZE, this.smallNucleusSize + "");
		}

		final String[] additionalNames = NucleiSegmentationParameters.getAdditionalChannelParameterNames();
		int channelNr = 0;
		if (!this.alternateChannels.isEmpty())
		{
			for (final TypedChannel channel : this.alternateChannels)
			{
				paramNames.put(additionalNames[channelNr], channel.channelNr + "");
				paramNames.put(additionalNames[channelNr + 1], channel.channelType + "");

				channelNr += 2;
			}
		}

		NucleiSegmentationParameters.writeToParametersFile(paramNames, true, this.workingDir);

		IJ.log("End Feature Extraction");
	}


	/**
	 * Save the two output images in the given directory.
	 *
	 * @param aOutlinesImage
	 *            The image of nucleus outlines and seeds
	 * @param aNucleusImage
	 *            The image of nucleus segmentation coloured to depict its correctness
	 * @param aTitle
	 *            The main title for both images
	 * @param aDirectory
	 *            The directory in which to save the images
	 */
	private void saveOutputImage(final ImagePlus aOutlinesImage, final ImagePlus aNucleusImage, final String aTitle, final File aDirectory)
	{

		final File fileImages = new File(aDirectory.getPath());
		if (!fileImages.exists())
		{
			if (fileImages.mkdir())
			{
				IJ.log("Directory is created!");
			}
			else
			{
				IJ.log("ERROR: Failed to create directory!");
			}
		}
		final String imageNameForResultImageOriginal = "\\" + aTitle + "_Outlines" + ".tiff";
		final String imageNameForResultImageBlack = "\\" + aTitle + "_Nucleus" + ".tiff";
		final String name1 = fileImages.getPath() + imageNameForResultImageOriginal;
		final String name2 = fileImages.getPath() + imageNameForResultImageBlack;

		IJ.saveAs(aOutlinesImage, "Tif", name1);
		IJ.saveAs(aNucleusImage, "Tif", name2);
		IJ.log("OutputImage is save as: " + imageNameForResultImageOriginal + " in " + name1);
	}


	/**
	 * Create a dialog to have the user define which image channels are relevant to the feature extraction. Two of the standard channels are the nucleus (DAPI) channel and the actin (mostly phalloidin) channel. Any additional channels can be defined
	 * as well including the type of measurement that should be performed on them. The measurement types consist of averages of intensity in the nucleus, a specified volume around the nucleus seed, the full cell segment or the cell inus the nucleus.
	 *
	 * @param aImage
	 *            The ImagePlus on which the feature extraction will take place. This determines the number of channels to be chosen.
	 *
	 * @return The List of TypedChannels (channel plus measurement type String) that have been selected; this includes the standard channels.
	 */
	private List<TypedChannel> selectChannels(final ImagePlus aImage)
	{
		final String[] channelPrefs = { Prefs.get(NucleiSegmentationParameters.FE_ADDITIONAL_CHANNEL_1, "0"), Prefs.get(NucleiSegmentationParameters.FE_ADDITIONAL_CHANNEL_2, "0"),
				Prefs.get(NucleiSegmentationParameters.FE_ADDITIONAL_CHANNEL_3, "0"), Prefs.get(NucleiSegmentationParameters.FE_ADDITIONAL_CHANNEL_4, "0") };
		final String[] measurePrefs = { Prefs.get(NucleiSegmentationParameters.FE_ADDITIONAL_MEASUREMENT_1, NUCLEAR), Prefs.get(NucleiSegmentationParameters.FE_ADDITIONAL_MEASUREMENT_2, NUCLEAR),
				Prefs.get(NucleiSegmentationParameters.FE_ADDITIONAL_MEASUREMENT_3, NUCLEAR), Prefs.get(NucleiSegmentationParameters.FE_ADDITIONAL_MEASUREMENT_4, NUCLEAR) };
		final String dapiChannelPref = Prefs.get(NucleiSegmentationParameters.NS_NUCLEUS_CHANNEL, "1");
		final String actinChannelPref = Prefs.get(NucleiSegmentationParameters.NS_ACTIN_CHANNEL, "2");

		final int channels = aImage.getNChannels();
		final GenericDialog dialog = new GenericDialog("Set channels");
		final String[] channelchooser = new String[channels + 1];
		for (int i = 0; i <= channels; i++)
		{
			channelchooser[i] = i + "";
		}
		final String[] channelType = { NUCLEAR_CENTER, NUCLEAR, CELL, NUCLEUS_SURROUND, CELL_NONUCLEUS };

		dialog.addMessage("Please select which signal is on what image channel.\nSelect 0 to ignore a channel.");
		dialog.addChoice("Nucleus channel", channelchooser, dapiChannelPref);
		dialog.addChoice("Actin channel", channelchooser, actinChannelPref);
		final Component message = dialog.getMessage();
		final Font underlineFont = message.getFont();
		final Map<TextAttribute, Object> attributes = new HashMap<>(underlineFont.getAttributes());
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
		dialog.addMessage("Additional channels:", underlineFont.deriveFont(attributes));
		for (int i = 0; i < NR_OF_ADDITIONAL_CHANNELS; i++)
		{
			dialog.addChoice("Additional channel " + (i + 1), channelchooser, channelPrefs[i]);
			dialog.addChoice("Additional channel type " + (i + 1), channelType, measurePrefs[i]);
		}

		@SuppressWarnings("unchecked")
		final Vector<Choice> choices = dialog.getChoices();
		boolean nonChoiceFound = false;
		for (int i = 0; i < NR_OF_ADDITIONAL_CHANNELS; i++)
		{
			// Set all but the first additional channel choices to disabled
			Choice nextChoice = choices.get((2 * i) + 2); // 2 = nucleus + actin
			if (nextChoice.getSelectedItem().equals(NO_CHANNEL) && nonChoiceFound)
			{
				nextChoice.setEnabled(false);
				// And the measure type choice as well
				nextChoice = choices.get((2 * i) + 3);
				nextChoice.setEnabled(false);
			}
			else if (nextChoice.getSelectedItem().equals(NO_CHANNEL))
			{
				nonChoiceFound = true;
			}
		}

		dialog.addDialogListener(new DialogListener()
		{
			// This listener will enable and disable subsequent choices if a channel choice is (de)selected.
			@Override
			public boolean dialogItemChanged(final GenericDialog aDialog, final AWTEvent aEvent)
			{
				if (aEvent instanceof ItemEvent)
				{
					final Choice changedChoice = (Choice) ((ItemEvent) aEvent).getItemSelectable();
					int changedChoiceIndex = -1;
					@SuppressWarnings("unchecked")
					final Vector<Choice> choices = aDialog.getChoices();

					// First find the last enabled channel choice.
					// Note that the first two choices are always nucleus and actin and next are a number of pairs of a channel choice and a type of measure
					int lastEnabledIndex = -1;
					for (int i = choices.size() - 2; i > 1; i = i - 2)
					{
						final Choice currentChoice = choices.get(i);
						if (changedChoice.equals(currentChoice))
						{
							changedChoiceIndex = i;
						}
						if (currentChoice.isEnabled() && lastEnabledIndex < 1)
						{
							// Working backward, so the first one found is the last enabled one
							lastEnabledIndex = i;
						}
					}

					if (changedChoiceIndex > 1)
					{
						if (lastEnabledIndex < choices.size() - 2 && changedChoiceIndex == lastEnabledIndex && !changedChoice.getSelectedItem().equals(NO_CHANNEL))
						{
							// The changed choice is the last enabled one, there are more choices after it (to enable) and it has been changed to a valid choice
							// Enable the next two choices (one channel and one measure type)
							choices.get(lastEnabledIndex + 2).setEnabled(true);
							choices.get(lastEnabledIndex + 3).setEnabled(true);
						}
						else if (changedChoiceIndex != lastEnabledIndex && changedChoice.getSelectedItem().equals(NO_CHANNEL))
						{
							// A choice (not the last one) is set to no channel: clean up the following choices and disable them
							for (int i = changedChoiceIndex + 2; i <= lastEnabledIndex; i = i + 2) // Start at the next channel choice and end at the last enabled one
							{
								final Choice channelChoice = choices.get(i);
								channelChoice.setEnabled(false);
								channelChoice.select(NO_CHANNEL);
								choices.get(i + 1).setEnabled(false);
							}
						}
					}
					aDialog.revalidate();
					aDialog.repaint();
				}

				return true;
			}
		});

		dialog.showDialog();

		final List<TypedChannel> resultChannels = new ArrayList<>();
		resultChannels.add(new TypedChannel(Integer.parseInt(dialog.getNextChoice()), NUCLEAR));
		resultChannels.add(new TypedChannel(Integer.parseInt(dialog.getNextChoice()), CELL));
		for (int i = 2; i < dialog.getChoices().size(); i = i + 2)
		{
			resultChannels.add(new TypedChannel(Integer.parseInt(dialog.getNextChoice()), dialog.getNextChoice()));
		}

		Prefs.set(NucleiSegmentationParameters.NS_NUCLEUS_CHANNEL, resultChannels.get(0).channelNr);
		Prefs.set(NucleiSegmentationParameters.NS_ACTIN_CHANNEL, resultChannels.get(1).channelNr);
		Prefs.set(NucleiSegmentationParameters.FE_ADDITIONAL_CHANNEL_1, resultChannels.get(2).channelNr);
		Prefs.set(NucleiSegmentationParameters.FE_ADDITIONAL_MEASUREMENT_1, resultChannels.get(2).channelType);
		Prefs.set(NucleiSegmentationParameters.FE_ADDITIONAL_CHANNEL_2, resultChannels.get(3).channelNr);
		Prefs.set(NucleiSegmentationParameters.FE_ADDITIONAL_MEASUREMENT_2, resultChannels.get(3).channelType);
		Prefs.set(NucleiSegmentationParameters.FE_ADDITIONAL_CHANNEL_3, resultChannels.get(4).channelNr);
		Prefs.set(NucleiSegmentationParameters.FE_ADDITIONAL_MEASUREMENT_3, resultChannels.get(4).channelType);
		Prefs.set(NucleiSegmentationParameters.FE_ADDITIONAL_CHANNEL_4, resultChannels.get(5).channelNr);
		Prefs.set(NucleiSegmentationParameters.FE_ADDITIONAL_MEASUREMENT_4, resultChannels.get(5).channelType);
		Prefs.savePreferences();

		return resultChannels;
	}


	/**
	 * Set the parameters used in this feature extraction plugin. This, for now, includes a setting for dam usage and a setting for migration mode analysis if an actin channel is available.
	 *
	 * @return True if action migration mode analysis needs to take place, false otherwise.
	 */
	private Boolean selectFeatureExtractionParameters()
	{
		final boolean migrationModeAnalysis = Prefs.get(NucleiSegmentationParameters.FE_MIGRATION_MODE_MEASURE, true);
		final boolean dapiDams = Prefs.get(NucleiSegmentationParameters.WS_DAPI_DAMS, true);
		final boolean actinDams = Prefs.get(NucleiSegmentationParameters.WS_ACTIN_DAMS, false);
		final boolean excludeBorder = Prefs.get(NucleiSegmentationParameters.FE_EXCLUDE_BORDER, false);
		final int borderZone = (int) Prefs.get(NucleiSegmentationParameters.FE_BORDER_ZONE, 3);
		final boolean excludeSize = Prefs.get(NucleiSegmentationParameters.FE_EXCLUDE_SIZE, false);
		final int exclusionSize = (int) Prefs.get(NucleiSegmentationParameters.FE_EXCLUSION_SIZE, 100);

		final GenericDialog gd = new GenericDialog("Select available features");

		gd.addMessage("Settings for the segments");
		gd.addCheckbox("Segmented images are calculated with dams", dapiDams);

		if (this.actinImage != null)
		{
			gd.addMessage("Settings for the Actin segments");
			gd.addCheckbox("Migration Mode Analysis", migrationModeAnalysis);
			gd.addCheckbox("Segmented Images (Actin) are calculated with dams", actinDams);
		}

		gd.addMessage("Settings for post processing");
		gd.addCheckbox("Exclude cells with a nucleus touching/crossing any border of the image", excludeBorder);
		gd.addNumericField("Border exclusion zone width", borderZone, 0);
		gd.addCheckbox("Exclude cells with a very small nucleus volume", excludeSize);
		gd.addNumericField("Size exclusion volume", exclusionSize, 0);

		gd.showDialog();
		Boolean migrationMode = null;

		if (gd.wasOKed())
		{
			this.calculateDams[0] = gd.getNextBoolean();

			if (this.actinImage != null)
			{
				migrationMode = gd.getNextBoolean();
				this.calculateDams[1] = gd.getNextBoolean();
			}
			else
			{
				migrationMode = false;
			}

			this.excludeBorderNuclei = gd.getNextBoolean();
			this.exclusionZone = Integer.valueOf((int) gd.getNextNumber());
			this.excludeTooSmallNuclei = gd.getNextBoolean();
			this.smallNucleusSize = Integer.valueOf((int) gd.getNextNumber());
		}

		Prefs.set(NucleiSegmentationParameters.FE_MIGRATION_MODE_MEASURE, migrationMode);
		Prefs.set(NucleiSegmentationParameters.WS_DAPI_DAMS, this.calculateDams[0]);
		Prefs.set(NucleiSegmentationParameters.WS_ACTIN_DAMS, this.calculateDams[1]);
		Prefs.set(NucleiSegmentationParameters.FE_EXCLUDE_BORDER, this.excludeBorderNuclei);
		Prefs.set(NucleiSegmentationParameters.FE_BORDER_ZONE, this.exclusionZone);
		Prefs.set(NucleiSegmentationParameters.FE_EXCLUDE_SIZE, this.excludeTooSmallNuclei);
		Prefs.set(NucleiSegmentationParameters.FE_EXCLUSION_SIZE, this.smallNucleusSize);
		Prefs.savePreferences();

		return migrationMode;
	}


	/**
	 * Handle the selection of the image and marker files via a series of dialogs.
	 *
	 * @return The list of possible marker files or null if the selection process was aborted for some reason.
	 */
	private File[] selectInputFiles()
	{
		// Check the active amount of windows and make a list of names to select the original image and the segmented images
		final int amountWindows = WindowManager.getImageCount();
		if (amountWindows < 2)
		{
			IJ.log("Error: There are not enhoug images open");
			return null;
		}

		final String[] imageNames = new String[amountWindows + 1];
		imageNames[0] = NONE;
		for (int i = 0; i < amountWindows; i++)
		{
			imageNames[i + 1] = WindowManager.getImage(i + 1).getTitle();
		}

		// Ask the which is the original image
		if (!selectOriginalImage(imageNames))
		{
			return null;
		}

		// Select all the segmented images (nucleus and optionally actin)
		if (!selectSegmentedImages(imageNames))
		{
			return null;
		}

		// Select the directory where the marker files (not images) are located
		this.workingDir = NucleiSegmentationParameters.getWorkingDirectory();
		if (this.workingDir == null)
		{
			return null;
		}
		final File markerFilesDir = NucleiSegmentationParameters.getMarkerFilesDir(this.workingDir);

		// Make an array with the file names in the selected folder to select the manual point file
		final File[] filenamesFile = markerFilesDir.listFiles();

		return filenamesFile;
	}


	/**
	 * A Dialog asks the user to select the original image, which can be a 2D, 3D or a hyperstack image. The user can also assign an adjusted version of the original image as input. This adjusted image will then be used as the base for the output
	 * images.
	 *
	 * @param aNames
	 *            The list of names from which the user can choose. The first name is taken as default.
	 * @return False if the Dialog has been cancelled has been cancelled, true otherwise.
	 */
	private boolean selectOriginalImage(final String[] aNames)
	{
		ImagePlus originalImage = null;

		final boolean saveImagesPref = Prefs.get(NucleiSegmentationParameters.FE_SAVE_RESULTS, false);
		final boolean manualMarkersPref = Prefs.get(NucleiSegmentationParameters.FE_MANUAL_MARKERS, false);

		while (originalImage == null)
		{
			final GenericDialog gd = new GenericDialog("Original image");
			gd.addMessage("Select the original image and, optionally, an adjusted version of the original image, \n" + "which will be used for the output images");
			gd.addChoice("Original image", aNames, aNames[0]);
			gd.addChoice("Adjusted image for output", aNames, aNames[0]);
			gd.addMessage(""); // Add an empty line for readability
			gd.addCheckbox("Save the results", saveImagesPref);
			gd.addCheckbox("Include manual markers", manualMarkersPref);
			gd.showDialog();

			if (gd.wasCanceled())
			{
				return false;
			}

			final String originalImageName = gd.getNextChoice();
			final String originalImageName2 = gd.getNextChoice();
			this.saveImages = gd.getNextBoolean();
			this.manualMarkers = gd.getNextBoolean();
			originalImage = WindowManager.getImage(originalImageName);
			this.originalDir = new File(originalImage.getOriginalFileInfo().directory);
			this.resultImage = WindowManager.getImage(originalImageName2);
		}

		Prefs.set(NucleiSegmentationParameters.FE_SAVE_RESULTS, this.saveImages);
		Prefs.set(NucleiSegmentationParameters.FE_MANUAL_MARKERS, this.manualMarkers);
		Prefs.savePreferences();

		// Check if the original image is a hyperstack or Z-Stack
		// If it is a hyperstack, split the different channels
		if (originalImage.getNChannels() > 1)
		{
			final List<TypedChannel> channels = selectChannels(originalImage);

			if (channels.get(0).channelNr == 0)
			{
				return false; // No DAPI channel chosen
			}

			final ImagePlus[] channel = ChannelSplitter.split(originalImage);
			this.dapiImage = channel[channels.get(0).channelNr - 1];
			this.dapiImage.setTitle(originalImage.getShortTitle());
			if (channels.get(1).channelNr != 0)
			{
				this.actinImage = channel[channels.get(1).channelNr - 1];
			}

			this.alternateChannels = new ArrayList<>();
			for (int channelNr = 2; channelNr < channels.size(); channelNr++)
			{
				final TypedChannel nextChannel = channels.get(channelNr);
				if (nextChannel.channelNr != 0)
				{
					nextChannel.channelImage = channel[nextChannel.channelNr - 1];
					this.alternateChannels.add(nextChannel);
				}
				else
				{
					break;
				}
			}

			// Save memory by closing the unneeded images
			for (int i = 0; i < channel.length; i++)
			{
				if (i != channels.get(0).channelNr - 1 && i != channels.get(1).channelNr - 1 && i != channels.get(2).channelNr - 1)
				{
					channel[i].close();
				}
			}
		}
		else
		{
			this.dapiImage = originalImage;
		}

		if (this.resultImage == null)
		{
			this.resultImage = this.dapiImage.duplicate();
			this.resultImage.show();
		}

		return true;
	}


	/**
	 * This Dialog asks the user for the input of segmented images. The Dialog can be cancelled.
	 *
	 * @param aimages,
	 *            the list where the images will be located
	 * @param anames,
	 *            a array with names of the active images in FIJI @return, if the dialogue was oked
	 */
	private boolean selectSegmentedImages(final String[] aNames) // Dialog for the Labeled nucleus images
	{
		while (this.dapiSegments == null)
		{
			final GenericDialog gd = new GenericDialog("Options");
			gd.addMessage("Please select the segmented images\n ");
			gd.addChoice("Nucleus segmented image", aNames, aNames[0]);
			gd.addChoice("Actin segmented image (optional)", aNames, aNames[0]);
			gd.showDialog();

			if (gd.wasCanceled())
			{
				return false;
			}

			final String imageIndex1 = gd.getNextChoice();
			final String imageIndex2 = gd.getNextChoice();

			if (imageIndex1 != NONE)
			{
				this.dapiSegments = WindowManager.getImage(imageIndex1);
			}

			if (imageIndex2 != NONE)
			{
				this.actinSegments = WindowManager.getImage(imageIndex2);
			}
		}

		// Check image sizes
		final int width = this.dapiImage.getWidth();
		final int height = this.dapiImage.getHeight();
		if (width != this.dapiSegments.getWidth() || height != this.dapiSegments.getHeight()
				|| (this.actinSegments != null && (width != this.actinSegments.getWidth() || height != this.actinSegments.getHeight())))
		{
			IJ.error("Feature_Extractor_3D input error", "Error: segmented images must have the same size as the original image");
			return false;
		}

		return true;
	}


	/**
	 * Calculate and set the distance of all cells compared to the spheroid centre and edge.
	 *
	 * @param aCells
	 *            The list of Cell3Ds to process
	 * @param aCoreCentre
	 *            The Coordinates of the spheroid centre
	 * @param aCoreRadius
	 *            The radius of the spheroid
	 */
	private void setDistanceToSpheroid(final Cell3D[] aCells, final Spheroid aSpheroid)
	{
		for (final Cell3D cell : aCells)
		{
			final Nucleus3D nucleus = cell.getNucleus();
			nucleus.setDistanceToCentre(aSpheroid.getDistanceFromPointWithZCoefficient(nucleus.getSeed()));
			nucleus.setDistanceToCore(aSpheroid.getRadiusDistanceFromPointWithZCoefficient(nucleus.getSeed()));
		}
	}
}
