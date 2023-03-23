package featureextractor;

import static featureextractor.Feature_Extractor_3D.*;
import static migrationmodeanalysis.MigrationModeAnalyser.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import configuration.Measurement_Selector;
import data.Cell3D;
import data.Cell3D_Group;
import data.Coordinates;
import data.Nucleus3D;
import data.SegmentMeasurements;
import ij.IJ;
import ij.measure.ResultsTable;

/**
 * Helper class to generate data summaries (as ResultsTables) for the feature extractor data.
 *
 * @author Merijn van Erp
 * @author Esther Markus
 *
 */
public class ResultsTableGenerator
{
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


	private static void addGroupMeasurements(final SegmentMeasurements aMeasurements, final String[] aMeasurementNames, final ResultsTable aResultsTable)
	{
		addGroupMeasurements(aMeasurements, aMeasurementNames, "", aResultsTable);
	}


	private static void addGroupMeasurements(final SegmentMeasurements aMeasurements, final String[] aMeasurementNames, final String aPrefix, final ResultsTable aResultsTable)
	{
		for (final String measurementName : aMeasurementNames)
		{
			if (Measurement_Selector.getMeasurementPreference(measurementName))
			{
				final Double measurementValue = aMeasurements.getMeasurement(measurementName);
				if (measurementValue != null)
				{
					aResultsTable.addValue(aPrefix + measurementName, measurementValue);
				}
			}
		}
	}


