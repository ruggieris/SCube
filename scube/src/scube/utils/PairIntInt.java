/*
 *    This program is free software; you can redistribute it and/or modify
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

import java.io.Serializable;
import java.util.Comparator;

/** Pair of integers. */
public class PairIntInt implements Comparable<PairIntInt>, Serializable {
	/** Serial ID. */
	private static final long serialVersionUID = -1176424477266730707L;
	/** First element */
	public int first;
	/** Second element */
	public int second;

	/** Constructor */
	public PairIntInt() {		
	}
	
	/** Constructor */
	public PairIntInt(int first, int second) {
		this.first = first;
		this.second = second;
	}

	/** Constructor */
	public PairIntInt(PairIntInt c) {
		this.first = c.first;
		this.second = c.second;
	}
	
	/** Lexicographic comparison */
	public int compareTo(PairIntInt arg0) {
		if( first < arg0.first )
			return -1;
		if( first > arg0.first )
			return 1;
		if( second < arg0.second )
			return -1;
		if( second > arg0.second )
			return 1;
		return 0;
	}
	
	public boolean equals(PairIntInt o) {
		return first == o.first && second == o.second;
	}

	public int hashCode() {
		return (first % 9999) * (second % 9999);
	}
	
	public PairIntInt clone() {
		return new PairIntInt(first, second);
	}
	
	public String toString() {
		return "("+first+", "+second+")";
	}	
	
    public static Comparator<PairIntInt> sortFirst = new Comparator<PairIntInt>() {
        @Override
        public int compare(PairIntInt o1, PairIntInt o2) {
        	if(o1==null)
        		if(o2==null)
        			return 0;
        		else
        			return -1;
        	if(o2==null)
        		return 1;
    		if( o1.first < o2.first )
    			return -1;
    		if( o1.first > o2.first )
    			return 1;
    		return 0;
       }
    };
	
}
