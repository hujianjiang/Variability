package dataanalyser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.NucleiSegmentationParameters;
import ij.IJ;
import ij.plugin.PlugIn;

public class Data_Aggregator implements PlugIn
{
	private class CellInfo
	{
		// public int label;
		// public double migDist;
		// public double yapNucleus;
		// public double yapCyto;
		// public Coordinates coords;
		// public String workDir;
		//
		//
		// public CellInfo(final int aLabel)
		// {
		// this.label = aLabel;
		// }
		//
		//
		// public double getRatio()
		// {
		// return this.yapNucleus / this.yapCyto;
		// }
		public int label;
		public String wellName;


		@Override
		public boolean equals(final Object aCellInfo)
		{
			return this.label == ((CellInfo) aCellInfo).label && this.wellName.equals(((CellInfo) aCellInfo).wellName);
		}


		@Override
		public int hashCode()
		{
			return this.label + this.wellName.hashCode();
		}
	}


	private double getYAPRatio(final String[] aCellData)
	{
		final double cellYapValue = Math.max(Double.parseDouble(aCellData[19]), 1); // Use 1 as a min value and let the image work with that
		final double nucleusYapValue = Math.max(Double.parseDouble(aCellData[10]), 1); // Use 1 as a min value and let the image work with that
		return nucleusYapValue / cellYapValue;
	}


	private void handleDensityDirs(final String aMainDirName, final String aCollagenDensity)
	{
		final File collagenDir = new File(aMainDirName + aCollagenDensity);
		final String mainDirName = (new File(aMainDirName)).getName();
		if (collagenDir.exists())
		{
			final File[] workingDirs = collagenDir.listFiles();
			final List<File> medYAPFiles = new ArrayList();
			final List<File> medIgGFiles = new ArrayList();
			final List<File> dmsoYAPFiles = new ArrayList();
			final List<File> dmsoIgGFiles = new ArrayList();
			final List<File> fakYAPFiles = new ArrayList();
			final List<File> fakIgGFiles = new ArrayList();

			for (final File workDir : workingDirs)
			{
				final String workName = workDir.getName();
				if (workName.contains("med") || workName.contains("Med"))
				{
					if (workName.contains("YAP"))
					{
						medYAPFiles.add(workDir);
					}
					else if (workName.contains("IgG"))
					{
						medIgGFiles.add(workDir);
					}
					else
					{
						IJ.log("Could not determine the conditions of " + workName + ". Please check spelling (YAP/IgG).");
					}
				}
				else if (workName.contains("DMSO"))
				{
					if (workName.contains("YAP"))
					{
						dmsoYAPFiles.add(workDir);
					}
					else if (workName.contains("IgG"))
					{
						dmsoIgGFiles.add(workDir);
					}
					else
					{
						IJ.log("Could not determine the conditions of " + workName + ". Please check spelling (YAP/IgG).");
					}
				}
				else if (workName.contains("FAK"))
				{
					if (workName.contains("YAP"))
					{
						fakYAPFiles.add(workDir);
					}
					else if (workName.contains("IgG"))
					{
						fakIgGFiles.add(workDir);
					}
					else
					{
						IJ.log("Could not determine the conditions of " + workName + ". Please check spelling (YAP/IgG).");
					}
				}
				else
				{
					IJ.log("Could not determine the conditions of " + workName + ". Please check spelling (med/DMSO/FAK).");
				}
			}

			if (medYAPFiles.isEmpty() || !handleResultsFiles(medYAPFiles, aMainDirName + mainDirName + "_" + aCollagenDensity + "_medium_YAP.xls"))
			{
				IJ.log("No files found for the condition medium - YAP");
			}
			if (medYAPFiles.isEmpty() || !handleResultsFiles(medIgGFiles, aMainDirName + mainDirName + "_" + aCollagenDensity + "_medium_IgG.xls"))
			{
				IJ.log("No files found for the condition medium - IgG");
			}
			if (medYAPFiles.isEmpty() || !handleResultsFiles(dmsoYAPFiles, aMainDirName + mainDirName + "_" + aCollagenDensity + "_DMSO_YAP.xls"))
			{
				IJ.log("No files found for the condition DMSO - YAP");
			}
			if (medYAPFiles.isEmpty() || !handleResultsFiles(dmsoIgGFiles, aMainDirName + mainDirName + "_" + aCollagenDensity + "_DMSO_IgG.xls"))
			{
				IJ.log("No files found for the condition DMSO - IgG");
			}
			if (medYAPFiles.isEmpty() || !handleResultsFiles(fakYAPFiles, aMainDirName + mainDirName + "_" + aCollagenDensity + "_FAKi_YAP.xls"))
			{
				IJ.log("No files found for the condition FAKi - YAP");
			}
			if (medYAPFiles.isEmpty() || !handleResultsFiles(fakIgGFiles, aMainDirName + mainDirName + "_" + aCollagenDensity + "_FAKi_IgG.xls"))
			{
				IJ.log("No files found for the condition FAKi - IgG");
			}
		}
		else
		{
			IJ.log("No " + aCollagenDensity + " folder detected. Please check the folder names.");
		}
	}


