package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ij.IJ;

/**
 * A representation of a connected group of cells in the image. This can be a single cells, a group of migrating cells or the spheroid and connected strands.
 *
 * @author Merijn van Erp, Esther Markus
 */
public class Cell3D_Group
{
	// The migration mode possibilities
	public static final String CORE = "CORE", SINGLE = "SINGLE_CELL", DUAL = "DUAL_CLUSTER", MULTI = "MULTI_CLUSTER", NONE = "NONE";

	private final ArrayList<Cell3D> members = new ArrayList<>();
	// Map with the mean measurements
	private final HashMap<String, Double> meanCellMeasurements;
	private final HashMap<String, Double> meanNucleusMeasurements;

	// The migration modes based on automatic cells and manual markers
	private String migrationMode = NONE;
	private String manualMigrationMode = null;

	// The list of different cells by marker migration mode
	private final ArrayList<Integer> core = new ArrayList<>();
	private final ArrayList<Integer> multiCell = new ArrayList<>();
	private final ArrayList<Integer> dualCell = new ArrayList<>();
	private final ArrayList<Integer> singleCell = new ArrayList<>();
	private final ArrayList<Integer> noMigrationModeCell = new ArrayList<>();

	// Store for centre calculations
	private double meanX = 0;
	private double meanY = 0;
	private double meanZ = 0;

	// Count the cell identification correctness
	private boolean countNucleusMarkers = false;
	private double nucleusSelected = 0;
	private int nucleusNOTSelected = 0;
	private int nucleusExcludedBorder = 0;
	private int nucleusTooSmall = 0;
	private int nucleusExludedBorderAndSize = 0;
	private int excludedWasCorrectSegmented = 0;
	private int excludedWasOverSegmented = 0;
	private int excludedWasUnderSegmented = 0;
	private int nucleusTwiceExcluded = 0;
	private int nucleuswithMulipleMarkers = 0;
	private int nucleusWithSeed = 0;
	private int nucleusWithSeedAndMarker = 0;

	// Counts of the migration mode correctness
	private boolean countNucleusMigrationMode = false;
	private int nucleusWithCorrectMigrationMode = 0;
	private int nucleusWithWrongMigrationMode = 0;
	private int nucleusWithoutMigrationMode = 0;
	private int singleCellWithoutMigrationMode = 0;
	private int singleCellWithWrongMigrationMode = 0;
	private int singleCellWithCorrectMigrationMode = 0;
	private int coreCellWithoutMigrationMode = 0;
	private int coreCellDetectedAsCore = 0;
	private int coreCellWithMigrationMode = 0;
	private int dualCellWithCorrectMigrationMode = 0;
	private int dualCellWithoutMigrationMode = 0;
	private int dualCellWithWrongMigrationMode = 0;
	private int multiCellWithCorrectMigrationMode = 0;
	private int multiCellWithWrongMigrationMode = 0;
	private int multiCellWithoutMigrationMode = 0;
	private int singleCellFalsePositive = 0;

	// Sizes
	private double totalVolume = 0;
	private double totalVolumeCell = 0;

	// Mean of the cell measurements
	private double meanVolume = 0;
	private double meanNumberOfVoxels = 0;

	// Additional channels
	private ArrayList<Double> meanExtraChannels = null;
	private ArrayList<Double> backgroundExtraChannels = null;


	/**
	 * Create an empty Cell3D_Group.
	 */
	public Cell3D_Group()
	{
		this.meanCellMeasurements = new HashMap<>();
		this.meanNucleusMeasurements = new HashMap<>();
	}


	/**
	 * Create a Cell3D_Group out of a List of Cell3D objects.
	 *
	 * @param aStartMembers
	 *            The first set of Cell3D objects of this Cell3D_Group
	 */
	public Cell3D_Group(final List<Cell3D> aStartMembers)
	{
		this.members.addAll(aStartMembers);
		this.meanCellMeasurements = new HashMap<>();
		this.meanNucleusMeasurements = new HashMap<>();
	}


