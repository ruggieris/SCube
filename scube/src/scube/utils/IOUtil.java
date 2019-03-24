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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import scube.utils.PairIntInt;

/** 
 * Static methods for getting readers and writers
 * from filenames or URLs, reading tokens from readers.
 */
public abstract class IOUtil {

    /** Newline character */
    public static final String newline = System.getProperty("line.separator");

    /** Get a Reader given a resource name.
     * Resource names can be filenames or urls possibly gzipped (if ending
     * with the ".gz" extension).
     * @param name resource name
     * @return reader
     */
	public static BufferedReader getReader(String name) throws IOException {
        InputStream is;

        if (name.equals(""))
            is = System.in;
        else {
            try {
                is = new FileInputStream(name);
            } catch (FileNotFoundException e) {
                try {
                    URL url = new URL(name);
                    is = url.openStream();
                } catch (MalformedURLException m) {
                    throw (new IOException(name + ": not a file or an URL."));
                }
            }
            if (name.endsWith(".gz"))
                is = new GZIPInputStream(is);
        }
        return new BufferedReader(new InputStreamReader(is));
    }

	/** Return number of rows in a file.
     * @param fileName resource name
     * @return the number of rows
     */
	public static int countRows(String fileName, String sep)
	throws IOException 
	{
		BufferedReader in = null;
		try {
			in = getReader(fileName);
		} catch(IOException e ) {
			return 0;
		}
	    int rows = 0;
		while( in.readLine() != null)
			++rows;
		in.close();
		return rows;
	}

	/** Return number of rows and columns in a file.
     * @param fileName resource name
     * @return the number of rows
     */
	public static PairIntInt countColumnsRows(String fileName, String sep)
	throws IOException 
	{
		BufferedReader in = null;
		try {
			in = getReader(fileName);
		} catch(IOException e ) {
			return new PairIntInt(0,0);
		}
	    int rows = 0;
	    String[] line = readTokens(in, sep);
	    if( line == null )
	    	return new PairIntInt(0,0);
	    ++rows;
	    int cols = line.length;	    
		while( in.readLine() != null)
			++rows;
		in.close();
		return new PairIntInt(cols, rows);
	}

	/** Return the content of a resource as a String.
     * @param name resource name
     * @return the content of the resource, accessed with getReader()
     */
    public static String getResourceContent(String name) throws IOException {
        BufferedReader r = getReader(name);
        StringBuffer s = new StringBuffer(4096);
        String line;
        while ((line = r.readLine()) != null) {
            s.append(line);
            s.append(IOUtil.newline);
        }
        r.close();
        return s.toString();
    }

    /** Recursively delete a directory. 
    * @param dirName directory name
    */
   public static boolean deleteDir(String dirName) {
    	return deleteDir(new File(dirName));
    }

   /** Recursively delete a directory. 
    * @param dir directory object
    */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /** Get a PrintWriter given a resource name, possibly in append mode.
     * Resource names can be filenames possibly gzipped (if ending
     * with the ".gz" extension).
     * @param name resource name
     * @param append true if the resource is opened in append mode 
     * @return a PrintWriter object 
     */
    public static PrintWriter getWriter(String name, boolean append) throws
            IOException {
        OutputStream os;

        if (name.equals("")) {
            os = System.out;
        } else {
            os = new FileOutputStream(name, append);

            if (name.endsWith(".gz"))
                os = new GZIPOutputStream(os);
        }

        return new PrintWriter(
                new BufferedWriter(
                    new OutputStreamWriter(os)), true);
    }
    
    /** Get a PrintWriter given a resource name.
     * Resource names can be filenames possibly gzipped (if ending
     * with the ".gz" extension).
     * @param name resource name
     * @return a PrintWriter object 
     */
    public static PrintWriter getWriter(String name) throws IOException {
       return getWriter(name, false);
    }

