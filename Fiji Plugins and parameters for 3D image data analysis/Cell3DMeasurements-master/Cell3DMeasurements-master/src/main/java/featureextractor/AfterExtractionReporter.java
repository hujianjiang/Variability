package featureextractor;

import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;

import data.Cell3D;
import data.Cell3D_Group;
import data.NucleiSegmentationParameters;
import data.Nucleus3D;
import data.SegmentMeasurements;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import utils.MyMath;
import utils.NucleiDataVisualiser;

/**
 * A non-structural class for the feature extraction. In this class all 'do After measuring' methods can be implemented. These are meant as quick and dirty methods to display a specific data set which is needed at that moment.
 *
 * @author Merijn van Erp
 *
 */
public class AfterExtractionReporter
{
	private static final int BIN_TOTAL = 20;


	/**
	 * Make a histogram comparing the migration distances of single vs collective cells
	 *
	 * @param aCells
	 *            The measured cells
	 */
	public static void doModeMigrationHistogram(final Cell3D[] aCells, final File aWorkingDir)
	{
		final List<Double> migrationDists = new ArrayList<>();
		final List<Double> migrationDistsSingle = new ArrayList<>();

		if (aCells.length > 0)
		{
			for (final Cell3D cell : aCells)
			{
				// Only count cells that have actually migrated from the spheroid
				final double distance = cell.getNucleus().getDistanceToCore();
				if (distance > 0)
				{
					if (cell.getMigrationMode().equals(Cell3D_Group.SINGLE))
					{
						migrationDistsSingle.add(distance);
					}
					else
					{
						migrationDists.add(distance);
					}
				}
			}

			// Setup the distance lists
			final double maxDist = Math.max(MyMath.getMaximum(migrationDists), MyMath.getMaximum(migrationDistsSingle));
			final double stepSize = maxDist / BIN_TOTAL; // Step size is determined by the max migration distance to get the best, most granular division
			final double[] bins = new double[BIN_TOTAL];
			final double[] binsSingle = new double[BIN_TOTAL];
			final double[] binNumbers = new double[BIN_TOTAL];
			for (int i = 0; i < BIN_TOTAL; i++)
			{
				bins[i] = 0;
				binsSingle[i] = 0;
				binNumbers[i] = i * stepSize; // The minimal migration distance associated with the bin
			}

			// Bin the collective cell migrations
			for (final double distance : migrationDists)
			{
				int bin = (int) (distance / stepSize);
				bin = bin == BIN_TOTAL ? bin - 1 : bin; // Make sure that the max value is in the last bin
				bins[bin]++;
			}

			// Bin the single cell migration distance
			for (final double distance : migrationDistsSingle)
			{
				int bin = (int) (distance / stepSize);
				bin = bin == BIN_TOTAL ? bin - 1 : bin; // Make sure that the max value is in the last bin
				binsSingle[bin]++;
			}

			// Show histogram
			final double[][] allTheBins = { bins, binsSingle };
			final String[] yAxes = { "Number of collective cells", "Number of single cells" };
			final ChartPanel histogramPanel = NucleiDataVisualiser.plotDistanceHistogram(binNumbers, allTheBins, "Migration distance", "Migration distance (bin size = " + stepSize + ")",
					"Number of cells", yAxes, null);
			IJ.wait(2000); // Small wait to avoid concurrent modification
			if (aWorkingDir != null)
			{
				final String imageNameForResultImage = "\\MigrationModeAndDistanceHistogram.jpg";
				final File histoOutputFile = new File(NucleiSegmentationParameters.getResultsDir(aWorkingDir) + imageNameForResultImage);
				try
				{
					ChartUtils.saveChartAsJPEG(histoOutputFile, histogramPanel.getChart(), histogramPanel.getWidth(), histogramPanel.getHeight());
				}
				catch (final IOException ioe)
				{
					IJ.handleException(ioe);
				}
			}
		}
	}