	/**
	 * Compare the migration mode of the group as determined by the automated migration analysis with the 'golden truth' of the manual migration mode(s) of the marker(s) contained in the Cell3Ds themselves. Note that it compares on a pairwise (group
	 * vs cell) basis, so it is theoretically possible to have a single-cell group containing two manually-annotated single-cell markers and count that as two successes.
	 */
	private void countMigrationModeAccuracy()
	{
		final String migrationModeGroup = getMigrationmode();

		// Now check the manual migration mode of all member Cell3Ds
		for (final Cell3D cell : this.members)
		{
			final String markerMigModeCell = cell.getMarkerMigrationMode();
			if (!migrationModeGroup.equals(markerMigModeCell))
			{
				// We have a disagreement, determine which specific type of migration resulted in an error and keep count of it.
				if (migrationModeGroup.equals(NONE))
				{
					// No migration mode has been assigned to the group while we asked for it.
					this.nucleusWithoutMigrationMode++;

					// Now determine what should have been
					if (markerMigModeCell.equals(SINGLE))
					{
						this.singleCellWithoutMigrationMode++;
					}
					else if (markerMigModeCell.equals(CORE))
					{
						this.coreCellWithoutMigrationMode++;
					}
					else if (markerMigModeCell.equals(DUAL))
					{
						this.dualCellWithoutMigrationMode++;
					}
					else if (markerMigModeCell.equals(MULTI))
					{
						this.multiCellWithoutMigrationMode++;
					}
				}
				else
				{
					if (migrationModeGroup.equals(SINGLE))
					{
						// This should not have been a single cell
						this.singleCellFalsePositive++;
					}
					else
					{
						// Not single or no migration (the two major error cases). Differentiation of the false positives is not needed.
						this.nucleusWithWrongMigrationMode++;
					}

					// What should it have been though?.
					if (markerMigModeCell.equals(SINGLE))
					{
						this.singleCellWithWrongMigrationMode++;
					}
					else if (markerMigModeCell.equals(DUAL))
					{
						this.dualCellWithWrongMigrationMode++;
					}
					else if (markerMigModeCell.equals(MULTI))
					{
						this.multiCellWithWrongMigrationMode++;
					}
					else if (markerMigModeCell.equals(CORE))
					{
						this.coreCellWithMigrationMode++;
					}
				}
			}
			else
			{
				// Yay, correct migration analysis
				this.nucleusWithCorrectMigrationMode++;

				// Now count what type of migration was detected successfully
				if (markerMigModeCell.equals(SINGLE))
				{
					this.singleCellWithCorrectMigrationMode++;
				}
				else if (markerMigModeCell.equals(CORE))
				{
					this.coreCellDetectedAsCore++;
				}
				else if (markerMigModeCell.equals(DUAL))
				{
					this.dualCellWithCorrectMigrationMode++;
				}
				else if (markerMigModeCell.equals(MULTI))
				{
					this.multiCellWithCorrectMigrationMode++;
				}
			}
		}

		// Register that the counting has been cached
		this.countNucleusMigrationMode = true;
	}


