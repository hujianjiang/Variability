package todo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import data.PointValue;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;

public class MarkerImageCreator2D extends BaseMarkerImageCreator
{

	/**
	 * Display a dialog that asks the user for the threshold and filter methods plus the radius to apply them on for the watershed or distance transform.
	 *
	 * @param aMethodName
	 *            The method for which the information is needed. For display purposed only.
	 * @return A set of three strings containing resp. the threshold method name, the filter method name and the radius (double to String)
	 */
	static private String[] dialogSettingsForThreshold(final String aMethodName) // Dialog for the original image
	{
		// The available choices
		final String[] thresholds = { "Li", "Huang", "Default", "Triangle", "InterModes", "IsoData", "Yen" };
		final String[] filters = { "Median", "Mean", "Minimum", "Maximum" };

		final GenericDialog gd = new GenericDialog("Settings for the applied method");
		gd.addMessage("Set the settings for: " + aMethodName);
		gd.addChoice("Automated threshold: ", thresholds, thresholds[0]);
		gd.addChoice("Set filter for ", filters, filters[0]);
		gd.addNumericField("Set radius for filter", 2, 0);
		gd.showDialog();
		final String threshold = gd.getNextChoice();
		final String filter = gd.getNextChoice();
		final String radius = gd.getNextNumber() + "";
		final String[] result = { threshold, filter, radius };
		return result;
	}


	/**
	 * Display a dialog that asks the user which slice to use for the manual points.
	 *
	 * @return The slice number of interest.
	 */
	static private int dialogSettingsManualPoints()
	{
		final GenericDialog gd = new GenericDialog("Settings to select the manual points");
		gd.addNumericField("Please select the slice of interest", 1, 0);

		gd.showDialog();
		final int slice = (int) (gd.getNextNumber() - 1);

		return slice;
	}


	/**
	 * Method distanceTransformPoints performs a distance transform on the original image. Find the maxima on the Distance transformed image with the 2D maxima finder.
	 *
	 * @param aImage
	 *            The image on which the method will work
	 * @param aSettings
	 *            The set of three settings for threshold, filter and radius (as Strings)
	 *
	 * @return A list of seed coordinates, where each seed is one of the maxima found on the distance-transformed image.
	 */
	static private List<PointValue> distanceTransformPoints(final ImagePlus aImage, final String[] aSettings)
	{
		// Create a new list of coordinates
		final List<PointValue> listOfSeeds = new ArrayList<>();

		final String thresholdInput = aSettings[0] + " dark";
		final String filterInput = aSettings[1] + "...";
		final String radiusInput = "radius=" + aSettings[2];
		IJ.log("   Filter is " + aSettings[1] + " Radius =" + aSettings[2]);
		IJ.log("   Thereshold is " + aSettings[0]);

		final ImagePlus image2 = aImage.duplicate();
		// Set filter and threshold
		IJ.run(image2, filterInput, radiusInput);
		IJ.setAutoThreshold(image2, thresholdInput);
		IJ.run(image2, "Make Binary", "");

		// Create a gradient image using Exact Euclidean Distance Transform (3D)
		IJ.run(image2, "Exact Euclidean Distance Transform (3D)", "");
		final ImagePlus imagedistance = IJ.getImage();

		// Find maxima with the 2D maxima finder
		IJ.run(imagedistance, "Find Maxima...", "noise=1 output=List");

		// Add all the maxima to the list of seeds
		final ResultsTable results = ResultsTable.getResultsTable();
		IJ.log(results.getCounter() + " size");
		for (int i = 0; i < results.getCounter(); i++)
		{
			final double xValue = results.getValue("X", i);
			final double yValue = results.getValue("Y", i);
			listOfSeeds.add(new PointValue((int) xValue, (int) yValue, 0, 0));
		}
		return listOfSeeds;
	}


	static private List<PointValue> manualPoints2D(final File aInputDirectory, final String aFilename, final double aFactor, final int aSliceOfInterestNr) throws IOException
	{

		IJ.log("  Start Manual Points");

		final List<PointValue> listOfSeeds = new ArrayList<>();

		FileReader fileReader;
		try
		{
			fileReader = new FileReader(aInputDirectory.getParentFile() + File.separator + aFilename);
			final BufferedReader br = new BufferedReader(fileReader);
			String line;
			while ((line = br.readLine()) != null)
			{
				final String[] columns = line.split("\t");
				final int sliceNr = Integer.parseInt(columns[5]);
				if (sliceNr == aSliceOfInterestNr)
				{
					final int xValue = Integer.parseInt(columns[3]);
					final int yValue = Integer.parseInt(columns[4]);
					listOfSeeds.add(new PointValue(xValue, yValue, aSliceOfInterestNr, 0));
				}
			}
			br.close();
		}
		catch (final FileNotFoundException e)
		{
			IJ.error("FileNotFoundException: " + e.getMessage());
			e.printStackTrace();
		}
		return listOfSeeds;
	}


