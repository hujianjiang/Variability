package featureextractor.measurements;

import configuration.Measurement_Selector;
import data.Cell3D;
import data.SegmentMeasurements;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import inra.ijpb.measure.GeometricMeasures3D;

/**
 * Class ParticleAnalyzer3D is a edited version of the MorpholibJ ParticleAnalyzer3D plugin.
 *
 * @author Esther
 *
 */
class ParticleAnalyzer3D
{
	private static int surfaceAreaDirs = 13;
	private static int connectivity = 26;


	/**
	 * Method getEllipsoid calculate the Ellipsoid and return this
	 *
	 * @param aInputStack
	 * @param aLables
	 * @param aResol
	 * @return * The feature ellipsoid has 9 variables: 0= Elli.Center.X 1=Elli.Center.Y 2= Elli.Center.Z 3= Elli.R1 4= Elli.R2 5= Elli.R3 6= Elli.Azim 7= Elli.Elev 8= Elli.Roll
	 */
	private static double[][] getEllipsoid(final ImageStack aInputStack, final int[] aLables, final double[] aResol)
	{
		final double[][] ellipsoids = GeometricMeasures3D.inertiaEllipsoid(aInputStack, aLables, aResol);
		return ellipsoids;
	}


	/**
	 * Method getSurfaces calculate the surface area and return this as an array of doubles
	 *
	 * @param aInputStack
	 * @param aLables
	 * @param aResol
	 * @return array of doubles surfacearea
	 */
	private static double[] getSurfaces(final ImageStack aInputStack, final int[] aLables, final double[] aResol)
	{
		final double[] surfaces = GeometricMeasures3D.surfaceAreaCrofton(aInputStack, aLables, aResol, surfaceAreaDirs);
		return surfaces;
	}


	/**
	 * Return add 3D Surfaces area to the nucleus
	 *
	 * @return
	 */
	private static double[] getVolumes(final ImageStack aInputStack, final int[] aLables, final double[] aResol)
	{
		final double[] volumes = GeometricMeasures3D.volume(aInputStack, aLables, aResol);
		return volumes;
	}


	public static void runParticleAnalyzer3D(final Cell3D[] aCells, final ImagePlus aOriginalImage, final ImagePlus aLabelImage, final int[] aLabels)
	{
		IJ.log("Start ParticleAnalyzer3D");
		final ImageStack inputStack = aLabelImage.getImageStack();
		final Calibration calibration = aOriginalImage.getCalibration();

		final double[] resol = new double[] { 1, 1, 1 };
		if (calibration != null && calibration.scaled())
		{
			resol[0] = calibration.pixelWidth;
			resol[1] = calibration.pixelHeight;
			resol[2] = calibration.pixelDepth;
		}

		if (Measurement_Selector.getMeasurementPreference(SegmentMeasurements.SURFACE_AREA))
		{
			setSurfaces(aCells, inputStack, aLabels, resol);
		}
		if (Measurement_Selector.getMeasurementPreference(SegmentMeasurements.SPHERICITY_MORPHOLIBJ))
		{
			setSphericities(aCells, inputStack, aLabels, resol);
		}
		if (Measurement_Selector.getMeasurementPreference(SegmentMeasurements.EULER_NUMBER))
		{
			setEulerNumber(aCells, inputStack, aLabels);
		}
		if (Measurement_Selector.getMeasurementPreference(SegmentMeasurements.ELLIPSOID_CENTER_X) || Measurement_Selector.getMeasurementPreference(SegmentMeasurements.ELLIPSOID_RADIUS_1))
		{
			setEllipsoid(aCells, inputStack, aLabels, resol);
		}
		if (Measurement_Selector.getMeasurementPreference(SegmentMeasurements.ELLOGATION_R1_R2))
		{
			setElongations(aCells, inputStack, aLabels, resol);
		}
		if (Measurement_Selector.getMeasurementPreference(SegmentMeasurements.INSCRIBED_SPHERE_CENTER_X) || Measurement_Selector.getMeasurementPreference(SegmentMeasurements.INSCRIBED_SPHERE_RADIUS))
		{
			setInscribedSphere(aCells, inputStack, aLabels, resol);
		}
		IJ.log("Ended ParticleAnalyzer3D");
	}


