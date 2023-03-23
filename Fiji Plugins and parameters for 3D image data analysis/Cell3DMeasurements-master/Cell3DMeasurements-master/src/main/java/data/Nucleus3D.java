package data;

import java.util.ArrayList;
import java.util.List;

/**
 * The data needed to describe the measured nucleus of a cell. This is stored separately from the cell information as the nucleus is often the target of an experiment by itself.
 *
 * @author Merijn van Erp, Esther Markus
 *
 */
public class Nucleus3D extends Volume3D
{
	// The label/ID/colour of the nucleus
	private final int label;

	// The volume per voxel setting
	private final double volumePerVoxel;

	// The full set of coordinates of both the entire nucleus or just its surface
	private List<Coordinates> nucleusCoordinates = new ArrayList<>();
	private List<Coordinates> nucleusOutlines = new ArrayList<>();

	// The automated detection delivers the seed, manual detection the markers.
	private Coordinates seed = null;
	private final List<Coordinates> markers = new ArrayList<>();

	// The set of measurements done
	private final SegmentMeasurements measurements;
	private double distanceToCore;
	private double distanceToCentre;

	// Reasons to disqualify a nucleus
	private boolean borderNucleus = false;
	private boolean tooSmall = false;
	private boolean disqualifiied = false; // Any reason to disqualify the nucleus for further calculation: e.g., logistic regression


	/**
	 * Create a new Nucleus3D object. This represents one nucleus segment in the image stack and all its measurements.
	 *
	 * @param aLabel
	 *            The identification number (and colour in the image) of the nucleus segment
	 * @param aNucleusCoordinates
	 *            The list of points included in this segment
	 * @param aIntensityValues
	 *            The intensity values in the original image for all the points in the segment
	 * @param aNucleusOutlines
	 *            The coordinates of the points making up the outside of the nucleus segment
	 * @param aVolumePerVoxel
	 *            The volume that each voxel represents in the image
	 * @param aBackgroundMeasure
	 *            The background (i.e. non-segment) measure on the nucleus signal
	 */
	public Nucleus3D(final int aLabel, final List<Coordinates> aNucleusCoordinates, final List<Double> aIntensityValues, final List<Coordinates> aNucleusOutlines, final double aVolumePerVoxel,
			final double aBackgroundMeasure)
	{
		super(aNucleusCoordinates);
		this.label = aLabel;
		this.nucleusCoordinates = aNucleusCoordinates;
		this.nucleusOutlines = aNucleusOutlines;
		this.volumePerVoxel = aVolumePerVoxel;
		this.measurements = new SegmentMeasurements(aIntensityValues, aBackgroundMeasure);
	}


	/**
	 * Add a manual marker that corresponds to this nucleus. A nucleus can contain more than one manual marker.
	 *
	 * @param aMarker
	 *            The coordinates of the manual marker.
	 */
	public void addMarker(final Coordinates aMarker)
	{
		this.markers.add(aMarker);
	}


	/**
	 * Get the distance of the nucleus seed to the centre of the spheroid.
	 *
	 * @return The distance to the centre of the spheroid core.
	 */
	public double getDistanceToCentre()
	{
		return this.distanceToCentre;
	}


	/**
	 * Get the distance of the nucleus seed to the edge of the spheroid.
	 *
	 * @return The distance to the edge of the spheroid.
	 */
	public double getDistanceToCore()
	{
		return this.distanceToCore;
	}


	/**
	 * The identification label for this nucleus. This is the colour that is used to represent this nucleus in the image stack.
	 *
	 * @return The identification number of this nucleus.
	 */
	public int getLabel()
	{
		return this.label;
	}


	/**
	 * Get all the manual markers that have been assigned to this nucleus.
	 *
	 * @return The list of manual marker coordinates.
	 */
	public List<Coordinates> getMarkers()
	{
		return this.markers;
	}


	/**
	 * Get the number of manual markers assigned to this nucleus.
	 *
	 * @return The number of manual marker assigned to this nucleus.
	 */
	public int getMarkersCount()
	{
		return this.markers.size();
	}


	/**
	 * Get the measurements on the nucleus segment.
	 *
	 * @return The SegmentMeasurements object containing the measurements on the nucleus segment.
	 */
	public SegmentMeasurements getMeasurements()
	{
		return this.measurements;
	}


