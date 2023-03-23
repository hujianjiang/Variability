import java.awt.Color;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import data.BaseNucleus;
import data.Coordinates;
import data.NucleiSet2D;
import data.NucleiSet3D;
import data.Nucleus;
import data.NucleusEvent;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.io.FileInfo;
import ij.plugin.Duplicator;
import ij.plugin.PlugIn;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import utils.NucleiDataVisualiser;
import utils.Nucleus3DFileUtils;

public class Nucleus_Picker3D implements PlugIn
{

	// Colour coding etc
	private static final int ANY_TRUE = Color.GREEN.getRGB();
	private static final int ANY_UNFOCUSED = Color.YELLOW.getRGB();
	private static final int SINGLE_TRUE = Color.BLUE.getRGB();
	private static final int DUAL_CLUSTER = Color.MAGENTA.getRGB();
	private static final int MULTI_CLUSTER = Color.CYAN.getRGB();
	private static final int SINGLE_UNFOCUSED = Color.RED.getRGB();

	// Constants
	private static final String CORRECTIONS_TXT = "_corrections.txt";
	final static int RADIUS = 5;

	// Variables
	private NucleiSet2D[] annotatedNuclei;
	private NucleiSet3D actualNuclei;
	private ImagePlus imagePlus;
	private ImagePlus imageOriginal;
	private Coordinates startOfNucleus;
	private FileInfo imageFileInfo;

	private MouseListener listener;


	/**
	 * Get the colour that the NucleusEvent should be painted in depending on its features.
	 *
	 * @param aNucleusEvent
	 * @return The RGB colour integer for the NucleusEvent.
	 */
	private int getColor(final NucleusEvent aNucleusEvent)
	{
		if (aNucleusEvent.isSingleCell())
		{
			return aNucleusEvent.isTrueNucleus() ? SINGLE_TRUE : SINGLE_UNFOCUSED;
		}
		else if (aNucleusEvent.isPartOfDualCluster())
		{
			return aNucleusEvent.isTrueNucleus() ? SINGLE_TRUE : DUAL_CLUSTER;
		}
		else if (aNucleusEvent.isPartOfMultiCluster())
		{
			return aNucleusEvent.isTrueNucleus() ? SINGLE_TRUE : MULTI_CLUSTER;
		}
		else
		{
			return aNucleusEvent.isTrueNucleus() ? ANY_TRUE : ANY_UNFOCUSED;
		}
	}


