package dataanalyser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import data.Coordinates;
import data.NucleiSegmentationParameters;
import ij.IJ;
import ij.measure.CurveFitter;
import ij.plugin.PlugIn;
import ij.plugin.frame.Fitter;

public class Data_Aggregator_Backup implements PlugIn
{
	private class CellInfo
	{
		public int label;
		public double migDist;
		public double yapNucleus;
		public double yapCyto;
		public Coordinates coords;
		public String workDir;


		public CellInfo(final int aLabel)
		{
			this.label = aLabel;
		}


		public double getRatio()
		{
			return this.yapNucleus / this.yapCyto;
		}
	}


	private void handleResultsFiles(final File[] aWorkingDirs, final String aTitle)
	{
		final ArrayList<CellInfo> cells = new ArrayList<>();
		for (final File workDir : aWorkingDirs)
		{
			final Map<Integer, CellInfo> fileCells = new HashMap<>();
			final File resultsDir = NucleiSegmentationParameters.getResultsDir(workDir);
			if (resultsDir.exists())
			{
				final File[] resultsFiles = resultsDir.listFiles();
				File cellFile = null;
				File nucleusFile = null;
				for (final File resultsFile : resultsFiles)
				{
					if (resultsFile.getName().contains("CellFeature"))
					{
						cellFile = resultsFile;
						if (nucleusFile != null)
						{
							break;
						}
					}
					else if (resultsFile.getName().contains("NucleusFeatures"))
					{
						nucleusFile = resultsFile;
						if (cellFile != null)
						{
							break;
						}
					}
				}

				if (nucleusFile != null)
				{
					try
					{
						final FileReader fileReader = new FileReader(nucleusFile);
						final BufferedReader br = new BufferedReader(fileReader);
						String line = br.readLine();
						final String splitter = "\t";
						// Read lines until they run out
						while ((line = br.readLine()) != null)
						{
							// Split line into columns
							final String[] columns = line.split(splitter);
							if (columns[6].equals("false")) // Border nucleus?
							{
								final CellInfo cell = new CellInfo(Integer.parseInt(columns[0]));
								cell.migDist = Double.parseDouble(columns[8]);
								cell.coords = new Coordinates(Integer.parseInt(columns[1]), Integer.parseInt(columns[2]), Integer.parseInt(columns[3]));
								cell.workDir = workDir.getName();
								if (cell.migDist > 0)
								{
									fileCells.put(cell.label, cell);
								}
							}
						}
						br.close();
					}
					catch (final IOException ioe)
					{
						IJ.handleException(ioe);
					}
					catch (final NumberFormatException nfe)
					{
						IJ.handleException(nfe);
					}
				}

				if (cellFile != null)
				{
					try
					{
						final FileReader fileReader = new FileReader(cellFile);
						final BufferedReader br = new BufferedReader(fileReader);
						String line = br.readLine();
						final String splitter = "\t";
						// Read lines until they run out
						while ((line = br.readLine()) != null)
						{
							// Split line into columns
							final String[] columns = line.split(splitter);
							if (fileCells.containsKey(Integer.parseInt(columns[0])))
							{
								final CellInfo cell = fileCells.get(Integer.parseInt(columns[0]));
								cell.yapNucleus = Double.parseDouble(columns[10]);
								cell.yapCyto = Double.parseDouble(columns[19]);
							}
						}
						br.close();
					}
					catch (final IOException ioe)
					{
						IJ.handleException(ioe);
					}
					catch (final NumberFormatException nfe)
					{
						IJ.handleException(nfe);
					}
				}

				if (nucleusFile != null && cellFile != null)
				{
					cells.addAll(fileCells.values());
				}
			}
		}

		final double[] migd = new double[cells.size()];
		final double[] yapr = new double[cells.size()];
		final double[] migl1 = new double[cells.size()];
		final double[] yapl1 = new double[cells.size()];
		final double[] migg1 = new double[cells.size()];
		final double[] yapg1 = new double[cells.size()];
		final double[] yapn = new double[cells.size()];
		final double[] yapc = new double[cells.size()];

		int index = 0;
		int indexl1 = 0;
		int indexg1 = 0;
		for (final CellInfo cell : cells)
		{
			if (cell.yapNucleus > 2 && cell.yapCyto > 2)
			{
				migd[index] = cell.migDist;
				final double ratio = cell.getRatio();
				yapr[index] = ratio;
				if (ratio <= 1)
				{
					migl1[indexl1] = cell.migDist;
					yapl1[indexl1] = ratio;
					indexl1++;
				}
				else
				{
					migg1[indexg1] = cell.migDist;
					yapg1[indexg1] = ratio;
					indexg1++;
				}

				yapn[index] = cell.yapNucleus;
				yapc[index] = cell.yapCyto;
				if (cell.yapCyto > 256)
				{
					IJ.log(cell.workDir + " : " + cell.label + " : " + cell.yapCyto);
				}
				index++;
			}
		}

		final double[] migs = new double[index];
		final double[] yaps = new double[index];
		final double[] migsl1 = new double[index];
		final double[] yapsl1 = new double[index];
		final double[] migsg1 = new double[index];
		final double[] yapsg1 = new double[index];
		final double[] yapsn = new double[index];
		final double[] yapsc = new double[index];
		for (int i = 0; i < index; i++)
		{
			migs[i] = migd[i];
			yaps[i] = yapr[i];
			yapsn[i] = yapn[i];
			yapsc[i] = yapc[i];
		}

		for (int i = 0; i < indexl1; i++)
		{
			migsl1[i] = migl1[i];
			yapsl1[i] = yapl1[i];
		}

		for (int i = 0; i < indexg1; i++)
		{
			migsg1[i] = migg1[i];
			yapsg1[i] = yapg1[i];
		}

		CurveFitter fitter = new CurveFitter(migs, yaps);
		fitter.doFit(CurveFitter.STRAIGHT_LINE);
		Fitter.plot(fitter);
		IJ.getImage().setTitle(aTitle + " ratio");

		fitter = new CurveFitter(migsl1, yapsl1);
		fitter.doFit(CurveFitter.STRAIGHT_LINE);
		Fitter.plot(fitter);
		IJ.getImage().setTitle(aTitle + " less than 1 ratio");

		fitter = new CurveFitter(migsg1, yapsg1);
		fitter.doFit(CurveFitter.STRAIGHT_LINE);
		Fitter.plot(fitter);
		IJ.getImage().setTitle(aTitle + " greater than 1 ratio");

		fitter = new CurveFitter(migs, yapsn);
		fitter.doFit(CurveFitter.STRAIGHT_LINE);
		Fitter.plot(fitter);
		IJ.getImage().setTitle(aTitle + " nucleus");

		fitter = new CurveFitter(migs, yapsc);
		fitter.doFit(CurveFitter.STRAIGHT_LINE);
		Fitter.plot(fitter);
		IJ.getImage().setTitle(aTitle + " cytoplasm");
	}


	@Override
	public void run(final String arg)
	{
		final String mainDirName = IJ.getDir("Please select a cell line directory");

		// Do 2.5
		File collagenDir = new File(mainDirName + "2.5");
		File[] workingDirs = collagenDir.listFiles();
		handleResultsFiles(workingDirs, "2.5");

		// Do 6.0
		collagenDir = new File(mainDirName + "6.0");
		workingDirs = collagenDir.listFiles();
		handleResultsFiles(workingDirs, "6.0");
	}
}