	/**
	 * Get the list of all the points contained in the nucleus segment.
	 *
	 * @return The List of all the Coordinates contained in the segment of this nucleus.
	 */
	public List<Coordinates> getNucleusCoordinates()
	{
		return this.nucleusCoordinates;
	}


	/**
	 * Get the list of all the points which are on the outside of the nucleus segment.
	 *
	 * @return The List of all the Coordinates detailing the outline of the segment of this nucleus.
	 */
	public List<Coordinates> getNucleusOutlines()
	{
		return this.nucleusOutlines;
	}


	/**
	 * Get the number of voxels that make up the nucleus segment.
	 *
	 * @return The number of all the points that make up the nucleus segment.
	 */
	public double getNumberOfVoxels()
	{
		return this.nucleusCoordinates.size();
	}


	/**
	 * Get the coordinates of the automatic nucleus seed used for creating the segment.
	 *
	 * @return The Coordinates of the automatic nucleus seed.
	 */
	public Coordinates getSeed()
	{
		return this.seed;
	}


	/**
	 * The total volume of the nucleus segment according to the voxel volume times the number of voxels.
	 *
	 * @return The precise pixel volume of the nucleus segment.
	 */
	public double getVolume()
	{
		return this.nucleusCoordinates.size() * this.volumePerVoxel;
	}


	/**
	 * Get the actual volume that each voxel of this nucleus segment represents.
	 *
	 * @return The voxel volume in cubic micrometers.
	 */
	public double getVolumePerVoxel()
	{
		return this.volumePerVoxel;
	}


	/**
	 * Is the nucleus segment touching the border of the image?
	 *
	 * @return True if the segement is touching any of the image borders.
	 */
	public boolean isBorderNucleus()
	{
		return this.borderNucleus;
	}


	/**
	 * Is this nucleus disqualified for any non-standard reason. An example could be logistic regresion, for example.
	 *
	 * @return True is the nucleus has been disqualified. False otherwise (default).
	 */
	public boolean isDisqualified()
	{
		return this.disqualifiied;
	}


	/**
	 * Is the nucleus based on a segment that has been found as 'too small' to be recognized as a true separate nucleus?
	 *
	 * @return the True if the nucleus is said to be 'too small'.
	 */
	public boolean isTooSmall()
	{
		return this.tooSmall;
	}


	/**
	 * Set if the nucleus has been disqualified because the segment it was touching the edge of the image.
	 *
	 * @param aBorderNucleus
	 *            Use true to disqualify, false if not.
	 */
	public void setBorderNucleus(final boolean aBorderNucleus)
	{
		this.borderNucleus = aBorderNucleus;
	}


	/**
	 * Mark the nucleus as disqualified for further processing. This disqualification does not signify a specific reason like the border or size qualifiers do.
	 *
	 * @param Use
	 *            true to disqualify the nucleus and false to tae into account again.
	 */
	public void setDisqualifiied(final boolean disqualifiied)
	{
		this.disqualifiied = disqualifiied;
	}


	/**
	 * Set the distance of the nucleus seed to the centre of the spheroid.
	 *
	 * @param aDistanceToCentre
	 *            The distance to the centre of the spheroid
	 */
	public void setDistanceToCentre(final double aDistanceToCentre)
	{
		this.distanceToCentre = aDistanceToCentre;
	}


	/**
	 * Set the distance of the nucleus seed to the edge of the spheroid.
	 *
	 * @param aDistanceToCore
	 *            The distance to the edge of the spheroid
	 */
	public void setDistanceToCore(final double aDistanceToCore)
	{
		this.distanceToCore = aDistanceToCore;
	}


	/**
	 * Set the seed for this nucleus. The seed is and the value is the one that the automatic process used to pick this specific point.
	 *
	 * @param aSeed
	 *            The coordinates that the automatic process found for the nucleus
	 * @param aAutomaticValue
	 *            The value the automatic process used to identify this coordinate
	 */
	public void setSeed(final Coordinates aSeed, final double aAutomaticValue)
	{
		getMeasurements().setMeasurement(SegmentMeasurements.LOG_VALUE, aAutomaticValue);
		this.seed = aSeed;
	}


	/**
	 * Set if the nucleus has been disqualified because the segment it was based on was 'too small'.
	 *
	 * @param aTooSmall
	 *            Use true to disqualify, false if not.
	 */
	public void setTooSmall(final boolean aTooSmall)
	{
		this.tooSmall = aTooSmall;
	}
}