package todo;

import ij.plugin.PlugIn;

/****
 * This plugins use the following plugins:]-MorpholibJ-Measure 3D-Particle analyzer 3D-Region adjacency graph-3D ImageJ Suite-3D Intensity measure-3D Geometric measure-3D Shape Measure****
 *
 * @author Esther
 *
 */
public class Feature_Extractor_Trainer implements PlugIn
{
	// private class Labeled_Coordinate
	// {
	// Coordinates coordinates;
	// int label;
	// int grayValue;
	// String migrationMode;
	//
	//
	// /**
	// *
	// * @param aLabel
	// * @param aXValue
	// * @param aYValue
	// * @param aZValue
	// */
	// public Labeled_Coordinate(final int aLabel, final Coordinates aCoordinates, final int aValue, final String aMigrationMode)
	// {
	// this.coordinates = aCoordinates;
	// this.label = aLabel;
	// this.grayValue = aValue;
	// this.migrationMode = aMigrationMode;
	// }
	//
	//
	// public Coordinates getCoordinates()
	// {
	// return this.coordinates;
	// }
	//
	//
	// public int getGrayValue()
	// {
	// return this.grayValue;
	// }
	//
	//
	// /**
	// * @return the label of the Labled_Coordinate
	// */
	// public int getLabel()
	// {
	// return this.label;
	// }
	//
	//
	// public String getMigrationMode()
	// {
	// return this.migrationMode;
	// }
	//
	//
	// public double getXCoordinate()
	// {
	// return this.coordinates.getXcoordinate();
	// }
	//
	//
	// public double getYCoordinate()
	// {
	// return this.coordinates.getYcoordinate();
	// }
	//
	//
	// public double getZCoordinate()
	// {
	// return this.coordinates.getZcoordinate();
	// }
	//
	//
	// /**
	// * @return: label (xValue, yValue, zValue) value
	// */
	// @Override
	// public String toString()
	// {
	// final String toString = this.label + " (" + getXCoordinate() + "," + getYCoordinate() + "," + getZCoordinate() + ") " + this.grayValue;
	// return toString;
	// }
	// }

	// private static final String YES = "Add extra segmented images", NO = "Next step", CANCEL = "Exit";
	// private static final String NONE = "None";
	// private final boolean[] calculateDams = new boolean[2];
	// private int channels;
	// private int actinChannel;
	// private double distance;
	// private double threhsold;
	// private double minNoActinDistance;
	// private double sizeLine;
	// private boolean measureActinSignal;
	// private final List<ImagePlus[]> originalImage = null;
	// private final List<ImagePlus> segmentedImages = new ArrayList<>();
	// private double[][] amountOfNuclei;
	// private double[] amountOfNucleiMigration;
	// private int slices;
	// private boolean saveImages;
	// private final boolean[] particleMeasure = { true, true, true, true, true, true };
	// private final boolean[] suite3DMeasure = { true, true, true, true, true, true, true, true, true, true, true, true, true, true };

