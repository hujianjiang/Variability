package featureextractor.measurements;

import java.util.ArrayList;

import configuration.Measurement_Selector;
import data.Cell3D;
import data.SegmentMeasurements;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageLabeller;
import mcib_plugins.analysis.simpleMeasure;

/**
 * Class Suite_3D is a edited version of the 3D ImageJ Suite plugins. In this version the following plugins were combined: - 3D Intensity measure - 3D Geometric measure - 3D Shape Measure
 *
 * http://imagejdocu.tudor.lu/doku.php?id=plugin:stacks:3d_ij_suite:start
 *
 * @author Esther
 *
 */
class MCIB3DMeasurements
{
	/**
	 * Method getAreaPixels calculate the areaPixels and add to the nucleus
	 *
	 * @param aLables
	 */
	private static void getAreaPixels(final Cell3D[] aCells, final ArrayList<double[]> aResBase)
	{
		for (int i = 0; i < aCells.length; i++)
		{
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.SURFACE_IN_PIXELS, aResBase.get(i)[3]);
		}
	}


	/**
	 * Method getAreaUnit calculate the areaUnits and add to the nucleus
	 *
	 * @param aLables
	 */
	private static void getAreaUnit(final Cell3D[] aCells, final ArrayList<double[]> aResBase)
	{
		for (int i = 0; i < aCells.length; i++)
		{
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.SURFACE_IN_UNITS, aResBase.get(i)[4]);
		}
	}


	/**
	 * Method getCompactness calculate the compactness and add to the nucleus
	 *
	 * @param aLables
	 */
	private static void getCompactness(final Cell3D[] aCells, final ArrayList<double[]> aRes)
	{
		for (int i = 0; i < aCells.length; i++)
		{
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.COMPACTNESS, aRes.get(i)[1]);
		}
	}


	/**
	 * Method getElongatio calculate the elongatio and add to the nucleus
	 *
	 * @param aLables
	 */
	private static void getElongatio(final Cell3D[] aCells, final ArrayList<double[]> aRes)
	{

		for (int i = 0; i < aCells.length; i++)
		{
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.ELONGATIO, aRes.get(i)[3]);
		}
	}


	/**
	 * Method getFlatness calculate the flatness and add to the nucleus
	 *
	 * @param aLables
	 */
	private static void getFlatness(final Cell3D[] aCells, final ArrayList<double[]> aRes)
	{
		for (int i = 0; i < aCells.length; i++)
		{
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.FLATNESS, aRes.get(i)[4]);
		}
	}


	/**
	 * Method getIntegratedDensity calculate the integratedDensity and add to the nucleus
	 *
	 * @param aLables
	 */
	private static void getIntegratedDensity(final Cell3D[] aCells, final ArrayList<double[]> aResIntens)
	{
		for (int i = 0; i < aCells.length; i++)
		{
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.INTEGRATED_DENSITY, aResIntens.get(i)[4]);
		}
	}


	/**
	 * Method getSpareness calculate the spareness and add to the nucleus
	 *
	 * @param aLables
	 */
	private static void getSpareness(final Cell3D[] aCells, final ArrayList<double[]> aRes)
	{
		for (int i = 0; i < aCells.length; i++)
		{
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.SPARENESS, aRes.get(i)[5]);
		}
	}


	/**
	 * Method getSphericity calculate the sphericity and add to the nucleus
	 *
	 * @param aLables
	 */
	private static void getSphericity(final Cell3D[] aCells, final ArrayList<double[]> aRes)
	{
		for (int i = 0; i < aCells.length; i++)
		{
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.SPHERICITY_MCIB3D, aRes.get(i)[2]);
		}
	}


	/**
	 * Method getVolumePixels calculate the volumePixels and add to the nucleus
	 *
	 * @param aLables
	 */
	private static void getVolumePixels(final Cell3D[] aCells, final ArrayList<double[]> aResBase)
	{
		for (int i = 0; i < aCells.length; i++)
		{
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.VOLUME_IN_PIXELS, aResBase.get(i)[1]);
		}
	}


	/**
	 * Method getVolumeUnit calculate the volumeUnits and add to the nucleus
	 *
	 * @param aLables
	 */
	private static void getVolumeUnit(final Cell3D[] aCells, final ArrayList<double[]> aResBase)
	{
		for (int i = 0; i < aCells.length; i++)
		{
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.VOLUME_IN_UNITS, aResBase.get(i)[2]);
		}
	}


	public static void setMeasurements(final Cell3D[] aCells, final ImagePlus aOriginalImage, final ImagePlus aLabelImage)
	{
		IJ.log("Start MCIB 3D measurements");

		final Calibration cal = aOriginalImage.getCalibration();
		// Create a scaled image
		final ImagePlus labeldImageScaled = aLabelImage.duplicate();
		labeldImageScaled.setCalibration(cal);

		final ImageInt img = ImageInt.wrap(labeldImageScaled);
		ImagePlus seg;
		if (img.isBinary(0))
		{
			final ImageLabeller label = new ImageLabeller();
			seg = label.getLabels(img).getImagePlus();
			seg.show("Labels");
		}
		else
		{
			seg = labeldImageScaled;
		}
		final simpleMeasure mes = new simpleMeasure(seg);

		final ArrayList<double[]> resBase = mes.getMeasuresBase();
		final ArrayList<double[]> res = mes.getMeasuresShape();
		final ArrayList<double[]> resIntens = mes.getMeasuresStats(aOriginalImage);

		if (Measurement_Selector.getMeasurementPreference(SegmentMeasurements.INTEGRATED_DENSITY))
		{
			getIntegratedDensity(aCells, resIntens);
		}
		if (Measurement_Selector.getMeasurementPreference(SegmentMeasurements.VOLUME_IN_PIXELS))
		{
			getVolumePixels(aCells, resBase);
		}
		if (Measurement_Selector.getMeasurementPreference(SegmentMeasurements.VOLUME_IN_UNITS))
		{
			getVolumeUnit(aCells, resBase);
		}
		if (Measurement_Selector.getMeasurementPreference(SegmentMeasurements.SURFACE_IN_PIXELS))
		{
			getAreaPixels(aCells, resBase);
		}
		if (Measurement_Selector.getMeasurementPreference(SegmentMeasurements.SURFACE_IN_UNITS))
		{
			getAreaUnit(aCells, resBase);
		}
		if (Measurement_Selector.getMeasurementPreference(SegmentMeasurements.COMPACTNESS))
		{
			getCompactness(aCells, res);
		}
		if (Measurement_Selector.getMeasurementPreference(SegmentMeasurements.SPHERICITY_MCIB3D))
		{
			getSphericity(aCells, res);
		}
		if (Measurement_Selector.getMeasurementPreference(SegmentMeasurements.ELONGATIO))
		{
			getElongatio(aCells, res);
		}
		if (Measurement_Selector.getMeasurementPreference(SegmentMeasurements.FLATNESS))
		{
			getFlatness(aCells, res);
		}
		if (Measurement_Selector.getMeasurementPreference(SegmentMeasurements.SPARENESS))
		{
			getSpareness(aCells, res);
		}

		labeldImageScaled.changes = false;
		labeldImageScaled.close();

		IJ.log("Ended MCIB 3D measurements");
	}
}
