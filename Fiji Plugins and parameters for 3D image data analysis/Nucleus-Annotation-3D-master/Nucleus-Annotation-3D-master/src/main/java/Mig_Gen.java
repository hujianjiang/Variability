import java.util.Random;

import data.BaseNucleus.PartOf;
import ij.IJ;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class Mig_Gen implements PlugIn
{
	@Override
	public void run(final String arg)
	{

		final PartOf[] po = { PartOf.NO_PART_OF, PartOf.DUAL_CLUSTER, PartOf.SINGLE_CELL, PartOf.MULTI_CLUSTER, PartOf.STRAND, PartOf.SPHEROID };
		int spheroid_nr = -1;
		int curNum = 0;
		int collSize = 0;
		int strandSize = 0;
		int collNum = -1;
		int strandNum = -1;
		int dualNum = -1;

		final GenericDialog parametersDialog = new GenericDialog("Nr");
		parametersDialog.addNumericField("Nr", 500, 5);
		parametersDialog.showDialog();

		final int max = (int) parametersDialog.getNextNumber();

		for (int i = 0; i < max; i++)
		{
			final Random rand = new Random();
			final int r = rand.nextInt(6);
			String num;
			switch (r)
			{
			case 0:
				num = "";
				break;
			case 1:
				final int dual = dualNum == -1 ? curNum : dualNum;
				curNum = dualNum == -1 ? curNum + 1 : curNum;
				num = "" + dual;
				dualNum = dualNum == -1 ? dual : -1;
				break;
			case 2:
				num = "" + curNum;
				curNum++;
				break;
			case 3:
				if (collSize <= 0)
				{
					collNum = curNum;
					curNum++;
					collSize = rand.nextInt(9) + 2;
				}
				num = "" + collNum;
				collSize--;
				break;
			case 4:
				if (strandSize <= 0)
				{
					strandNum = curNum;
					curNum++;
					strandSize = rand.nextInt(9) + 2;
				}
				num = "" + strandNum;
				strandSize--;
				break;
			default:
				if (spheroid_nr < 0)
				{
					spheroid_nr = curNum;
					curNum++;
				}
				num = "" + spheroid_nr;
				break;
			}
			IJ.log(po[r].name() + "\t" + num);
		}
	}
}
