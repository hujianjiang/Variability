package data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The 3D cell object. Contains a 3D nucleus object, measurements and the coordinates and outlines of the cell. It also keeps track of its touching neighbouring cells.
 *
 * @author Merijn van Erp
 *
 */
public class Cell3D
{
	public static final String DUAL_IDENTITY = "DUAL_IDENTITY"; // Specifically, this cell has gotten two (manual) markers with a different migration mode

	// The Nucleus 3D object describing the nucleus of this cell in a similar manner
	private final Nucleus3D nucleus;

	// The two migration modes: automatic and by manual marker
	private String migrationMode = Cell3D_Group.NONE;
	private String markerMigrationMode = Cell3D_Group.NONE;

	// Coordinate lists giving a precise description of the volume of the cell
	private List<Coordinates> coordinates = new ArrayList<>();
	private List<Coordinates> outline = new ArrayList<>();
	private List<Coordinates> nucleusSurroundings = new ArrayList<>();

	// The list of cells that are in contact with this cell
	private Set<Integer> connectedNeighbours;

	// Measurements on this cell on both actin and any other type of signal
	private SegmentMeasurements actinMeasurements;
	private List<SegmentMeasurements> signalMeasurements = null;


	/**
	 * Create a new 3D cell based on a 3D nucleus.
	 *
	 * @param aNucleus
	 *            The nucleus base
	 */
	public Cell3D(final Nucleus3D aNucleus)
	{
		this.nucleus = aNucleus;
	}


	/**
	 * Give the details of the cell. Without this, the cell is just a placeholder for the nucleus.
	 *
	 * @param aCoordinates
	 *            The list of Coordinates for the complete volume of the cell
	 * @param aGrayValueVoxels
	 *            The gray values for this cell. No specific ordering guaranteed, just a list of all present intensity values.
	 * @param aOutlines
	 *            The list of coordinates for just the surface of the cell
	 * @param aConnectedNeighbourCells
	 *            A list of neighbouring cells that are directly adjacent to (i.e. touching) this cell. Each neighbour is identified by its label number.
	 * @param aBackgroundIntensity
	 *            The background intensity measure for the cell (non-nucleus) signal
	 */
	public void addCellFeatures(final List<Coordinates> aCoordinates, final List<Double> aGrayValueVoxels, final List<Coordinates> aOutlines, final List<Coordinates> aNucleusSurroundings,
			final Set<Integer> aConnectedNeighbourCells, final Double aBackgroundIntensity)
	{
		this.coordinates = aCoordinates;
		this.actinMeasurements = new SegmentMeasurements(aGrayValueVoxels, aBackgroundIntensity);
		this.outline = aOutlines;
		this.connectedNeighbours = aConnectedNeighbourCells;
		this.nucleusSurroundings = aNucleusSurroundings;
	}


	/**
	 * Add a manual marker and its associated cellular migration mode to the list of markers stored in the nucleus. The list can contain multiple markers in case of a under-segmented nucleus. The migration mode is stored in this class and is only
	 * overruled if multiple markers are added with a different migration mode. This leads to the erroneous situation flagged as DUAL_IDENTITY.
	 *
	 * @param aMarker
	 *            The coordinates for the new marker
	 * @param aMigrationMode
	 *            The migration mode (manually determined) associated with the marker
	 */
	public void addMarker(final Coordinates aMarker, final String aMigrationMode)
	{
		if (this.nucleus.getMarkersCount() == 0)
		{
			this.markerMigrationMode = aMigrationMode;
		}
		else
		{
			if (!this.markerMigrationMode.equals(aMigrationMode))
			{
				this.markerMigrationMode = DUAL_IDENTITY;
			}
		}

		this.nucleus.addMarker(aMarker);
	}