	/**
	 * Create a set of scatter plots based on the YAP data. Needs a nucleus YAP measure and a cell (without nucleus) measure.
	 *
	 * @param aCells
	 *            The measured cells
	 * @param aYAPBackgroundValue
	 *            An intensity value for the background of the YAP signal. Will be subtracted from all the YAP intensity values
	 * @param aNucleusYAPIndex
	 *            The additional measurement containing the nucleus YAP signal
	 * @param aCellYAPIndex
	 *            The additional measurement containing the cell YAP signal
	 */
	public static void doYAPReporting(final Cell3D[] aCells, final int aNucleusYAPIndex, final int aCellYAPIndex, final File aWorkingDirectory)
	{
		// Colour list for the plot
		final Paint[] colours = new Paint[aCells.length];
		// Cell data is stored in arrays
		final float[] migrationDistance = new float[aCells.length];
		final float[] meanYAPRatio = new float[aCells.length];
		final float[] medianYAPRatio = new float[aCells.length];
		final float[] cellElongation = new float[aCells.length];

		// Get the YAP data for the figures
		int cellsFound = 0;
		if (aCells.length > 0)
		{
			for (final Cell3D cell : aCells)
			{
				// Only count cells that have actually migrated from the spheroid
				final double distance = cell.getNucleus().getDistanceToCore();
				if (distance > 0)
				{
					migrationDistance[cellsFound] = (float) distance;
					final SegmentMeasurements nucleusMeasure = cell.getSignalMeasurements().get(aNucleusYAPIndex);
					final SegmentMeasurements cellMeasure = cell.getSignalMeasurements().get(aCellYAPIndex);
					double cellYapValue = Math.max((cellMeasure.getMeasurement(SegmentMeasurements.MEAN_INTENSITY) - cellMeasure.getMeasurement(SegmentMeasurements.BACKGROUND_INTENSITY)), 1); // Use 1 as a min value and let the image work with that
					double nucleusYapValue = Math.max((nucleusMeasure.getMeasurement(SegmentMeasurements.MEAN_INTENSITY) - nucleusMeasure.getMeasurement(SegmentMeasurements.BACKGROUND_INTENSITY)), 1); // Use 1 as a min value and let the image work
																																																			// with that
					meanYAPRatio[cellsFound] = (float) (nucleusYapValue / cellYapValue);
					cellYapValue = Math.max((cellMeasure.getMeasurement(SegmentMeasurements.MEDIAN_INTENSITY) - cellMeasure.getMeasurement(SegmentMeasurements.BACKGROUND_INTENSITY)), 1); // Use 1 as a min value and let the image work with that
					nucleusYapValue = Math.max((nucleusMeasure.getMeasurement(SegmentMeasurements.MEDIAN_INTENSITY) - nucleusMeasure.getMeasurement(SegmentMeasurements.BACKGROUND_INTENSITY)), 1); // Use 1 as a min value and let the image work with
																																																	// that
					medianYAPRatio[cellsFound] = (float) (nucleusYapValue / cellYapValue);
					cellElongation[cellsFound] = cell.getNucleus().getMeasurements().getMeasurement(SegmentMeasurements.ELONGATIO).floatValue();

					// Determine the colour of the dot in the graph
					if (cell.getMigrationMode().equals(Cell3D_Group.SINGLE))
					{
						colours[cellsFound] = Color.RED;
					}
					else
					{
						colours[cellsFound] = Color.GREEN;
					}
					cellsFound++;
				}
			}

			// Copy results to double arrays that are sized to the actual number of cells counted.
			// Double arrays are needed for the plot function
			final float[][] cellDataConstrained = new float[2][cellsFound];
			final float[][] cellDataMedianConstrained = new float[2][cellsFound];
			final float[][] cellDataElongationConstrained = new float[2][cellsFound];
			System.arraycopy(migrationDistance, 0, cellDataConstrained[0], 0, cellsFound);
			System.arraycopy(meanYAPRatio, 0, cellDataConstrained[1], 0, cellsFound);
			System.arraycopy(migrationDistance, 0, cellDataMedianConstrained[0], 0, cellsFound);
			System.arraycopy(medianYAPRatio, 0, cellDataMedianConstrained[1], 0, cellsFound);
			System.arraycopy(migrationDistance, 0, cellDataElongationConstrained[0], 0, cellsFound);
			System.arraycopy(cellElongation, 0, cellDataElongationConstrained[1], 0, cellsFound);

			// Plot the YAP data
			final ChartPanel meanIntensityPlot = NucleiDataVisualiser.plotData(cellDataConstrained, colours, "Migration distance vs nucleus_to_cell mean YAP ratio", "Migration distance",
					"Nucleus to cell mean YAP ratio");
			final ChartPanel medianIntensityPlot = NucleiDataVisualiser.plotData(cellDataMedianConstrained, colours, "Migration distance vs nucleus_to_cell median YAP ratio", "Migration distance",
					"Nucleus to cell median YAP ratio");
			final ChartPanel elongatioPlot = NucleiDataVisualiser.plotData(cellDataElongationConstrained, colours, "Migration distance vs elongation measure", "Migration distance",
					"Nuclear elongation");
			if (aWorkingDirectory != null)
			{
				String imageNameForResultImage = "\\MeanYAPNucVSCellPlot.jpg";
				final File meanOutputFile = new File(NucleiSegmentationParameters.getResultsDir(aWorkingDirectory) + imageNameForResultImage);
				imageNameForResultImage = "\\MedianYAPNucVSCellPlot.jpg";
				final File medianOutputFile = new File(NucleiSegmentationParameters.getResultsDir(aWorkingDirectory) + imageNameForResultImage);
				imageNameForResultImage = "\\ElongatioPlot.jpg";
				final File elongatioOutputFile = new File(NucleiSegmentationParameters.getResultsDir(aWorkingDirectory) + imageNameForResultImage);
				try
				{
					ChartUtils.saveChartAsJPEG(meanOutputFile, meanIntensityPlot.getChart(), meanIntensityPlot.getWidth(), meanIntensityPlot.getHeight());
					ChartUtils.saveChartAsJPEG(medianOutputFile, medianIntensityPlot.getChart(), medianIntensityPlot.getWidth(), medianIntensityPlot.getHeight());
					ChartUtils.saveChartAsJPEG(elongatioOutputFile, elongatioPlot.getChart(), elongatioPlot.getWidth(), elongatioPlot.getHeight());
				}
				catch (final IOException ioe)
				{
					IJ.handleException(ioe);
				}
			}
		}
	}


