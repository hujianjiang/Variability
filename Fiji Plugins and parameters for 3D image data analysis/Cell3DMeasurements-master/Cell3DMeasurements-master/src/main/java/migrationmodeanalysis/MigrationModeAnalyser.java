package migrationmodeanalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import configuration.Measurement_Selector;
import data.Cell3D;
import data.Cell3D_Group;
import data.SegmentMeasurements;
import data.Sort_Groups;
import featureextractor.Visualiser;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;

/**
 * Analyses the cell segments and determines if a cell is part of the core or migrating and, if migrating, the mode of migration (single vs collective).
 *
 * @author Merijn van Erp
 *
 */
public class MigrationModeAnalyser
{
	// The different migration mode counter sets
	public final static int TOTAL_COUNT = 0, SPHEROID_COUNT = 1, SINGLE_COUNT = 2, DUAL_COUNT = 3, COLLECTIVE_COUNT = 4;
	// The types of things that are aggregated per counter set
	public final static int NUMBER_COUNT = 0, VOLUME_NUCLEI = 1, VOLUME_CELL = 2;

	// The correct/incorrect/no mode counters per migration mode
	public static final int CORTOT = 0, ERRTOT = 1, NOTOT = 2, CORSIN = 3, ERRSIN = 4;
	public static final int NOSIN = 5, CORCOR = 6, ERRCOR = 7, NOCOR = 8, CORDUO = 9;
	public static final int ERRDUO = 10, NODUO = 11, CORCOL = 12, ERRCOL = 13, NOCOL = 14;
	public static final int FALSEPOS = 15;


	private static void addGroupMeasurements(final Cell3D_Group aCells, final String[] aGroupNames, final ResultsTable aResultsTable)
	{
		for (final String measurementName : aGroupNames)
		{
			if (Measurement_Selector.getMeasurementPreference(measurementName))
			{
				final Double measurement = aCells.getMeanNucleusMeasure(measurementName);
				if (measurement != null)
				{
					aResultsTable.addValue("nucleus " + measurementName, measurement);
				}
			}
		}
	}

	// Class variables
	private final double[][] migrationSetData = new double[5][3];
	private final double[] migrationAccuracyData = new double[16];
	private ResultsTable resultsPerGroup;

	private final ImagePlus[] resultImages = new ImagePlus[3];


	/**
	 * The constructor of the MigrationModeAnalyser which contains all the necessary data to run.
	 *
	 * @param aCells
	 *            The list of cells
	 * @param aDAPIImage
	 *            The DAPI image for visualisation
	 * @param aImageTitle
	 *            The title for the visualisation
	 */
	public MigrationModeAnalyser(final Cell3D[] aCells, final ImagePlus aDAPIImage, final String aImageTitle)
	{
		final List<Cell3D_Group> nucleusGroups = groupBasedOnTouchingNeighbours(aCells);
		sortGroupsOnSize(nucleusGroups);
		extractInfo(nucleusGroups);

		final ImagePlus migrationModeImage = aDAPIImage.duplicate();
		Visualiser.drawCellGroups(migrationModeImage, nucleusGroups);
		this.resultImages[0] = migrationModeImage;
		this.resultImages[0].setTitle(aImageTitle + "_MigrationGroups");
		this.resultImages[0].show();
		this.resultImages[1] = Visualiser.drawMigrationMode(aDAPIImage, nucleusGroups);
		this.resultImages[1].setTitle(aImageTitle + "_GroupMigrationMode");
		this.resultImages[1].show();

		// TODO What to do about the 'correctness' measure
		// this.resultImages[2] = Visualiser.drawCorrectMigrationMode(this.dapiImage, nucleusGroupsTouchingNeighbours);
		// this.resultImages[2].setTitle(this.imageTitle + "_CorrectMigrationMode");
		// this.resultImages[2].show();
	}