	/**
	 * Fill a ResultsTable with the data for the entire image instead of per cell/nucleus/group.
	 *
	 * @param aMigrationSetData
	 *            The data set in total numbers such as total number of single cells or the volume of all collective cells
	 * @param aMigrationAccuracyData
	 *            How well are the different types of migration identified by the feature extractor
	 * @param aMarkerAccuracyData
	 *            How well did the segmentation go when compared to a manual nucleus identification
	 * @param aCells
	 *            The identified cells as Cell3D
	 * @param aNumberOfSeeds
	 *            The number of seeds that was detected
	 * @param aTitle
	 *            The title of the image to add to the summary line
	 */
	public static ResultsTable getImageSummary(final double[][] aMigrationSetData, final double[] aMigrationAccuracyData, final int[] aMarkerAccuracyData, final Cell3D_Group aCells,
			final int aNumberOfSeeds, final String aTitle)
	{
		final ResultsTable resultsTable = new ResultsTable();
		resultsTable.incrementCounter(); // Fill the new table
		resultsTable.addValue("Amount", aCells.getMemberCount()); // Add the amount value of the image

		resultsTable.addValue("% Nucleus segmentation precision", aCells.getSegmentationPrecision());
		resultsTable.addValue("% Nucleus oversegmented", aCells.getPercentageNucleusOverSegmented());
		resultsTable.addValue("% Nucleus undersegmented", aCells.getPercentageNucleusUnderSegmented());

		resultsTable.addValue("Nucleus correct segmented", aCells.getNucleusCorrectSegmented());
		resultsTable.addValue("Nucleus oversegmented", aCells.getNucleusOverSegmented());
		resultsTable.addValue("Nucleus undersegmented", aCells.getNucleusUnderSegmented());

		resultsTable.addValue("Nucleus selected", aCells.getNucleusSelected());
		resultsTable.addValue("Nucleus excluded (correct segmented)", aCells.getNucleusExcludedType()[0]);
		resultsTable.addValue("Nucleus excluded (oversegmented)", aCells.getNucleusExcludedType()[1]);
		resultsTable.addValue("Nucleus excluded (undersegmented)", aCells.getNucleusExcludedType()[2]);
		resultsTable.addValue("Nucleus deselected on borderand regression", aCells.getNucleusTwiceExcluded());
		resultsTable.addValue("Excluded nuclei on size and border", aCells.getNucleusExcludedBorderAndSize());
		resultsTable.addValue("Border nucleus", aCells.getNucleusExcludedBorder());
		resultsTable.addValue("Excluded nuclei on size", aCells.getNucleusToSmall());
		resultsTable.addValue("Excluded nucleus", aCells.getNucleusNOTSelected());

		// If there is migration data, add it here
		if (aMigrationSetData != null)
		{
			final double totalNuclei = aMigrationSetData[TOTAL_COUNT][NUMBER_COUNT];
			final double percentageSpheroid = (aMigrationSetData[SPHEROID_COUNT][NUMBER_COUNT] / totalNuclei) * 100.0;
			final double percentageCollective = (aMigrationSetData[COLLECTIVE_COUNT][NUMBER_COUNT] / totalNuclei) * 100.0;
			final double percentageDualCell = (aMigrationSetData[DUAL_COUNT][NUMBER_COUNT] / totalNuclei) * 100.0;
			final double percentageSingleCell = (aMigrationSetData[SINGLE_COUNT][NUMBER_COUNT] / totalNuclei) * 100.0;
			final double percentageVolumeSpheroid = (aMigrationSetData[SPHEROID_COUNT][VOLUME_NUCLEI] / aMigrationSetData[TOTAL_COUNT][VOLUME_NUCLEI]) * 100.0;
			final double percentageVolumeCollective = (aMigrationSetData[COLLECTIVE_COUNT][VOLUME_NUCLEI] / aMigrationSetData[TOTAL_COUNT][VOLUME_NUCLEI]) * 100.0;
			final double percentageVolumeDualCell = (aMigrationSetData[DUAL_COUNT][VOLUME_NUCLEI] / aMigrationSetData[TOTAL_COUNT][VOLUME_NUCLEI]) * 100.0;
			final double percentageVolumeSingleCell = (aMigrationSetData[SINGLE_COUNT][VOLUME_NUCLEI] / aMigrationSetData[TOTAL_COUNT][VOLUME_NUCLEI]) * 100.0;
			final double percentageVolumeSpheroidCell = (aMigrationSetData[SPHEROID_COUNT][VOLUME_CELL] / aMigrationSetData[TOTAL_COUNT][VOLUME_CELL]) * 100.0;
			final double percentageVolumeCollectiveCell = (aMigrationSetData[COLLECTIVE_COUNT][VOLUME_CELL] / aMigrationSetData[TOTAL_COUNT][VOLUME_CELL]) * 100.0;
			final double percentageVolumeDualCellCell = (aMigrationSetData[DUAL_COUNT][VOLUME_CELL] / aMigrationSetData[TOTAL_COUNT][VOLUME_CELL]) * 100.0;
			final double percentageVolumeSingleCellCell = (aMigrationSetData[SINGLE_COUNT][VOLUME_CELL] / aMigrationSetData[TOTAL_COUNT][VOLUME_CELL]) * 100.0;

			final double amountOfCells = aMigrationAccuracyData[CORTOT] + aMigrationAccuracyData[ERRTOT] + aMigrationAccuracyData[NOTOT];
			resultsTable.addValue("% Nucleus correct migration mode", (aMigrationAccuracyData[CORTOT] / amountOfCells) * 100.0);
			resultsTable.addValue("% Nucleus wrong migration mode", (aMigrationAccuracyData[ERRTOT] / amountOfCells) * 100.0);
			resultsTable.addValue("% Nucleus without migration mode", (aMigrationAccuracyData[NOTOT] / amountOfCells) * 100.0);

			final double amountOfSingleCells = aMigrationAccuracyData[CORSIN] + aMigrationAccuracyData[ERRSIN] + aMigrationAccuracyData[NOSIN];
			resultsTable.addValue("% SingleCell correct migration mode", (aMigrationAccuracyData[CORSIN] / amountOfSingleCells) * 100.0);
			resultsTable.addValue("% SingleCell wrong migration mode", (aMigrationAccuracyData[ERRSIN] / amountOfSingleCells) * 100.0);
			resultsTable.addValue("% SingleCell without migration mode", (aMigrationAccuracyData[NOSIN] / amountOfSingleCells) * 100.0);

			resultsTable.addValue("True positive single cells", aMigrationAccuracyData[CORSIN]);
			resultsTable.addValue("False positivie single cells", aMigrationAccuracyData[FALSEPOS]);
			resultsTable.addValue("False negative single cells", aMigrationAccuracyData[ERRSIN] + aMigrationAccuracyData[NOSIN]);

			resultsTable.addValue("Single cell segmentation accuracy", (aMigrationAccuracyData[CORSIN] / (aMigrationAccuracyData[CORSIN] + aMigrationAccuracyData[FALSEPOS])) * 100.0);
			resultsTable.addValue("Single cell sensitivity", (aMigrationAccuracyData[CORSIN] / amountOfSingleCells) * 100.0);

			final double amountOfCoreCells = aMigrationAccuracyData[CORCOR] + aMigrationAccuracyData[ERRCOR] + aMigrationAccuracyData[NOCOR];
			if (amountOfCoreCells > 0)
			{
				resultsTable.addValue("% Core cells correct as core", (aMigrationAccuracyData[CORCOR] / amountOfCoreCells) * 100.0);
				resultsTable.addValue("% Core cells with (erroneous) migration mode", (aMigrationAccuracyData[ERRCOR] / amountOfCoreCells) * 100.0);
				resultsTable.addValue("% Core cells with no migration mode", (aMigrationAccuracyData[NOCOR] / amountOfCoreCells) * 100.0);
			}

			final double amountOfDualCells = aMigrationAccuracyData[CORDUO] + aMigrationAccuracyData[ERRDUO] + aMigrationAccuracyData[NODUO];
			if (amountOfDualCells > 0)
			{
				resultsTable.addValue("% Dual cell correct migration mode", (aMigrationAccuracyData[CORDUO] / amountOfDualCells) * 100.0);
				resultsTable.addValue("% Dual cell wrong migration mode", (aMigrationAccuracyData[ERRDUO] / amountOfDualCells) * 100.0);
				resultsTable.addValue("% Dual cell without migration mode", (aMigrationAccuracyData[NODUO] / amountOfDualCells) * 100.0);
			}

			final double amountOfMultiCells = aMigrationAccuracyData[CORCOL] + aMigrationAccuracyData[ERRCOL] + aMigrationAccuracyData[NOCOL];
			if (amountOfMultiCells > 0)
			{
				resultsTable.addValue("% Multi cell correct migration mode", (aMigrationAccuracyData[CORCOL] / amountOfMultiCells) * 100.0);
				resultsTable.addValue("% Multi cell wrong migration mode", (aMigrationAccuracyData[ERRCOL] / amountOfMultiCells) * 100.0);
				resultsTable.addValue("% Multi cell without migration mode", (aMigrationAccuracyData[NOCOL] / amountOfMultiCells) * 100.0);
			}

			resultsTable.addValue("% Nuclei in Spheroid", percentageSpheroid);
			resultsTable.addValue("% Nuclei in Collective mode", percentageCollective);
			resultsTable.addValue("% Nuclei in Dual mode", percentageDualCell);
			resultsTable.addValue("% Nuclei in SingleCell mode", percentageSingleCell);
			resultsTable.addValue("% Volume nucleus in Spheroid", percentageVolumeSpheroid);
			resultsTable.addValue("% Volume nucleus in Collective mode", percentageVolumeCollective);
			resultsTable.addValue("% Volume nucleus in Dual mode", percentageVolumeDualCell);
			resultsTable.addValue("% Volume nucleus in SingleCell mode", percentageVolumeSingleCell);
			resultsTable.addValue("% Volume cell in Spheroid", percentageVolumeSpheroidCell);
			resultsTable.addValue("% Volume cell in Collective mode", percentageVolumeCollectiveCell);
			resultsTable.addValue("% Volume cell in Dual mode", percentageVolumeDualCellCell);
			resultsTable.addValue("% Volume cell in SingleCell mode", percentageVolumeSingleCellCell);

			resultsTable.addValue("# Nuclei in spheroid", aMigrationSetData[SPHEROID_COUNT][NUMBER_COUNT]);
			resultsTable.addValue("# Nuclei in collective mode", aMigrationSetData[COLLECTIVE_COUNT][NUMBER_COUNT]);
			resultsTable.addValue("# Nuclei in dual mode", aMigrationSetData[DUAL_COUNT][NUMBER_COUNT]);
			resultsTable.addValue("# Nuclei in single cell mode", aMigrationSetData[SINGLE_COUNT][NUMBER_COUNT]);
			resultsTable.addValue("Total nuclei counted", totalNuclei);
			resultsTable.addValue("Nuclei volume in spheroid", aMigrationSetData[SPHEROID_COUNT][VOLUME_NUCLEI]);
			resultsTable.addValue("Nuclei volume in collective mode", aMigrationSetData[COLLECTIVE_COUNT][VOLUME_NUCLEI]);
			resultsTable.addValue("Nuclei volume in single cell mode", aMigrationSetData[SINGLE_COUNT][VOLUME_NUCLEI]);
		}

		resultsTable.addValue("Mean volume", aCells.getMeanVolume()); // Add the mean area value of the image
		resultsTable.addValue("Mean number of Voxels", aCells.getMeanNumberOfVoxels());

		addGroupMeasurements(aCells, SegmentMeasurements.STANDARD_GROUP, resultsTable);

		final List<Double> extraChannelsMeans = aCells.getMeanExtraChannels();
		if (extraChannelsMeans != null)
		{
			final List<Double> backgrounds = aCells.getBackgrounExtraChannels();
			for (int extraIndex = 0; extraIndex < extraChannelsMeans.size(); extraIndex++)
			{
				resultsTable.addValue("Mean additional channel " + (extraIndex + 1), extraChannelsMeans.get(extraIndex));
				resultsTable.addValue("Background additionall channel " + (extraIndex + 1), backgrounds.get(extraIndex));
			}
		}

		addGroupMeasurements(aCells, SegmentMeasurements.MORPHOLIBJ_GROUP, resultsTable);
		addGroupMeasurements(aCells, SegmentMeasurements.MCIB3D_GROUP, resultsTable);

		resultsTable.addValue("Total seeds", aNumberOfSeeds);

		// Handle the manual marker results
		if (aMarkerAccuracyData != null)
		{
			final double amountMarkersCounted = aMarkerAccuracyData[MARKER_OK] + aMarkerAccuracyData[MARKER_DOUBLE] + aMarkerAccuracyData[MARKER_NONUC];
			final double totalAmountMarkersCounted = amountMarkersCounted + aMarkerAccuracyData[MARKER_EX] + aMarkerAccuracyData[MARKER_DQ];
			resultsTable.addValue("Total markers", totalAmountMarkersCounted);
			resultsTable.addValue("Marker in a nucleus", aMarkerAccuracyData[MARKER_OK]);
			resultsTable.addValue("Markers double in a nucleus", aMarkerAccuracyData[MARKER_DOUBLE]);
			resultsTable.addValue("Marker without a nucleus", aMarkerAccuracyData[MARKER_NONUC]);
			resultsTable.addValue("Markers in an excluded nucleus", aMarkerAccuracyData[MARKER_EX]);
			resultsTable.addValue("Marker in a disqualified nucleus", aMarkerAccuracyData[MARKER_DQ]);

			resultsTable.addValue("% Markers in a nucleus (sensitivity)", ((aMarkerAccuracyData[MARKER_OK]) / totalAmountMarkersCounted) * 100.0);
			resultsTable.addValue("% Markers outside a nucleus", ((aMarkerAccuracyData[MARKER_NONUC]) / totalAmountMarkersCounted) * 100.0);
			resultsTable.addValue("% Markers double in a nucleus", ((aMarkerAccuracyData[MARKER_DOUBLE]) / totalAmountMarkersCounted) * 100.0);
			resultsTable.addValue("% Markers excluded", ((aMarkerAccuracyData[MARKER_EX] + aMarkerAccuracyData[MARKER_DQ]) / totalAmountMarkersCounted) * 100.0);
		}

		resultsTable.setLabel(aTitle, resultsTable.getCounter() - 1);
		return resultsTable;
	}


