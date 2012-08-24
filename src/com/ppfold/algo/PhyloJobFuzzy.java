package com.ppfold.algo;

import java.util.ArrayList;
import java.util.List;

public class PhyloJobFuzzy {

	FuzzyAlignment fuzzyAlignment;

	public PhyloJobFuzzy(FuzzyAlignment fuzzyAlignment)
	{
		this.fuzzyAlignment = fuzzyAlignment;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3916558259505621364L;

	public List<FuzzyNucleotide[]> columns = new ArrayList<FuzzyNucleotide[]>(); // single pairs only
	public List<FuzzyNucleotide[]> columns2 = new ArrayList<FuzzyNucleotide[]>(); // pairing columns

	public List<Integer> columnIndices = new ArrayList<Integer>(); // single pairs only
	public List<Integer> columnIndices2 = new ArrayList<Integer>(); // pairing columns

	public Tree tree;

	public List<String> names = new ArrayList<String>();
	public int startcol; // starting column number
	public int endcol; // end column number (the last one included)
	boolean type; // true = double-column job; false = single-column job
	boolean finished = false;
	public int jobid; // identifier
	public int size;
	Parameters param;

	@Override
	public String toString() {
		return "" + jobid + (isType() ? "(double-column)" : " (single-column)");
	}

	public long getMemoryRequirement() {
		int totaluse = tree.numberOfNodes()
				* (12 + 16 * 4 + 12 + 16 * 12 + 16 * 16 * 4 + 3 * 4 + 100 + 4); // tree
		// nodes
		totaluse += 8; // tree overhead
		totaluse += columns.size() == 0 ? 0 : (columns.get(0).length
				* columns.size() * 2 + 12); // columns
		totaluse += columns2.size() == 0 ? 0 : (columns2.get(0).length
				* columns2.size() * 2 + 12); // columns2
		totaluse += 3000; // names; assuming this approximation irrespective of
		// content
		totaluse += 13000; // parameters = 13 kB;
		totaluse += 16; // small things
		totaluse += columns.size() == 0 ? 0 : (columns2.size() == 0 ? columns
				.size() * 8 + 12 : columns.size() * (columns2.size() + 1) * 8
				+ 12); // processing space for results.
		totaluse *= 1.25; // add 25% extra for safety
		return totaluse;
	}

	public long getDataTransportRequirement() {
		int totaluse = tree.numberOfNodes()
				* (12 + 16 * 4 + 12 + 16 * 12 + 16 * 16 * 4 + 3 * 4 + 100 + 4); // tree
		// nodes
		totaluse += 8; // tree overhead
		totaluse += columns.size() == 0 ? 0 : (columns.get(0).length
				* columns.size() * 2 + 12); // columns
		totaluse += columns2.size() == 0 ? 0 : (columns2.get(0).length
				* columns2.size() * 2 + 12); // columns2
		totaluse += 3000; // names; assuming this approximation irrespective of
		// content
		totaluse += 13000; // parameters = 13 kB;
		totaluse += 16; // small things
		return totaluse;
	}

	public long getResultTransportRequirement() {
		int totaluse = 0;
		totaluse += columns.size() == 0 ? 0 : (columns2.size() == 0 ? columns
				.size() * 8 + 12 : columns.size() * (columns2.size() + 1) * 8
				+ 12); // storing the probmatrix.
		return totaluse;
	}

	public long getExecutionTimeEstimate() {
		// scaling does not work (much faster in reality than given here)
		if (isType())
			return pairs(columns.size(), columns2.size())
					* columns.get(0).length / 100000;
		else
			return columns.size() * columns.get(0).length / 100000;
	}

	static int pairs(int a, int b) {
		int answer = 0;
		for (int c1 = 0; c1 < a; c1++) {
			for (int c2 = c1 + 1; c2 < b; c2++) {
				answer++;
			}
		}
		return answer;
	}

	public boolean isType() {
		return type;
	}
}