	/**
	 * Collect all the migration info and store it in the publicly accessible variables.
	 *
	 * @param aNucleusGroups
	 *            A list of cell groups, where each group is considered to be a separately migration set (or the core of the spheroid)
	 */
	private void extractInfo(final List<Cell3D_Group> aNucleusGroups)
	{
		// Initialise all counters
		double amountNucleusSpheroid = 0, volumeNucleiSpheroid = 0, volumeCellSpheroid = 0;
		double amountNucleiCollective = 0, volumeNucleiCollective = 0, volumeCellsCollective = 0;
		double amountNucleiDualCell = 0, volumeNucleiDualCell = 0, volumeCellDualCell = 0;
		double amountNucleiSingleCell = 0, volumeNucleiSingle = 0, volumeCellSingle = 0;
		double amountNucleiCorrectMigration = 0;
		double amountNucleiWrongMigration = 0;
		double amountNucleuWithoutMigration = 0;
		double amountSingleCellCorrectMigration = 0;
		double amountSingleCellWrongMigration = 0;
		double amountSingleCellWithoutMigration = 0;
		double amountCoreCellsWithoutMigration = 0;
		double amountCoreCellsWithMigration = 0;
		double amountCoreCellsDetectedAsCore = 0;
		double amountDualCellsWithCorrectMigration = 0;
		double amountDualCellsWrongMigration = 0;
		double amountDualCellsWithoutMigration = 0;
		double amountMultiCellsWithCorrectMigration = 0;
		double amountMultiCellsWrongMigration = 0;
		double amountMultiCellsWithoutMigration = 0;
		double amountSingleCellFalsePositives = 0;

		// Go through each cell group to collect data
		for (int h = 0; h < aNucleusGroups.size(); h++)
		{
			final Cell3D_Group nucGroup = aNucleusGroups.get(h);

			// Set the group migration mode and counts cells and total nuclear and cellular volume.
			// Add all data to the right migration-mode totals.
			if (h == 0)
			{
				// The first and largest group of cells is assumed to be the core of the spheroid
				nucGroup.setMigrationmode(Cell3D_Group.CORE);
				amountNucleusSpheroid += nucGroup.getMemberCount();
				volumeNucleiSpheroid += nucGroup.getTotalVolume();
				volumeCellSpheroid += nucGroup.getTotalVolumeCell();
			}
			else if (nucGroup.getMemberCount() > 2)
			{
				nucGroup.setMigrationmode(Cell3D_Group.MULTI);
				amountNucleiCollective += nucGroup.getMemberCount();
				volumeNucleiCollective += nucGroup.getTotalVolume();
				volumeCellsCollective += nucGroup.getTotalVolumeCell();
			}
			else if (nucGroup.getMemberCount() == 2)
			{
				nucGroup.setMigrationmode(Cell3D_Group.DUAL);
				amountNucleiDualCell += nucGroup.getMemberCount();
				volumeNucleiDualCell += nucGroup.getTotalVolume();
				volumeCellDualCell += nucGroup.getTotalVolumeCell();
			}
			else if (nucGroup.getMemberCount() == 1)
			{
				nucGroup.setMigrationmode(Cell3D_Group.SINGLE);
				amountNucleiSingleCell += nucGroup.getMemberCount();
				volumeNucleiSingle += nucGroup.getTotalVolume();
				volumeCellSingle += nucGroup.getTotalVolumeCell();
			}

			for (final Cell3D cell : nucGroup.getMembers())
			{
				cell.setMigrationMode(nucGroup.getMigrationmode());
			}

			amountNucleiCorrectMigration += nucGroup.getNucleusWithCorrectMigrationMode();
			amountNucleiWrongMigration += nucGroup.getNucleusWithWrongMigrationMode();
			amountNucleuWithoutMigration += nucGroup.getNucleusWithoutMigrationMode();

			amountSingleCellCorrectMigration += nucGroup.getSingleCellWithCorrectMigrationMode();
			amountSingleCellWrongMigration += nucGroup.getSingleCellWithWrongMigrationMode();
			amountSingleCellWithoutMigration += nucGroup.getSingleCellWithoutMigrationMode();
			amountSingleCellFalsePositives += nucGroup.getSingleCellFalsePositive();

			amountCoreCellsWithoutMigration += nucGroup.getCoreCellWithoutMigrationMode();
			amountCoreCellsDetectedAsCore += nucGroup.getCoreCellDetectedAsCore();
			amountCoreCellsWithMigration += nucGroup.getCoreCellWithMigrationMode();

			amountDualCellsWithCorrectMigration += nucGroup.getDualCellWithCorrectMigrationMode();
			amountDualCellsWrongMigration += nucGroup.getDualCellWithWrongMigrationMode();
			amountDualCellsWithoutMigration += nucGroup.getDualCellWithoutMigrationMode();

			amountMultiCellsWithCorrectMigration += nucGroup.getMultiCellWithCorrectMigrationMode();
			amountMultiCellsWrongMigration += nucGroup.getMultiCellWithWrongMigrationMode();
			amountMultiCellsWithoutMigration += nucGroup.getMultiCellWithoutMigrationMode();

			IJ.log("Group " + h + " migration mode: " + nucGroup.getMigrationmode() + " Members are:");
			IJ.log(nucGroup.getMemberNames());
		}

		this.resultsPerGroup = summarizePerCellGroup(aNucleusGroups);

		this.resultsPerGroup.show("ResultsOfTheGroup");
		this.migrationSetData[TOTAL_COUNT][NUMBER_COUNT] = amountNucleusSpheroid + amountNucleiCollective + amountNucleiDualCell + amountNucleiSingleCell;
		this.migrationSetData[TOTAL_COUNT][VOLUME_NUCLEI] = volumeNucleiSpheroid + volumeNucleiCollective + volumeNucleiDualCell + volumeNucleiSingle;
		this.migrationSetData[TOTAL_COUNT][VOLUME_CELL] = volumeCellSpheroid + volumeCellsCollective + volumeCellDualCell + volumeCellSingle;
		this.migrationSetData[SPHEROID_COUNT][NUMBER_COUNT] = amountNucleusSpheroid;
		this.migrationSetData[SPHEROID_COUNT][VOLUME_NUCLEI] = volumeNucleiSpheroid;
		this.migrationSetData[SPHEROID_COUNT][VOLUME_CELL] = volumeCellSpheroid;
		this.migrationSetData[SINGLE_COUNT][NUMBER_COUNT] = amountNucleiSingleCell;
		this.migrationSetData[SINGLE_COUNT][VOLUME_NUCLEI] = volumeNucleiSingle;
		this.migrationSetData[SINGLE_COUNT][VOLUME_CELL] = volumeCellSingle;
		this.migrationSetData[DUAL_COUNT][NUMBER_COUNT] = amountNucleiDualCell;
		this.migrationSetData[DUAL_COUNT][VOLUME_NUCLEI] = volumeNucleiDualCell;
		this.migrationSetData[DUAL_COUNT][VOLUME_CELL] = volumeCellDualCell;
		this.migrationSetData[COLLECTIVE_COUNT][NUMBER_COUNT] = amountNucleiCollective;
		this.migrationSetData[COLLECTIVE_COUNT][VOLUME_NUCLEI] = volumeNucleiCollective;
		this.migrationSetData[COLLECTIVE_COUNT][VOLUME_CELL] = volumeCellsCollective;

		this.migrationAccuracyData[CORTOT] = amountNucleiCorrectMigration;
		this.migrationAccuracyData[ERRTOT] = amountNucleiWrongMigration;
		this.migrationAccuracyData[NOTOT] = amountNucleuWithoutMigration;
		this.migrationAccuracyData[CORSIN] = amountSingleCellCorrectMigration;
		this.migrationAccuracyData[ERRSIN] = amountSingleCellWrongMigration;
		this.migrationAccuracyData[NOSIN] = amountSingleCellWithoutMigration;
		this.migrationAccuracyData[CORCOR] = amountCoreCellsDetectedAsCore;
		this.migrationAccuracyData[ERRCOR] = amountCoreCellsWithoutMigration;
		this.migrationAccuracyData[NOCOR] = amountCoreCellsWithMigration;
		this.migrationAccuracyData[CORDUO] = amountDualCellsWithCorrectMigration;
		this.migrationAccuracyData[ERRDUO] = amountDualCellsWrongMigration;
		this.migrationAccuracyData[NODUO] = amountDualCellsWithoutMigration;
		this.migrationAccuracyData[CORCOL] = amountMultiCellsWithCorrectMigration;
		this.migrationAccuracyData[ERRCOL] = amountMultiCellsWrongMigration;
		this.migrationAccuracyData[NOCOL] = amountMultiCellsWithoutMigration;
		this.migrationAccuracyData[FALSEPOS] = amountSingleCellFalsePositives;

		IJ.log("Total amount of cells: " + this.migrationSetData[TOTAL_COUNT][NUMBER_COUNT]);
		IJ.log("Total amount of spheroid cells: " + this.migrationSetData[SPHEROID_COUNT][NUMBER_COUNT]);
		IJ.log("Total amount of collective cells: " + this.migrationSetData[COLLECTIVE_COUNT][NUMBER_COUNT]);
		IJ.log("Total amount of dual cells: " + this.migrationSetData[DUAL_COUNT][NUMBER_COUNT]);
		IJ.log("Total amount of single cells: " + this.migrationSetData[SINGLE_COUNT][NUMBER_COUNT]);
	}


