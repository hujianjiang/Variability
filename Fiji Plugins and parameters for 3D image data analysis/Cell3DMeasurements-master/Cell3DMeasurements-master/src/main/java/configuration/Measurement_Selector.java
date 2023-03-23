package configuration;

import java.awt.Font;

import data.SegmentMeasurements;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

/**
 * Allow the user to set which of the measurements that are available in the Feature Extractor are going to be active. The choices are being stored as preferences via the Prefs class and can be retrieved as such. This class also provides the methods
 * to do this.
 *
 * @author Merijn van Erp
 *
 */
public class Measurement_Selector implements PlugIn
{
	private static final String PREF_START = "Measurement_Selector_3D.Selected_Measurement.";
	private static final int COLUMNS = 3;


	/**
	 * Based on a measurement name, retrieve the preference setting.
	 *
	 * @param aMeasurementName
	 *            The name of the measurement
	 *
	 * @return True if the measurement should be calculated and be part of the Feature Extractor results, false otherwise
	 */
	public static boolean getMeasurementPreference(final String aMeasurementName)
	{
		return Prefs.get(getPreferenceLabel(aMeasurementName), true);
	}


	/**
	 * Get the preference name under which the measurement choice has been saved.
	 *
	 * @param aMeasurementName
	 *            The name of the measurement
	 * @return The name with which the measurement choice should be stored in the Prefs
	 */
	private static String getPreferenceLabel(final String aMeasurementName)
	{
		return PREF_START + aMeasurementName;
	}


	/**
	 * Based on a measurement name, set the choice preference setting.
	 *
	 * @param aMeasurementName
	 *            The name of the measurement
	 * @param aChoice
	 *            The boolean choice to set
	 */
	public static void setMeasurementPreference(final String aMeasurementName, final boolean aChoice)
	{
		Prefs.set(getPreferenceLabel(aMeasurementName), aChoice);
	}


	/**
	 * Add a list of measurements as a nicely configured group of check boxes to a dialog.
	 *
	 * @see getGroupMeasurements
	 *
	 * @param aNames
	 *            The list of measurement names to add. The order is very important and should coincide with the retrieval order after the dialog is closed!
	 * @param aDialog
	 *            The dialog to which the measurements check boxes are added
	 */
	private void addCheckboxGroup(final String[] aNames, final GenericDialog aDialog)
	{
		final int rows = (aNames.length / COLUMNS) + 1;
		final boolean[] defaultValues = new boolean[aNames.length];
		int measureNr = 0;
		for (final String measureName : aNames)
		{
			final boolean currentChoice = getMeasurementPreference(measureName);
			defaultValues[measureNr] = currentChoice;
			measureNr++;
		}
		aDialog.addCheckboxGroup(rows, COLUMNS, aNames, defaultValues);
	}


	/**
	 * Get the next set of measurement choices and store these in the preferences.
	 *
	 * @see addCheckboxGroup
	 *
	 * @param aMeasurementNames
	 *            The list of names of the next measurement choices. The order is very important and should coincide with the order in which the names were added to the dialog!
	 * @param aDialog
	 *            The dialog to retrieve the choices from
	 */
	private void getGroupMeasurements(final String[] aMeasurementNames, final GenericDialog aDialog)
	{
		for (final String measureName : aMeasurementNames)
		{
			final boolean currentChoice = aDialog.getNextBoolean();
			setMeasurementPreference(measureName, currentChoice);
		}
	}


	/**
	 * Create the measurement choice dialog based on the groups of measurements that are available and store the results in the Prefs.
	 */
	@Override
	public void run(final String arg)
	{
		final GenericDialog dialog = new GenericDialog("Measurement Selector");
		final Font font = new Font("SansSerif", Font.BOLD, 12);

		dialog.addMessage(SegmentMeasurements.STANDARD_MEASUREMENTS, font);
		addCheckboxGroup(SegmentMeasurements.STANDARD_GROUP_NUCLEUS, dialog);

		dialog.addMessage(SegmentMeasurements.MORPHOLIBJ_MEASUREMENTS, font);
		addCheckboxGroup(SegmentMeasurements.MORPHOLIBJ_GROUP, dialog);

		dialog.addMessage(SegmentMeasurements.MCIB3D_MEASUREMENTS, font);
		addCheckboxGroup(SegmentMeasurements.MCIB3D_GROUP, dialog);

		dialog.showDialog();

		if (dialog.wasOKed())
		{

			getGroupMeasurements(SegmentMeasurements.STANDARD_GROUP_NUCLEUS, dialog);
			getGroupMeasurements(SegmentMeasurements.MORPHOLIBJ_GROUP, dialog);
			getGroupMeasurements(SegmentMeasurements.MCIB3D_GROUP, dialog);

			Prefs.savePreferences();
		}
	}

}