	public static void markerImage2D(final ImagePlus aOriginalImage, final File aInPutDirectory, final File OutputDirectory, final String aName, final String aDetectionMethod) throws IOException
	{
		int sliceOfInterest = 0;
		// Ask slice of interest for the manual points
		if (aDetectionMethod.equals(MANUALPOINTS))
		{
			sliceOfInterest = dialogSettingsManualPoints();
		}

		// Determine the point detection method and create the marker image name
		final String titlemethod = createOutputFileName(aOriginalImage, aDetectionMethod);
		IJ.log("Method " + aDetectionMethod);
		switch (aDetectionMethod)
		{
		case MANUALPOINTS:
		{
			// Create a list (n= point detection points per slide)
			final List<List<PointValue>> seedsOfOneSlide = new ArrayList<>();
			seedsOfOneSlide.add(manualPoints2D(aInPutDirectory, aName, 0, sliceOfInterest));
			// Create and save the marker image and the marker file
			createMarkerImage(aOriginalImage, seedsOfOneSlide, OutputDirectory, titlemethod);
			break;
		}
		case LAPLACIAN_OF_GAUSSIAN:
		{
			// Perform the mexican hat and save the marker image and marker file
			mexicanHatMaster(aOriginalImage, titlemethod, OutputDirectory);
			break;
		}
		case DISTANCETRANSFORM:
		{
			IJ.log("Start Distance transform");
			final String[] results = dialogSettingsForThreshold("Distance Transform");
			final List<List<PointValue>> seedsOfOneSlide = new ArrayList<>();
			seedsOfOneSlide.add(distanceTransformPoints(aOriginalImage, results));
			createMarkerImage(aOriginalImage, seedsOfOneSlide, OutputDirectory, titlemethod + "_" + results[0]);
			break;
		}
		case WATERSHEDPOINTS:
		{
			IJ.log("Start Watershed");
			// Start dialog for asking the threshold
			final String[] results = dialogSettingsForThreshold("Watershed");
			final List<List<PointValue>> seedsOfOneSlide = new ArrayList<>();
			seedsOfOneSlide.add(watershedMarks(aOriginalImage, results));
			createMarkerImage(aOriginalImage, seedsOfOneSlide, OutputDirectory, titlemethod + "_" + results[0]);
			break;
		}
		}
	}


	/**
	 * Method watershedMarks performs the normal watershed and analysed the particles and extract the XY coordinates Method returns a list of seeds with the XYZ coordinates of the watersehd particles
	 *
	 * @param directory,
	 *            the directory where the Image and point file need to be saved
	 * @param image,
	 *            the original image where the watershed need to be performed
	 * @param title,
	 */
	static private List<PointValue> watershedMarks(final ImagePlus aOriginalImage, final String[] aSettings)
	{

		// Create a new list of coordinates
		final List<PointValue> listofseeds = new ArrayList<>();
		final String thresholdInput = aSettings[0] + " dark";
		final String filterInput = aSettings[1] + "...";
		final String radiusInput = "radius=" + aSettings[2];
		IJ.log("Filter is " + aSettings[1] + " Radius = " + aSettings[2]);
		IJ.log("Thereshold is " + aSettings[0]);

		// Set filter and threshold
		final ImagePlus image2 = aOriginalImage.duplicate();
		IJ.run(image2, filterInput, radiusInput);
		IJ.setAutoThreshold(image2, thresholdInput);
		Prefs.blackBackground = true;
		IJ.run(image2, "Convert to Mask", "");
		IJ.run(image2, "Make Binary", "");

		// Run the classical 2D watershed
		IJ.run(image2, "Watershed", "");
		final ResultsTable resultsParticle = new ResultsTable();

		// Analyze all the nucleus
		final ParticleAnalyzer analyze = new ParticleAnalyzer(1, 32, resultsParticle, 40.0, Double.MAX_VALUE, 0.0, 1.0);
		analyze.analyze(image2);

		// Add all the maxima to the list of seeds
		for (int i = 0; i < resultsParticle.getCounter(); i++)
		{
			final double xValue = aOriginalImage.getCalibration().getRawX(resultsParticle.getValue("X", i));
			final double yValue = aOriginalImage.getCalibration().getRawY(resultsParticle.getValue("Y", i));
			listofseeds.add(new PointValue((int) xValue, (int) yValue, 0, 0));
		}
		return listofseeds;
	}

}