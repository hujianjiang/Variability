package data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import javax.swing.JFileChooser;

import ij.IJ;
import ij.Prefs;

public class NucleiSegmentationParameters
{
	// ---------------------------------------------------------------------------------------------------------------------------------------------------------
	// -------------------------------------------------- Plugin dialog parameters ---------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------------------------------------------------------------------

	public static final String NS_NUCLEUS_CHANNEL = "Nuclei_Segmentation_3D.NucleusChannel";
	public static final String NS_ACTIN_CHANNEL = "Nuclei_Segmentation_3D.ActinChannel";
	public static final String MI_DETECTION_METHOD = "MarkerImage.ActinChannel";
	public static final String MI_MINIMUM_SIZE = "MarkerImage.MinimumSize";
	public static final String MI_MAXIMUM_SIZE = "MarkerImage.MaximumSize";
	public static final String MI_STEPSIZE = "MarkerImage.StepSize";
	public static final String MI_PROCESS_PER_SLICE = "MarkerImage.ProcessPerSlice";
	public static final String MI_ATTENUATION_ADJUSTMENT = "MarkerImage.attenuationAdjustment";
	public static final String MI_NOISE = "MarkerImage.Noise";
	public static final String MI_MINIMUM_LOG_VALUE = "MarkerImage.MinimumLoGValue";
	public static final String MI_XY_RADIUS = "MarkerImage.XYRadius";
	public static final String WS_DAPI_DAMS = "MarkerControlledWatershed.DAPIDams";
	public static final String WS_SEGMENT_ACTIN = "MarkerControlledWatershed.SegmentActin";
	public static final String WS_ACTIN_DAMS = "MarkerControlledWatershed.ActinDams";
	public static final String WS_DAPI_FILTER = "MarkerControlledWatershed.DAPIFilter";
	public static final String WS_DAPI_THRESHOLD = "MarkerControlledWatershed.DAPIThreshold";
	public static final String WS_ACTIN_FILTER = "MarkerControlledWatershed.ActinFilter";
	public static final String WS_ACTIN_THRESHOLD = "MarkerControlledWatershed.ActinThreshold";
	public static final String WS_ADJUST_ATTENUATION = "MarkerControlledWatershed.AdjustAttenuation";
	public static final String WS_ATN_PERCENTILE = "MarkerControlledWatershed.AtnPercentile";
	public static final String WS_ATN_STANDARD_SLICE = "MarkerControlledWatershed.AtnStandardSlice";
	public static final String WS_ATN_START_INTENS = "MarkerControlledWatershed.AtnStartIntens";
	public static final String WS_ATN_END_INTENS = "MarkerControlledWatershed.AtnEndIntens";
	public static final String WS_ATN_FIT_METHOD = "MarkerControlledWatershed.AtnFitMethod";
	public static final String WS_DO_EXP_THRESHOLD = "MarkerControlledWatershed.DoExpThreshold";
	public static final String WS_EXPERIMENTAL_THRESHOLD = "MarkerControlledWatershed.ExperimentalThreshold";
	public static final String FE_SAVE_RESULTS = "Feature_Extractor_3D.SaveResults";
	public static final String FE_MANUAL_MARKERS = "Feature_Extractor_3D.ManualMarkers";
	public static final String FE_ADDITIONAL_CHANNEL_1 = "Feature_Extractor_3D.AdditionalChannel1";
	public static final String FE_ADDITIONAL_MEASUREMENT_1 = "Feature_Extractor_3D.AdditionalMeasurement1";
	public static final String FE_ADDITIONAL_CHANNEL_2 = "Feature_Extractor_3D.AdditionalChannel2";
	public static final String FE_ADDITIONAL_MEASUREMENT_2 = "Feature_Extractor_3D.AdditionalMeasurement2";
	public static final String FE_ADDITIONAL_CHANNEL_3 = "Feature_Extractor_3D.AdditionalChannel3";
	public static final String FE_ADDITIONAL_MEASUREMENT_3 = "Feature_Extractor_3D.AdditionalMeasurement3";
	public static final String FE_ADDITIONAL_CHANNEL_4 = "Feature_Extractor_3D.AdditionalChannel4";
	public static final String FE_ADDITIONAL_MEASUREMENT_4 = "Feature_Extractor_3D.AdditionalMeasurement4";
	public static final String FE_MIGRATION_MODE_MEASURE = "Feature_Extractor_3D.MigrationModeMeasure";
	public static final String FE_EXCLUDE_SIZE = "Feature_Extractor_3D.ExcludeSize";
	public static final String FE_EXCLUSION_SIZE = "Feature_Extractor_3D.ExclusionSize";
	public static final String FE_EXCLUDE_BORDER = "Feature_Extractor_3D.ExcludeBorder";
	public static final String FE_BORDER_ZONE = "Feature_Extractor_3D.BorderExclusionZone";

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------
	// -------------------------------------------------- Work dir layout ----------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------------------------------------------------------------------