    /** Get a ObjectOutputStream given a resource name, possibly in append mode.
     * Resource names can be filenames possibly gzipped (if ending
     * with the ".gz" extension).
     * @param name resource name
     * @param append true if the resource is opened in append mode 
     * @return a ObjectOutputStream object 
     */
    public static ObjectOutputStream getObjectWriter(String name, boolean append) throws
            IOException {
    	OutputStream os;

        if (name.equals("")) {
            return null;
        } else {
            os = new FileOutputStream(name, append);

            if (name.endsWith(".gz"))
                os = new GZIPOutputStream(os);
        }

        return new ObjectOutputStream(os);
    }

    /** Get a ObjectOutputStream given a resource name.
     * Resource names can be filenames possibly gzipped (if ending
     * with the ".gz" extension).
     * @param name resource name
     * @return a ObjectOutputStream object 
     */
    public static ObjectOutputStream getObjectWriter(String name) 
    throws IOException {
    	return getObjectWriter(name, false);
    }

    /** Get a ObjectInputStream given a resource name.
     * Resource names can be filenames possibly gzipped (if ending
     * with the ".gz" extension).
     * @param name resource name 
     * @return a ObjectOutputStream object 
     */
    public static ObjectInputStream getObjectReader(String name) throws
            IOException {
    	InputStream os;

        if (name.equals("")) {
            return null;
        } else {
            os = new FileInputStream(name);

            if (name.endsWith(".gz"))
                os = new GZIPInputStream(os);
        }

        return new ObjectInputStream(os);
    }

    /** Return the array of comma-separated tokens in a line read from a BufferedReader.
     * @param in input reader
     * @return the array of tokens or null if no line was read. 
     */
    public static String[] readTokens(BufferedReader in) throws IOException {
        return readTokens(in, ",");
    }

    /** Return the array of tokens in a line read from a BufferedReader.
     * Implementation is faster than using split.
     * @param in input reader
     * @param sep token separator
     * @return the array of tokens or null if no line was read. 
     */
    public static String[] readTokens(BufferedReader in, String sep) throws
            IOException {
        String line = in.readLine();
        if (line == null)
            return null;
        return line.split(sep, -1);
   }
    
    public static String[] specialReadTokens(BufferedReader in) throws
            IOException {
        String line = in.readLine();
        if (line == null)
            return null;
        return line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
    }
    
    /** Set an array with tokens in a line read from a BufferedReader.
     * @param in input reader
     * @param sep token separator
     * @param result allocated array with enough elements
     * @return the array of tokens or null if no line was read. 
     */
    public static int readTokens(BufferedReader in, String sep, String[] result) throws
            IOException {
        String line = in.readLine();
        if (line == null)
            return -1;
        StringTokenizer st = new StringTokenizer(line, sep);
        int pos = 0;
        while (st.hasMoreTokens())
            result[pos++] = st.nextToken();
        return pos;
    }
   
    /** Copy a resource line by line.
     * @param in input resource name
     * @param out output resource name. 
     */
    public static void copy(String in, String out) throws IOException {
        BufferedReader r = getReader(in);
        PrintWriter w = getWriter(out);
        String line;
        while ((line = r.readLine()) != null)
            w.println(line);
        r.close();
        w.close();
    }

    /** Rename a file.
     * @param in file to be renamed
     * @param out new name.
     * @return result of the operation 
     */
    public static boolean rename(String in, String out) throws IOException {
    	File fileIn = new File(in);
    	File fileOut = new File(out);
    	if(fileOut.exists())
    		fileOut.delete();
    	return fileIn.renameTo(fileOut);
    }
    
    
    /** Read a file of properties.
     * @param propsFileName the filename containing properties
     * @return a Property object 
     */
    public static Properties readProps(String propsFileName) throws
            IOException {
        Properties props = new Properties();
        FileInputStream in = new FileInputStream(propsFileName);
        props.load(in);
        in.close();
        return props;
    }
 }