	/**
	 * Get the accuracy data of the migration mode analysis. The resulting list of doubles is the number of correctly, incorrectly or even not-handled migration mode classifications expressed in number of cells. For each type of migration mode all
	 * three numbers are available plus the number of falsely identified single cells.
	 *
	 * Indexes
	 * <p>
	 * Total numbers (correct, error, no mode found): CORTOT = 0, ERRTOT = 1, NOTOT = 2
	 * </p>
	 * <p>
	 * Number of (manual) single cells (correct, error, no mode found): CORSIN = 3, ERRSIN = 4, NOSIN = 5
	 * </p>
	 * <p>
	 * Number of (manual) core cells (correct, error, no mode found): CORCOR = 6, ERRCOR = 7, NOCOR = 8
	 * </p>
	 * <p>
	 * Number of (manual) dual cells (correct, error, no mode found): CORDUO = 9, ERRDUO = 10, NODUO = 11
	 * </p>
	 * <p>
	 * Number of (manual) collective cells (correct, error, no mode found): CORCOL = 12, ERRCOL = 13, NOCOL = 14
	 * </p>
	 * <p>
	 * Number of cells misidentified as single cells: FALSEPOS = 15;
	 * </p>
	 *
	 * @return The list with migration-data accuracy results. For indexes see above.
	 */
	public double[] getMigrationAccuracyData()
	{
		return this.migrationAccuracyData;
	}


