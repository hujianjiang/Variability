package featureextractor;

import java.awt.Choice;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import ij.gui.GenericDialog;

public class ChannelChooserItemListener implements ItemListener
{
	private final Choice parent;
	private final GenericDialog dialog;
	boolean extraOptionAdded = false;
	int index;
	String[] channelOptions;
	String[] measureOptions;


	public ChannelChooserItemListener(final Choice aParent, final GenericDialog aDialog, final int aIndexNr, final String[] aChannelOptions, final String[] aMeasureOptions)
	{
		this.parent = aParent;
		this.dialog = aDialog;
		this.index = aIndexNr;
		this.channelOptions = aChannelOptions;
		this.measureOptions = aMeasureOptions;
	}


	@Override
	public void itemStateChanged(final ItemEvent e)
	{
		if (!this.parent.getSelectedItem().equals("0") && !this.extraOptionAdded)
		{
			this.dialog.addChoice("Additional channel " + this.index, this.channelOptions, this.channelOptions[0]);
			this.dialog.addChoice("Additional channel type " + this.index, this.measureOptions, this.measureOptions[0]);
			final Choice choice = (Choice) this.dialog.getChoices().get(this.dialog.getChoices().size() - 2);
			choice.addItemListener(new ChannelChooserItemListener(choice, this.dialog, this.index + 1, this.channelOptions, this.measureOptions));
			this.dialog.revalidate();
			this.dialog.repaint();

			this.extraOptionAdded = true;
		}
	}

}
