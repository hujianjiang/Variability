
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import data.Coordinates;
import ij.gui.ImageCanvas;

public class SpheroidPickerListener implements MouseListener
{
	final private Spheroid_Picker3D correction;
	private final ImageCanvas canvas;


	public SpheroidPickerListener(final Spheroid_Picker3D aSpheroidPicker, final ImageCanvas aCanvas)
	{
		this.correction = aSpheroidPicker;
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
				this.correction.removeSpherePoint(new Coordinates(x, y));
			}
			else
			{
				this.correction.addSpherePointInSlice(new Coordinates(x, y));
			}
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