	/**
	 * Add a new set of measurements on a specific channel. Note that only the measurements ar stored and not their context!
	 *
	 * @param aSegmentMeasurements
	 *            The set of measurements
	 */
	public void addSignalMeasurements(final SegmentMeasurements aSegmentMeasurements)
	{
		if (this.signalMeasurements == null)
		{
			this.signalMeasurements = new ArrayList<>();
		}

		this.signalMeasurements.add(aSegmentMeasurements);
	}


	/**
	 * Get the list of id-labels of cells that are directly connected to this cell. The connected attribute is used to determine which cells belong to the same migration group.
	 *
	 * @return The list of labels of cells that touch this cell. A label can be retrieved via the Nucleus3D object of a cell.
	 */
	public Set<Integer> getConnectedNeighbours()
	{
		return this.connectedNeighbours;
	}


	/**
	 * Get the number of connected neighbour cells. This number can be used to determine the migration mode of the cell.
	 *
	 * @return The number of cells that are considered to be connected to this cell.
	 */
	public int getConnectedNeighboursCount()
	{
		return this.connectedNeighbours.size();
	}


	/**
	 * Gets the list of coordinates of all the points that make up this cell.
	 *
	 * @return A List of Coordinates for all points in the image that belong to this cell
	 */
	public List<Coordinates> getCoordinates()
	{
		return this.coordinates;
	}


	/**
	 * Get the manually set marker mode. Note that if several markers have been given with a different mode, the migration mode will be set to DUAL_IDENTITY.
	 *
	 * @return The manual marker mode for this cell
	 */
	public String getMarkerMigrationMode()
	{
		return this.markerMigrationMode;
	}


	/**
	 * Get the set of measurements that have been taken on the actin signal of this cell.
	 *
	 * @return The SegmentMeasurements object containing the measurements or null if no measurements have been taken.
	 */
	public SegmentMeasurements getMeasurements()
	{
		return this.actinMeasurements;
	}


	/**
	 * Get the automatically detected migration mode for this cell.
	 *
	 * @return The migration mode as a String
	 */
	public String getMigrationMode()
	{
		return this.migrationMode;
	}


	/**
	 * Get the nucleus object of this cell. Each cell should at least contain a Nucleus3D.
	 *
	 * @return The nucleus object for this cell
	 */
	public Nucleus3D getNucleus()
	{
		return this.nucleus;
	}


	/**
	 * Get the coordinates of all the pixels that lay within the direct surroundings outside of the nucleus. Does not contain this or other nucleus' pixels and only contains pixles that are in an actin segment. NOTE: may contain pixels that are from
	 * another actin segment!
	 *
	 * @return The list of pixel Coordinates that make up the cell surroundings outside the nucleus. May be an empty List if no actin segmentation has been done or if the nucleus has no adjacent actin pixels.
	 */
	public List<Coordinates> getNucleusSurroundings()
	{
		return this.nucleusSurroundings;
	}


	/**
	 * Get the coordinates of all the pixels that lay on the outer edges of the cell segment.
	 *
	 * @return The list of pixel Coordinates that make up the outline of the cell
	 */
	public List<Coordinates> getOutline()
	{
		return this.outline;
	}


	/**
	 * Get a list of the measurements done on any non-standard (i.e. nucleus and actin) channels. This list may be empty.
	 *
	 * @return The list of SegmentMeasurements for the alternate channels that have been measured.
	 */
	public List<SegmentMeasurements> getSignalMeasurements()
	{
		return this.signalMeasurements;
	}


	/**
	 * Get the volume of this cell (in units defined by the image).
	 *
	 * @return The total volume of this cell (including nucleus).
	 */
	public double getVolume()
	{
		return this.coordinates.size() * this.nucleus.getVolumePerVoxel();
	}


	/**
	 * Sets the automatically detected migration mode for this cell.
	 *
	 * @param aMigrationMode
	 *            The migration mode, @see Cell3D_Group for valid arguments
	 */
	public void setMigrationMode(final String aMigrationMode)
	{
		this.migrationMode = aMigrationMode;
	}
}
