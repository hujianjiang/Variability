package todo;

import java.util.ArrayList;
import java.util.Set;

import data.Cell3D;

public class Cell3D_Pair implements Comparable<Cell3D_Pair>
{
	private Cell3D cell1;
	private Cell3D cell2;
	private final ArrayList<Integer> candidateGroups = new ArrayList<>();
	private final ArrayList<Integer> candidateGroupsNucleus1 = new ArrayList<>();
	private final ArrayList<Integer> candidateGroupsNucleus2 = new ArrayList<>();

	private boolean touchingNeighbors;


	public Cell3D_Pair(final Cell3D aNucleus1, final Cell3D aNucleus2)
	{
		if (aNucleus1.getNucleus().getLabel() < aNucleus2.getNucleus().getLabel())
		{
			this.cell1 = aNucleus1;
			this.cell2 = aNucleus2;
		}
		else
		{
			this.cell1 = aNucleus2;
			this.cell2 = aNucleus1;
		}

		final Set<Integer> adjList = this.cell1.getConnectedNeighbours();
		for (final Integer neighbour : adjList)
		{
			if (neighbour == this.cell2.getNucleus().getLabel())
			{
				this.touchingNeighbors = true;
				break;
			}
		}
	}


	@Override
	public int compareTo(final Cell3D_Pair pair)
	{
		if (this.cell1.getNucleus().getLabel() < pair.cell1.getNucleus().getLabel())
			return -1;
		if (this.cell1.getNucleus().getLabel() > pair.cell1.getNucleus().getLabel())
			return +1;
		if (this.cell2.getNucleus().getLabel() < pair.cell2.getNucleus().getLabel())
			return -1;
		if (this.cell2.getNucleus().getLabel() > pair.cell2.getNucleus().getLabel())
			return +1;
		return 0;
	}


	/**
	 * Method getCandidateGroup returns the name (number) of the candidate group at position (index) of the list
	 *
	 * @param index
	 * @return Name (number) of the candidateGroup
	 */
	public int getCandidateGroup(final int index)
	{
		return this.candidateGroups.get(index);
	}


	/**
	 * Method getCandidateGroupNucleus1 returns the name (number) of the candidate group at position (index) of the list
	 *
	 * @param index
	 * @return Name (number) of the candidateGroup nucleus1
	 */
	public int getCandidateGroupNucleus1(final int index)
	{
		return this.candidateGroupsNucleus1.get(index);
	}


	/**
	 * Method getCandidateGroupNucleus2 returns the name (number) of the candidate group at position (index) of the list
	 *
	 * @param index
	 * @return Name (number) of the candidateGroupNucleus2
	 */
	public int getCandidateGroupNucleus2(final int number)
	{
		return this.candidateGroupsNucleus2.get(number);
	}


	/**
	 *
	 * @return amount of candidateGroups
	 */
	public int getCandidateGroupsCount()
	{
		return this.candidateGroups.size();
	}


	/**
	 *
	 * @return amount of candidateGroups of nucleus 1
	 */
	public int getCandidateGroupsNucleus1Count()
	{
		return this.candidateGroupsNucleus1.size();
	}


	/**
	 *
	 * @return amount of candidateGroups of nucleus 2
	 */
	public int getCandidateGroupsNucleus2Count()
	{
		return this.candidateGroupsNucleus2.size();
	}


	/**
	 * @return list with candidate groups: "CandidateGroups: " + (candidateGroups) +" CandidateGroups Nucleus1: " + (candidateGroupsNucleus1) + " CandidateGroups Nucleus2: " + (candidateGroupsNucleus2)
	 */
	public String getCandidateGroupsToString()
	{
		final StringBuilder list = new StringBuilder("CandidateGroups: ");

		for (int i = 0; i < this.candidateGroups.size(); i++)
		{
			list.append(this.candidateGroups.get(i) + " ");
		}
		list.append(" CandidateGroups Nucleus1: ");

		for (int i = 0; i < this.candidateGroupsNucleus1.size(); i++)
		{
			list.append(this.candidateGroupsNucleus1.get(i) + " ");
		}
		list.append(" CandidateGroups Nucleus2: ");

		for (int i = 0; i < this.candidateGroupsNucleus2.size(); i++)
		{
			list.append(this.candidateGroupsNucleus2.get(i) + " ");
		}
		return list.toString();
	}


	/**
	 *
	 * @return Nucleus3D: nucleus1
	 */
	public Cell3D getCell1()
	{
		return this.cell1;
	}


	/**
	 *
	 * @return Nucleus3D: nucleus2
	 */
	public Cell3D getCell2()
	{
		return this.cell2;
	}


	public double getDistance(final double aPixelWidth, final double aPixelHeight, final double aPixelDepth)
	{
		return this.cell1.getNucleus().getSeed().distanceFromPoint(this.cell2.getNucleus().getSeed());
	}


	public double getMaximalNucleusLength(final double aPixelWidth, final double aPixelHeigth, final double aPixelDepth)
	{
		final double nucleus1MaximalLength = this.cell1.getNucleus().getMaximalLength(aPixelWidth, aPixelHeigth, aPixelDepth);
		final double nucleus2MaximalLength = this.cell2.getNucleus().getMaximalLength(aPixelWidth, aPixelHeigth, aPixelDepth);
		return Math.max(nucleus1MaximalLength, nucleus2MaximalLength);
	}


	/**
	 * Method setCandidategroupNucleus1 add the candidate group to variable: -candidateGroupNucleus1 -candidateGroups
	 *
	 * @param number;
	 *            the number of the group
	 */
	public void setCandidategroupNucleus1(final int number)
	{
		this.candidateGroupsNucleus1.add(number);
		this.candidateGroups.add(number);
	}


	/**
	 * Method setCandidategroupNucleus2 add the candidate group to variable: -candidateGroupNucleus2 -candidateGroups
	 *
	 * @param number;
	 *            the number of the group
	 */
	public void setCandidategroupNucleus2(final int number)
	{
		this.candidateGroupsNucleus2.add(number);
		this.candidateGroups.add(number);
	}


	public boolean touchingNeighbours()
	{
		return this.touchingNeighbors;
	}
}