	/**
	 * Get the metric data on each type of migration. The data is divided in five sets (total, core, single, dual, and collective in that order) and each set contains the number of cells classified in that migration mode, and the aggregate nucleus
	 * and cell volume of the segments of these cells (resp.).
	 *
	 * @return A double list containing the five sets of data collected on the four migration modes and the combined totals.
	 */
	public double[][] getMigrationSetData()
	{
		return this.migrationSetData;
	}


	/**
	 * Get the images produced by the migration analysis. These consist of one image that colour codes the migration groups individually and one that colours the groups according to their migration mode.
	 *
	 * @return An array of two ImagePlus, the first depicting the different migrating cell groups identified by different colours and the second colour-coding the cell groups based on their migration mode.
	 */
	public ImagePlus[] getResultsImages()
	{
		return this.resultImages;
	}


	/**
	 * Get the ResultsTable that list all the measurements and migration analysis results per migrating cell group (core included).
	 *
	 * @return The list of results per group in a ResultsTable (one line per group).
	 */
	public ResultsTable getResultsPerCellGroup()
	{
		return this.resultsPerGroup;
	}


	/**
	 * Get all cells that can be connected to a seed cell via direct adjacency ('touching') or via other adjacency to other connected cells. The method is recursive until all connection have been found. The to-do list of cells is the pool from which
	 * the connected cells can be taken and this list will be updated by removing found connected cells.
	 *
	 * @param aSeed
	 *            The seed cell to find connected cells. Note that this may be an already connected cell used as seed in a further recursive step.
	 * @param aTodoCells
	 *            The list of potential connected cells. These cells have not been connected to any other cell before. This list will be adapted by removing any connected cell found.
	 *
	 * @return The List of all directly or indirectly connected cells to the seed cell (exclusive, due to recursion)
	 */
	private List<Cell3D> getTouchingNeighbours(final Cell3D aSeed, final List<Cell3D> aTodoCells)
	{
		// Find all directly connected cells
		final ArrayList<Cell3D> neighbours = new ArrayList<>();
		for (final Cell3D cell : aTodoCells)
		{
			if (aSeed.getConnectedNeighbours().contains(cell.getNucleus().getLabel()))
			{
				neighbours.add(cell);
			}
		}

		final List<Cell3D> resultGroup = new ArrayList<>();
		resultGroup.addAll(neighbours);
		// Remove found cells from to-do list to prevent endless recursion
		aTodoCells.removeAll(neighbours);
		// Recurse over all found direct connections.
		for (final Cell3D neighbour : neighbours)
		{
			final List<Cell3D> foundNeighbours = getTouchingNeighbours(neighbour, aTodoCells);
			resultGroup.addAll(foundNeighbours);
		}

		return resultGroup;
	}