	/**
	 * For all cell nuclei in the group, count two things: 1) have they been disqualified for any reason and 2) have they been correctly segmented. The latter is determined by the number of manual markers that is contained in the cell. The aim is 1
	 * marker per cell. If this is the case, they are correctly segmented. Too few (i.e. 0) and the cell is over-segmented, while too many (> 1) is an under-segmented cell.
	 */
	private void countNucleusMarkers()
	{
		for (final Cell3D cell : this.members)
		{
			final Nucleus3D nucleus = cell.getNucleus();
			final boolean hasSeed = nucleus.getSeed() != null;
			final int countMarkers = nucleus.getMarkersCount();
			if (nucleus.isBorderNucleus() || nucleus.isDisqualified() || nucleus.isTooSmall())
			{
				// Why was is excluded?
				if ((nucleus.isBorderNucleus() || nucleus.isTooSmall()) && nucleus.isDisqualified())
				{
					this.nucleusTwiceExcluded = this.nucleusTwiceExcluded + 1;
				}
				else if (nucleus.isBorderNucleus() && nucleus.isTooSmall())
				{
					this.nucleusExludedBorderAndSize = this.nucleusExludedBorderAndSize + 1;
				}
				else if (nucleus.isBorderNucleus())
				{
					this.nucleusExcludedBorder = this.nucleusExcludedBorder + 1;
				}
				else if (nucleus.isDisqualified())
				{
					this.nucleusNOTSelected = this.nucleusNOTSelected + 1;
				}
				else if (nucleus.isTooSmall())
				{
					this.nucleusTooSmall = this.nucleusTooSmall + 1;
				}

				// Count correct segmentation
				if (nucleus.getMarkersCount() == 0)
				{
					this.excludedWasOverSegmented = this.excludedWasOverSegmented + 1;
				}
				else if (nucleus.getMarkersCount() == 1)
				{
					this.excludedWasCorrectSegmented = this.excludedWasCorrectSegmented + 1;
				}
				else
				{
					this.excludedWasUnderSegmented = this.excludedWasUnderSegmented + 1;
				}
			}
			else
			{
				// Not excluded or disqualified!
				this.nucleusSelected = this.nucleusSelected + 1;
				if (hasSeed)
				{
					// Has seed, so now see if correctly segmented
					if (countMarkers == 0)
					{
						this.nucleusWithSeed = this.nucleusWithSeed + 1;
					}
					else if (countMarkers == 1)
					{
						this.nucleusWithSeedAndMarker = this.nucleusWithSeedAndMarker + 1;
					}
					else
					{
						this.nucleuswithMulipleMarkers = this.nucleuswithMulipleMarkers + 1;
					}
				}
				else
				{
					// No seed? -> Error
					if (countMarkers == 0)
					{
						IJ.log("ERROR: there is a nucleus: " + nucleus.getLabel() + " without a seed and without a marker");
					}
					else if (countMarkers == 1)
					{
						IJ.log("ERROR there is a nucleus: " + nucleus.getLabel() + " without a seed with a marker");
					}
					else
					{
						IJ.log("ERROR: nucleus: " + nucleus.getLabel() + " failed");
					}
				}
			}
		}

		// Mark that the nucleus count has been cached
		this.countNucleusMarkers = true;
	}


	public List<Double> getBackgrounExtraChannels()
	{
		if (this.backgroundExtraChannels == null && this.members != null)
		{
			final List<SegmentMeasurements> segmentMeasurements = this.members.get(0).getSignalMeasurements();
			this.backgroundExtraChannels = new ArrayList<>();
			for (int i = 0; i < segmentMeasurements.size(); i++)
			{
				this.backgroundExtraChannels.add(segmentMeasurements.get(i).getMeasurement(SegmentMeasurements.BACKGROUND_INTENSITY));
			}
		}

		return this.backgroundExtraChannels;
	}


	public String getCellLabelsWithoutMigrationMode()
	{
		final StringBuilder names = new StringBuilder("");
		for (int i = 0; i < this.noMigrationModeCell.size(); i++)
		{
			names.append(" " + this.noMigrationModeCell.get(i));
		}
		return names.toString();
	}


	/**
	 * Get the point that has as coordinates the mean of all x-, y-, and z-coordinates.
	 *
	 * @return The Coordinates containing the mean of all x-, y-, and z-coordinates.
	 */
	public double[] getCenterXYZ()
	{
		for (final Cell3D cell : this.members)
		{
			final Nucleus3D nucleus = cell.getNucleus();
			this.meanX = this.meanX + nucleus.getSeed().getXcoordinate();
			this.meanY = this.meanY + nucleus.getSeed().getYcoordinate();
			this.meanZ = this.meanZ + nucleus.getSeed().getZcoordinate();
		}
		this.meanX = this.meanX / getMemberCount();
		this.meanY = this.meanY / getMemberCount();
		this.meanZ = this.meanZ / getMemberCount();
		final double[] result = { this.meanX, this.meanY, this.meanZ };
		return result;
	}