	/**
	 * Method countSeedsAndAddToNucleus, count the amount of seeds which are located : 0= In the nucleus 1= double in the nucleus 2= out the nucleus And returns these values in an int array
	 *
	 * @param listOfSeeds
	 * @param cells
	 * @return
	 */
	// private int[] countSeedsAndAddToNucleus(final List<Labeled_Coordinate> listOfSeeds, final Cell3D[] aCells)
	// {
	//
	// int seedInNucleus = 0;
	// int seedDoubleInNucleus = 0;
	// int seedsWithoutNucleus = 0;
	//
	// boolean contain = false;
	// // If the nucleus and the seed contains the same value the seeds is added to the nucleus
	// for (final Labeled_Coordinate seed : listOfSeeds)
	// {
	// for (final Cell3D cell : aCells)
	// {
	// final Nucleus3D nucleus = cell.getNucleus();
	// if (seed.getLabel() == nucleus.getLabel())
	// {
	// contain = true;
	// if (nucleus.getSeed() != null)
	// {
	// seedDoubleInNucleus = seedDoubleInNucleus + 1;
	// }
	// else
	// {
	// seedInNucleus = seedInNucleus + 1;
	// }
	// nucleus.setSeed(seed.getCoordinates(), seed.getGrayValue());
	// break;
	// }
	// }
	// if (!contain)
	// {
	// seedsWithoutNucleus = seedsWithoutNucleus + 1;
	// }
	// }
	// final int[] seedsInDoubleWihtoutNucleus = { seedInNucleus, seedDoubleInNucleus, seedsWithoutNucleus };
	// return seedsInDoubleWihtoutNucleus;
	// }
	//
	//
	// private void createResultsTableSummaryOfTheImages(final boolean migrationmode, final List<Labeled_Coordinate> listOfMarkers, final ResultsTable resultsSum, final String shortTitle,
	// final Cell3D_Group nucleusGroup, final double amount, final List<Labeled_Coordinate> listOfSeeds, final int[] seedsInDoubleWihtoutNucleus, final int[] markerInDoubleWithoutNucleus,
	// final boolean excludeBorderNuclei, final boolean aLogisticRegression, final boolean aSelectionOnSize)
	// {
	//
	// resultsSum.addValue("Total seeds", listOfSeeds.size());
	// resultsSum.addValue("Seeds without nucleus", seedsInDoubleWihtoutNucleus[2]);
	// resultsSum.addValue("Seeds double in nucleus", seedsInDoubleWihtoutNucleus[1]);
	// resultsSum.addValue("Seeds in nucleus", seedsInDoubleWihtoutNucleus[0]);
	//
	// }

	// /**
	// * Method searchFile opens the Pointdetection file and read the coordinates of all the markers/seeds This method creats a list with labeled_coordinates
	// *
	// * @param title
	// * @param directory
	// * @param filenames
	// * @param twoD
	// * @return, a list with labeled_Coordinate
	// */
	//
	// private boolean[] DialogueMeasureFeatures()
	// {
	// final GenericDialog gd = new GenericDialog("Select available features or post-processig options");
	//
	// if (this.slices > 1)
	// {
	// gd.addCheckbox("Logisitic regression", false);
	// gd.addCheckbox("Create tabel to determine LogisticRegresion", false);
	// }
	// return null;
	// }

	// /**
	// * Dialog to get parameters to measure actin-line connectivity.
	// */
	// public void DialogueMigrationModeAnalysis() // Dialog for the original image
	// {
	// final GenericDialog gd = new GenericDialog("Settings For the nearstNeighbour");
	// gd.addCheckbox("Analyse actin signal", true);
	// gd.addNumericField("Set the Actin channel", 1, 0);
	// gd.addNumericField("Set the distance between nuclei (microns)", 100, 0);
	// gd.addNumericField("Set the Threshold", 100, 0);
	// gd.addNumericField("Set the minimal distance no actin singal (microns)", 15, 0);
	// gd.addNumericField("Set size of perpendicular line (pixels) ", 5, 0);
	//
	// gd.showDialog();
	// this.measureActinSignal = gd.getNextBoolean();
	// this.actinChannel = (int) gd.getNextNumber();
	// this.distance = gd.getNextNumber();
	// this.threhsold = gd.getNextNumber();
	// this.minNoActinDistance = gd.getNextNumber();
	// this.sizeLine = gd.getNextNumber();
	// }

	/**
	 * This Dialogue asks the user for the input of segmented images, unless the use clicked on cancel
	 *
	 * @param aimages,
	 *            the list where the images will be located
	 * @param anames,
	 *            a array with names of the active images in FIJI @return, if the dialogue was oked
	 */
	// private String DialogueSegmentedImage(final String[] aNames) // Dialog for the Labeled nucleus images
	// {
	// final GenericDialog gd = new GenericDialog("Options");
	// gd.setCancelLabel(CANCEL);
	// gd.enableYesNoCancel(YES, NO);
	//
	// gd.addMessage("Select the point detection methods of interest\n ");
	// gd.addChoice("Segmented Image " + (this.segmentedImages.size() + 1), aNames, aNames[0]);
	// gd.addChoice("Segmented Image " + (this.segmentedImages.size() + 2), aNames, aNames[0]);
	// gd.showDialog();
	//
	// final String imageIndex1 = gd.getNextChoice();
	// final String imageIndex2 = gd.getNextChoice();
	//
	// if (imageIndex1 != NONE)
	// {
	// this.segmentedImages.add(WindowManager.getImage(imageIndex1));
	// }
	//
	// if (imageIndex2 != NONE)
	// {
	// this.segmentedImages.add(WindowManager.getImage(imageIndex2));
	// }
	//
	// String result = "";
	// if (gd.wasCanceled())
	// {
	// result = CANCEL;
	// }
	// else if (gd.wasOKed())
	// {
	// result = YES;
	// }
	// else
	// {
	// result = NO;
	// }
	//
	// return result;
	// }

