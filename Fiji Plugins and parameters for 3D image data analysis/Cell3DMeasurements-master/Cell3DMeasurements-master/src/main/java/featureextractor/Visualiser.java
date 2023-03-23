package featureextractor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import data.Cell3D;
import data.Cell3D_Group;
import data.Coordinates;
import data.Nucleus3D;
import geometry.Line3D;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import utils.MyMath;

public final class Visualiser
{
	/**
	 * Draw the outlines of a nucleus in a 2D image. The method checks each pixel in the label image to see if it is of the right label/colour and if it is on the edge of the nucleus (i.e. it has a differently coloured pixel horizontally or
	 * vertically next to it). If so, the that pixel position given the given colour in the result image.
	 *
	 * @param aNucleus
	 *            The nucleus of which the outline will be drawn
	 * @param aResultImage
	 *            The image in which the outline will be drawn
	 * @param aLabelImage
	 *            The image that contains the segmentation of the nuclei in colours according to their label ID
	 * @param aColour
	 *            The label ID colour for this nucleus in the result image
	 */
	private static void draw2DOutlines(final Nucleus3D aNucleus, final ImageStack aResultImage, final ImageStack aLabelImage, final int aColour)
	{
		// For each pixel in the image check if the next pixel in the segmented image have an other value compare to that image
		// In that case color this pixel
		final int sizeX = aResultImage.getWidth();
		final int sizeY = aResultImage.getHeight();
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				final double value = aLabelImage.getVoxel(x, y, 0);
				if (value == aNucleus.getLabel())
				{
					if (x < sizeX - 2 && x > 1)
					{
						if (aLabelImage.getVoxel(x + 1, y, 0) != value || aLabelImage.getVoxel(x - 1, y, 0) != value)
						{
							aResultImage.setVoxel(x, y, 0, aColour);
						}
					}
					if (y < sizeY - 2 && y > 1)
					{
						if (aLabelImage.getVoxel(x, y + 1, 0) != value || aLabelImage.getVoxel(x, y - 1, 0) != value)
						{
							aResultImage.setVoxel(x, y, 0, aColour);
						}
					}
				}
			}
		}
	}


	/**
	 * Draw the entire nucleus (not just the outline) in a 3D image.
	 *
	 * @param aImage
	 *            The image in which to draw
	 * @param aColour
	 *            The integer for the colour/intensity in which the nucleus will be drawn
	 * @param aNucleusCoordinates
	 *            The coordinates for every pixel of the nucleus
	 */
	public static void draw3DNucleus(final ImageStack aImage, final int aColour, final List<Coordinates> aNucleusCoordinates)
	{
		// Draw the outline on a 3D image or 2D image
		for (int m = 0; m < aNucleusCoordinates.size(); m++)
		{
			final int x = (int) aNucleusCoordinates.get(m).getXcoordinate();
			final int y = (int) aNucleusCoordinates.get(m).getYcoordinate();
			final int z = (int) aNucleusCoordinates.get(m).getZcoordinate();
			aImage.setVoxel(x, y, z, aColour);
		}
	}


	/**
	 * For all cell groups, draw the outlines of all the members of a cell group in the same colour (a different colour per group).
	 *
	 * @param aImage
	 *            The image on which to draw
	 * @param aCellGroups
	 *            The list of cell groups to draw
	 */
	public static void drawCellGroups(final ImagePlus aImage, final List<Cell3D_Group> aCellGroups)
	{
		final ImageConverter con = new ImageConverter(aImage);
		con.convertToRGB();
		final ImageStack groupedImageStack = aImage.getStack();
		final Random randomGenerator = new Random();
		final List<Color> coloursUsed = new ArrayList<>();
		Color randomColour = new Color(randomGenerator.nextInt(256), randomGenerator.nextInt(256), randomGenerator.nextInt(256));

		for (final Cell3D_Group cellGroup : aCellGroups)
		{
			while (coloursUsed.contains(randomColour))
			{
				randomColour = new Color(randomGenerator.nextInt(256), randomGenerator.nextInt(256), randomGenerator.nextInt(256));
			}
			coloursUsed.add(randomColour);

			for (final Cell3D cell : cellGroup.getMembers())
			{
				final int colour = randomColour.getRGB();
				for (final Coordinates coord : cell.getOutline())
				{
					final int x = (int) coord.getXcoordinate();
					final int y = (int) coord.getYcoordinate();
					final int z = (int) coord.getZcoordinate();
					groupedImageStack.setVoxel(x, y, z, colour);
				}
			}
		}
		aImage.updateAndDraw();
	}


	/**
	 * Draws the outlines of the cells given in the cell group list in a colour based on the successful identification of the migration mode. If the individual cell migration mode doesn't match up with that of its group, it will be coloured either
	 * magenta (if the group has no known migration mode) or red (if the group has a known, but different migration mode set. When the group and cell modes match, the outline will be blue. Note that the outlines will be drawn on a completely blank
	 * (black background) image.
	 *
	 * @param aOriginalImage
	 *            The image from which the cell data came; for its dimensions only
	 * @param aNucleusGroups
	 *            The list of Cell3D groups to draw
	 * @return The resulting image with the colour-coded cell outlines on a black background.
	 */
	public static ImagePlus drawCorrectMigrationMode(final ImagePlus aOriginalImage, final List<Cell3D_Group> aNucleusGroups)
	{
		final int height = aOriginalImage.getHeight();
		final int width = aOriginalImage.getWidth();
		final int slices = aOriginalImage.getNSlices();
		final ImagePlus resultImageBlack = IJ.createImage("BlackImage", "16-bit Black", width, height, slices);
		final ImageConverter con2 = new ImageConverter(resultImageBlack);
		con2.convertToRGB();
		final ImageStack resultsImageBlackStack = resultImageBlack.getImageStack();

		for (final Cell3D_Group nucGroup : aNucleusGroups)
		{
			final String migrationModeGroup = nucGroup.getMigrationmode();
			for (final Cell3D cellMember : nucGroup.getMembers())
			{
				final String migrationModeIndividual = cellMember.getMigrationMode();
				int colour;

				if (!migrationModeGroup.equals(migrationModeIndividual))
				{
					if (migrationModeGroup.equals("UNKNOWN"))
					{
						colour = Color.MAGENTA.getRGB();
					}
					else
					{
						colour = Color.RED.getRGB();
					}
				}
				else
				{
					colour = Color.BLUE.getRGB();
				}

				final List<Coordinates> outlines = cellMember.getOutline();
				for (final Coordinates coord : outlines)
				{
					final int x = (int) coord.getXcoordinate();
					final int y = (int) coord.getYcoordinate();
					final int z = (int) coord.getZcoordinate();
					resultsImageBlackStack.setVoxel(x, y, z, colour);
				}
			}
		}

		resultImageBlack.updateAndDraw();
		resultImageBlack.setTitle("Outlines");
		resultImageBlack.show();
		return resultImageBlack;
	}


	// TODO: what todo with this method?
	public static void drawLine3D(final Coordinates aPoint1, final Coordinates aPoint2, final ImagePlus imageDraw, final Color color)
	{
		final Line3D line = new Line3D(aPoint1, aPoint2);
		final double startZ = aPoint1.getZcoordinate();
		final double endZ = aPoint2.getZcoordinate();
		final double zDiff = startZ - endZ;
		final int direction = startZ > endZ ? -1 : 1;
		if (!MyMath.isAboutZero(zDiff))
		{
			final double steps = 2 * (endZ - startZ);
			for (double z = 0; z < steps; z++)
			{
				final double zCoord1 = (startZ + ((z * 0.5) * direction));
				final double zCoord2 = (zCoord1 + (0.5 * direction));
				Coordinates cor1 = null;
				Coordinates cor2 = null;
				if (zCoord1 % 1 == 0)
				{
					cor1 = new Coordinates(line.getXY(zCoord1)[0], line.getXY(zCoord1)[1], (int) zCoord1);
					cor2 = new Coordinates(line.getXY(zCoord2)[0], line.getXY(zCoord2)[1], (int) zCoord1);
				}
				else if (zCoord2 % 1 == 0)
				{
					cor1 = new Coordinates(line.getXY(zCoord1)[0], line.getXY(zCoord1)[1], zCoord2);
					cor2 = new Coordinates(line.getXY(zCoord2)[0], line.getXY(zCoord2)[1], zCoord2);
				}
				// TODO : What if neither of the above is true???
				final Line roi = new Line(cor1.getXcoordinate(), cor1.getYcoordinate(), cor2.getXcoordinate(), cor2.getYcoordinate());
				roi.setStrokeWidth(1);
				imageDraw.setSlice((int) (cor1.getZcoordinate() + 1));
				final ImageProcessor imageDrawProc = imageDraw.getProcessor();
				imageDrawProc.setColor(color);
				imageDrawProc.draw(roi);
			}
		}
		else
		{
			final Line roi = new Line(aPoint1.getXcoordinate(), aPoint1.getYcoordinate(), aPoint2.getXcoordinate(), aPoint2.getYcoordinate());
			roi.setStrokeWidth(1);
			imageDraw.setSlice((int) (aPoint1.getZcoordinate() + 1));
			final ImageProcessor imageDrawProc = imageDraw.getProcessor();
			imageDrawProc.setColor(color);
			imageDrawProc.draw(roi);
		}
	}


	/**
	 * Draw a circle (2D) at each of the coordinates in the list.
	 *
	 * @param aImage
	 *            The image (2D/3D) on which to draw
	 * @param aListOfSeeds
	 *            The list of Coordinates
	 * @param aColour
	 *            The colour in which the circle will be drawn
	 * @param aDiameter
	 *            The diameter of the circle (in pixels)
	 */
	static public void drawMarkers(final ImagePlus aImage, final List<Coordinates> aListOfSeeds, final Color aColour, final int aDiameter)
	{

		// Create of each Coordinate a Roi and draw this in the image
		for (int i = 0; i < aImage.getNSlices(); i++)
		{
			final ImageProcessor imageOutlineProc = aImage.getImageStack().getProcessor(i + 1);
			for (int j = 0; j < aListOfSeeds.size(); j++)
			{
				if (aListOfSeeds.get(j).getZcoordinate() == i)
				{
					final int radius = aDiameter / 2;
					aImage.setSlice(i + 1);
					aImage.setRoi(new OvalRoi(aListOfSeeds.get(j).getXcoordinate() - radius, aListOfSeeds.get(j).getYcoordinate() - radius, aDiameter, aDiameter));
					final Roi roi = aImage.getRoi();
					imageOutlineProc.setColor(aColour);
					imageOutlineProc.draw(roi);
				}
			}
		}
	}


	/**
	 * Draws the (solid) segments as given by the list of Cell3D_Groups and colours all the cells of a group according to the group's migration mode.
	 *
	 * @param aImage
	 *            The image on which the groups data is based (for dimensions only)
	 * @param aCellGroups
	 *            The list of Cell3D_Groups
	 * @return The constructed image in which each cell is drawn (on an empty black background) in a colour according to the migration mode of the group.
	 */
	public static ImagePlus drawMigrationMode(final ImagePlus aImage, final List<Cell3D_Group> aCellGroups)
	{
		final int height = aImage.getHeight();
		final int width = aImage.getWidth();
		final int slices = aImage.getNSlices();
		final ImagePlus resultImageBlack = IJ.createImage("BlackImage", "16-bit Black", width, height, slices);
		final ImageConverter con2 = new ImageConverter(resultImageBlack);
		con2.convertToRGB();
		final ImageStack resultsImageBlackStack = resultImageBlack.getImageStack();

		for (final Cell3D_Group cellGroup : aCellGroups)
		{
			int colour;
			final String migrationModeGroup = cellGroup.getMigrationmode();
			if (migrationModeGroup.equals("UNKNOWN"))
			{
				colour = Color.YELLOW.getRGB();
			}
			else if (migrationModeGroup.equals("SINGLE_CELL"))
			{
				colour = Color.MAGENTA.getRGB();
			}
			else if (migrationModeGroup.equals("DUAL_CLUSTER"))
			{
				colour = Color.GREEN.getRGB();
			}
			else if (migrationModeGroup.equals("MULTI_CLUSTER"))
			{
				colour = Color.RED.getRGB();
			}
			else
			{
				colour = Color.BLUE.getRGB();
			}

			for (final Cell3D cell : cellGroup.getMembers())
			{
				for (final Coordinates coord : cell.getCoordinates())
				{
					final int x = (int) coord.getXcoordinate();
					final int y = (int) coord.getYcoordinate();
					final int z = (int) coord.getZcoordinate();
					resultsImageBlackStack.setVoxel(x, y, z, colour);
				}
				// for (final Coordinates coord : cell.getNucleus().getNucleusCoordinates())
				// {
				// final int x = (int) coord.getXcoordinate();
				// final int y = (int) coord.getYcoordinate();
				// final int z = (int) coord.getZcoordinate();
				// resultsImageBlackStack.setVoxel(x, y, z, colour);
				// }
			}
		}

		resultImageBlack.updateAndDraw();
		resultImageBlack.setTitle("Outlines");
		resultImageBlack.show();
		return resultImageBlack;
	}


	/**
	 * Method drawNucleus draws two sets of images of the nucleus. In the first, each nucleus outline is drawn. This is ideally done over the original nucleus signal of the image. In the second image, the nucleus segmentation is drawn in colours
	 * representing the validity of the segment (correct/over-under/disqualified).
	 *
	 * @param aCells
	 *            The list of Cell3D that will be drawn on the images
	 * @param aOutlineImage
	 *            The (preferably original nucleus signal) image on which the outlines will be drawn
	 * @param aValidityImage
	 *            The (preferably empty black) image on which the segment validity will be drawn
	 * @param aLabelImage
	 *            The segmented image for 2D nucleus identification
	 */
	public static void drawNucleusResults(final Cell3D[] aCells, final ImagePlus aOutlineImage, final ImagePlus aValidityImage, final ImagePlus aLabelImage)
	{
		int colour;
		for (int k = 0; k < aCells.length; k++)
		{
			final Nucleus3D nucleus = aCells[k].getNucleus();
			if (nucleus.isBorderNucleus() == true)
			{
				colour = Color.ORANGE.getRGB();
			}
			// else if (nucleus.isSelected() == false)
			// {
			// colour = Color.CYAN.getRGB();
			// }
			else if (nucleus.isTooSmall() == true)
			{
				colour = Color.WHITE.getRGB();
			}
			else
			{

				if (nucleus.getMarkersCount() == 0)
				{
					colour = Color.RED.getRGB();
				}
				else if (nucleus.getMarkersCount() == 1)
				{
					colour = Color.BLUE.getRGB();
				}
				else
				{
					colour = Color.MAGENTA.getRGB();
				}
			}

			final ImageStack outlineStack = aOutlineImage.getImageStack();
			if (aLabelImage.getNSlices() == 1)
			{
				draw2DOutlines(nucleus, outlineStack, aLabelImage.getImageStack(), colour);
			}
			else
			{
				draw3DNucleus(outlineStack, colour, nucleus.getNucleusOutlines());
			}

			final ImageStack validityStack = aValidityImage.getImageStack();
			final List<Coordinates> nucleusCordinates = nucleus.getNucleusCoordinates();
			draw3DNucleus(validityStack, colour, nucleusCordinates);
		}
		aOutlineImage.updateAndDraw();
		aValidityImage.updateAndDraw();
	}
}
