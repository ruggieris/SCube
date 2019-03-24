/*    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *    ------------------------------------------------------------------------
 *    Copyright (C) 2013-
 *    Salvatore Ruggieri
 *    Dipartimento di Informatica, Universita' di Pisa
 *    Pisa, Italy
 *    ------------------------------------------------------------------------
 *
 */

package scube.fim;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import scube.Options;
import scube.utils.IOUtil;

/** Table for Discrimination Discovery analyses. */
public class FIM {
	/** Dataset base-name. */
	public String dataset;
	/** Decode itemsets. */
	public Decode dec;

	/** Reader of closed itemsets. */
	private BufferedReader closedin;

	private List<Integer> attSet;
	long attSubSet;
	/** How many to return in the cartesian product. */
	private int itemSubSet;
	private long maxAttSubSet;
	/** Cartesian product. */
	private List<List<Integer>> itemList;
	private int nrows;

	/** Constructor. */
	public FIM(String dataset, String ignoreColumns) throws IOException {
		if (dataset.endsWith(".arff"))
			this.dataset = dataset.substring(0, dataset.length() - 5);
		else if (dataset.endsWith(".csv"))
			this.dataset = dataset.substring(0, dataset.length() - 4);
		else if (dataset.endsWith(".gz"))
			this.dataset = dataset.substring(0, dataset.length() - 3);
		else
			this.dataset = dataset;
		this.nrows = CSV2FIMI.createFIMI(dataset, this.dataset + ".fimi ", this.dataset + ".decode", ignoreColumns);
		this.dec = new Decode(this.dataset + ".decode");
	}
	
	public String getFimiFile() {
		return this.dataset + ".fimi";
	}

	/** Number of attributes in the dataset. */
	public int getNoAtts() {
		return dec.getNoAtts();
	}

	/** Number of attributes in the dataset. */
	public int getNoRows() {
		return nrows;
	}

	/**
	 * Extract closed itemsets (not required if extracted items already on disk).
	 */
	public void extractItemsets(String pathExecubleFile, String excludeAtts, int minSupp, boolean closedItemset, boolean exclude)
			throws IOException, InterruptedException {
		int nc = dec.numberOfCodes();
		String datafile = this.dataset + ".fimi";
		if(!excludeAtts.equals("")) {
			TreeSet<Integer> excludeAttsNo = new TreeSet<Integer>();
			for (String s : excludeAtts.split(Options.getDelimiter()))
				excludeAttsNo.add(dec.getAttNo(s));
			for (int i = 0; i < nc; ++i) {
				int att = dec.getAttribute(i);
				dec.setRemoved(i, excludeAttsNo.contains(att) == exclude);
			}
			datafile += ".pruned";
			dec.removeItems(this.dataset + ".fimi", datafile);
		}
		// run FPGrowth
		/*
		 * "An Implementation of the FP-growth Algorithm - Christian Borgelt"
		 * http://www.borgelt.net/doc/fpgrowth/fpgrowth.html
		 */
		// String cmd = pathExecubleFile + (closedItemset ? " -tc" : "") + "
		// -s-" + minSupp + " -m0 " + temp + " " + this.dataset + ".patt";
		String cmd = pathExecubleFile + (closedItemset ? " -tc" : "") + " -s-" + minSupp + " -m0 " + datafile + " " + this.dataset + ".patt";
		System.out.println("Execution of external Fpgrowth program for FIMI:");
		System.out.println(cmd);
		Runtime run = Runtime.getRuntime();
		Process p = run.exec(cmd);
		BufferedReader bis = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		String lines;
		while ((lines = bis.readLine()) != null) {
			System.out.println(lines);
		}
		bis.close();
		p.waitFor();
		if(!excludeAtts.equals("")) {
			File f = new File(datafile);
			f.delete();
		}
	}

	/** Start a scan of frequent (closed) itemsets. */
	public void initScanFrequent() throws IOException {
		// process closed itemsets
		if (closedin != null)
			closedin.close();
		closedin = IOUtil.getReader(this.dataset + ".patt");
	}

	/** Return a new object with the next contingency table, if any. */
	public Itemset nextFrequent() throws Exception {
		String line;
		if ((line = closedin.readLine()) == null)
			return null;
		return Itemset.parse(line);
	}

	/** End action of a scan. */
	public void endScanFrequent() throws IOException {
		// closed itemsets
		if (closedin != null)
			closedin.close();
	}

	/**
	 * Extract closed itemsets (not required if extracted items already on
	 * disk).
	 */
	public void initScanSubsets(String includeAtts) {
		attSet = new ArrayList<Integer>();
		for (String s : includeAtts.split(Options.getDelimiter()))
			attSet.add(dec.getAttNo(s));
		if (attSet.size() > 63)
			throw new RuntimeException("too many attributes");
		attSubSet = 0;
		maxAttSubSet = (attSet.size() == 63) ? Long.MAX_VALUE : ((1 << attSet.size()) - 1);
		itemSubSet = 0;
	}

	/**
	 * Extract closed itemsets (not required if extracted items already on
	 * disk).
	 */
	public void initScanSubsets(List<String> includeAtts) {
		attSet = new ArrayList<Integer>();
		for (String s : includeAtts)
			attSet.add(dec.getAttNo(s));
		if (attSet.size() > 63)
			throw new RuntimeException("too many attributes");
		attSubSet = 0;
		maxAttSubSet = (attSet.size() == 63) ? Long.MAX_VALUE : ((1 << attSet.size()) - 1);
		itemSubSet = 0;
	}

	/** Return a new object with the next contingency table, if any. */
	public List<Integer> nextSubset() throws Exception {
		if (itemSubSet == 0) {
			if (attSubSet > maxAttSubSet)
				return null;
			// from attSubSet get all att numbers, then join all their values
			itemList = new ArrayList<List<Integer>>();
			cartesianProduct(0, new ArrayList<Integer>());
			itemSubSet = itemList.size();
			// System.out.println("size = " + itemSubSet);
			++attSubSet;
		}
		return itemList.get(--itemSubSet);
	}

	private void cartesianProduct(int bit, ArrayList<Integer> tuple) {
		if (bit == attSet.size()) {
			itemList.add(tuple);
			return;
		}
		boolean bitVal = (attSubSet & (1 << bit)) != 0;
		if (!bitVal) {
			cartesianProduct(bit + 1, tuple);
			return;
		}
		for (Integer item : dec.getAllItems(attSet.get(bit))) {
			ArrayList<Integer> tupleIn = new ArrayList<Integer>(tuple);
			tupleIn.add(item);
			cartesianProduct(bit + 1, tupleIn);
		}
	}

}
