import data.Coordinates;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import utils.MyMath;

public class Sphere_Blocker implements PlugIn
{

	/**
	 * Draw a (filled) sphere with the colour provided in the ImageProcessor on the coordinates of the given Nucleus. The sphere will be brightest at the centre and slowly fade to black.
	 *
	 * @param aNucleus
	 *            The Nucleus which provides the Coordinates for the sphere
	 * @param aImageStack
	 *            The ImageStack in which the sphere will be drawn
	 * @param aRadius
	 *            The radius of the sphere
	 * @param aColour
	 *            The RGB colour value to be used
	 * @param aIsOpen
	 *            Should the circle be filled (false) or open (true)
	 */
	public static void drawSphere(final Coordinates aCoord, final ImageStack aImageStack, final double aRadius, final double aXYFactor, final double aZFactor, final int aNChannels, final int aChannel)
	{
		final int xCoord = (int) (aCoord.getXcoordinate() / aXYFactor);
		final int yCoord = (int) (aCoord.getYcoordinate() / aXYFactor);
		final int zCoord = (int) (aCoord.getZcoordinate() / aZFactor);
		final int xyRad = (int) (aRadius / aXYFactor);
		final int zRad = (int) (aRadius / aZFactor);
		final int xyPow = xyRad * xyRad;
		final int zPow = zRad * zRad;
		for (int x = -xyRad; x <= xyRad; x++)
			for (int y = -xyRad; y <= xyRad; y++)
				for (int z = -zRad; z <= zRad; z++)
				{
					{
						final double dist = (double) (x * x) / (xyPow) + (double) (y * y) / (xyPow) + (double) (z * z) / (zPow);
						if (x <= 1 && x >= -1 && y <= 1 && y >= -1)
						{
							int dud = 55;
							dud = x + 1;
							dud = dud - 1;
						}
						if (dist <= 1)
						{
							final int ex = xCoord + x;
							final int why = yCoord + y;
							final int zed = zCoord + z;
							if (ex >= 0 && why >= 0 && zed >= 0 //
									&& ex < aImageStack.getWidth() && why < aImageStack.getHeight() && zed < aImageStack.getSize())
							{
								aImageStack.setVoxel(ex, why, (zed * aNChannels) + aChannel - 1, 0);
							}
						}
					}
				}
	}


	@Override
	public void run(final String arg)
	{

		final GenericDialog parametersDialog = new GenericDialog("Coordinates input");
		parametersDialog.addMessage("Please input three coordinates with a similar Z coordinate:");
		parametersDialog.addStringField("Coordinate 1", "0.0\t0.0\t0.0", 20);
		parametersDialog.addStringField("Coordinate 2", "0.0\t0.0\t0.0", 20);
		parametersDialog.addStringField("Coordinate 3", "0.0\t0.0\t0.0", 20);
		parametersDialog.addMessage("Please input three coordinates with a similar X coordinate:");
		parametersDialog.addStringField("Coordinate 4", "0.0\t0.0\t0.0", 20);
		parametersDialog.addStringField("Coordinate 5", "0.0\t0.0\t0.0", 20);
		parametersDialog.addStringField("Coordinate 6", "0.0\t0.0\t0.0", 20);
		parametersDialog.addMessage("Please choose the channels to apply the sphere on:");
		parametersDialog.addStringField("Channels", "2, 3");
		parametersDialog.showDialog();

		final Coordinates coord1 = stringToCoordinate(parametersDialog.getNextString());
		final Coordinates coord2 = stringToCoordinate(parametersDialog.getNextString());
		final Coordinates coord3 = stringToCoordinate(parametersDialog.getNextString());
		final Coordinates coord4 = stringToCoordinate(parametersDialog.getNextString());
		final Coordinates coord5 = stringToCoordinate(parametersDialog.getNextString());
		final Coordinates coord6 = stringToCoordinate(parametersDialog.getNextString());
		swapXZ(coord4);
		swapXZ(coord5);
		swapXZ(coord6);
		Coordinates center = MyMath.circleCentre(coord4, coord5, coord6);

		final Coordinates centerTot = new Coordinates(0, 0, 0);
		centerTot.setYcoordinate(center.getYcoordinate());
		centerTot.setZcoordinate(center.getXcoordinate());
		System.out.println("" + center);
		center = MyMath.circleCentre(coord1, coord2, coord3);
		centerTot.setXcoordinate(center.getXcoordinate());
		centerTot.setYcoordinate((center.getYcoordinate() + centerTot.getYcoordinate()) / 2);
		final double radius = centerTot.distanceFromPoint(coord1);
		System.out.println("" + center);
		System.out.println("" + centerTot);
		System.out.println("" + radius + "\n");
		final String[] channels = parametersDialog.getNextString().split(", ");

		final ImagePlus image = IJ.getImage();
		for (final String channel : channels)
		{
			drawSphere(centerTot, image.getImageStack(), (int) radius, image.getCalibration().pixelWidth, image.getCalibration().pixelDepth, image.getNChannels(), Integer.parseInt(channel));
		}
		image.updateAndDraw();
	}


	private Coordinates stringToCoordinate(final String aString)
	{
		final String[] coords = aString.split("\t");
		final Coordinates result = new Coordinates(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]));
		return result;
	}


	private void swapXZ(final Coordinates aCoordinates)
	{
		final double temp = aCoordinates.getXcoordinate();
		aCoordinates.setXcoordinate(aCoordinates.getZcoordinate());
		aCoordinates.setZcoordinate(temp);
	}

}