	public static ResultsTable getKarolinskaResults(final Cell3D[] aCells)
	{
		final ResultsTable resultsTable = new ResultsTable();
		int nrMigratedCells = 0;
		int nrSingleCells = 0;

		for (final Cell3D cell : aCells)
		{
			final Nucleus3D nucleus = cell.getNucleus();
			final double migrationDistance = nucleus.getDistanceToCore();
			if (!nucleus.isBorderNucleus() && migrationDistance > 0)
			{
				nrMigratedCells++;
				if (cell.getMigrationMode().equals(Cell3D_Group.SINGLE))
				{
					nrSingleCells++;
				}
			}
		}

		for (final Cell3D cell : aCells)
		{
			final Nucleus3D nucleus = cell.getNucleus();
			final double migrationDistance = nucleus.getDistanceToCore();
			if (!nucleus.isBorderNucleus() && migrationDistance > 0)
			{
				resultsTable.incrementCounter();
				resultsTable.addValue("Label", nucleus.getLabel());
				resultsTable.addValue("Migration distance", nucleus.getDistanceToCore());

				final SegmentMeasurements nucleusMeasure = cell.getSignalMeasurements().get(0);
				final SegmentMeasurements cellMeasure = cell.getSignalMeasurements().get(1);
				final double cellYapValue = Math.max(cellMeasure.getMeasurement(SegmentMeasurements.MEAN_INTENSITY), 1); // Use 1 as a min value and let the image work with that
				final double nucleusYapValue = Math.max(nucleusMeasure.getMeasurement(SegmentMeasurements.MEAN_INTENSITY), 1); // Use 1 as a min value and let the image work
				resultsTable.addValue("YAP ratio", nucleusYapValue / cellYapValue);

				resultsTable.addValue("nrMigratedCells", nrMigratedCells);
				resultsTable.addValue("nrSingleCells", nrSingleCells);
				resultsTable.addValue("Single cell?", "" + cell.getMigrationMode().equals(Cell3D_Group.SINGLE));
			}
		}

		return resultsTable;
	}


