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
 *    Copyright (C) 2008-
 *    Salvatore Ruggieri
 *    Dipartimento di Informatica, Universita' di Pisa
 *    Pisa, Italy
 *    ------------------------------------------------------------------------
 *
 */

package scube.fim;

import java.io.*;
import java.util.*;

import scube.Options;
import scube.utils.Cronos;
import scube.utils.IOUtil;
import scube.utils.MInteger;

/** Decode integers item(set)s from an encoding file. */
public class Decode {
	public int minSupp = Integer.MAX_VALUE;
	public int maxSupp = Integer.MIN_VALUE;
	/** Map a string item into its code. */
	TreeMap<String, Integer> deco;
	/** String of an item code. */
	String[] code;
	/** Support of item code. */
	int[] supp;
	/** Number of attributes. */
	int noattribute;
	/** Name of attributes. */
	String[] attName;
	/** Attribute number of item code. */
	int[] attNo;
	/** Number of items per attribute. */
	int[] attributeNitems;
	/** Mark removed items. */
	boolean[] removed;
	/** Number of an attribute name. */
	TreeMap<String, Integer> attcode;

	/** Constructor. */
	public Decode(String encodingFileName) throws IOException {
		Cronos t = new Cronos();
		t.start();
		BufferedReader in = IOUtil.getReader(encodingFileName);
		// count number of codes
		int maxv = 0;
		int minv = Integer.MAX_VALUE;
		String[] linev;
		String line;
		while ((line = in.readLine()) != null) {
			linev = line.split(Options.getDelimiter());
			int n = Integer.parseInt(linev[0]);
			int sup = Integer.parseInt(linev[2]);
			if (sup > maxSupp)
				maxSupp = sup;
			if (sup < minSupp)
				minSupp = sup;
			if (n > maxv)
				maxv = n;
			if (n < minv)
				minv = n;
		}
		in.close();
		// allocate arrays
		code = new String[++maxv];
		supp = new int[maxv];
		removed = new boolean[maxv];
		// store codes and decodes
		in = IOUtil.getReader(encodingFileName);
		deco = new TreeMap<String, Integer>();
		noattribute = 0;
		attcode = new TreeMap<String, Integer>();
		TreeMap<Integer, MInteger> attnoitems = new TreeMap<Integer, MInteger>();
		attNo = new int[maxv];
		while ((linev = IOUtil.readTokens(in, Options.getDelimiter())) != null) {
			int n = Integer.parseInt(linev[0]);
			code[n] = linev[1];
			supp[n] = Integer.parseInt(linev[2]);
			deco.put(code[n], new Integer(n));
			String attributeName = attributeName(code[n]);
			Integer attPos = attcode.get(attributeName);
			if (attPos == null) {
				attcode.put(attributeName, noattribute);
				attnoitems.put(noattribute, new MInteger(1));
				attNo[n] = noattribute;
				++noattribute;
			} else {
				attNo[n] = attPos;
				attnoitems.get(attPos).value++;
			}
		}
		in.close();
		attName = new String[noattribute];
		for (Map.Entry<String, Integer> e : attcode.entrySet())
			attName[e.getValue()] = e.getKey();
		attributeNitems = new int[noattribute];
		for (Map.Entry<Integer, MInteger> e : attnoitems.entrySet())
			attributeNitems[e.getKey()] = e.getValue().value;
		System.out.println("Loaded " + encodingFileName + " in + " + t.elapsed() + " sec.");
	}

	/** Number of attributes. */
	public int getNoAtts() {
		return noattribute;
	}

	/** Number of items for a given attribute. */
	public int getAttributeNItems(int attno) {
		return attributeNitems[attno];
	}

	/** Return the list of all items. */
	public List<Integer> getAllItems(int attno) {
		List<Integer> res = new ArrayList<Integer>(attributeNitems[attno]);
		for (int i = 0; i < code.length; ++i)
			if (attNo[i] == attno)
				res.add(i);
		return res;
	}

