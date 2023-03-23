import java.awt.Color;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import data.Coordinates;
import data.NucleusEvent;
import data.spheroid.SphereIO;
import data.spheroid.Spheroid;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.io.FileInfo;
import ij.plugin.Duplicator;
import ij.plugin.PlugIn;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import utils.MyMath;
import utils.NucleiDataVisualiser;

public class Spheroid_Picker3D implements PlugIn
{
	// Constants
	final static int RADIUS = 5;
	private static final int SPHEROID_POINT = Color.MAGENTA.getRGB();

	// Variables
	private Map<Integer, List<Coordinates>> spheroidPoints;
	private ImagePlus imagePlus;
	private ImagePlus imageOriginal;
	private Spheroid calcSpheroid;
	private FileInfo imageFileInfo;

	private MouseListener listener;


	/**
	 * Adds a sphere annotation point to the set and updates the visualisation of the sphere if needed. If only three points are given in a single slice, a circle will be drawn that has been fitted through those points. If a single other point
	 * becomes available on a different slice, the complete sphere can be calculated and will be drawn in the image. In both cases, a spheroid is calculated and stored.
	 *
	 * @param aCoordinates
	 *            The Coordinates of the new sphere annotation point.
	 */
	private void addSpherePoint(final Coordinates aCoordinates)
	{
		final int zCoord = (int) aCoordinates.getZcoordinate();
		List<Coordinates> zCoords = this.spheroidPoints.get(zCoord);
		if (zCoords == null)
		{
			zCoords = new ArrayList<>();
			this.spheroidPoints.put((int) aCoordinates.getZcoordinate(), zCoords);
		}
		zCoords.add(aCoordinates);
		NucleiDataVisualiser.drawDiamond(aCoordinates, this.imagePlus.getStack().getProcessor(zCoord + 1), RADIUS, SPHEROID_POINT, true);
		this.imagePlus.updateAndDraw();

		final double zCoef = this.imagePlus.getCalibration().pixelDepth / this.imagePlus.getCalibration().pixelWidth;
		if (this.spheroidPoints.size() == 2)
		{
			List<Coordinates> zCoords2 = null;
			final Iterator<Map.Entry<Integer, List<Coordinates>>> iterator = this.spheroidPoints.entrySet().iterator();
			while (iterator.hasNext())
			{
				final Map.Entry<Integer, List<Coordinates>> entry = iterator.next();

				if (!entry.getKey().equals(zCoord))
				{
					zCoords2 = entry.getValue();
					break;
				}
			}
			Coordinates center = null;
			if (zCoords.size() == 1 && zCoords2.size() == 3)
			{
				center = MyMath.sphereCentre(zCoords2.get(0), zCoords2.get(1), zCoords2.get(2), aCoordinates, zCoef);
			}
			else if (zCoords.size() == 3 && zCoords2.size() == 1)
			{
				center = MyMath.sphereCentre(zCoords.get(0), zCoords.get(1), zCoords.get(2), zCoords2.get(0), zCoef);
			}

			if (center != null)
			{
				if (this.calcSpheroid != null)
				{
					final Coordinates circleCenter = this.calcSpheroid.getCentre();
					final int zCircle = (int) circleCenter.getZcoordinate() + 1;
					NucleiDataVisualiser.drawCirle(new NucleusEvent(circleCenter), this.imagePlus.getStack().getProcessor(zCircle), (int) this.calcSpheroid.getRadius(), Color.YELLOW.getRGB(), true,
							this.imageOriginal.getStack().getProcessor(zCircle));
				}

				final double size = center.correctedDistanceFromPoint(aCoordinates, zCoef);
				this.calcSpheroid = new Spheroid(center, size, zCoef);
				drawSpheroid(false);
			}
		}
		else if (zCoords.size() == 3)
		{
			// Three points can draw up a circle already, but just on this slice
			final Coordinates center = MyMath.circleCentre(zCoords.get(0), zCoords.get(1), zCoords.get(2));

			final double size = center.distanceFromPoint(aCoordinates);
			this.calcSpheroid = new Spheroid(center, size, zCoef);
			NucleiDataVisualiser.drawCirle(new NucleusEvent(center), this.imagePlus.getStack().getProcessor((int) (center.getZcoordinate() + 1)), (int) size, Color.YELLOW.getRGB(), true, null);
			this.imagePlus.updateAndDraw();
		}
	}


