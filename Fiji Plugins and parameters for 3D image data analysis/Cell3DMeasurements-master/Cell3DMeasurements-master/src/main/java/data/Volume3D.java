package data;

import java.util.ArrayList;
import java.util.List;

import utils.MyMath;

/**
 * A set of 3D Coordinates that makes up one volume. Note: does not check for contingence.
 *
 * @author Merijn van Erp
 *
 */
public abstract class Volume3D
{
	// The set of Coordinates that define this volume
	private List<Coordinates> coordinates = new ArrayList<>();

	// The bounding box of the nucleus (for quick 'not-contains' checks)
	private final Coordinates minCoord;
	private final Coordinates maxCoord;


	public Volume3D(final List<Coordinates> aCoordinates)
	{
		this.coordinates = aCoordinates;
		final Coordinates[] minMax = MyMath.getBoundingBox(aCoordinates);
		this.minCoord = minMax[0];
		this.maxCoord = minMax[1];
	}


	/**
	 * Does this volume contain the coordinates in question.
	 *
	 * @param aCoordinate
	 *            The Coordinates that may be in this volume
	 *
	 * @return True if the Coordinates fall within (including edges) the Volume3D, false otherwise.
	 */
	public boolean contains(final Coordinates aCoordinate)
	{
		// First check the bounding box. If it is outside that, don't bother checking the individual contained Coordinates.
		final boolean containX = (aCoordinate.getXcoordinate() >= this.minCoord.getXcoordinate() && aCoordinate.getXcoordinate() <= this.maxCoord.getXcoordinate());
		final boolean containY = (aCoordinate.getYcoordinate() >= this.minCoord.getYcoordinate() && aCoordinate.getYcoordinate() <= this.maxCoord.getYcoordinate());
		final boolean containZ = (aCoordinate.getZcoordinate() >= this.minCoord.getZcoordinate() && aCoordinate.getZcoordinate() <= this.maxCoord.getZcoordinate());
		if (containX && containY && containZ)
		{
			for (final Coordinates coord : this.coordinates)
			{
				if (aCoordinate.equals(coord))
				{
					return true;
				}
			}
		}

		return false;
	}


	/**
	 * Get the size of the largest dimension of the bounding box of this volume depending on the scale of each dimension.
	 *
	 * @param aPixelWidth
	 *            The actual size of a pixel in the x-direction
	 * @param aPixelHeigth
	 *            The actual size of a pixel in the y-direction
	 * @param aPixelDepth
	 *            The actual size of a pixel in the z-direction
	 *
	 * @return The maximum of either the width, height or depth of the bounding box adjusted for actual pixel dimension sizes.
	 */
	public double getMaximalLength(final double aPixelWidth, final double aPixelHeigth, final double aPixelDepth)
	{
		final double maximalX = (this.maxCoord.getXcoordinate() - this.minCoord.getXcoordinate()) * aPixelWidth;
		final double maximalY = (this.maxCoord.getYcoordinate() - this.minCoord.getYcoordinate()) * aPixelHeigth;
		final double maximalZ = (this.maxCoord.getZcoordinate() - this.minCoord.getZcoordinate()) * aPixelDepth;
		double length = maximalX;
		if (maximalY >= maximalZ)
		{
			if (maximalY > maximalX)
			{
				length = maximalY;
			}
		}
		else if (maximalZ > maximalX)
		{
			length = maximalZ;
		}

		return length;
	}

}