	private boolean handleResultsFiles(final List<File> aWorkingDirs, final String aTitle)
	{
		final Map<CellInfo, String[][]> fileCells = new HashMap<>();
		final String[][] headers = new String[2][];
		for (final File workDir : aWorkingDirs)
		{
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
						if (headers[0] == null)
						{
							headers[0] = line.split(splitter);
						}
						// Read lines until they run out
						while ((line = br.readLine()) != null)
						{
							// Split line into columns
							final String[] columns = line.split(splitter);
							if (columns[6].equals("false")) // Border nucleus?
							{
								final String[][] dataLines = new String[2][];
								final CellInfo cellInfo = new CellInfo();
								dataLines[0] = columns;

								cellInfo.label = Integer.parseInt(columns[0]);
								cellInfo.wellName = workDir.getName();
								fileCells.put(cellInfo, dataLines);
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
						if (headers[1] == null)
						{
							headers[1] = line.split(splitter);
						}
						// Read lines until they run out
						while ((line = br.readLine()) != null)
						{
							// Split line into columns
							final String[] columns = line.split(splitter);
							final int cellLabel = Integer.parseInt(columns[0]);
							final CellInfo cellInfo = new CellInfo();
							cellInfo.label = cellLabel;
							cellInfo.wellName = workDir.getName();
							if (fileCells.containsKey(cellInfo))
							{
								fileCells.get(cellInfo)[1] = columns;
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
			}
			else
			{
				IJ.log("No results found for well " + workDir);
			}
		}

		if (!fileCells.isEmpty())
		{
			try
			{
				final File outputFile = new File(aTitle);
				final FileWriter fileWriter = new FileWriter(outputFile);
				final BufferedWriter bufWriter = new BufferedWriter(fileWriter);

				final Set<Integer> exclusionNuc = new HashSet<>();
				final Set<Integer> exclusionCell = new HashSet<>();
				exclusionCell.add(0);
				final String headersAll = "Well name\t" + makeString(headers[0], exclusionNuc) + "\t" + makeString(headers[1], exclusionCell) + "\tYap ratio\n";
				bufWriter.write(headersAll);
				for (final CellInfo key : fileCells.keySet())
				{
					final StringBuffer buffer = new StringBuffer();
					buffer.append(key.wellName + "\t");
					final String[][] data = fileCells.get(key);
					final double ratio = getYAPRatio(data[1]);
					buffer.append(makeString(data[0], exclusionNuc) + "\t");
					buffer.append(makeString(data[1], exclusionCell) + "\t" + ratio + "\n");
					bufWriter.write(buffer.toString());
				}
				bufWriter.close();
			}
			catch (final IOException ioe)
			{
				IJ.handleException(ioe);
			}

			return true;
		}

		return false;
	}


	String makeString(final String[] aFeatureSet, final Set<Integer> aRemovedLabels)
	{
		final StringBuffer result = new StringBuffer();
		for (int i = 0; i < aFeatureSet.length; i++)
		{
			if (!aRemovedLabels.contains(i))
			{
				if (i > 0)
				{
					result.append("\t");
				}
				result.append(aFeatureSet[i]);
			}
		}

		return result.toString();
	}


	@Override
	public void run(final String arg)
	{
		final String mainDirName = IJ.getDir("Please select a cell line directory");

		IJ.log("Data aggregation started.");

		// Do 2.5
		String collagenDensity = "2.5";
		handleDensityDirs(mainDirName, collagenDensity);

		// Do 6.0
		collagenDensity = "6.0";
		handleDensityDirs(mainDirName, collagenDensity);

		IJ.log("Data aggregation ended.");
	}
}