	/**
	 * Add a point to the spheroid edge reconstruction points. Note that the point will be added to the currently active slice. Any z-coordinate of the parameter point is ignored and overwritten by the slice number -1.
	 *
	 * @param aCoordinates
	 *            The coordinates of the new spheroid edge point
	 */
	public void addSpherePointInSlice(final Coordinates aCoordinates)
	{
		final int zCoord = this.imagePlus.getCurrentSlice() - 1;
		aCoordinates.setZcoordinate(zCoord);

		addSpherePoint(aCoordinates);
	}


	/**
	 * Draw the calculated spheroid in the image. Either in colour or in the original pixels (for erasing the spheroid).
	 *
	 * @param aUseOriginal
	 *            If true, the original pixels of the image will be restored, ersaing the spheroid drwaing. If false, the spheroid will be drawn in a solid colour.
	 */
	private void drawSpheroid(final boolean aUseOriginal)
	{
		final int centerZ = (int) this.calcSpheroid.getCentre().getZcoordinate() + 1;
		final int top = (int) (centerZ + this.calcSpheroid.getRadius()) > this.imagePlus.getNSlices() ? this.imagePlus.getNSlices() : (int) (centerZ + this.calcSpheroid.getRadius());
		final int bottom = (int) (centerZ - this.calcSpheroid.getRadius()) < 1 ? 1 : (int) (centerZ - this.calcSpheroid.getRadius());
		final ImageStack original = this.imageOriginal.getStack();
		for (int z = bottom; z <= top; z++)
		{
			final Coordinates coord = new Coordinates(this.calcSpheroid.getCentre().getXcoordinate(), this.calcSpheroid.getCentre().getYcoordinate(), z - 1);
			NucleiDataVisualiser.drawCirle(new NucleusEvent(coord), this.imagePlus.getStack().getProcessor(z), (int) this.calcSpheroid.getRadiusAtZCoordinate(z - 1), Color.YELLOW.getRGB(), true,
					aUseOriginal ? original.getProcessor(z) : null);
		}
		this.imagePlus.updateAndDraw();
	}


	private void readSpheroidPointsFromFile()
	{
		String fileName = this.imageFileInfo.fileName;
		fileName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
		fileName += SphereIO.SPHERE_TXT;
		final File spheroidFile = new File(this.imageFileInfo.directory + File.separator + fileName);
		try
		{
			final FileReader reader = new FileReader(spheroidFile);
			final BufferedReader buffer = new BufferedReader(reader);
			String line;
			while ((line = buffer.readLine()) != null)
			{
				addSpherePoint(Coordinates.parseCoordinates(line));
			}

			buffer.close();
		}
		catch (final FileNotFoundException e)
		{
			// No such file exists, no problem.
			return;
		}
		catch (final IOException e)
		{
			// Oops, that isn't right.
			IJ.handleException(e);
		}

		this.imagePlus.updateAndDraw();
	}


	//
	// private void setOverlay()
	// {
	// if(this.calcSpheroidCentre != null)
	// {
	// final int sliceNr = this.imagePlus.getSlice();
	// final Overlay overlay = this.imagePlus.getOverlay();
	// if(overlay == null || overlay.get(0).getZPosition() != sliceNr)
	// {
	// final int zDist = Math.abs(sliceNr - this.calcSpheroidCentre.getZcoordinate() + 1)
	// circSize = (this.calcSpheroidSize * this.calcSpheroidSize) -
	// }
	// }
	// }

