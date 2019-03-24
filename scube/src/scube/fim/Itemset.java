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

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

/** A set of items with its support. Items are kept in ascending order. */
public class Itemset implements Comparable<Itemset>, Serializable {
	/** Serial ID. */
	private static final long serialVersionUID = -5819566846531530506L;
	/** Itemset support. */
	private int support;
	/** Items. */
	private List<Integer> items;

	/** Constructor. */
	public Itemset(int supp) {
		support = supp;
		items = new ArrayList<Integer>();
	}

	/** Parse string format "i-1 i-2 ... i-n (supp)" or "i-1 i-2 ... i-n". */
	public static Itemset parse(String specs) {
		int bracket = specs.indexOf("(");
		String ids = bracket < 0 ? specs : specs.substring(0, bracket);
		int support = 0;
		if (bracket >= 0) {
			String meta = specs.substring(bracket + 1, specs.lastIndexOf(")"));
/*			StringTokenizer st = new StringTokenizer(meta, " ");
			support = Integer.parseInt(st.nextToken());
*/			support = Integer.parseInt(meta);
		}
		StringTokenizer st = new StringTokenizer(ids, " ");
		int nt = st.countTokens();
		Itemset itemset = new Itemset(support);
		for (int i = 0; i < nt; ++i)
			itemset.items.add(new Integer(st.nextToken()));
		Collections.sort(itemset.items);
		return itemset;
	}

	/** Number of items. */
	public int size() {
		return items.size();
	}

	/** Return items. */
	public List<Integer> getItems() {
		return items;
	}
	
	/** Selection operator. */
	public List<Integer> getFilteredItems(Predicate<Integer> c) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for(Integer i : items)
			if(c.test(i))
				result.add(i);
		return result;		
	}	

	/** Get support. */
	public int getSupport() {
		return support;
	}

	/** String representation. */
	public String toString() {
		String res = "";
		for (int i : items)
			res += i + " ";
		return res + "(" + support + ")";
	}

	/** String representation. */
	public String toString(Decode d) {
		String res = "";
		for (int i : items)
			res += d.decode(i) + " ";
		return res + "(" + support + ")";
	}
	
	public boolean isIncludedIn(Itemset o) {
		int sz = items.size();
		int osz = o.items.size();
		if(sz>osz)
			return false;
		List<Integer> oitems = o.items;
		int oi = 0;
		int i = 0;
		while(i < sz) {
			if( oi == osz )
				return false;
			int ival = items.get(i);
			int oival = oitems.get(oi);
			if( ival == oival ) {
				++i;
				++oi;
				continue;
			}
			if( ival < oival )
				return false;
			++oi;
		}
		return true;
	}
	
	public boolean include(int item) {
		return Collections.binarySearch(items, item) < 0 ? false : true;
	}

	@Override
	public int compareTo(Itemset o) {
		int sz = items.size();
		int osz = o.items.size();
		if (sz < osz)
			return -1;
		if (sz > osz)
			return 1;
		Iterator<Integer> it = items.iterator();
		Iterator<Integer> oit = o.items.iterator();
		while (it.hasNext()) {
			Integer v = it.next();
			Integer ov = oit.next();
			if (v < ov)
				return -1;
			if (v > ov)
				return 1;
		}
		return 0;
	}

	public void set(int i) {
		items.add(i);
	}

	public void setSupport(int supp) {
		support = supp;
	}
}
