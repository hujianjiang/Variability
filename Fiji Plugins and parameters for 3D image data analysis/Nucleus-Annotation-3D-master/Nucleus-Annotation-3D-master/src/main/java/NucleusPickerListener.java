
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import data.Coordinates;
import ij.gui.ImageCanvas;

public class NucleusPickerListener implements MouseListener
{
	final private Nucleus_Picker3D correction;
	private final ImageCanvas canvas;


	public NucleusPickerListener(final Nucleus_Picker3D aPointCorrection, final ImageCanvas aCanvas)
	{
		this.correction = aPointCorrection;
		this.canvas = aCanvas;
	}


	@Override
	public void mouseClicked(final MouseEvent e)
	{
		final int button = e.getButton();
		final int modifiers = e.getModifiersEx();

		final int x = this.canvas.offScreenX(e.getX());
		final int y = this.canvas.offScreenY(e.getY());

		if (button == MouseEvent.BUTTON1)
		{
			if ((modifiers & MouseEvent.CTRL_DOWN_MASK) != 0)
			{
				if ((modifiers & MouseEvent.SHIFT_DOWN_MASK) != 0)
				{
					this.correction.removeNucleus(x, y);
				}
				else if ((modifiers & MouseEvent.ALT_DOWN_MASK) != 0)
				{
					this.correction.setDualCluster(x, y);
				}
				else
				{
					this.correction.setTrueNucleus(x, y);
				}
			}
			else if ((modifiers & MouseEvent.ALT_DOWN_MASK) != 0)
			{
				if ((modifiers & MouseEvent.SHIFT_DOWN_MASK) != 0)
				{
					this.correction.setMultiCluster(x, y);
				}
				else
				{
					this.correction.setEndOfNucleus(new Coordinates(x, y));
				}
			}
			else if ((modifiers & MouseEvent.SHIFT_DOWN_MASK) != 0)
			{
				this.correction.setSingleNucleus(x, y);
			}
			else
			{
				this.correction.setStartOfNucleus(new Coordinates(x, y));
			}
		}
		else if (button == MouseEvent.BUTTON2)
		{
			this.correction.removeNucleus(x, y);
		}
	}


	@Override
	public void mouseEntered(final MouseEvent e)
	{
		// TODO Auto-generated method stub

	}


	@Override
	public void mouseExited(final MouseEvent e)
	{
		// TODO Auto-generated method stub

	}


	@Override
	public void mousePressed(final MouseEvent e)
	{
		// TODO Auto-generated method stub

	}


	@Override
	public void mouseReleased(final MouseEvent e)
	{
		// TODO Auto-generated method stub

	}
}