	public void removeSpherePoint(final Coordinates aCoordinates)
	{
		final int zCoordinate = this.imagePlus.getCurrentSlice() - 1;
		aCoordinates.setZcoordinate(zCoordinate);
		Coordinates closestPoint = null;
		double closeDist = -1;
		int nrOfmatches = 0;
		final List<Coordinates> candidates = this.spheroidPoints.get(zCoordinate);
		if (candidates != null)
		{
			// There is a point on this slice; find it.
			for (final Coordinates coord : candidates)
			{
				nrOfmatches++;
				final double dist = coord.distanceFromPoint(aCoordinates);
				if (closestPoint == null || closeDist > dist)
				{
					closeDist = dist;
					closestPoint = coord;
				}
			}

			// Remove the point and clean up the image
			candidates.remove(closestPoint);
			restoreOriginal(closestPoint);
			final int nrOfKeys = this.spheroidPoints.keySet().size();
			if (candidates.size() == 0)
			{
				// If there are no points on the slice left, remove the slice registration
				this.spheroidPoints.remove(zCoordinate);
			}

			// Remove the drawn circle/sphere if need be
			if (this.calcSpheroid != null)
			{
				if (nrOfmatches == 3 && nrOfKeys < 2)
				{
					// We only have the circle to redo
					NucleiDataVisualiser.drawCirle(new NucleusEvent(this.calcSpheroid.getCentre()), this.imagePlus.getStack().getProcessor(zCoordinate + 1), (int) this.calcSpheroid.getRadius(),
							Color.YELLOW.getRGB(), true, this.imageOriginal.getStack().getProcessor(zCoordinate + 1));
					this.calcSpheroid = null;
				}
				else if ((nrOfmatches == 3 || nrOfmatches == 1) && nrOfKeys == 2)
				{
					// Erase the entire spheroid
					drawSpheroid(true);
					if (nrOfmatches == 3)
					{
						this.calcSpheroid = null;
					}
					else
					{
						// The circle of three points remains
						final Iterator<Map.Entry<Integer, List<Coordinates>>> iterator = this.spheroidPoints.entrySet().iterator();
						while (iterator.hasNext())
						{
							final List<Coordinates> coords = iterator.next().getValue();
							if (coords.size() != 1)
							{
								if (coords.size() == 3)
								{
									final Coordinates center = MyMath.circleCentre(coords.get(0), coords.get(1), coords.get(2));

									final double size = center.distanceFromPoint(coords.get(0));
									this.calcSpheroid = new Spheroid(center, size, this.calcSpheroid.getZCoefficient());
									NucleiDataVisualiser.drawCirle(new NucleusEvent(center), this.imagePlus.getStack().getProcessor((int) center.getZcoordinate() + 1), (int) size,
											Color.YELLOW.getRGB(), true, null);
									break;
								}
							}
						}
					}
				}
			}
			this.imagePlus.updateAndDraw();
		}
	}


	private void restoreOriginal(final Coordinates aCoordinates)
	{
		final double xCoord = aCoordinates.getXcoordinate();
		final double yCoord = aCoordinates.getYcoordinate();
		final double zCoord = aCoordinates.getZcoordinate();
		this.imageOriginal.setSlice((int) zCoord + 1);
		final ImageProcessor imageProc = this.imagePlus.getStack().getProcessor((int) (zCoord + 1));
		final ImageProcessor imageProcOrig = this.imageOriginal.getStack().getProcessor((int) (zCoord + 1));
		for (int x = -RADIUS; x <= RADIUS; x++)
		{
			for (int y = -RADIUS; y <= RADIUS; y++)
			{
				if (xCoord + x >= 0 && yCoord + y >= 0 //
						&& xCoord + x < this.imagePlus.getWidth() && yCoord + y < this.imagePlus.getHeight())
				{
					imageProc.setf((int) xCoord + x, (int) yCoord + y, imageProcOrig.getf((int) xCoord + x, (int) yCoord + y));
				}
			}
		}
	}


