package featureextractor;

import java.util.List;

import data.Cell3D;
import data.Coordinates;
import data.Nucleus3D;
import ij.IJ;
import ij.ImagePlus;

/**
 * This helper class contains the tools to filter a list of Cell3D based on several conditions.
 */
public class PostProcessor
{
	// /**
	// * Method createLogisticRegressionTable, creates a logistic regression table which can be used to determine the logistic regression values.
	// *
	// * @param nucleus
	// */
	// public static void createLogisticRegressionTable(final Cell3D[] aCells)
	// {
	// final ResultsTable resultsStatistics = new ResultsTable();
	// for (final Cell3D cell : aCells)
	// {
	// final Nucleus3D nucleus = cell.getNucleus();
	// // IJ.log("Nucleus: " + aNucleus[j].getLabel()+ " Value: "+aNucleus[j].getSeed(0).getGrayValue() +" ZCoordinate: " +aNucleus[j].getSeed(0).getzValue() + " Amount of markers " +aNucleus[j].getMarkersCount() );
	// if (nucleus.getMarkersCount() < 2)
	// {
	// resultsStatistics.incrementCounter();
	// resultsStatistics.addValue("Label", nucleus.getLabel());
	// final double count = nucleus.getMarkersCount();
	// if (count == 1)
	// {
	// resultsStatistics.addValue("inout", 1);
	// }
	// if (count == 0)
	// {
	// resultsStatistics.addValue("inout", 0);
	// }
	//
	// final NuclearMeasurements measurements = nucleus.getMeasurements();
	// resultsStatistics.addValue("size", nucleus.getVolume());
	// resultsStatistics.addValue("LoGValue", measurements.getMexicanHatValue());
	// resultsStatistics.addValue("Skewness", measurements.getSkewness());
	// resultsStatistics.addValue("SurfaceArea", measurements.getSurfaceArea());
	// resultsStatistics.addValue("Sphericites", measurements.getSphericities());
	// resultsStatistics.addValue("EulerNumber", measurements.getEulerNumber());
	// resultsStatistics.addValue("EulerNumber", measurements.getEulerNumber());
	// resultsStatistics.addValue("Elli.R1", measurements.getEllipsoid()[3]);
	// resultsStatistics.addValue("Elli.R2", measurements.getEllipsoid()[4]);
	// resultsStatistics.addValue("Elli.R3", measurements.getEllipsoid()[5]);
	// resultsStatistics.addValue("Elli.Azim", measurements.getEllipsoid()[6]);
	// resultsStatistics.addValue("Elli.Elev", measurements.getEllipsoid()[7]);
	// resultsStatistics.addValue("Elli.Roll", measurements.getEllipsoid()[8]);
	// resultsStatistics.addValue("Elli.R1/R2", measurements.getElongations()[0]);
	// resultsStatistics.addValue("Elli.R1/R3", measurements.getElongations()[1]);
	// resultsStatistics.addValue("Elli.R2/R3", measurements.getElongations()[2]);
	// resultsStatistics.addValue("InscrSphere.Center.X", measurements.getInscribedSphere()[0]);
	// resultsStatistics.addValue("InscrSphere.Center.Y", measurements.getInscribedSphere()[1]);
	// resultsStatistics.addValue("InscrSphere.Center.Z", measurements.getInscribedSphere()[2]);
	// resultsStatistics.addValue("InscrSphere.Center.Radius", measurements.getInscribedSphere()[3]);
	// }
	// }
	// resultsStatistics.show("Results Statistics");
	// }

	private static void excludeBorderNucleiOnNuclearCentre(final Cell3D[] aCells, final int aWidth, final int aHeight, final int aDepth, final int aExclusionZone, final int aZFactor)
	{
		for (int i = 0; i < aCells.length; i++)
		{
			final Nucleus3D nucleus = aCells[i].getNucleus();
			final Coordinates nucleusSeed = nucleus.getSeed();

			if (nucleusSeed.getXcoordinate() < (aZFactor * aExclusionZone) || //
					nucleusSeed.getXcoordinate() > aWidth - (aZFactor * aExclusionZone) || //
					nucleusSeed.getYcoordinate() < (aZFactor * aExclusionZone) || //
					nucleusSeed.getYcoordinate() > aHeight - (aZFactor * aExclusionZone) || //
					(aDepth > 1 && (nucleusSeed.getZcoordinate() < aExclusionZone || nucleusSeed.getZcoordinate() >= aDepth - aExclusionZone)))
			{
				nucleus.setBorderNucleus(true);
			}
		}
	}


