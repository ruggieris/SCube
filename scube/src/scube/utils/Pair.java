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


/** Pair of objects. */
public class Pair<A,B> implements Comparable<Pair<A,B>>, Serializable {
	/** Serial ID. */
	private static final long serialVersionUID = 5420059688561235405L;
	/** First element. */
	public A first;
	/** Second element. */
	public B second;
	
	/** Constructor. */
	public Pair() {
	}

	/** Constructor. */
	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}
	
	/** Lexicographic comparison. */
	@SuppressWarnings("unchecked")
	public int compareTo(Pair<A, B> arg0) {
		int cmpa = ((Comparable<A>)first).compareTo(arg0.first);
		if( cmpa != 0)
			return cmpa;
		return ((Comparable<B>)second).compareTo(arg0.second);
	}

    public boolean equals(Object other) {
    	if (other instanceof Pair) {
    		Pair<?, ?> otherPair = (Pair<?, ?>) other;
    		return 
    		((  this.first == otherPair.first ||
    			( this.first != null && otherPair.first != null &&
    			  this.first.equals(otherPair.first))) &&
    		 (	this.second == otherPair.second ||
    			( this.second != null && otherPair.second != null &&
    			  this.second.equals(otherPair.second))) );
    	}

    	return false;
    }
	
	public String toString() {
		return "("+first+", "+second+")";
	}
	
    public int hashCode() {
    	int hashFirst = first != null ? first.hashCode() : 0;
    	int hashSecond = second != null ? second.hashCode() : 0;

    	return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }
    
	 public A getFirst() {
	    	return this.first;
	    }

	    public void setFirst(A first) {
	    	this.first = first;
	    }

	    public B getSecond() {
	    	return this.second;
	    }

	    public void setSecond(B second) {
	    	this.second = second;
	    }
}