	@Override
	public void run(final String arg)
	{
		// Open the image. If the image isn't RGB-bits yet, convert it.
		ImagePlus imageOrig = IJ.openImage();

		if (imageOrig == null)
		{
			// Probably opened with Bio-Formats
			imageOrig = IJ.getImage();

			final GenericDialog parametersDialog = new GenericDialog("Bio-Formats check");
			parametersDialog.addMessage("Did you open image " + imageOrig.getTitle() + " via the Bio-Formats importer?");
			parametersDialog.setOKLabel("Yes");
			parametersDialog.setCancelLabel("No");
			parametersDialog.showDialog();

			if (parametersDialog.wasCanceled())
			{
				IJ.showMessage("No image selection detected.");
				return;
			}
		}

		this.imageFileInfo = imageOrig.getOriginalFileInfo();
		if (imageOrig.getType() != ImagePlus.COLOR_RGB)
		{
			IJ.run("Grays"); // Make sure that the LUT is gray
			if (imageOrig.getNChannels() > 1)
			{

				final int channels = imageOrig.getNChannels();
				final GenericDialog dialog = new GenericDialog("Set channels");
				final String[] channelchooser = new String[channels];
				for (int i = 1; i <= channels; i++)
				{
					channelchooser[i - 1] = i + "";
				}

				dialog.addMessage("Please select the spheroid chooser channel.");
				dialog.addChoice("Spheroid channel", channelchooser, "1");
				dialog.showDialog();

				if (dialog.wasCanceled())
				{
					return;
				}
				final int nucChannel = Integer.parseInt(dialog.getNextChoice());

				this.imagePlus = new Duplicator().run(imageOrig, nucChannel, nucChannel, 1, imageOrig.getNSlices(), 1, 1);
			}
			else
			{
				this.imagePlus = imageOrig;
			}

			final ImageConverter ic = new ImageConverter(this.imagePlus);
			ic.convertToRGB();
		}
		else
		{
			this.imagePlus = imageOrig;
		}

		this.imagePlus.show();

		final NonBlockingGenericDialog nonBlockDialog = new NonBlockingGenericDialog("Image adjustment");
		nonBlockDialog.addMessage("Please adjust the image to fit your preferences.");
		nonBlockDialog.setOKLabel("Done");
		nonBlockDialog.hideCancelButton();
		nonBlockDialog.showDialog();

		this.imageOriginal = this.imagePlus.duplicate();

		this.spheroidPoints = new HashMap<>();

		readSpheroidPointsFromFile();

		if (runCorrections())
		{
			// Create a file with the measure information
			SphereIO.writeSpheroidPointsToFile(this.imageFileInfo, this.spheroidPoints);
		}

		this.imageOriginal.changes = false;
		this.imagePlus.changes = false;
		this.imageOriginal.close();
		imageOrig.close();
	}


	private boolean runCorrections()
	{
		this.listener = new SpheroidPickerListener(this, this.imagePlus.getCanvas());
		this.imagePlus.getCanvas().addMouseListener(this.listener);

		boolean proceed = false;
		boolean wasOked = false;

		while (!proceed)
		{
			final NonBlockingGenericDialog nonBlockDialog = new NonBlockingGenericDialog("Correction selection");
			final StringBuilder message = new StringBuilder("Please add corrections.\n");
			message.append(" -------------------------------------------------------- \n");
			message.append(" - Mouse click: Select a point on the spheroid edge.\n");
			message.append(" - Ctrl + click: Remove neraest point on spheroid edge.\n");
			message.append(" -------------------------------------------------------- \n");
			message.append(" 1) First select a cental slice. \n");
			message.append(" 2) On that slice select three points on the edge of the spheroid. \n");
			message.append(" 3) A circle will be drawn through these points as soon as the third point is added.\n");
			message.append(" 4) Remove and adjust points to get a reasonable match between circle and edge. (Note, do not add > 3 points.)\n");
			message.append(" 5) Select a significantly higher or lower slice that still contains the spheroid edge.\n");
			message.append(" 6) The application will now draw a sphere through all points (i.e. a circle per slice containing the sphere).\n");
			message.append(" 7) Adjust points (by removing and adding them elsewhere) so the sphere generally matches the spheroid.\n");
			nonBlockDialog.addMessage(message.toString());
			nonBlockDialog.enableYesNoCancel("Done", "Save");
			nonBlockDialog.showDialog();

			proceed = nonBlockDialog.wasCanceled() || nonBlockDialog.wasOKed();
			if (!proceed)
			{
				SphereIO.writeSpheroidPointsToFile(this.imageFileInfo, this.spheroidPoints);
			}
			else
			{
				wasOked = nonBlockDialog.wasOKed();
			}
		}

		if (this.imagePlus.getCanvas() != null)
		{
			this.imagePlus.getCanvas().removeMouseListener(this.listener);
		}
		return wasOked;
	}
}