	/**
	 * Get all the measurements that are available on each cell (excluding nucleus specific measurements).
	 *
	 * @param aCells
	 *            The list of cells
	 * @param aAddMigrationData
	 *            Should migration mode data be added
	 *
	 * @return A ResultsTable with all the cell measurements (one line per cell).
	 */
	public static ResultsTable getResultsPerCell(final Cell3D[] aCells, final boolean aAddMigrationData)
	{

		final ResultsTable resultsTable = new ResultsTable();
		for (final Cell3D cell : aCells)
		{
			final Nucleus3D nucleus = cell.getNucleus();
			resultsTable.incrementCounter();
			resultsTable.addValue("Label", nucleus.getLabel());

			final SegmentMeasurements measurements = cell.getMeasurements();
			addGroupMeasurements(measurements, SegmentMeasurements.STANDARD_GROUP, resultsTable);
			final List<SegmentMeasurements> signalMeasurements = cell.getSignalMeasurements();
			if (signalMeasurements != null && !signalMeasurements.isEmpty())
			{
				int i = 0;
				for (final SegmentMeasurements measure : signalMeasurements)
				{
					addGroupMeasurements(measure, SegmentMeasurements.STANDARD_GROUP, "Additional channel " + i + " ", resultsTable);
					// resultsTable.addValue("Additional channel " + i + " mean intensity signal ", measure.getMeasurement(SegmentMeasurements.MEAN_INTENSITY));
					// resultsTable.addValue("Additional channel " + i + " background signal ", measure.getMeasurement(SegmentMeasurements.BACKGROUND_INTENSITY));
					i++;
				}
			}
			if (nucleus.getDistanceToCentre() != nucleus.getDistanceToCore())
			{
				resultsTable.addValue("Migration distance", nucleus.getDistanceToCore());
			}
			resultsTable.addValue("Number of voxels", nucleus.getNumberOfVoxels());
			resultsTable.addValue("Volume", nucleus.getVolume());

			if (aAddMigrationData)
			{
				resultsTable.addValue("Migration mode", cell.getMigrationMode());
				resultsTable.addValue("Manual migration mode", cell.getMarkerMigrationMode());
			}

			// Do the rest of the measurements as well, just remove the previous standard ones.
			final Set<String> restOfNames = measurements.getMeasurementNames();
			restOfNames.removeAll(Arrays.asList(SegmentMeasurements.STANDARD_GROUP));
			addGroupMeasurements(measurements, restOfNames.toArray(new String[restOfNames.size()]), resultsTable);
		}

		return resultsTable;

	}


