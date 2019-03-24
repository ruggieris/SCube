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
 *    Copyright (C) 2004-
 *    Salvatore Ruggieri
 *    Dipartimento di Informatica, Universita' di Pisa
 *    Pisa, Italy
 *    ------------------------------------------------------------------------
 *
 */

package scube.utils;

/** Elapsed time cronometer. */
public class Cronos {

    /** Start time. */
    long start;
    /** End time. */
    long stop;
    /** Counted time. */
    long count;

    /** Constructor. */
    public Cronos() {
        start = stop = count = 0;
    }

    /** Start the cronometer. */
    public void start() {
        start = System.currentTimeMillis();
        count = 0;
    }

    /** Stop the cronometer. */
    public void stop() {
        stop = System.currentTimeMillis();
        count += stop - start;
    }

    /** Suspend the cronometer. */
    public void pause() {
        stop = System.currentTimeMillis();
        count += stop - start;
    }

    /** Restart the cronometer. */
    public void restart() {
        start = System.currentTimeMillis();
    }

    /** Get elapsed time in seconds. */
    public double elapsed() {
        return count / 1000.0;
    }
    
    /** Get elapsed time in seconds. */
    public double elapsedSoFar() {
        return (System.currentTimeMillis() - start) / 1000.0;
    }
}