	@Override
	public void run(final String arg)
	{
		// // Image variables
		// final Visualiser draw = new Visualiser();
		// final ResultsTable resultsSum = new ResultsTable();
		//
		// // Check the active amount of windows and make a list of names to select the original image and the segmented images
		// final int amountWindows = WindowManager.getImageCount();
		// if (amountWindows < 2)
		// {
		// IJ.log("Error: There are not enhoug images open");
		// return;
		// }
		//
		// final String[] imageNames = new String[amountWindows + 1];
		// imageNames[0] = NONE;
		// for (int i = 0; i < amountWindows; i++)
		// {
		// imageNames[i + 1] = WindowManager.getImage(i + 1).getTitle();
		// }
		//
		// // Dialogue 'DialogueOriginalImage' ask the input of the original image
		// // boolean wasCanceld = false;
		// // do
		// // {
		// // wasCanceld = DialogueOriginalImage(imageNames);
		// // if (wasCanceld == true)
		// // {
		// // return;
		// // }
		// // } while (this.originalImage == null);
		//
		// final String nameOriginalImage = subTitle(this.originalImage.get(0)[0]);
		//
		// // Check if the original image is a 2D image
		// this.slices = this.originalImage.get(0)[0].getNSlices();
		// this.channels = this.originalImage.get(0)[0].getNChannels();
		//
		// // Check if the original image is a hyperstack or Z-Stack
		// // If it is a hyperstack, split the different channels
		// if (this.channels > 1)
		// {
		// final ImagePlus[] channel = ChannelSplitter.split(this.originalImage.get(0)[0]);
		// this.originalImage.add(channel);
		// }
		// else
		// {
		// final ImagePlus[] channel = { null };
		// this.originalImage.add(channel);
		// }
		//
		// // Second dialog to select all the nucleus labeled images until the dialog was cancelled
		//
		// String yesNoChanceld;
		// do
		// {
		// yesNoChanceld = DialogueSegmentedImage(imageNames);
		// } while (yesNoChanceld.equals(YES) || yesNoChanceld.equals(NO) && this.segmentedImages.size() == 0);
		// if (yesNoChanceld.equals(CANCEL))
		// {
		// return;
		// }
		// final int width = this.originalImage.get(0)[0].getWidth();
		// final int height = this.originalImage.get(0)[0].getHeight();
		// final int slices = this.originalImage.get(0)[0].getNSlices();
		//
		// // TODO misschien moet ik dit ook bij de create_Marker_image en Marker-Controlled Watershed implementeren
		// for (int i = 0; i < this.segmentedImages.size(); i++)
		// {
		// if (width != this.segmentedImages.get(i).getWidth() || height != this.segmentedImages.get(i).getHeight())
		// {
		// IJ.error("Feature_Extractor_3D input error", "Error: input" + " and segmented images must have the same size as the original image");
		// return;
		// }
		// }
		// this.segmentedImages.get(0).getNChannels();
		//
		// // Dialogue to select the directory where the marker files are located
		// final String messageInPutFile = "Select the directory where the marker files are located";
		// final String prefInputFile = "Feature_Extractor_3D.InputDir";
		// final File directoryInputFile = fileLoadOrFileSave(messageInPutFile, prefInputFile, "Directory");
		// if (directoryInputFile == null)
		// {
		// return;
		// }
		//
		// // Make an array with the file names in the selected folder to select the manual point file
		// final File[] filenamesFile = directoryInputFile.listFiles();
		//
		// final String manualFileName = selectManualMarkerFile(filenamesFile);
		// if (manualFileName == null)
		// {
		// return;
		// }
		//
		// final File directoryOutputFile = null;
		// // if (this.saveImages == true)
		// // {
		// // // Select the output directory
		// // final String messageOutPutFile = "Select the directory where the results images need to be saved";
		// // final String prefOutputFile = "Feature_Extractor_3D.OutputDir";
		// // directoryOutputFile = fileLoadOrFileSave(messageOutPutFile, prefOutputFile, "Directory");
		// // if (directoryOutputFile == null)
		// // {
		// // return;
		// // }
		// // }
		//
		// // Dialogue to ask the
		// final boolean[] measurefeatures = DialogueMeasureFeatures();
		// // if (measurefeatures[4] == true)
		// // {
		// // DialogueMigrationModeAnalysis();
		// // }
		//
		// final boolean excludeBorderNuclei = measurefeatures[0];
		// final boolean selectionOnSize = measurefeatures[5];
		// final boolean logisticRegression = measurefeatures[1];
		// final boolean logisticRegressionTabel = measurefeatures[2];
		// final boolean secondChannel = measurefeatures[3];
		// final boolean migrationmode = measurefeatures[4];
		//
		// // If there is a marker file selected extract all the markers
		// final List<Coordinates> markerCoordinates = new ArrayList<>();
		// List<Labeled_Coordinate> listOfMarkers = null;
		// if (manualFileName != NONE)
		// {
		// final String shortTitleFileMarker = manualFileName.substring(manualFileName.indexOf("Markers_"), manualFileName.lastIndexOf("."));
		// listOfMarkers = searchFile(shortTitleFileMarker, directoryInputFile, filenamesFile, markerCoordinates);
		// }
		//
		// // For each Segmented image do:
		// for (int i = 0; i < this.segmentedImages.size(); i++)
		// {
		// if (this.segmentedImages.get(i) != null)
		// {
		// final String titleImage = subTitle(this.segmentedImages.get(i));
		// IJ.log("Analyze 3D number " + (i + 1) + ", Name: " + titleImage);
		//
		// // Create names which will be used to name and save the files
		// final String shortTitleFile = titleImage.substring(titleImage.indexOf("MCWatershed") + 12, titleImage.length());
		//
		// // Measure all the features of the nucleus
		// Cell3D[] cells = null;
		//
		// // If original image is a hyperstack measure the features on the separated DAPI signal
		// if (this.originalImage.get(2)[0] != null)
		// {
		//
		// cells = featureMeasurement(2, i, this.particleMeasure, this.suite3DMeasure);
		// // Draw the nucleus outlines on the adjusted image
		// if (this.originalImage.get(1)[0] != null)
		// {
		// draw.createBackgroundResultImages(this.originalImage.get(1), titleImage);
		// }
		// // Draw the nucleus outlines on the original image
		// else if (this.originalImage.get(1)[0] == null)
		// {
		// draw.createBackgroundResultImages(this.originalImage.get(2), titleImage);
		// }
		// }
		// // When the image is a Z-stack of 2D image
		// else if (this.originalImage.get(2)[0] == null)
		// {
		// cells = featureMeasurement(0, i, this.particleMeasure, this.suite3DMeasure);
		// // Draw the nucleus outlines on the adjusted image
		// if (this.originalImage.get(1)[0] != null)
		// {
		// draw.createBackgroundResultImages(this.originalImage.get(1), titleImage);
		// }
		// // Draw the nucleus outlines on the original
		// else if (this.originalImage.get(1)[0] == null)
		// {
		// draw.createBackgroundResultImages(this.originalImage.get(0), titleImage);
		// }
		// }
		//
		// // If the exclusion of the border nucleus is true, set nucleus.setBorderNucleus() == true
		// // if (excludeBorderNuclei)
		// // {
		// // excludeBorderNuclei(width, height, slices, cells);
		// // }
		// // if (selectionOnSize)
		// // {
		// // excludeNucleusOnSize(cells);
		// // }
		//
		// // If the logistic regression is true, set nucleus.setNOTSelected() == false
		// if (logisticRegression)
		// {
		// logisticRegression(cells);
		// }
		//
		// // Extract all the seeds that are created by met Point detection method and add them to the nucleus
		// final double amount = cells.length;
		// final List<Coordinates> seedCoords = new ArrayList<>();
		// final List<Labeled_Coordinate> listOfSeeds = searchFile(shortTitleFile, directoryInputFile, filenamesFile, seedCoords);
		// final int[] seedsInDoubleWihtoutNucleus = countSeedsAndAddToNucleus(listOfSeeds, cells);
		//
		// // count the seeds and add them to the nucleus
		// final int[] markerInDoubleWithoutNucleus = null;
		// // if (manualFileName != NONE)
		// // {
		// // markerInDoubleWithoutNucleus = countMarkerAndAddToNucleus(listOfMarkers, cells);
		// // }
		//
		// // Draw the nucleus and the coordinates of the markers and the seeds
		// draw.drawNucleusResults(this.segmentedImages.get(i), cells, logisticRegression, excludeBorderNuclei);
		// draw.drawCoordinate(seedCoords, Color.YELLOW);
		// if (manualFileName != NONE)
		// {
		// draw.drawCoordinate(markerCoordinates, Color.GREEN);
		// }
		//
		// // If logistic regression table == true produce the logstic regression table
		// if (logisticRegressionTabel)
		// {
		// createLogisticRegressionTable(cells);
		// }
		// // if second channel == true measure the stainig on second channel
		// if (secondChannel)
		// {
		// calculateGrayValueSecondChannelFocalPlane(this.originalImage, cells);
		// }
		//
		// final Cell3D_Group nucleusGroup = new Cell3D_Group(cells[0]);
		// final Cell3D_Group nucleusGroupSelection = new Cell3D_Group(cells[0]);
		// for (int k = 1; k < cells.length; k++)
		// {
		// nucleusGroup.setMember(cells[k]);
		// if (!cells[k].getNucleus().isBorderNucleus() && !cells[k].getNucleus().isTooSmall())
		// {
		// nucleusGroupSelection.setMember(cells[k]);
		// }
		// }
		//
		// if (migrationmode)
		// {
		// final MigrationModeAnalyser analysis = new MigrationModeAnalyser(this.segmentedImages.get(i), this.originalImage, nucleusGroupSelection, this.actinChannel, this.distance,
		// this.threhsold, this.minNoActinDistance, this.sizeLine, this.measureActinSignal, titleImage);
		// this.amountOfNuclei = analysis.getAmountOfNuclei();
		// this.amountOfNucleiMigration = analysis.getAmountOfNucleiMigrationMode();
		// final ResultsTable resultsperGroup = analysis.getResultsTableGroups();
		// final ImagePlus[] resultsImages = analysis.getResultsImages();
		// // if (this.saveImages == true)
		// // {
		// // saveOutputImage(directoryOutputFile, "saveDirectory", resultsImages);
		// // saveResultsTable(titleImage + "_ResultsPerGroup", directoryOutputFile, resultsperGroup, "saveDirectory");
		// // }
		// }
		//
		// // final ResultsTable mergedTable = createResultsPerNucleusTable(cells);
		// if (mergedTable == null)
		// {
		// IJ.log("ERROR: A nulceus segment can not be correlated to an automated nucleus marker");
		// IJ.log(" Check if the Marker file correspond with the segmented image");
		// return;
		// }
		// mergedTable.show("Results Per Nucleus");
		//
		// // if (this.saveImages == true)
		// // {
		// // saveResultsTable(titleImage + "_NucleusFeatures", directoryOutputFile, mergedTable, "saveDirectory");
		// // }
		//
		// createResultsTableSummaryOfTheImages(migrationmode, listOfMarkers, resultsSum, titleImage, nucleusGroup, amount, listOfSeeds, seedsInDoubleWihtoutNucleus, markerInDoubleWithoutNucleus,
		// excludeBorderNuclei, logisticRegression, selectionOnSize);
		//
		// // if (this.saveImages == true)
		// // {
		// // draw.saveOutputImage(titleImage, directoryOutputFile, "saveDirectory");
		// // }
		// }
		// }
		// resultsSum.show("Results summary");
		// // if (this.saveImages == true)
		// // {
		// // saveResultsTable(nameOriginalImage + "_Summary", directoryOutputFile, resultsSum, "saveDirectory");
		// // }
	}
}