	/**
	 * Detects if any of the nucleus outline coordinates is on one of the borders of the image (x, y or z). If so, the nucleus is set as a 'border' nucleus, which can be handled by further post processing.
	 *
	 * @param aCells
	 *            A list of Cells3Ds, each containing a nucleus
	 * @param aWidth
	 *            The width of the image, to determine the x border
	 * @param aHeight
	 *            The height of the image, to determine the x border
	 * @param aSlices
	 *            The depth of the image, to determine the x border
	 */
	private static void excludeBorderNucleiOnTouch(final Cell3D[] aCells, final int aWidth, final int aHeight, final int aDepth)
	{
		for (int i = 0; i < aCells.length; i++)
		{
			final Nucleus3D nucleus = aCells[i].getNucleus();
			final List<Coordinates> nucleusOutlines = nucleus.getNucleusOutlines();

			for (final Coordinates point : nucleusOutlines)
			{
				if (point.getXcoordinate() == 0 || //
						point.getXcoordinate() == aWidth - 1 || //
						point.getYcoordinate() == 0 || //
						point.getYcoordinate() == aHeight - 1 || //
						(aDepth > 1 && (point.getZcoordinate() == 0 || point.getZcoordinate() == aDepth - 1)))
				{
					nucleus.setBorderNucleus(true);
					break;
				}
			}
		}
	}


	/**
	 * Detect any cells of which the nuclear volume is below a certain threshold and set them to 'too small'. These can be handled in further post processing.
	 *
	 * @param aCells
	 *            The cells to be checked.
	 * @param aSmallNucleiSize
	 */
	private static void excludeNucleusOnSize(final Cell3D[] aCells, final Integer aSmallNucleiSize)
	{
		for (final Cell3D cell : aCells)
		{
			final Nucleus3D nucleus = cell.getNucleus();
			final double volume = nucleus.getVolume();
			if (volume < aSmallNucleiSize)
			{
				nucleus.setTooSmall(true);
			}
		}
	}


	/**
	 * The command function for the post-processing class. Given a list of Cell3Ds and an image, this function will be able to mark each cell according to any of the post-processing conditions. Note that the filtering is marked in each Cell3D object
	 * itself and not by exclusion on the input list. The marks in the cells can later be used to handle the cells that have a certain condition.
	 *
	 * @param aCells
	 *            The list of Cell3Ds to be filtered
	 * @param aImage
	 *            The ImagePlus image in which the cells have been detected
	 * @param aSmallNucleiSize
	 *            What is the minimum size of nucleus segments to be used in further calculations? Can be null if no exclusion on size is needed
	 * @param aExclusionZone
	 *            How close to the edge are nucleus seeds still viable? Can be null if no exclusion on border adjacency is needed
	 */
	public static void postProcessCellList(final Cell3D[] aCells, final ImagePlus aImage, final Integer aSmallNucleiSize, final Integer aExclusionZone)
	{
		if (aSmallNucleiSize != null)
		{
			IJ.log("Excluding cells on nucleus size");
			excludeNucleusOnSize(aCells, aSmallNucleiSize);
		}

		if (aExclusionZone != null)
		{
			IJ.log("Excluding cells on border placement");
			excludeBorderNucleiOnNuclearCentre(aCells, aImage.getWidth(), aImage.getHeight(), aImage.getNSlices(), aExclusionZone, 2);
		}
		// TODO logistic regression?
	}

	// TODO: If logistic regression is enabled, don't forget the Cell3D_Group!
	// public static void logisticRegression(final Cell3D[] aCells)
	// {
	// for (final Cell3D cell : aCells)
	// {
	// final Nucleus3D nuc = cell.getNucleus();
	// // -7.72338+ Elli.R1*0.09313+ Elli.R2*0.60769+ Elli.R3*-0.76127+InscrBall.Center.Radius*2.05705
	// final double ElliR1 = nuc.getMeasurements().getEllipsoid()[3];
	// final double ElliR2 = nuc.getMeasurements().getEllipsoid()[4];
	// final double ElliR3 = nuc.getMeasurements().getEllipsoid()[5];
	// final double BallCenterRadius = nuc.getMeasurements().getInscribedSphere()[3];
	// final double value = -7.72338 + (ElliR1 * 0.09313) + (ElliR2 * 0.60769) + (ElliR3 * -0.76127) + (BallCenterRadius * 2.05705);
	// if (value < -1.0)
	// {
	// nuc.setSelected(false);
	// }
	// }
	// }

