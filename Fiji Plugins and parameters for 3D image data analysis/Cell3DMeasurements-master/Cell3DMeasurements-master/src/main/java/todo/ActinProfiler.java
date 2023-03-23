// package todo;
//
// import java.awt.Color;
// import java.util.List;
//
// import FeatureExtractor.Visualiser;
// import MigrationModeAnalysis.Measurer;
// import MigrationModeAnalysis.Migration_Mode_Analysis;
// import data.Cell3D;
// import data.Cell3D_Group;
// import data.Cell3D_Pair;
// import ij.ImagePlus;
// import ij.gui.GenericDialog;
// import ij.measure.Calibration;
// import ij.measure.ResultsTable;
// import ij.process.ImageConverter;
//
// public class ActinProfiler extends Migration_Mode_Analysis
// {
//
// private final ImagePlus images;
// private int actinChannel;
// private final ImagePlus actinImage;
//
// private double distance;
// private double threshold;
// private double minNoActinDistance;
// private double sizeLine;
// private boolean measureActinSignal;
//
//
// public ActinProfiler(final ImagePlus aImages, final ImagePlus aOriginalImage, final ImagePlus aActinImage, final ImagePlus aDAPIImage, final Cell3D_Group aNucleus, final double aDistance,
// final double aThrehsold, final double aMinNoActinDistance, final double aSizeLine, final boolean aMeasureActinSignal, final String aImageTitle)
// {
// super(aOriginalImage, aDAPIImage, aNucleus, aImageTitle);
// this.images = aImages;
// this.actinImage = aActinImage;
// this.distance = aDistance;
// this.threshold = aThrehsold;
// this.minNoActinDistance = aMinNoActinDistance;
// this.sizeLine = aSizeLine;
// this.measureActinSignal = aMeasureActinSignal;
// }
//
//
// /**
// * Method addNeighbourPairToGroup
// *
// * @param nucleusPair
// * @param nucleusGroups
// * @return
// */
// private Boolean addNeighbourPairToGroup(final Cell3D_Pair nucleusPair, final List<Cell3D_Group> nucleusGroups)
// {
// /**
// * Example 1: Group 1: Nucleus 10 and 11
// *
// * Nucleus pair: nucleus 10 and 12 - Candidate groups are: Group 1 (because nucleus 10 is in this group) Results of this pair: CandidateGroup count = 1 CandidateGroupNucleus2count (Nucleus 12) = 1 (group 1)
// *
// * Example 2: Group 1: Nucleus 10 and 11 Group 2: Nucleus 12 and 13
// *
// * Nucleus pair: nucleus 10 and 12 - Candidate groups are: Group 1 (because nucleus 10 is in this group) Group 2 (because nucleus 12 is in this group) Results of this pair: CandidateGroup count = 2 --> Candidate groups: <1, 2>
// * CandidateGroupNucleus1count (Nucleus 10) = 1 Candidate groups: <2> CandidateGroupNucleus2count (Nucleus 12) = 1 Candidate groups: <1>
// *
// */
// // Check if nucleus pair members are present in any of the groups
// Boolean added = checkNucleusPairIsPresentInGroup(nucleusPair, nucleusGroups);
//
// // If both pair members are not in the same group
// if (added == false)
// {
// // When there is one candidate group for both nucleus
// // Check which nucleus is already in this group and add the other nucleus to this group
// if (nucleusPair.getCandidateGroupsCount() == 1) // Example 1
// {
// if (nucleusPair.getCandidateGroupsNucleus1Count() == 1)
// {
// nucleusGroups.get(nucleusPair.getCandidateGroupNucleus1(0)).setMember(nucleusPair.getCell1());
// added = true;
// }
// else if (nucleusPair.getCandidateGroupsNucleus2Count() == 1)
// {
// nucleusGroups.get(nucleusPair.getCandidateGroupNucleus2(0)).setMember(nucleusPair.getCell2());
// added = true;
// }
// }
// // When there is TWO candidate group for both nucleus
// else if (nucleusPair.getCandidateGroupsCount() >= 2) // Example 2
// {
// // Check which nucleus is grouped in the highest ranked nucleusGroup
// if (nucleusPair.getCandidateGroupNucleus1(0) == nucleusPair.getCandidateGroup(0))
// {
// // Add nucleus 12 to group 1 (example 2)
// nucleusGroups.get(nucleusPair.getCandidateGroup(0)).setMember(nucleusPair.getCell1());
// added = true;
// // Copy and delete members of group 2 (example2)
// for (int h = 0; h < nucleusPair.getCandidateGroupsNucleus2Count(); h++)
// {
// // nucleusGroups.get(nucleusPair.getCandidateGroup(0)).setMultipleLabelPair(nucleusGroups.get(nucleusPair.getCandidateGroupNucleus2(h)).getLabelPair());
// for (int k = 0; k < nucleusGroups.get(nucleusPair.getCandidateGroupNucleus2(h)).getMemberCount(); k++)
// {
// final Cell3D potentialmember = nucleusGroups.get(nucleusPair.getCandidateGroupNucleus2(h)).getMember(k);
//
// if (nucleusGroups.get(nucleusPair.getCandidateGroup(0)).getPresenceOfMember(potentialmember) == false)
// {
// // Set nucleus 13 to group 1 (example 2)
// nucleusGroups.get(nucleusPair.getCandidateGroup(0)).setMember(potentialmember);
// }
// }
// nucleusGroups.get(nucleusPair.getCandidateGroupNucleus2(h)).deleleteMembers();
// }
// }
// if (nucleusPair.getCandidateGroupNucleus2(0) == nucleusPair.getCandidateGroup(0))
// {
// // Add nucleus to the first group
// // nucleusGroups.get(nucleusPair.getCandidateGroup(0)).setMember(nucleusPair.getNucleus2(),nucleusPair.getNucleus1(), aCloseNeighbour, aActin);
// added = true;
//
// // Copy and delete members of the second group
// for (int h = 0; h < nucleusPair.getCandidateGroupsNucleus1Count(); h++)
// {
// // nucleusGroups.get(nucleusPair.getCandidateGroup(0)).setMultipleLabelPair(nucleusGroups.get(nucleusPair.getCandidateGroupNucleus2(h)).getLabelPair());
// for (int k = 0; k < nucleusGroups.get(nucleusPair.getCandidateGroupNucleus1(h)).getMemberCount(); k++)
// {
// final Cell3D potentialmember = nucleusGroups.get(nucleusPair.getCandidateGroupNucleus1(h)).getMember(k);
// if (nucleusGroups.get(nucleusPair.getCandidateGroup(0)).getPresenceOfMember(potentialmember) == false)
// {
// nucleusGroups.get(nucleusPair.getCandidateGroup(0)).setMember(potentialmember);
// }
// }
// nucleusGroups.get(nucleusPair.getCandidateGroupNucleus1(h)).deleleteMembers();
// }
// }
// }
//
// }
// return added;
// }
//
//
// private Boolean checkNucleusPairIsPresentInGroup(final Cell3D_Pair nucleusPair, final List<Cell3D_Group> nucleusGroups)
// {
// // Check if nucleus pair members are present in any of the groups
// // if pair member is present in group the candidate group name is added to a list
// // There are three candidate group list, for nucleus 1, nucleus 2 and for both nucleus
// Boolean added = false;
// for (int g = 0; g < nucleusGroups.size(); g++)
// {
// // If both nucleus are in the same group the nucleus pair is already added to a group
// if (nucleusGroups.get(g).getPresenceOfMember(nucleusPair.getCell1()) == true)
// {
// if (nucleusGroups.get(g).getPresenceOfMember(nucleusPair.getCell2()) == true)
// {
// added = true;
// }
// // If nucleus 2 is not in the same group as nucleus 1
// // create a candidate group for nucleus 2
// else if (nucleusGroups.get(g).getPresenceOfMember(nucleusPair.getCell2()) == false)
// {
// nucleusPair.setCandidategroupNucleus2(g);
// }
// }
// // If nucleus 1 is not in the same group as nucleus 2
// // Create a candidate group for nucleus 1
// else if (nucleusGroups.get(g).getPresenceOfMember(nucleusPair.getCell1()) == false)
// {
// if (nucleusGroups.get(g).getPresenceOfMember(nucleusPair.getCell2()) == true)
// {
// nucleusPair.setCandidategroupNucleus1(g);
// }
// }
// }
// return added;
// }
//
//
// private void chooseActinProfileAnalysisParameters()
// {
// final GenericDialog gd = new GenericDialog("Settings for the actin profile analysis");
// gd.addCheckbox("Analyse actin signal", true);
// gd.addNumericField("Set the Actin channel", 1, 0);
// gd.addNumericField("Set the distance between nuclei (microns)", 100, 0);
// gd.addNumericField("Set the maximum actin-background intensity threshold", 100, 0);
// gd.addNumericField("Set the 'no actin is no connection' distance (microns)", 15, 0);
// gd.addNumericField("Set the width of the profile line (pixels) ", 5, 0);
//
// gd.showDialog();
// this.measureActinSignal = gd.getNextBoolean();
// this.actinChannel = (int) gd.getNextNumber();
// this.distance = gd.getNextNumber();
// this.threshold = gd.getNextNumber();
// this.minNoActinDistance = gd.getNextNumber();
// this.sizeLine = gd.getNextNumber();
// }
//
//
// /**
// *
// * @param image2
// * @param nucleusGroupsTouchingNeighbours
// * @return
// */
// private ResultsTable groupedbasedOnActinSignal(final ImagePlus image2, final List<Cell3D_Group> nucleusGroupsTouchingNeighbours)
// {
//
// Calibration calibration;
// calibration = this.originalImage.getCalibration();
// final double pixelWidth = calibration.pixelWidth;
// final double pixelHeight = calibration.pixelHeight;
// final double pixelDepth = calibration.pixelDepth;
//
// this.sizeLine = ((int) this.sizeLine / pixelWidth) / 2;
//
// final ImagePlus drawImage = this.images.duplicate();
// final ImagePlus imageDrawActin = this.images.duplicate();
// final ImageConverter con = new ImageConverter(drawImage);
// con.convertToRGB();
// final ResultsTable res = new ResultsTable();
//
// int count = 0;
// for (int one = 0; one < this.nucleus.getMemberCount(); one++) // ;each nucleus nucleus.length
// {
// count = count + 1;
// for (int two = count; two < this.nucleus.getMemberCount(); two++) // 5 each neighbor of the nucleus
// {
// final Cell3D_Pair nucleusPair = new Cell3D_Pair(this.nucleus.getMember(one), this.nucleus.getMember(two));
// boolean added = false;
//
// if (nucleusPair.getDistance(pixelWidth, pixelHeight, pixelDepth) < this.distance)
// {
// for (int g = 0; g < nucleusGroupsTouchingNeighbours.size(); g++)
// {
// if (nucleusGroupsTouchingNeighbours.get(g).getPresenceOfMember(nucleusPair.getCell1()))
// {
// if (nucleusGroupsTouchingNeighbours.get(g).getPresenceOfMember(nucleusPair.getCell2()))
// {
// added = true;
// break;
// }
// }
// }
//
// if (!added)
// {
// // When the nucleus pair is located in a closer distance than the minNoActinDistance
// if (nucleusPair.getDistance(pixelWidth, pixelHeight, pixelDepth) < this.minNoActinDistance)
// {
// Visualiser.drawLine3D(nucleusPair.getCell1().getNucleus().getSeed(), nucleusPair.getCell2().getNucleus().getSeed(), drawImage, Color.BLUE);
// final boolean added2 = addNeighbourPairToGroup(nucleusPair, nucleusGroupsTouchingNeighbours);
//
// if (!added2)
// {
// nucleusGroupsTouchingNeighbours.add(new Cell3D_Group(nucleusPair.getCell1(), nucleusPair.getCell2(), false, true));
// }
// }
// else
// {
//
// final double distance = nucleusPair.getDistance(pixelWidth, pixelHeight, pixelDepth);
// final double[] results = Measurer.getLinePlotProfile3D(nucleusPair, this.actinImage, image2, (int) this.sizeLine, imageDrawActin, distance);
// if (results != null)
// {
// final double length = results.length;
// final double micronspersep = distance / length;
// double min = 1000;
// double max = 0;
// double underThreshold = 0;
// double maximaleRangeUnderTrheshold = 0;
// double aboveZero = 0;
//
// for (int j = 0; j < results.length; j++)
// {
// if (results[j] > max)
// {
// max = results[j];
// }
// if (results[j] < min)
// {
// if (results[j] > 0)
// {
// min = results[j];
// }
// }
// if (results[j] > 0)
// {
// aboveZero = aboveZero + 1;
// if (results[j] < this.threshold)
// {
// underThreshold = underThreshold + 1;
// if (underThreshold > maximaleRangeUnderTrheshold)
// {
// maximaleRangeUnderTrheshold = underThreshold;
// }
// }
// else
// {
// underThreshold = 0;
// }
// }
// }
// maximaleRangeUnderTrheshold = maximaleRangeUnderTrheshold * micronspersep;
// res.incrementCounter();
// res.addValue("Label nucleus 1", nucleusPair.getCell1().getNucleus().getLabel());
// res.addValue("Label nucleus 2", nucleusPair.getCell2().getNucleus().getLabel());
// res.addValue("Relation", "actin");
// res.addValue("Max value", max);
// res.addValue("Min Value", min);
// res.addValue("Maximum range", maximaleRangeUnderTrheshold);
// res.addValue("Microns per step", micronspersep);
//
// for (int j = 0; j < results.length; j++)
// {
// final String number = j + "";
// res.addValue(number, results[j]);
// }
// if (maximaleRangeUnderTrheshold < this.minNoActinDistance)
// {
// res.addValue("Relation", " Actin positive");
// Visualiser.drawLine3D(nucleusPair.getCell1().getNucleus().getSeed(), nucleusPair.getCell2().getNucleus().getSeed(), drawImage, Color.GREEN);
// final boolean added2 = addNeighbourPairToGroup(nucleusPair, nucleusGroupsTouchingNeighbours);
//
// if (added2 == false)
// {
// nucleusGroupsTouchingNeighbours.add(new Cell3D_Group(nucleusPair.getCell1(), nucleusPair.getCell2(), false, true));
// }
// }
// else
// {
// res.addValue("Relation", "Actin negative");
// Visualiser.drawLine3D(nucleusPair.getCell1().getNucleus().getSeed(), nucleusPair.getCell2().getNucleus().getSeed(), drawImage, Color.RED);
// }
// }
// }
// }
// }
// else
// {
// // res.addValue("Relation", "To Far");
// }
// }
// // }
//
// }
// drawImage.updateAndDraw();
// drawImage.setTitle("Hallo");
// drawImage.show();
// image2.updateAndDraw();
// image2.setTitle("Actin measured Lines");
// image2.show();
// res.show("Resultsss");
// return res;
// }
//
//
// @Override
// public List<Cell3D_Group> migrationModeAnalysis()
// {
// final ImagePlus image2 = this.images.duplicate();
//
// if (this.measureActinSignal)
// {
// final ResultsTable res = groupedbasedOnActinSignal(image2, nucleusGroupsTouchingNeighbours);
// nucleusGroupsFinal = removeAndSortNucleusGroups(nucleusGroupsTouchingNeighbours);
// migrationModeImage = this.dapiImage.duplicate();
// Visualiser.drawMigrationModeImage(migrationModeImage, nucleusGroupsFinal);
// migrationModeImage.setTitle("Actin grouped image");
// migrationModeImage.show();
// }
// else
// {
// nucleusGroupsFinal = nucleusGroupsTouchingNeighbours;
// }
// }
//
// }