	/**
	 * Group the cells together based on direct connection; i.e. each resulting cell group consists of all cells (and only those cells) that have a direct connection ('touch') to at least one other cell in the group. Groups of one cell are allowed
	 * (and in fact the major point of this method).
	 *
	 * @return A list of Cell3D_Groups where each group consists of all Cell3D that have a direct connection to at least one other cell in the group.
	 */
	private List<Cell3D_Group> groupBasedOnTouchingNeighbours(final Cell3D[] aTotalListOfCells)
	{
		final List<Cell3D> filteredCells = new ArrayList<>();
		final List<Cell3D> todoCells = new ArrayList<>();

		// Filter unwanted potential cells from the list
		for (final Cell3D cell : aTotalListOfCells)
		{
			if (!cell.getNucleus().isBorderNucleus() && !cell.getNucleus().isTooSmall()) // Note, these have been calculated only if the user has asked for it
			{
				filteredCells.add(cell);
				todoCells.add(cell);
			}
		}

		// Go through the list of cells and if a non-connected cell is found use it as the base of a new cell group. Next collect all cells that are connected to this new seed cell into group.
		final List<Cell3D_Group> nucleusGroups = new ArrayList<>();
		for (final Cell3D seedCell : filteredCells)
		{
			if (todoCells.contains(seedCell))
			{
				final List<Cell3D> cellGroup = new ArrayList<>();
				cellGroup.add(seedCell); // Start the group. Needed separately as getTouchingNeighbours is recursive and will not include the initial seed.
				todoCells.remove(seedCell);
				cellGroup.addAll(getTouchingNeighbours(seedCell, todoCells));
				nucleusGroups.add(new Cell3D_Group(cellGroup));
			}
		}

		return nucleusGroups;
	}


	/**
	 * Method saveOutputImage save the output images in a directory
	 *
	 * @param aDirectory
	 */
	public void saveResultImages(final File aDirectory)
	{

		final File fileImages = new File(aDirectory.getPath());
		if (!fileImages.exists())
		{
			if (fileImages.mkdir())
			{
				IJ.log("Directory " + fileImages.getName() + " has been created!");
			}
			else
			{
				IJ.log("ERROR: Failed to create directory " + fileImages.getName() + " !");
			}
		}
		final String name1 = fileImages.getPath() + "\\" + this.resultImages[0].getTitle() + ".tiff";
		final String name2 = fileImages.getPath() + "\\" + this.resultImages[1].getTitle() + ".tiff";
		// final String name3 = fileImages.getPath() + "\\" + this.resultImages[2].getTitle() + ".tiff";
		IJ.saveAs(this.resultImages[0], "Tif", name1);
		IJ.saveAs(this.resultImages[1], "Tif", name2);
		// IJ.saveAs(this.resultImages[2], "Tif", name3);
	}