	/**
	 * Get all the measurements that are available on each nucleus.
	 *
	 * @param aCells
	 *            The list of cells (containing the measured nuclei)
	 *
	 * @return A ResultsTable with all the nuclei measurements (one line per nucleus). May return null if a nucleus without a seed has been found (error).
	 */
	public static ResultsTable getResultsPerNucleus(final Cell3D[] aCells)
	{
		final ResultsTable resultsTable = new ResultsTable();
		for (final Cell3D cell : aCells)
		{
			final Nucleus3D nucleus = cell.getNucleus();
			resultsTable.incrementCounter();
			resultsTable.addValue("Label", nucleus.getLabel());
			final Coordinates seed = nucleus.getSeed();
			if (seed == null)
			{
				// A nucleus without a seed should not happen. Just return without any results!
				return null;
			}
			resultsTable.addValue("XCordinate", seed.getXcoordinate());
			resultsTable.addValue("YCordinate", seed.getYcoordinate());
			resultsTable.addValue("ZCordinate", seed.getZcoordinate());
			resultsTable.addValue("Amount markers", nucleus.getMarkersCount());
			resultsTable.addValue("Under volume threshold", nucleus.isTooSmall() + "");
			resultsTable.addValue("Border nucleus", nucleus.isBorderNucleus() + "");
			resultsTable.addValue("Distance to centre", nucleus.getDistanceToCentre() + "");
			resultsTable.addValue("Distance to spheroid border", nucleus.getDistanceToCore() + "");

			resultsTable.addValue("Manual migration mode", cell.getMarkerMigrationMode());

			// Separate loop for standard measurements to enforce order
			final SegmentMeasurements measurements = nucleus.getMeasurements();
			addGroupMeasurements(measurements, SegmentMeasurements.STANDARD_GROUP_NUCLEUS, resultsTable);
			resultsTable.addValue("NumberOfVoxels", nucleus.getNumberOfVoxels());
			resultsTable.addValue("Volume", nucleus.getVolume());

			// Do the rest of the measurements as well, just remove the previous standard ones.
			final List<String> restOfNames = new ArrayList<>();
			restOfNames.addAll(measurements.getMeasurementNames());
			restOfNames.removeAll(Arrays.asList(SegmentMeasurements.STANDARD_GROUP_NUCLEUS));
			addGroupMeasurements(measurements, restOfNames.toArray(new String[restOfNames.size()]), resultsTable);
		}

		return resultsTable;
	}


	/**
	 * Save a results table as a Excel readable file.
	 *
	 * @param aTitle
	 *            The title to save the table under (without extension!)
	 * @param aDirectory
	 *            The directory to save this results table to
	 * @param aResults
	 *            The ResultsTable to save
	 */
	public static void saveResultsTable(final String aTitle, final File aDirectory, final ResultsTable aResultsTable)
	{
		if (!aDirectory.exists())
		{
			if (aDirectory.mkdir())
			{
				IJ.log("Directory " + aDirectory + " has been created!");
			}
			else
			{
				IJ.log("ERROR: Failed to create directory " + aDirectory + "!");
			}
		}
		final String filename = aDirectory.getPath() + File.separator + aTitle + ".xls";
		aResultsTable.save(filename);
		IJ.log("Results tabel has been saved as: " + filename);
	}


	/**
	 * Made private to prevent instantiation.
	 */
	private ResultsTableGenerator()
	{
	}

}