	private void readPointsFromFile()
	{
		String fileName = this.imageFileInfo.fileName;
		fileName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
		fileName += CORRECTIONS_TXT;
		final File correctionsFile = new File(this.imageFileInfo.directory + File.separator + fileName);
		final HashMap<Coordinates, Nucleus> createdNuclei = new HashMap<>();
		try
		{
			final FileReader reader = new FileReader(correctionsFile);
			final BufferedReader buffer = new BufferedReader(reader);
			String line;
			while ((line = buffer.readLine()) != null)
			{
				final String[] data = line.split("\t");
				int correction = 0;
				if (data.length < Nucleus3DFileUtils.NUMBER_OF_COLUMNS)
				{
					// Added nucleus number at spot 1, so correct column numbers
					// if it is missing
					correction = -1;
				}
				final int zCoord = Float.valueOf(data[Nucleus3DFileUtils.Z_COORDINATE + correction]).intValue();
				final Coordinates id = Coordinates.fromString(data[Nucleus3DFileUtils.NUCLEUS_ID + correction]);
				if (Integer.parseInt(data[Nucleus3DFileUtils.NUCLEUS_NR]) > -1)
				{
					final NucleusEvent nucleusEvent = new NucleusEvent(Float.valueOf(data[Nucleus3DFileUtils.X_COORDINATE + correction]).intValue(),
							Float.valueOf(data[Nucleus3DFileUtils.Y_COORDINATE + correction]).intValue(), zCoord);
					nucleusEvent.setTrueNucleus(Boolean.valueOf(data[Nucleus3DFileUtils.TRUE_NUCLEUS + correction]));
					if (data[Nucleus3DFileUtils.IS_PART_OFF + correction] != null)
					{
						try
						{
							nucleusEvent.setIsPartOf(BaseNucleus.PartOf.valueOf(data[Nucleus3DFileUtils.IS_PART_OFF + correction]));
						}
						catch (final IllegalArgumentException aIAEx)
						{
							// Ignore. This is the old version of the column without
							// any actual data.
						}
					}
					this.annotatedNuclei[zCoord].addNucleusEvent(nucleusEvent);
					if (!createdNuclei.containsKey(id))
					{
						final Nucleus parent = new Nucleus(id);
						if (nucleusEvent.isPartOfSomething())
						{
							parent.setIsPartOf(nucleusEvent.getIsPartOf());
						}
						createdNuclei.put(id, parent);
						this.actualNuclei.addNucleus(parent);
					}
					final Nucleus parentNuc = createdNuclei.get(id);
					nucleusEvent.setParent(parentNuc);
					parentNuc.addNucleusEvent(nucleusEvent);
				}
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

		for (final Nucleus nucleus : this.actualNuclei)
		{
			final boolean hasTrueSet = nucleus.getTrueEvent() != null;
			for (final NucleusEvent nucleusEvent : nucleus.getEvents())
			{
				final int color = getColor(nucleusEvent);
				if (hasTrueSet)
				{
					NucleiDataVisualiser.drawSquare(nucleusEvent, this.imagePlus.getStack().getProcessor((int) (nucleusEvent.getZcoordinate() + 1)), RADIUS, color, true);
				}
				else
				{
					NucleiDataVisualiser.drawCirle(nucleusEvent, this.imagePlus.getStack().getProcessor((int) (nucleusEvent.getZcoordinate() + 1)), RADIUS, color, true, null);
				}
			}
		}

		this.imagePlus.updateAndDraw();
	}


	public void removeNucleus(final float aXCoordinate, final float aYCoordinate)
	{
		final int zCoordinate = this.imagePlus.getCurrentSlice() - 1;
		final NucleusEvent nearestNucleusEvent = this.annotatedNuclei[zCoordinate].getNearestNucleusEvent(new Coordinates(aXCoordinate, aYCoordinate, zCoordinate));
		if (nearestNucleusEvent != null)
		{
			final Nucleus nucleus = nearestNucleusEvent.getParent();
			this.actualNuclei.removeNucleus(nucleus);
			for (final NucleusEvent delNucleusEvent : nucleus.getEvents())
			{
				this.annotatedNuclei[(int) delNucleusEvent.getZcoordinate()].removeNucleusEvent(delNucleusEvent);
				restoreOriginal(delNucleusEvent);
			}
			this.imagePlus.setSlice(zCoordinate + 1);
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


	private void restoreOriginal(final NucleusEvent aNucleus)
	{
		restoreOriginal(aNucleus.getCoordinates());
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

				dialog.addMessage("Please select the nucleus channel.");
				dialog.addChoice("Nucleus channel", channelchooser, "1");
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

		final int nSlices = this.imagePlus.getNSlices();
		this.annotatedNuclei = new NucleiSet2D[nSlices];
		this.actualNuclei = new NucleiSet3D();
		for (int i = 0; i < nSlices; i++)
		{
			this.annotatedNuclei[i] = new NucleiSet2D();
		}

		readPointsFromFile();
		if (runCorrections())
		{
			// Create a file with the measure information
			writePointsToFile();
		}

		this.imageOriginal.changes = false;
		this.imagePlus.changes = false;
		this.imageOriginal.close();
		imageOrig.close();
	}


	private boolean runCorrections()
	{
		this.listener = new NucleusPickerListener(this, this.imagePlus.getCanvas());
		this.imagePlus.getCanvas().addMouseListener(this.listener);

		boolean proceed = false;
		boolean wasOked = false;

		while (!proceed)
		{
			final NonBlockingGenericDialog nonBlockDialog = new NonBlockingGenericDialog("Correction selection");
			final StringBuilder message = new StringBuilder("Please add corrections.\n");
			message.append(" -------------------------------------------------------- \n");
			message.append(" - Mouse click: Select top of nucleus (step 1)\n");
			message.append(" - Alt + click: Select bottom end of nucleus (step 2)\n");
			message.append(" - Ctrl + click: Select the center slice of nearest nucleus (step 3a)        \n");
			message.append(" - Shift + click: Set a nucleus as a Single cell (Step 3b)  \n");
			message.append(" - Alt + crtl + click: Set a nucleus as part of a dual cluster (Step 3c)  \n");
			message.append(" - Alt + shift + click: Set a nucleus as part of a multi-cell cluster (Step 3d)  \n");
			message.append(" -------------------------------------------------------- \n");
			message.append(" - Ctrl + shift + click: Delete the entire annotation of the nearest nucleus (for mistakes)       ");
			nonBlockDialog.addMessage(message.toString());
			nonBlockDialog.enableYesNoCancel("Done", "Save");
			nonBlockDialog.showDialog();

			proceed = nonBlockDialog.wasCanceled() || nonBlockDialog.wasOKed();
			if (!proceed)
			{
				writePointsToFile();
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


	public void setDualCluster(final int aXCoordinate, final int aYCoordinate)
	{
		final int zCoordinate = this.imagePlus.getCurrentSlice() - 1;
		final NucleusEvent nearestNucleusEvent = this.annotatedNuclei[zCoordinate].getNearestNucleusEvent(new Coordinates(aXCoordinate, aYCoordinate, zCoordinate));

		if (nearestNucleusEvent != null)
		{
			final Nucleus nucleus = nearestNucleusEvent.getParent();
			nucleus.setIsPartOf(Nucleus.PartOf.DUAL_CLUSTER);

			final boolean trueNucleus = nucleus.getTrueEvent() != null;
			for (final NucleusEvent nucleusEvent : nucleus.getEvents())
			{
				nucleusEvent.setIsPartOf(Nucleus.PartOf.DUAL_CLUSTER);
				final int color = nucleusEvent.isTrueNucleus() ? SINGLE_TRUE : DUAL_CLUSTER;
				restoreOriginal(nucleusEvent);
				final ImageProcessor proc = this.imagePlus.getStack().getProcessor((int) (nucleusEvent.getZcoordinate() + 1));
				if (trueNucleus)
				{
					NucleiDataVisualiser.drawSquare(nucleusEvent, proc, RADIUS, color, true);
				}
				else
				{
					NucleiDataVisualiser.drawCirle(nucleusEvent, proc, RADIUS, color, true, null);
				}
			}

			this.imagePlus.updateAndDraw();
		}
	}


	public void setEndOfNucleus(final Coordinates aCoordinates)
	{
		if (this.startOfNucleus != null)
		{
			final int xStart = (int) this.startOfNucleus.getXcoordinate();
			final int xEnd = (int) aCoordinates.getXcoordinate();
			final int yStart = (int) this.startOfNucleus.getYcoordinate();
			final int yEnd = (int) aCoordinates.getYcoordinate();
			final int zStart = (int) this.startOfNucleus.getZcoordinate();
			final int zEnd = this.imagePlus.getCurrentSlice() - 1;
			final float xStep = ((float) (xEnd - xStart)) / (Math.abs(zEnd - zStart));
			final float yStep = ((float) (yEnd - yStart)) / (Math.abs(zEnd - zStart));
			int currentZStep = 0;
			final Coordinates coords = new Coordinates(xStart, yStart, zStart);
			final Nucleus nucleus = new Nucleus(coords);
			this.actualNuclei.addNucleus(nucleus);
			for (int z = zStart; z <= zEnd; z++)
			{
				final NucleusEvent newNucleusEvent = new NucleusEvent((int) (xStart + (xStep * currentZStep)), (int) (yStart + (yStep * currentZStep)), z);
				newNucleusEvent.setParent(nucleus);
				nucleus.addNucleusEvent(newNucleusEvent);
				newNucleusEvent.setTrueNucleus(false);
				this.annotatedNuclei[(int) newNucleusEvent.getZcoordinate()].addNucleusEvent(newNucleusEvent);
				NucleiDataVisualiser.drawCirle(newNucleusEvent, this.imagePlus.getStack().getProcessor(z + 1), RADIUS, ANY_UNFOCUSED, true, null);
				currentZStep++;
			}
			this.startOfNucleus = null;
			this.imagePlus.updateAndDraw();
		}
	}


	public void setMultiCluster(final int aXCoordinate, final int aYCoordinate)
	{
		final int zCoordinate = this.imagePlus.getCurrentSlice() - 1;
		final NucleusEvent nearestNucleusEvent = this.annotatedNuclei[zCoordinate].getNearestNucleusEvent(new Coordinates(aXCoordinate, aYCoordinate, zCoordinate));

		if (nearestNucleusEvent != null)
		{
			final Nucleus nucleus = nearestNucleusEvent.getParent();
			nucleus.setIsPartOf(Nucleus.PartOf.MULTI_CLUSTER);

			final boolean trueNucleus = nucleus.getTrueEvent() != null;
			for (final NucleusEvent nucleusEvent : nucleus.getEvents())
			{
				nucleusEvent.setIsPartOf(Nucleus.PartOf.MULTI_CLUSTER);
				final int color = nucleusEvent.isTrueNucleus() ? SINGLE_TRUE : MULTI_CLUSTER;
				restoreOriginal(nucleusEvent);
				final ImageProcessor proc = this.imagePlus.getStack().getProcessor((int) (nucleusEvent.getZcoordinate() + 1));
				if (trueNucleus)
				{
					NucleiDataVisualiser.drawSquare(nucleusEvent, proc, RADIUS, color, true);
				}
				else
				{
					NucleiDataVisualiser.drawCirle(nucleusEvent, proc, RADIUS, color, true, null);
				}
			}

			this.imagePlus.updateAndDraw();
		}
	}


	public void setNucleusUntrue(final float aXCoordinate, final float aYCoordinate)
	{
		final int zCoordinate = this.imagePlus.getCurrentSlice() - 1;
		final NucleusEvent nearestNucleusEvent = this.annotatedNuclei[zCoordinate].getNearestNucleusEvent(new Coordinates(aXCoordinate, aYCoordinate, zCoordinate));
		if (nearestNucleusEvent != null)
		{
			nearestNucleusEvent.setTrueNucleus(false);
			final Nucleus nucleus = nearestNucleusEvent.getParent();
			final int colour = nucleus.isSingleCell() ? SINGLE_UNFOCUSED : ANY_UNFOCUSED;
			for (final NucleusEvent event : nucleus.getEvents())
			{
				NucleiDataVisualiser.drawCirle(event, this.imagePlus.getProcessor(), RADIUS, colour, true, null);
			}
			this.imagePlus.updateAndDraw();
		}
	}


	public void setSingleNucleus(final int aXCoordinate, final int aYCoordinate)
	{
		final int zCoordinate = this.imagePlus.getCurrentSlice() - 1;
		final NucleusEvent nearestNucleusEvent = this.annotatedNuclei[zCoordinate].getNearestNucleusEvent(new Coordinates(aXCoordinate, aYCoordinate, zCoordinate));

		if (nearestNucleusEvent != null)
		{
			final Nucleus nucleus = nearestNucleusEvent.getParent();
			nucleus.setIsPartOf(Nucleus.PartOf.SINGLE_CELL);

			final boolean trueNucleus = nucleus.getTrueEvent() != null;
			for (final NucleusEvent nucleusEvent : nucleus.getEvents())
			{
				nucleusEvent.setIsPartOf(Nucleus.PartOf.SINGLE_CELL);
				final int color = nucleusEvent.isTrueNucleus() ? SINGLE_TRUE : SINGLE_UNFOCUSED;
				restoreOriginal(nucleusEvent);
				final ImageProcessor proc = this.imagePlus.getStack().getProcessor((int) (nucleusEvent.getZcoordinate() + 1));
				if (trueNucleus)
				{
					NucleiDataVisualiser.drawSquare(nucleusEvent, proc, RADIUS, color, true);
				}
				else
				{
					NucleiDataVisualiser.drawCirle(nucleusEvent, proc, RADIUS, color, true, null);
				}
			}

			this.imagePlus.updateAndDraw();
		}
	}


	public void setStartOfNucleus(final Coordinates aCoordinates)
	{
		this.startOfNucleus = aCoordinates;
		this.startOfNucleus.setZcoordinate(this.imagePlus.getCurrentSlice() - 1);
	}


	public void setTrueNucleus(final float aXCoordinate, final float aYCoordinate)
	{
		final int zCoordinate = this.imagePlus.getCurrentSlice() - 1;
		final NucleusEvent nearestNucleusEvent = this.annotatedNuclei[zCoordinate].getNearestNucleusEvent(new Coordinates(aXCoordinate, aYCoordinate, zCoordinate));
		if (nearestNucleusEvent != null)
		{
			final Nucleus nucleus = nearestNucleusEvent.getParent();
			final NucleusEvent trueNuc = nucleus.getTrueEvent();
			if (trueNuc == null) // If already true, ignore new attempt.
			{
				nearestNucleusEvent.setTrueNucleus(true);
				nucleus.setCoordinates(nearestNucleusEvent.getCoordinates());
				for (final NucleusEvent nucleusEvent : nucleus.getEvents())
				{

					final int color = getColor(nucleusEvent);
					restoreOriginal(nucleusEvent);
					final ImageProcessor proc = this.imagePlus.getStack().getProcessor((int) (nucleusEvent.getZcoordinate() + 1));
					NucleiDataVisualiser.drawSquare(nucleusEvent, proc, RADIUS, color, true);
				}

				this.imagePlus.updateAndDraw();
			}
			else
			{
				java.awt.Toolkit.getDefaultToolkit().beep();
			}
		}
	}


	/**
	 * Write the information of the selected points to file. The file name is the same as the image name, but with the CORRECTIONS extension added.
	 */
	private void writePointsToFile()
	{
		String filename = this.imageFileInfo.fileName;
		filename = filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.')) : filename;
		filename += CORRECTIONS_TXT;
		try
		{
			final PrintWriter resultsFile = new PrintWriter(this.imageFileInfo.directory + File.separator + filename);

			int i = 0;
			int nucleusNumber = 0;
			for (final Nucleus nucleus : this.actualNuclei)
			{
				for (final NucleusEvent nucleusEvent : nucleus.getEvents())
				{
					// Write the nucleus info to file
					final StringBuilder sb = new StringBuilder();
					sb.append(i + "\t");
					sb.append(nucleusNumber + "\t");
					sb.append(nucleusEvent.getParent().getCoordinates().toString() + "\t");
					sb.append(nucleusEvent.getXcoordinate() + "\t");
					sb.append(nucleusEvent.getYcoordinate() + "\t");
					sb.append(nucleusEvent.getZcoordinate() + "\t");
					sb.append(nucleusEvent.getLocalIntensity() + "\t");
					sb.append(nucleusEvent.isTrueNucleus() + "\t");
					final String partOf = nucleusEvent.getIsPartOf() != null ? nucleusEvent.getIsPartOf().name() : BaseNucleus.PartOf.NO_PART_OF.name();
					sb.append(partOf);
					resultsFile.println(sb.toString());
					i++;
				}
				nucleusNumber++;
			}

			resultsFile.close();
		}
		catch (final FileNotFoundException fnfEx)
		{
			IJ.handleException(fnfEx);
		}
	}
}