	/**
	 * Method setEllipsoid calculate the Ellipsoid each nucleus, and add the Ellipsoid to the nucleus The feature ellipsoid has 9 variables: 0= Elli.Center.X 1=Elli.Center.Y 2= Elli.Center.Z 3= Elli.R1 4= Elli.R2 5= Elli.R3 6= Elli.Azim 7= Elli.Elev
	 * 8= Elli.Roll
	 *
	 * @param aInputStack
	 * @param aLables
	 * @param aResol
	 */
	private static void setEllipsoid(final Cell3D[] aCells, final ImageStack aInputStack, final int[] aLables, final double[] aResol)
	{
		final double[][] ellipsoids = GeometricMeasures3D.inertiaEllipsoid(aInputStack, aLables, aResol);
		for (int i = 0; i < aCells.length; i++)
		{
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.ELLIPSOID_CENTER_X, ellipsoids[i][0]);
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.ELLIPSOID_CENTER_Y, ellipsoids[i][1]);
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.ELLIPSOID_CENTER_Z, ellipsoids[i][2]);
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.ELLIPSOID_RADIUS_1, ellipsoids[i][3]);
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.ELLIPSOID_RADIUS_2, ellipsoids[i][4]);
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.ELLIPSOID_RADIUS_3, ellipsoids[i][5]);
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.ELLIPSOID_RADIUS_AZIMUT, ellipsoids[i][6]);
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.ELLIPSOID_RADIUS_ELEVATION, ellipsoids[i][7]);
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.ELLIPSOID_RADIUS_ROLL, ellipsoids[i][8]);
		}
	}


	/**
	 * Method setElongations calculate the Elongations each nucleus, and add the Elongations to the nucleus The feature Elongation has three variables: 0=Elli.R1/R2 1=Elli.R1/R3 2=Elli.R2/R3
	 *
	 * @return
	 */
	private static void setElongations(final Cell3D[] aCells, final ImageStack aInputStack, final int[] aLables, final double[] aResol)
	{
		final double[][] elongations = GeometricMeasures3D.computeEllipsoidElongations(getEllipsoid(aInputStack, aLables, aResol));
		for (int i = 0; i < aCells.length; i++)
		{
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.ELLOGATION_R1_R2, elongations[i][0]);
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.ELLOGATION_R1_R3, elongations[i][1]);
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.ELLOGATION_R2_R3, elongations[i][2]);
		}
	}


	/**
	 * Method getEulerNumber calculate the EulerNumber of each nucleus, and add the EulerNumber to the nucleus
	 *
	 * @param aInputStack
	 * @param aLables
	 */
	private static void setEulerNumber(final Cell3D[] aCells, final ImageStack aInputStack, final int[] aLables)
	{
		final double[] eulerNumbers = GeometricMeasures3D.eulerNumber(aInputStack, aLables, connectivity);
		for (int i = 0; i < aCells.length; i++)
		{
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.EULER_NUMBER, eulerNumbers[i]);
		}
	}


	/**
	 * Method setInscribedSphere calculate the InscribedSphere each nucleus, and add the InscribedSphere to the nucleus The feature InscribedSphere has four variables: 0= InscrSphere.Center.X 1=InscrSphere.Center.Y 2=InscrSphere.Center.Z
	 * 3=InscrSphere.Radius
	 *
	 * @return
	 */
	private static void setInscribedSphere(final Cell3D[] aCells, final ImageStack aInputStack, final int[] aLables, final double[] aResol)
	{
		final double[][] inscribedSphere = GeometricMeasures3D.maximumInscribedSphere(aInputStack, aLables, aResol);
		for (int i = 0; i < aCells.length; i++)
		{
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.INSCRIBED_SPHERE_CENTER_X, inscribedSphere[i][0]);
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.INSCRIBED_SPHERE_CENTER_Y, inscribedSphere[i][1]);
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.INSCRIBED_SPHERE_CENTER_Z, inscribedSphere[i][2]);
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.INSCRIBED_SPHERE_RADIUS, inscribedSphere[i][3]);
		}
	}


	/**
	 * Method setSphericities calculate the Sphericities of each nucleus, and add the surface area to the nucleus
	 *
	 * @param aInputStack
	 * @param aLables
	 * @param aResol
	 */
	private static void setSphericities(final Cell3D[] aCells, final ImageStack aInputStack, final int[] aLables, final double[] aResol)
	{
		final double[] sphericities = GeometricMeasures3D.computeSphericity(getVolumes(aInputStack, aLables, aResol), getSurfaces(aInputStack, aLables, aResol));
		for (int i = 0; i < aCells.length; i++)
		{
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.SPHERICITY_MORPHOLIBJ, sphericities[i]);
		}
	}


	/**
	 * Method setSurfaces calculate the surface area of each nucleus, and add the surface area to the nucleus
	 *
	 * @param aInputStack
	 * @param aLables
	 * @param aResol
	 */
	private static void setSurfaces(final Cell3D[] aCells, final ImageStack aInputStack, final int[] aLables, final double[] aResol)
	{
		final double[] surfaces = GeometricMeasures3D.surfaceAreaCrofton(aInputStack, aLables, aResol, surfaceAreaDirs);
		for (int i = 0; i < aCells.length; i++)
		{
			aCells[i].getNucleus().getMeasurements().setMeasurement(SegmentMeasurements.SURFACE_AREA, surfaces[i]);
		}
	}
}