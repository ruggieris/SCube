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

package scube.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import com.googlecode.javaewah.datastructure.BitSet;

/** Table as BitSets of attributes. */
public class BitTable {
	/** Attributes. */
	public BitSet[] atts;
	/** Rows. */
	public int nrows;

	/** Constructor. */
	public BitTable(int ncols, int nrows) {
		atts = new BitSet[ncols];
		this.nrows = nrows;
	}

	/** Load table from FIMI file. */
	public void loadFromFimi(String fileName) throws IOException {
		loadFromFimi(fileName, " ");
	}

	/** Load table from FIMI file. */
	public void loadFromFimi(String fileName, String sep) throws IOException {
		for (int i = 0; i < atts.length; ++i)
			atts[i] = new BitSet(nrows);
		BufferedReader in = IOUtil.getReader(fileName);
		String[] line;
		int pos = 0;
		while ((line = IOUtil.readTokens(in, sep)) != null) {
			for (int i = 0; i < line.length; ++i) {
				int item = Integer.parseInt(line[i]);
				atts[item].set(pos);
			}
			++pos;
		}
		in.close();
	}

	/** Compute support of an array of columns (items). */
	public int supp(int[] items) {
		int n = items.length;
		if (n == 0)
			return nrows;
		if (n == 1)
			return atts[items[0]].cardinality();
		if (n == 2)
			return atts[items[0]].andcardinality(atts[items[1]]);
		BitSet b = atts[items[0]].clone();
		int n1 = n - 1;
		for (int i = 1; i < n1; ++i)
			b.and(atts[items[i]]);
		return b.andcardinality(atts[items[n1]]);
	}

	/** Compute cover of an array of columns (items). */
	public BitSet cover(int[] items) {
		int n = items.length;
		if (n == 0) {
			BitSet b = new BitSet(nrows);
			b.set(0, nrows);
			return b;
		}
		BitSet b = atts[items[0]].clone();
		for (int i = 1; i < n; ++i)
			b.and(atts[items[i]]);
		return b;
	}

	/** Compute support of an ArrayList of columns (items). */
	public int supp(List<Integer> items) {
		int n = items.size();
		if (n == 0)
			return nrows;;
		if (n == 1)
			return atts[items.get(0)].cardinality();
		if (n == 2)
			return atts[items.get(0)].andcardinality(atts[items.get(1)]);
		BitSet b = atts[items.get(0)].clone();
		int n1 = n - 1;
		for (int i = 1; i < n1; ++i)
			b.and(atts[items.get(i)]);
		return b.andcardinality(atts[items.get(n1)]);
	}

	/** Compute cover of an ArrayList of columns (items). */
	public BitSet cover(List<Integer> items) {
		int n = items.size();
		if (n == 0) {
			BitSet b = new BitSet(nrows);
			b.set(0, nrows);
			return b;
		}
		BitSet b = atts[items.get(0)].clone();
		for (int i = 1; i < n; ++i)
			b.and(atts[items.get(i)]);
		return b;
	}

	/** Compute cover of an ArrayList of columns (items). */
	public BitSet cover(BitSet base, List<Integer> items) {
		BitSet b = base.clone();
		int n = items.size();
		if (n == 0) 
			return b;
		for (int it : items)
			b.and(atts[it]);
		return b;
	}

	/** Compute cover of an ArrayList of columns (items) with exception. */
	public BitSet cover(BitSet base, List<Integer> items, HashSet<Integer> exclude) {
		BitSet b = base.clone();
		int n = items.size();
		if (n == 0) 
			return b;
		for (Integer it : items)
			if(!exclude.contains(it))
				b.and(atts[it]);
		return b;
	}

	public BitSet cover(BitSet base, int item) {
		BitSet b = base.clone();
		b.and(atts[item]);
		return b;
	}

	public void andCover(BitSet base, int item) {
		base.and(atts[item]);
	}

}
