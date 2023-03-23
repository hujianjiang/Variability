package dataanalyser;

import java.awt.Checkbox;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;

public class Data_Analyser implements PlugIn, KeyListener
{
	int[][] numbers = new int[256][254];
	ResultsTable results = new ResultsTable();


	@Override
	public void keyPressed(final KeyEvent aEvent)
	{
		// Do nothing. Handled in typed.
	}


	@Override
	public void keyReleased(final KeyEvent aEvent)
	{
		// Do nothing. Handled in typed.
	}


	@Override
	public void keyTyped(final KeyEvent aEvent)
	{
		final String key = "" + aEvent.getKeyChar();

		if (key.equals("u"))
		{
			IJ.log("Yohoo!!");
			final ImagePlus currentImage = IJ.getImage();
			final Roi currentRoi = currentImage.getRoi();
			final int xCo = 0;
			final int yCo = 0;

			Data_Analyser.this.results.reset();
			for (int j = 0; j < 254; j++)
			{
				Data_Analyser.this.results.incrementCounter();
				for (int i = 0; i < 256; i++)
				{
					if (currentRoi.contains(i, j))
					{
						Data_Analyser.this.results.setValue(i, j, Data_Analyser.this.numbers[i][j]);
					}
				}
			}

			this.results.show("Ze Rezuldz");
		}
	}


	@Override
	public void run(final String arg)
	{
		for (int i = 0; i < 256; i++)
		{
			for (int j = 0; j < 254; j++)
			{
				this.numbers[i][j] = i + j;
			}
		}

		final NonBlockingGenericDialog nonBlockDialog = new NonBlockingGenericDialog("ROI removal");
		// nonBlockDialog.setLocation(dialogX, dialogY);
		final StringBuilder message = new StringBuilder("Please press the following keys for commands:\n");
		message.append(" -------------------------------------------------------- \n");
		message.append(" - 'l': Log message.\n");
		nonBlockDialog.enableYesNoCancel("Done", "Stop and Save");
		nonBlockDialog.addMessage(message.toString());
		nonBlockDialog.addCheckbox("Log message active", true);

		final ImagePlus currentImage = IJ.getImage();
		currentImage.getCanvas().addKeyListener(Data_Analyser.this);

		// Add listener to hide/show all ROIs
		((Checkbox) nonBlockDialog.getCheckboxes().get(0)).addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				final int stateChange = e.getStateChange();
				if (stateChange == ItemEvent.SELECTED)
				{
					currentImage.getCanvas().addKeyListener(Data_Analyser.this);
				}
				else
				{
					currentImage.getCanvas().removeKeyListener(Data_Analyser.this);
				}
			}
		});

		nonBlockDialog.showDialog();

		if (nonBlockDialog.wasOKed())
		{
			IJ.log("Done");
		}
	}
}
