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

import java.io.IOException;
import java.io.PrintWriter;

import scube.utils.IOUtil;

/** Calculate statistics on a sequence of numbers in an interval [min, max] with precision of step. 
 * Values higher than max are treated as max.
 */
public class Distribution {
	/** Min value. */
	public double min;
	/** Step value. */
	public double step;
	/** Counts of value (min + i*step). */
	public int count[];
	/** Number of values. */
	public int nValues;
	/** Sum of values. */
	private double sumValues;
	
	/** Constructor. */
	public Distribution(double min, double max, double step) {
		this.min = min;
		this.step = step;
		this.nValues = 0;
		int nvalues = (int)Math.floor((max-min)/step)+1;
		count = new int[nvalues];
		sumValues = 0;
	}
	/** Add v to the sequence. */
	public void add(double v) {
		if( Double.isNaN( v ) || Double.isInfinite( v ))
			return;
		int b = (int)Math.floor((v-min)/step);
		if( b >= count.length )
			b = count.length-1;
		count[b]++;
		nValues++;
		sumValues += v;
	}
	/** Average of distribution. */
	public double avg() {
		return sumValues/nValues;
	}
	/** Average of distribution except a given value. */
	public double avg(double v) {
		int b = (int)Math.floor((v-min)/step);
		if( b >= count.length )
			b = count.length-1;
		double sumValues2 = sumValues - count[b]*v;
		int nValues2 = nValues - count[b];
		return sumValues2/nValues2;
	}
	/** Add v a number of times to the sequence. */
	public void add(double v, int howmany){
		if( Double.isNaN( v ) || Double.isInfinite( v ))
			return;
		int b = (int)Math.floor((v-min)/step);
		if( b >= count.length )
			b = count.length-1;
		count[b] += howmany;		
	}
	/** Output statistics. 
	 *  Row format: 
	 *       value \t count \t ratio to total \t cumulative count \t cumulative ratio to total. 
	 */
	public void output(String fileName) 
	throws IOException 
	{
		PrintWriter out = IOUtil.getWriter(fileName);
		int total = 0;
	    for(int i=0; i<count.length;++i) 
	    	total += count[i];
		int cumulative = 0;
	    for(int i=count.length-1; i>=0;--i) {
	    	int n = count[i];
	    	if( n != 0 ) {
	    		double v = i*step+min;
	    		if( v < min)
	    			break;
	    		double r = (double)n/total;
	    		cumulative += n;
	    		double rcum = (double)cumulative/total;
	    		out.println("" + (float)v + "\t" + n + "\t" + (float)r 
	    				 + "\t" + (float)rcum+ "\t" + cumulative);
	    	 }
	     }
	    out.close();
	}
}