	public static final String NS_WORKDIR = "Nuclei_Segmentation_3D.WorkDir";
	public static final String MI_MARKERS_MAIN_DIR = "Markers";
	public static final String MI_MARKER_FILES_DIR = "Marker_Files";
	public static final String MI_MARKERS_IMAGE_DIR = "Marker_Image";
	public static final String WS_SEGMENTS_DIR = "Segments";
	public static final String FE_RESULTS_DIR = "Results";

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------
	// -------------------------------------------------- Parameters file name --------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------------------------------------------------------------------

	public static final String PARAM_FILE = "MigrationAnalysisParameters.txt";


	/**
	 * Get the Array of the parameter names for the additional parameter settings. Note that each additional parameter has two parts: the channel index and the channel measure type. Therefor, additional channel x has its parameters at index 2x and 2x
	 * + 1.
	 *
	 * @return
	 */
	public static String[] getAdditionalChannelParameterNames()
	{
		final String[] paramNames = { NucleiSegmentationParameters.FE_ADDITIONAL_CHANNEL_1, NucleiSegmentationParameters.FE_ADDITIONAL_MEASUREMENT_1,
				NucleiSegmentationParameters.FE_ADDITIONAL_CHANNEL_2, NucleiSegmentationParameters.FE_ADDITIONAL_MEASUREMENT_2, NucleiSegmentationParameters.FE_ADDITIONAL_CHANNEL_3,
				NucleiSegmentationParameters.FE_ADDITIONAL_MEASUREMENT_3, NucleiSegmentationParameters.FE_ADDITIONAL_CHANNEL_4, NucleiSegmentationParameters.FE_ADDITIONAL_MEASUREMENT_4 };

		return paramNames;
	}


	public static File getMarkerFilesDir(final File aWorkingDirectory)
	{
		return new File(aWorkingDirectory.getPath() + File.separator + NucleiSegmentationParameters.MI_MARKERS_MAIN_DIR + File.separator + NucleiSegmentationParameters.MI_MARKER_FILES_DIR);
	}


	public static File getParametersFile(final File aWorkingDirectory)
	{
		return new File(aWorkingDirectory.getPath() + File.separator + NucleiSegmentationParameters.PARAM_FILE);
	}


	public static File getResultsDir(final File aWorkingDirectory)
	{
		return new File(aWorkingDirectory.getPath() + File.separator + NucleiSegmentationParameters.FE_RESULTS_DIR);
	}


	public static File getSegmentsDir(final File aWorkingDirectory)
	{
		return new File(aWorkingDirectory.getPath() + File.separator + NucleiSegmentationParameters.WS_SEGMENTS_DIR);
	}


	public static File getWorkingDirectory()
	{
		final String prefInputFile = Prefs.get(NS_WORKDIR, null);
		final JFileChooser fileChooser = new JFileChooser(prefInputFile);
		fileChooser.setDialogTitle("Select the work directory for your measurements");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		final int number = fileChooser.showOpenDialog(IJ.getInstance());
		if (number == JFileChooser.CANCEL_OPTION)
		{
			return null;
		}

		final File workDirectory = fileChooser.getSelectedFile();
		Prefs.set(NS_WORKDIR, workDirectory.getParent()); // Store the parent to prevent unclear directory in re-use of the pref.
		Prefs.savePreferences();

		return workDirectory;
	}


	/**
	 * Write the parameters used into a file as a name/value pair.
	 *
	 * @param aParameters
	 *            The map of parameter names and their matching values
	 * @param aAppendToFile
	 *            Should the file be recreated from scratch or should the current list be added to an existing file?
	 * @param aWorkingDir
	 *            The directory in which the file should be found
	 */
	public static void writeToParametersFile(final Map<String, String> aParameters, final boolean aAppendToFile, final File aWorkingDir)
	{
		final File paramFile = NucleiSegmentationParameters.getParametersFile(aWorkingDir);
		BufferedWriter writer = null;
		try
		{
			writer = new BufferedWriter(new FileWriter(paramFile, aAppendToFile));
			for (final String paramName : aParameters.keySet())
			{
				writer.write(paramName + "\t" + aParameters.get(paramName) + "\n");
			}
			writer.close();
		}
		catch (final IOException ioe)
		{
			IJ.handleException(ioe);
		}
	}


	public static void writeToParametersFile(final String[] aParameterNames, final String[] aParameters, final boolean aAppendToFile, final File aWorkingDir)
	{
		final File paramFile = NucleiSegmentationParameters.getParametersFile(aWorkingDir);
		BufferedWriter writer = null;
		try
		{
			writer = new BufferedWriter(new FileWriter(paramFile, aAppendToFile));
			for (int i = 0; i < aParameterNames.length; i++)
			{
				writer.write(aParameterNames[i] + "\t" + aParameters[i] + "\n");
			}
			writer.close();
		}
		catch (final IOException ioe)
		{
			IJ.handleException(ioe);
		}
	}

	// TODO?
	// Manual marker files?
	// Spheroid files?
}