	// private void createResultsTableSummaryOfTheImages(final boolean migrationmode, final List<Labeled_Coordinate> listOfMarkers, final ResultsTable resultsSum, final String shortTitle,
	// final Cell3D_Group nucleusGroup, final double amount, final List<Labeled_Coordinate> listOfSeeds, final int[] seedsInDoubleWihtoutNucleus, final int[] markerInDoubleWithoutNucleus,
	// final boolean excludeBorderNuclei, final boolean aLogisticRegression, final boolean aSelectionOnSize)
	// {
	//
	// double percentageMarkersFound = 0;
	// double percentageMarkersNotFound = 0;
	// double percentageMarkersDouble = 0;
	// double amountMarkers = 0;
	// resultsSum.incrementCounter(); // Fill the new table
	// resultsSum.addValue("Amount", nucleusGroup.getMemberCount()); // Add the amount value of the image
	//
	// if (excludeBorderNuclei == false && aLogisticRegression == false && aSelectionOnSize == false)
	// {
	// amountMarkers = listOfMarkers.size();
	// }
	//
	// if (excludeBorderNuclei == true || aLogisticRegression == true || aSelectionOnSize == true)
	// {
	// amountMarkers = markerInDoubleWithoutNucleus[0] + markerInDoubleWithoutNucleus[1] + markerInDoubleWithoutNucleus[2];
	// resultsSum.addValue("Nucleus Selected", nucleusGroup.getNucleusSelected());
	// resultsSum.addValue("Nucleus Excluded (Correct segmented)", nucleusGroup.getNucleusExcludedType()[0]);
	// resultsSum.addValue("Nucleus Excluded (Over-segmented)", nucleusGroup.getNucleusExcludedType()[1]);
	// resultsSum.addValue("Nucleus Excluded (Under-Segmented)", nucleusGroup.getNucleusExcludedType()[2]);
	// }
	// percentageMarkersFound = (markerInDoubleWithoutNucleus[0] / amountMarkers) * 100;
	// percentageMarkersNotFound = (markerInDoubleWithoutNucleus[2] / amountMarkers) * 100;
	// percentageMarkersDouble = (markerInDoubleWithoutNucleus[1] / amountMarkers) * 100;
	//
	// resultsSum.addValue("% Nucleus correct segmented", nucleusGroup.getPercentageNucleusCorrectSegmented());
	// resultsSum.addValue("% Nucleus oversegmented", nucleusGroup.getPercentageNucleusOverSegmented());
	// resultsSum.addValue("% Nucleus undersegmented", nucleusGroup.getPercentageNucleusUnderSegmented());
	// resultsSum.addValue("% Markers in Nucleus", percentageMarkersFound);
	// resultsSum.addValue("% Markers outside Nucleus", percentageMarkersNotFound);
	// resultsSum.addValue("% Markers double in Nucleus", percentageMarkersDouble);
	//
	// resultsSum.addValue("Nucleus correct segmented", nucleusGroup.getNucleusCorrectSegmented());
	// resultsSum.addValue("Nucleus oversegmented", nucleusGroup.getNucleusOverSegmented());
	// resultsSum.addValue("Nucleus undersegmented", nucleusGroup.getNucleusUnderSegmented());
	//
	// if (excludeBorderNuclei == true && aLogisticRegression == true)
	// {
	// resultsSum.addValue("Nucleus deselected on border and regression", nucleusGroup.getNucleusTwiceExcluded());
	// }
	// if (excludeBorderNuclei == true && aSelectionOnSize == true)
	// {
	// resultsSum.addValue("Excluded nuclei on size and border", nucleusGroup.getNucleusExcludedBorderAndSize());
	// }
	// if (excludeBorderNuclei == true)
	// {
	// resultsSum.addValue("Border nucleus", nucleusGroup.getNucleusExcludedBorder());
	// }
	// if (aSelectionOnSize == true)
	// {
	// resultsSum.addValue("Excluded nuclei on size", nucleusGroup.getNucleusToSmall());
	// }
	// if (aLogisticRegression == true)
	// {
	// resultsSum.addValue("Excluded nucleus", nucleusGroup.getNucleusNOTSelected());
	// }
	//
	// if (excludeBorderNuclei == true)
	// {
	// resultsSum.addValue("Markers in border nucleus", markerInDoubleWithoutNucleus[3]);
	// }
	// if (aLogisticRegression == true)
	// {
	// resultsSum.addValue("Markers in excluded nucleus", markerInDoubleWithoutNucleus[4]);
	// }
	// }

}
