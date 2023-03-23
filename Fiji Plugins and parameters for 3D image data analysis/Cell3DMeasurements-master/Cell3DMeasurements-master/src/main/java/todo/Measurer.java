package todo;

import java.util.ArrayList;

import data.Coordinates;
import geometry.Line2D;
import geometry.Line3D;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import utils.MyMath;

public class Measurer
{

	/**
	 *
	 *
	 */
	private static double[] getLinePlotProfile2D(final Coordinates cor1, final Coordinates cor2, final double aLabel1, final double aLabel2, final ImagePlus aImage, final double aStrokeWidth,
			final ImagePlus imageDraw, final ImagePlus imageMeasure)
	{
		final boolean[] inOtherNucleus = { false, false };
		final ImageStack imageMesureStack = imageMeasure.getImageStack();
		final int slice = (int) cor1.getZcoordinate();
		final Roi oldRoi = aImage.getRoi();

		final double startX = cor1.getXcoordinate();
		final double startY = cor1.getYcoordinate();
		final double dx = cor2.getXcoordinate() - startX;
		final double dy = cor2.getYcoordinate() - startY;
		final int lineLength = (int) Math.round(Math.sqrt(dx * dx + dy * dy));
		final double xinc = dx / lineLength;
		final double yinc = dy / lineLength;

		// Create the (main) profileLine line in 2D
		final Line2D profileLine = MyMath.get2DLineFromTwoPoints(cor1, cor2);

		Line2D line = null;
		final double[] result = new double[lineLength];

		// For each step
		for (int step = 0; step < lineLength; step++)
		{
			inOtherNucleus[0] = false;
			if (!inOtherNucleus[1])
			{
				final ArrayList<Coordinates> allCor = new ArrayList<>();
				double x1 = 0, x2 = 0, y1 = 0, y2 = 0;

				// When the line between the nucleus is not vertical
				if (profileLine != null)
				{
					// Get the coordinates for the perpendicular line
					line = MyMath.getPerpendicularLine(profileLine, new Coordinates((startX + (step * xinc)), (startY + (step * yinc))));

					// When the line is not vertical
					if (line != null)
					{
						final double oneYstep = line.getY(startX + 1) - line.getY(startX);
						// The distance traveled along the line in one X-step
						final double oneLineStep = Math.sqrt(1 + (oneYstep * oneYstep));
						final double xStepsNeeded = aStrokeWidth / oneLineStep;
						x1 = startX + (step * xinc) - xStepsNeeded;
						x2 = startX + (step * xinc) + xStepsNeeded;
						y1 = line.getY(x1);
						y2 = line.getY(x2);

						for (double b = 0; b < xStepsNeeded; b++)
						{
							if (allCor.isEmpty())
							{
								final double x = startX + (step * xinc) - b;
								final double y = line.getY(x);
								allCor.add(new Coordinates(x, y));
							}
							else
							{
								final double x = startX + (step * xinc) - b;
								allCor.add(new Coordinates(x, line.getY(x)));
								allCor.add(new Coordinates(x2, line.getY(x2)));
							}
						}
						allCor.add(0, new Coordinates(x1, y1));
						allCor.add(new Coordinates(x2, y2));
					}

					// When line is vertical (profile line is horizontal)
					else
					{
						y1 = startY - aStrokeWidth;
						y2 = startY + aStrokeWidth;
						x1 = startX + step * xinc;
						x2 = x1;

						for (double b = y1; b < y2; b++)
						{
							allCor.add(new Coordinates(x1, b));
						}
					}
				}

				// When the line between two nucleus is vertical
				else
				{
					// IJ.log("Profileline is null");
					x1 = startX - aStrokeWidth;
					x2 = startX + aStrokeWidth;
					y1 = startY + step * yinc;
					y2 = y1;

					for (double b = x1; b < x2; b++)
					{
						allCor.add(new Coordinates(b, y1));
					}
				}

				lineCrossOtherNucleus(aLabel1, aLabel2, aImage, imageMesureStack, slice, allCor, inOtherNucleus);

				if (!inOtherNucleus[1])
				{
					final Line roi = new Line(x1, y1, x2, y2);
					roi.setStrokeWidth(1);
					aImage.setSlice(slice + 1);
					aImage.setRoi(roi, false);
					result[step] = new ProfilePlot(aImage).getMax();
					imageDraw.setSlice(slice + 1);
					final ImageProcessor imageDrawProc = imageDraw.getProcessor();
					imageDrawProc.setColor(aLabel2);
					imageDrawProc.draw(roi);
				}
				else if (inOtherNucleus[1])
				{
					result[step] = 100000;
					/**
					 * if (cor1.getLabel() == 522) { if (color == 525) { IJ.log("Hallo"); final Line roi = new Line(x1, y1, x2, y2); roi.setStrokeWidth(1); aImage.setSlice(slice+1); aImage.setRoi(roi, false); result[step] = new
					 * ProfilePlot(aImage).getMax(); imageDraw.setSlice(slice+1); ImageProcessor imageDrawProc = imageDraw.getProcessor(); imageDrawProc.setColor(color); imageDrawProc.draw(roi); } }
					 */

				}
				if (inOtherNucleus[0])
				{
					result[step] = -1;
				}

				if (oldRoi != null)
				{
					aImage.setRoi(oldRoi);
				}
				else
				{
					aImage.deleteRoi();
				}
			}
		}
		if (!inOtherNucleus[1])
		{
			return result;
		}
		else
		{
			return null;
		}
	}