	public int getCoreCellDetectedAsCore()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		return this.coreCellDetectedAsCore;
	}


	/**
	 * Get, as a String, the labels of all the cells marked as part of the core or at least the largest cluster (tagged as 'UNKNOWN').
	 *
	 * @return The String containing all the cell labels separated by single spaces.
	 */
	public String getCoreCellLabels()
	{
		final StringBuilder names = new StringBuilder("");
		for (int i = 0; i < this.core.size(); i++)
		{
			names.append(" " + this.core.get(i));
		}
		return names.toString();
	}


	/**
	 * Get the number of cells that should belong to the core of the spheroid and that were successfully detected as such.
	 *
	 * @return The number of correctly detected core cells.
	 */
	public int getCoreCellWithMigrationMode()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		return this.coreCellWithMigrationMode;
	}


	/**
	 * Get the number of spheroid-core cells that got no migration label assigned.
	 *
	 * @return The number of unlabelled core cells.
	 */
	public int getCoreCellWithoutMigrationMode()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		return this.coreCellWithoutMigrationMode;
	}


	/**
	 * Get, as a String, the labels of all the cells marked as part of a dual cell cluster.
	 *
	 * @return The String containing all the cell labels separated by single spaces.
	 */
	public String getDualCellLabels()
	{
		final StringBuilder names = new StringBuilder("");
		for (int i = 0; i < this.dualCell.size(); i++)
		{
			names.append(" " + this.dualCell.get(i));
		}
		return names.toString();
	}


	/**
	 * Get the number of cells correctly labelled as belonging to a pair of migrating cells.
	 *
	 * @return The number of correctly labelled dual migration cells in this Cell3D_Group
	 */
	public int getDualCellWithCorrectMigrationMode()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		return this.dualCellWithCorrectMigrationMode;
	}


	public int getDualCellWithoutMigrationMode()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		return this.dualCellWithoutMigrationMode;
	}


	public int getDualCellWithWrongMigrationMode()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		return this.dualCellWithWrongMigrationMode;
	}


	public String getManualMigrationModeGroup()
	{
		if (this.manualMigrationMode == null)
		{
			boolean dualIdentity = false;
			for (final Cell3D cell : this.members)
			{
				final Nucleus3D nucleus = cell.getNucleus();
				final String migrationMode = cell.getMarkerMigrationMode();
				switch (migrationMode)
				{
				case CORE:
					this.core.add(nucleus.getLabel());
					break;
				case MULTI:
					this.multiCell.add(nucleus.getLabel());
					break;
				case DUAL:
					this.dualCell.add(nucleus.getLabel());
					break;
				case SINGLE:
					this.singleCell.add(nucleus.getLabel());
					break;
				case NONE:
					this.noMigrationModeCell.add(nucleus.getLabel());
					break;
				default:
					// Nothing needed, though this should not occur
				}

				if (this.manualMigrationMode == null)
				{
					this.manualMigrationMode = migrationMode;
				}
				else if (!this.manualMigrationMode.equals(migrationMode))
				{
					dualIdentity = true;
				}
			}

			if (dualIdentity == true)
			{
				this.manualMigrationMode = "MULTIPLE_IDENTITIES";
				IJ.log("MULTIPLE_IDENTITIES " + this.singleCell.size() + " " + this.dualCell.size() + " " + this.multiCell.size() + " " + this.core.size());
			}
		}
		return this.manualMigrationMode;
	}


	public double getMeanCellMeasure(final String aMeasureName)
	{
		Double resultMeasure = this.meanCellMeasurements.get(aMeasureName);

		if (resultMeasure == null)
		{
			double measure = 0;
			for (final Cell3D cell : this.members)
			{
				measure = measure + cell.getMeasurements().getMeasurement(aMeasureName);
			}
			resultMeasure = measure / this.members.size();
		}
		this.meanCellMeasurements.put(aMeasureName, resultMeasure);

		return resultMeasure;
	}


	public List<Double> getMeanExtraChannels()
	{
		if (this.meanExtraChannels == null && this.members != null)
		{
			List<SegmentMeasurements> segmentMeasurements = this.members.get(0).getSignalMeasurements();
			if (segmentMeasurements != null)
			{
				this.meanExtraChannels = new ArrayList<>();
				for (int i = 0; i < segmentMeasurements.size(); i++)
				{
					this.meanExtraChannels.add(0.0);
				}
				for (final Cell3D cell : this.members)
				{
					segmentMeasurements = cell.getSignalMeasurements();
					for (int i = 0; i < segmentMeasurements.size(); i++)
					{
						this.meanExtraChannels.set(i, this.meanExtraChannels.get(i) + segmentMeasurements.get(i).getMeasurement(SegmentMeasurements.MEAN_INTENSITY));
					}
				}

				for (int i = 0; i < segmentMeasurements.size(); i++)
				{
					this.meanExtraChannels.set(i, this.meanExtraChannels.get(i) / this.members.size());
				}
			}
		}

		return this.meanExtraChannels;
	}


	public Double getMeanNucleusMeasure(final String aMeasureName)
	{
		Double resultMeasure = this.meanNucleusMeasurements.get(aMeasureName);

		if (resultMeasure == null)
		{
			double measure = 0;
			for (final Cell3D cell : this.members)
			{
				final Double nucMeasure = cell.getNucleus().getMeasurements().getMeasurement(aMeasureName);
				if (nucMeasure != null)
				{
					measure = measure + nucMeasure;
				}
				else
				{
					return null;
				}
			}
			resultMeasure = measure / this.members.size();
		}
		this.meanNucleusMeasurements.put(aMeasureName, resultMeasure);

		return resultMeasure;
	}


	public double getMeanNumberOfVoxels()
	{
		if (this.meanNumberOfVoxels == 0)
		{
			for (final Cell3D cell : this.members)
			{
				this.meanNumberOfVoxels = this.meanNumberOfVoxels + cell.getNucleus().getNumberOfVoxels();
			}
			this.meanNumberOfVoxels = this.meanNumberOfVoxels / this.members.size();
		}
		return this.meanNumberOfVoxels;
	}


	public double getMeanVolume()
	{
		if (this.meanVolume == 0)
		{
			for (final Cell3D cell : this.members)
			{
				this.meanVolume = this.meanVolume + cell.getNucleus().getVolume();
			}
			this.meanVolume = this.meanVolume / this.members.size();
		}
		return this.meanVolume;
	}


	public Cell3D getMember(final int index)
	{
		return this.members.get(index);
	}


	public int getMemberCount()
	{
		return this.members.size();
	}


	public String getMemberNames()
	{
		final StringBuilder names = new StringBuilder("");
		for (final Cell3D cell : this.members)
		{
			if (cell.getNucleus() != null)
			{
				names.append(cell.getNucleus().getLabel() + " ");
			}
		}
		return names.toString();
	}


	public List<Cell3D> getMembers()
	{
		return this.members;
	}


	/**
	 * Get the migration mode of this group as determined by the number of cells in the group (or UNKNOWN for the first and largest group).
	 *
	 * @return The migration mode as a String (or null if not set).
	 */
	public String getMigrationmode()
	{
		return this.migrationMode;
	}


	/**
	 * Get, as a String, the labels of all the cells marked as part of a multi-cell group.
	 *
	 * @return The String containing all the cell labels separated by single spaces.
	 */
	public String getMultiCellLabels()
	{
		final StringBuilder labels = new StringBuilder("");
		for (final int cellLabel : this.multiCell)
		{
			labels.append(cellLabel + " ");
		}

		String result = labels.toString();
		if (!result.equals(""))
		{
			// Remove the last " "
			result = result.substring(0, labels.length() - 1);
		}
		return result;
	}


	/**
	 * Get the number of cells correctly labelled as belonging to a group (more than 2) of migrating cells.
	 *
	 * @return The number of correctly labelled multi-migration cells in this Cell3D_Group
	 */
	public int getMultiCellWithCorrectMigrationMode()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		// IJ.log("multiCellWithCorrectMigrationMode" + multiCellWithCorrectMigrationMode);
		return this.multiCellWithCorrectMigrationMode;
	}


	public int getMultiCellWithoutMigrationMode()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		/// IJ.log("MultiCellsWithoutMigrationmode" + multiCellWithoutMigrationMode);
		return this.multiCellWithoutMigrationMode;
	}


	public int getMultiCellWithWrongMigrationMode()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		// IJ.log("multiCellWithWrongMigrationMode" + multiCellWithWrongMigrationMode);
		return this.multiCellWithWrongMigrationMode;
	}


	public int getNucleusCorrectSegmented()
	{
		if (!this.countNucleusMarkers)
		{
			countNucleusMarkers();
		}
		return this.nucleusWithSeedAndMarker;
	}


	public int getNucleusExcludedBorder()
	{
		if (!this.countNucleusMarkers)
		{
			countNucleusMarkers();
		}
		return this.nucleusExcludedBorder;
	}


	public int getNucleusExcludedBorderAndSize()
	{
		if (!this.countNucleusMarkers)
		{
			countNucleusMarkers();
		}
		return this.nucleusExludedBorderAndSize;
	}


	public int[] getNucleusExcludedType()
	{
		if (!this.countNucleusMarkers)
		{
			countNucleusMarkers();
		}
		final int[] result = { this.excludedWasCorrectSegmented, this.excludedWasOverSegmented, this.excludedWasUnderSegmented };
		return result;
	}


	public int getNucleusNOTSelected()
	{
		if (!this.countNucleusMarkers)
		{
			countNucleusMarkers();
		}
		return this.nucleusNOTSelected;
	}


	public int getNucleusOverSegmented()
	{
		if (!this.countNucleusMarkers)
		{
			countNucleusMarkers();
		}
		return this.nucleusWithSeed;
	}


	public double getNucleusSelected()
	{
		if (!this.countNucleusMarkers)
		{
			countNucleusMarkers();
		}
		return this.nucleusSelected;
	}


	public int getNucleusToSmall()
	{
		if (!this.countNucleusMarkers)
		{
			countNucleusMarkers();
		}
		return this.nucleusTooSmall;
	}


	public int getNucleusTwiceExcluded()
	{
		if (!this.countNucleusMarkers)
		{
			countNucleusMarkers();
		}
		return this.nucleusTwiceExcluded;
	}


	public int getNucleusUnderSegmented()
	{
		if (!this.countNucleusMarkers)
		{
			countNucleusMarkers();
		}
		return this.nucleuswithMulipleMarkers;
	}


	public int getNucleusWithCorrectMigrationMode()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}

		return this.nucleusWithCorrectMigrationMode;
	}


	public int getNucleusWithoutMigrationMode()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		return this.nucleusWithoutMigrationMode;
	}


	public int getNucleusWithWrongMigrationMode()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		return this.nucleusWithWrongMigrationMode;
	}


	public double getPercentageNucleusOverSegmented()
	{
		if (!this.countNucleusMarkers)
		{
			countNucleusMarkers();
		}
		final double percentageOverSegmented = (this.nucleusWithSeed / this.nucleusSelected) * 100;

		return percentageOverSegmented;
	}


	public double getPercentageNucleusUnderSegmented()
	{
		if (!this.countNucleusMarkers)
		{
			countNucleusMarkers();
		}
		final double percentage = (this.nucleuswithMulipleMarkers / this.nucleusSelected) * 100;
		return percentage;
	}


	public double getPercentageNucleusWithCorrectMigrationMode()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		if (!this.countNucleusMarkers)
		{
			countNucleusMarkers();
		}

		final double percentageNucleusWithCorrectMigrationMode = (this.nucleusWithCorrectMigrationMode / this.nucleusSelected) * 100;
		return percentageNucleusWithCorrectMigrationMode;
	}


	public double getPercentageNucleusWithoutMigrationMode()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		if (!this.countNucleusMarkers)
		{
			countNucleusMarkers();
		}

		final double percentageWithoutMigrationMode = (this.nucleusWithoutMigrationMode / this.nucleusSelected) * 100;
		return percentageWithoutMigrationMode;
	}


	public double getPercentageNucleusWrongMigrationMode()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		if (!this.countNucleusMarkers)
		{
			countNucleusMarkers();
		}

		final double percentageWrongMigrationMode = (this.nucleusWithWrongMigrationMode / this.nucleusSelected) * 100;
		return percentageWrongMigrationMode;
	}


	public boolean getPresenceOfMember(final Cell3D aCandidateMember)
	{
		boolean present = false;
		final int labelCandidateMember = aCandidateMember.getNucleus().getLabel();
		for (final Cell3D cell : this.members)
		{
			if (cell != null)
			{
				final int labelMember = cell.getNucleus().getLabel();
				if (labelMember == labelCandidateMember)
				{
					present = true;
					break;
				}
			}
		}
		return present;
	}


	/**
	 * Gets the precision value of the segmentation; i.e. # true positives / # all positives.
	 *
	 * @return The precision of the segmentation as a percentage.
	 */
	public double getSegmentationPrecision()
	{
		if (!this.countNucleusMarkers)
		{
			countNucleusMarkers();
		}
		final double percentage = (this.nucleusWithSeedAndMarker / this.nucleusSelected) * 100;
		return percentage;
	}


	public int getSingleCellFalsePositive()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		return this.singleCellFalsePositive;
	}


	/**
	 * Get, as a String, the labels of all the cells marked as a single cell.
	 *
	 * @return The String containing all the cell labels separated by single spaces.
	 */
	public String getSingleCellLabels()
	{
		final StringBuilder names = new StringBuilder("");
		for (int i = 0; i < this.singleCell.size(); i++)
		{
			names.append(" " + this.singleCell.get(i));
		}
		return names.toString();
	}


	/**
	 * Get the number of cells correctly labelled as single migrating cells.
	 *
	 * @return The number of correctly labelled single migrating cells in this Cell3D_Group
	 */
	public int getSingleCellWithCorrectMigrationMode()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		return this.singleCellWithCorrectMigrationMode;
	}


	/**
	 * Get the number of single migrating cells without a migration label (i.e. core cells).
	 *
	 * @return The number of single migrating cells without a migration label in this Cell3D_Group
	 */
	public int getSingleCellWithoutMigrationMode()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		return this.singleCellWithoutMigrationMode;
	}


	public int getSingleCellWithWrongMigrationMode()
	{
		if (!this.countNucleusMigrationMode)
		{
			countMigrationModeAccuracy();
		}
		return this.singleCellWithWrongMigrationMode;
	}


	public double getTotalVolume()
	{
		if (this.totalVolume == 0)
		{
			for (int i = 0; i < getMemberCount(); i++)
			{
				this.totalVolume = this.totalVolume + getMember(i).getNucleus().getVolume();
			}
		}
		return this.totalVolume;
	}


	public double getTotalVolumeCell()
	{
		if (this.totalVolumeCell == 0)
		{
			for (int i = 0; i < getMemberCount(); i++)
			{
				this.totalVolumeCell = this.totalVolumeCell + getMember(i).getVolume();
			}
		}
		return this.totalVolumeCell;
	}


	public void setMember(final Cell3D aMember)
	{
		this.members.add(aMember);
	}


	public void setMigrationmode(final String aMigrationMode)
	{
		this.migrationMode = aMigrationMode;
	}
}