	/** Attribute name. */
	public String getAttName(int attNo) {
		return attName[attNo];
	}

	/** @return attribute number if it exists else -1. */
	public int getAttNo(String attributeName) {
		Integer attPos = attcode.get(attributeName);
		if (attPos == null)
			return -1;
		return attPos;
	}

	/** Attribute of an encoded item. */
	public int getAttribute(int item) {
		return attNo[item];
	}

	/** Support of an encoded item. */
	public int support(int item) {
		return supp[item];
	}

	/** Number of items encoded. */
	public int numberOfCodes() {
		return code.length;
	}

	/** Decode an encoded item. */
	public String decode(int item) {
		return code[item];
	}

	/** Decode an itemset, with items separated by sep. */
	public String decode(String items, String sep) {
		StringTokenizer tk = new StringTokenizer(items, sep);
		String res = "";
		boolean flag = true;
		while (tk.hasMoreTokens()) {
			String token = tk.nextToken();
			try {
				int n = Integer.parseInt(token);
				if (n >= 0 && n < code.length)
					res += Options.getDelimiter() + code[n];
				else
					res += Options.getDelimiter() + token;
			} catch (NumberFormatException e) {
				res += (flag ? "" : Options.getDelimiter()) + token;
				flag = false;
			}
		}
		return res;
	}

	/** Decode an itemset, with items separated by space. */
	public String decode(String items) {
		return decode(items, Options.getDelimiter());
	}

	/** Encode an item. */
	public int encode(String item) {
		Integer n = deco.get(item);
		if (n == null)
			throw new RuntimeException("encoding " + item);
		return n.intValue();
	}

	/** Encode an itemset. */
	public String encodeAll(String items) {
		if (items.equals(""))
			return "";
		StringTokenizer tk = new StringTokenizer(items, Options.getDelimiter());
		String res = "";
		boolean flag = true;
		while (tk.hasMoreTokens()) {
			String token = tk.nextToken();
			res += (flag ? "" : Options.getDelimiter()) + encode(token);
			flag = false;
		}
		return res;
	}

	/** Extract attribute name from an item. */
	private static String attributeName(String item) {
		return item.substring(0, item.indexOf("="));
	}

	/** Check whether an item is marked as removed. */
	public boolean isRemoved(int item) {
		return removed[item];
	}

	/** Mark an item as removed or not. */
	public void setRemoved(int item, boolean value) {
		removed[item] = value;
	}

	/** Mark an item as removed or not. */
	public void setRemoved(String item, boolean value) {
		removed[deco.get(item).intValue()] = value;
	}

	/** Check whether is there any removed item. */
	public boolean isAnyRemoved() {
		for (int i = 0; i < code.length; ++i)
			if (removed[i])
				return true;
		return false;
	}

	/** Parse a FIMI file, removing items marked as removed. */
	public void removeItems(String inFimiFile, String outFimiFile) throws IOException {
		BufferedReader in = IOUtil.getReader(inFimiFile);
		PrintWriter out = IOUtil.getWriter(outFimiFile);
		StringBuilder row = new StringBuilder();
		String[] tokens;
		while ((tokens = IOUtil.readTokens(in)) != null) {
			if (tokens.length == 0)
				continue;
			boolean flag = true;
			row.setLength(0);
			for (String token : tokens) {
					int item = Integer.parseInt(token);
					if (removed[item])
						continue;
					if (flag) 
						flag = false;
					else
						row.append( Options.getDelimiter() );
					row.append( token );
			}
			if (!flag)
				out.println(row);
		}
		in.close();
		out.close();
	}

	/** Test command line. */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Usage: java Decode input.decode input output");
			System.exit(0);
		}
		try {
			Decode d = new Decode(args[0]);
			BufferedReader in = IOUtil.getReader(args[1]);
			PrintWriter out = IOUtil.getWriter(args[2]);
			String line;
			while ((line = in.readLine()) != null)
				out.println(d.decode(line));
			in.close();
			out.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
}