	/**
	 * Get a max-intensity line profile on a 3D line. Note that with the z-stack often not being contiguous, a proper line profile cannot be given. As a solution, this method will give a max intensity readout for every z-slice that the given
	 * NucleusLink touches or crosses. The readout is done in a circle of aStrokeWidth wide around the point that the NuclearLink intersects the z-slice.
	 *
	 * In case the NuclearLink is wholly within one z-slice, the getLinePlotProfile2D result is returned instead.
	 *
	 * @param aLink
	 *            The NuclearLink for which the profile will be constructed.
	 * @param aImage
	 *            The image. Note that the proper channel must have been set beforehand.
	 * @param aStrokeWidth
	 *            The width of the circle to sample the intensity values from
	 * @return A double array containing the subsequent max intensities for each z-slice around the point the NuclearLink intersects the z-slice or the 2D profile if there is no z-component to the NuclearLink.
	 */
	public static double[] getLinePlotProfile3D(final Cell3D_Pair pair, final ImagePlus aImage, final ImagePlus imageDraw, final int aStrokeWidth, final ImagePlus imageMesure, final double aDistance)
	{
		// ImageStack imageStack = imageDraw.getImageStack();

		boolean inOtherNucleus = false;
		final double startZ = pair.getCell1().getNucleus().getSeed().getZcoordinate();
		final double endZ = pair.getCell2().getNucleus().getSeed().getZcoordinate();

		final double steps = 2 * (endZ - startZ);
		// IJ.log("Steps " + steps);
		// double grooteeStappen = aDistance/steps;
		// IJ.log("Groote van de stappen " +grooteeStappen );

		final double zDiff = startZ - endZ;
		double[] results = null;

		final Line3D line = new Line3D(pair.getCell1().getNucleus().getSeed(), pair.getCell2().getNucleus().getSeed());
		final int direction = startZ > endZ ? -1 : 1;
		final ArrayList<double[]> resultsList = new ArrayList<>();
		int length = 0;

		if (!MyMath.isAboutZero(zDiff))
		{
			/**
			 * if (steps == 52) { //RoiManager rm = new RoiManager(); final int dif = (int) Math.abs(zDiff); final double[] result = new double[dif + 1]; for (int zStep = 0; zStep <= dif; zStep++) {
			 *
			 *
			 * final int zCoord = (int) (startZ + (zStep * direction)); imageDraw.setSlice(zCoord +1); ImageProcessor imageProc = imageDraw.getProcessor(); final double[] xyCoord = line.getXY(zCoord); for (int x=0; x<xyCoord.length; x++) {
			 *
			 * //imageStack.setVoxel((int)xyCoord[0],(int)xyCoord[1], zCoord, labelnucleus2); imageProc.setColor(labelnucleus2); Roi roi = new PointRoi((int)xyCoord[0], (int)xyCoord[1]); //rm.addRoi(roi); imageProc.draw(roi); //rm.remove(0); }
			 * result[zStep] = getMaxIntensity(new Coordinate((int)xyCoord[0], (int)xyCoord[1], (int)zCoord), aStrokeWidth, aImage.getImageStack().getProcessor(zCoord + 1), imageProc, imageStack, zCoord, labelnucleus2); } //rm.close(); return result;
			 * }
			 */

			// if (steps == 52)
			// {
			// For each step per pixel
			for (double z = 0; z < steps; z++)
			{
				if (inOtherNucleus == false)
				{
					// Calculate the Z position of this step
					final double zCoord1 = (startZ + ((z * 0.5) * direction));
					final double zCoord2 = (zCoord1 + (0.5 * direction));
					Coordinates cor1 = null;
					Coordinates cor2 = null;

					// Determine the nearest Z-slice
					if (zCoord1 % 1 == 0)
					{
						cor1 = new Coordinates(line.getXY(zCoord1)[0], line.getXY(zCoord1)[1], zCoord1);
						cor2 = new Coordinates(line.getXY(zCoord2)[0], line.getXY(zCoord2)[1], zCoord1);
					}
					else if (zCoord2 % 1 == 0)
					{
						cor1 = new Coordinates(line.getXY(zCoord1)[0], line.getXY(zCoord1)[1], zCoord2);
						cor2 = new Coordinates(line.getXY(zCoord2)[0], line.getXY(zCoord2)[1], zCoord2);
					}
					// TODO : What if neither of the above is true
					// Plot this line in 3D
					final double[] resultsPart = getLinePlotProfile2D(cor1, cor2, pair.getCell1().getNucleus().getLabel(), pair.getCell2().getNucleus().getLabel(), aImage, aStrokeWidth, imageDraw,
							imageMesure);

					if (resultsPart != null)
					{
						length = length + resultsPart.length;
						resultsList.add(resultsPart);
					}
					else
					{
						inOtherNucleus = true;
					}
				}
			}

			// }
			if (inOtherNucleus == false)
			{

				/**
				 * double micronspersep = aDistance/length; if (micronspersep > 0.3) { final int dif = (int) Math.abs(zDiff); final double[] result = new double[dif + 1]; for (int zStep = 0; zStep <= dif; zStep++) { final int zCoord = (int) (startZ +
				 * (zStep * direction)); imageDraw.setSlice(zCoord +1); ImageProcessor imageProc = imageDraw.getProcessor(); final double[] xyCoord = line.getXY(zCoord); for (int x=0; x<xyCoord.length; x++) {
				 * //imageStack.setVoxel((int)xyCoord[0],(int)xyCoord[1], zCoord, labelnucleus2); imageProc.setColor(labelnucleus2); Roi roi = new PointRoi((int)xyCoord[0], (int)xyCoord[1]); //rm.addRoi(roi); imageProc.draw(roi); //rm.remove(0); }
				 * result[zStep] = getMaxIntensity(new Coordinate((int)xyCoord[0], (int)xyCoord[1], (int)zCoord),aStrokeWidth, aImage.getImageStack().getProcessor(zCoord + 1), imageProc, zCoord, labelnucleus2); } //rm.close(); return result; }
				 */

				results = new double[length];
				int count = 0;
				for (int i = 0; i < resultsList.size(); i++)
				{
					for (int j = 0; j < resultsList.get(i).length; j++)
					{
						results[count] = resultsList.get(i)[j];
						count = count + 1;
					}
				}

			}
			else if (inOtherNucleus == true)
			{
				return null;
			}
		}
		else
		{
			// The line is in just one level, so in practice just 2D -> return
			// 2D profile.
			final Coordinates cor1 = pair.getCell1().getNucleus().getSeed();
			final Coordinates cor2 = pair.getCell2().getNucleus().getSeed();
			// imageDraw.setSlice(pair.getNucleus1().getSeed().getzValue()+1);

			final double[] results2D = getLinePlotProfile2D(cor1, cor2, pair.getCell1().getNucleus().getLabel(), pair.getCell2().getNucleus().getLabel(), aImage, aStrokeWidth, imageDraw, imageMesure);
			if (results2D != null)
			{
				length = results2D.length;
				// IJ.log("Nucleus 1 " + pair.getNucleus1().getLabel() +" Nucleus 2 " + pair.getNucleus2().getLabel() + " Distance = " + aDistance);
				// IJ.log("Amount of steps: " + length);
				// double micronspersep = aDistance/length;
				// IJ.log("Microns per step: " +micronspersep );
			}
			return results2D;

		}
		IJ.log("Lenght " + length);

		return results;

		/**
		 *
		 *
		 * if (!MyMath.isAboutZero(zDiff)) { final Line3D line = new Line3D(nucleus1, nucleus2); final int dif = (int) Math.abs(zDiff); final double[] result = new double[dif + 1]; final int direction = startZ > endZ ? -1 : 1; for (int zStep = 0;
		 * zStep <= dif; zStep++) { final int zCoord = (int) (startZ + (zStep * direction)); final double[] xyCoord = line.getXY(zCoord); for (int x=0; x<xyCoord.length; x++) { imageStack.setVoxel((int)xyCoord[0],(int)xyCoord[1], zCoord,
		 * Color.WHITE.getRGB()); } result[zStep] = getMaxIntensity(new Coordinate((int)xyCoord[0], (int)xyCoord[1], (int)zCoord), aStrokeWidth, aImage.getImageStack().getProcessor(zCoord + 1), imageStack, zCoord); }
		 *
		 * return result; } else { // The line is in just one level, so in practice just 2D -> return // 2D profile. return getLinePlotProfile2D(pair, aImage, aStrokeWidth); }
		 */
	}


	private static void lineCrossOtherNucleus(final double aFirstLabel, final double aSecondLabel, final ImagePlus aImage, final ImageStack aImageMesureStack, final int aSlice,
			final ArrayList<Coordinates> allCor, final boolean[] aLinecrossNucleus)
	{
		final int width = aImage.getWidth();
		final int height = aImage.getHeight();
		for (int i = 0; i < allCor.size(); i++)
		{
			final double x = allCor.get(i).getXcoordinate();
			final double y = allCor.get(i).getYcoordinate();
			double vox = 0;
			if (x < width && x >= 0)
			{
				if (y < height && y >= 0)
				{
					vox = aImageMesureStack.getVoxel((int) x, (int) y, aSlice);
				}
			}
			if (vox != 0)
			{
				if (vox == aFirstLabel)
				{
					aLinecrossNucleus[0] = true;
				}
				else if (vox == aSecondLabel)
				{
					aLinecrossNucleus[0] = true;
				}
				else
				{
					aLinecrossNucleus[1] = true;
				}
			}
		}
	}

}
