package featureextractor;

import data.Coordinates;

public class Labeled_Coordinate
{
	Coordinates coordinates;
	int label;
	int grayValue;
	String migrationMode;


	/**
	 *
	 * @param aLabel
	 * @param aXValue
	 * @param aYValue
	 * @param aZValue
	 */
	public Labeled_Coordinate(final int aLabel, final Coordinates aCoordinates, final int aValue, final String aMigrationMode)
	{
		this.coordinates = aCoordinates;
		this.label = aLabel;
		this.grayValue = aValue;
		this.migrationMode = aMigrationMode;
	}


	public Coordinates getCoordinates()
	{
		return this.coordinates;
	}


	public int getGrayValue()
	{
		return this.grayValue;
	}


	/**
	 * @return the label of the Labled_Coordinate
	 */
	public int getLabel()
	{
		return this.label;
	}


	public String getMigrationMode()
	{
		return this.migrationMode;
	}


	public double getXCoordinate()
	{
		return this.coordinates.getXcoordinate();
	}


	public double getYCoordinate()
	{
		return this.coordinates.getYcoordinate();
	}


	public double getZCoordinate()
	{
		return this.coordinates.getZcoordinate();
	}


	/**
	 * @return: label (xValue, yValue, zValue) value
	 */
	@Override
	public String toString()
	{
		final String toString = this.label + " (" + getXCoordinate() + "," + getYCoordinate() + "," + getZCoordinate() + ") " + this.grayValue;
		return toString;
	}
}