	/**
	 * Remove any empty groups from the group list and sort the list group size (largest to smallest).
	 *
	 * @param aNucleusGroupsToSort
	 *            The list of Cell3D_Groups to sort
	 */
	private void sortGroupsOnSize(final List<Cell3D_Group> aNucleusGroupsToSort)
	{
		final ArrayList<Cell3D_Group> emptyGroups = new ArrayList<>();
		for (final Cell3D_Group cellGroup : aNucleusGroupsToSort)
		{
			if (cellGroup.getMemberCount() == 0 || cellGroup.getMember(0) == null)
			{
				emptyGroups.add(cellGroup);
			}
		}
		aNucleusGroupsToSort.removeAll(emptyGroups);
		Collections.sort(aNucleusGroupsToSort, new Sort_Groups());
	}


	private ResultsTable summarizePerCellGroup(final List<Cell3D_Group> aCellGroups)
	{
		final ResultsTable resultsTable = new ResultsTable();

		for (final Cell3D_Group cellGroup : aCellGroups)
		{
			resultsTable.incrementCounter();

			resultsTable.addValue("Migration mode", cellGroup.getMigrationmode());
			resultsTable.addValue("Manual migration mode", cellGroup.getManualMigrationModeGroup());
			resultsTable.addValue("Amount of cells", cellGroup.getMemberCount());

			resultsTable.addValue("% Nucleus correct migration mode", cellGroup.getPercentageNucleusWithCorrectMigrationMode());
			resultsTable.addValue("% Nucleus wrong migration mode", cellGroup.getPercentageNucleusWrongMigrationMode());
			resultsTable.addValue("% Nucleus without migration mode", cellGroup.getPercentageNucleusWithoutMigrationMode());

			resultsTable.addValue("% Nucleus correctly segmented", cellGroup.getSegmentationPrecision());
			resultsTable.addValue("% Nucleus oversegmented", cellGroup.getPercentageNucleusOverSegmented());
			resultsTable.addValue("% Nucleus undersegmented", cellGroup.getPercentageNucleusUnderSegmented());

			resultsTable.addValue("XCoordinate center", cellGroup.getCenterXYZ()[0]);
			resultsTable.addValue("YCoordinate center", cellGroup.getCenterXYZ()[1]);
			resultsTable.addValue("ZCoordinate center", cellGroup.getCenterXYZ()[2]);

			resultsTable.addValue("Total volume", cellGroup.getTotalVolume());

			addGroupMeasurements(cellGroup, SegmentMeasurements.STANDARD_GROUP_NUCLEUS, resultsTable);

			resultsTable.addValue("Mean nucleus volume", cellGroup.getMeanVolume());
			resultsTable.addValue("Number of voxels nucleus", cellGroup.getMeanNumberOfVoxels());

			addGroupMeasurements(cellGroup, SegmentMeasurements.MORPHOLIBJ_GROUP, resultsTable);
			addGroupMeasurements(cellGroup, SegmentMeasurements.MCIB3D_GROUP, resultsTable);

			resultsTable.addValue("Member core", cellGroup.getCoreCellLabels());
			resultsTable.addValue("Member cluster", cellGroup.getMultiCellLabels());
			resultsTable.addValue("Member dual", cellGroup.getDualCellLabels());
			resultsTable.addValue("Member single", cellGroup.getSingleCellLabels());
			resultsTable.addValue("Member no migration", cellGroup.getCellLabelsWithoutMigrationMode());
		}

		return resultsTable;
	}

}