	public static ImagePlus drawYAPRatio(final Cell3D[] aCells, final ImagePlus aOutlineImage, final int aNucleusYAPIndex, final int aCellYAPIndex)
	{
		final ImagePlus slidingScaleImage = aOutlineImage.duplicate();
		final int SCALE_BOTTOM = 55;
		final int SCALE_STEP = 25;
		final ImageStack outlineStack = aOutlineImage.getImageStack();
		final ImageStack slidingScaleImageStack = slidingScaleImage.getImageStack();

		for (int k = 0; k < aCells.length; k++)
		{
			final Cell3D cell = aCells[k];
			final Nucleus3D nucleus = aCells[k].getNucleus();

			final double distance = cell.getNucleus().getDistanceToCore();
			if (distance > 0 && !cell.getNucleus().isBorderNucleus())
			{
				final SegmentMeasurements nucleusMeasure = cell.getSignalMeasurements().get(aNucleusYAPIndex);
				final SegmentMeasurements cellMeasure = cell.getSignalMeasurements().get(aCellYAPIndex);
				final double cellYapValue = Math.max(cellMeasure.getMeasurement(SegmentMeasurements.MEAN_INTENSITY), 1); // Use 1 as a min value and let the image work with that
				final double nucleusYapValue = Math.max(nucleusMeasure.getMeasurement(SegmentMeasurements.MEAN_INTENSITY), 1); // Use 1 as a min value and let the image work with that
				double yapRatio = nucleusYapValue / cellYapValue;
				int yapColour;
				// if (yapRatio > 1 && yapRatio <= 2)
				// {
				// yapColour = SCALE_BOTTOM + 4 * SCALE_STEP;
				// }
				// else if (yapRatio > 2 && yapRatio <= 3)
				// {
				// yapColour = SCALE_BOTTOM + 5 * SCALE_STEP;
				// }
				// else if (yapRatio > 3 && yapRatio <= 4)
				// {
				// yapColour = SCALE_BOTTOM + 6 * SCALE_STEP;
				// }
				// else if (yapRatio > 4 && yapRatio <= 5)
				// {
				// yapColour = SCALE_BOTTOM + 7 * SCALE_STEP;
				// }
				// else if (yapRatio > 5)
				// {
				// yapColour = SCALE_BOTTOM + 8 * SCALE_STEP;
				// }
				// else if (yapRatio > 0.5 && yapRatio <= 1)
				// {
				// yapColour = SCALE_BOTTOM + 3 * SCALE_STEP;
				// }
				// else if (yapRatio > 0.33 && yapRatio <= 0.5)
				// {
				// yapColour = SCALE_BOTTOM + 2 * SCALE_STEP;
				// }
				// else if (yapRatio > 0.25 && yapRatio <= 0.33)
				// {
				// yapColour = SCALE_BOTTOM + 1 * SCALE_STEP;
				// }
				// else // if (yapRatio <= 0.25)
				// {
				// yapColour = SCALE_BOTTOM + 0 * SCALE_STEP;
				// }

				// final int yapRatioLimited = (int) Math.min(yapRatio, 5);
				// yapColour = 50 * yapRatioLimited;
				// Visualiser.draw3DNucleus(outlineStack, yapColour, cell.getNucleusSurroundings());

				if (yapRatio > 1)
				{
					yapRatio = Math.min(yapRatio, 5);
					yapColour = (int) Math.round(105 + (yapRatio * 25));
				}
				else
				{
					yapRatio = Math.min(1 / yapRatio, 5);
					yapColour = (int) Math.round(155 - ((1 / yapRatio) * 25));
				}

				Visualiser.draw3DNucleus(slidingScaleImageStack, yapColour, cell.getNucleusSurroundings());

				// final Coordinates nucSeed = nucleus.getSeed();
				// aOutlineImage.setSlice((int) (nucSeed.getZcoordinate() + 1));
				// aOutlineImage.setRoi(new OvalRoi(nucSeed.getXcoordinate() - 1, nucSeed.getYcoordinate() - 1, 3, 3));
				// final Roi roi = aOutlineImage.getRoi();
				// final ImageProcessor proc = outlineStack.getProcessor((int) (nucSeed.getZcoordinate() + 1));
				// proc.setColor(yapColour);
				// proc.draw(roi);

				// NucleiDataVisualiser.drawSphere(new Nucleus(nucleus.getSeed()), outlineStack, 3, yapColour.getRGB(), false);
			}
		}
		slidingScaleImage.setTitle("Sliding scale nucleus surroundings");
		slidingScaleImage.updateAndDraw();
		slidingScaleImage.show();
		// aOutlineImage.setTitle("Categorized nucleus surroundings");
		// aOutlineImage.updateAndDraw();
		// aOutlineImage.show();

		return slidingScaleImage;
	}
}
