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

package scube.fim;

import java.io.*;
import java.util.*;

import scube.Options;
import scube.utils.Cronos;
import scube.utils.IOUtil;
import scube.utils.PairIntInt;

/**
 * CSV2FIMI: command line program for producing a FIMI file format 
 * 			 starting from a CSV file format with header or from a ARFF file format.
 */
public class CSV2FIMI {
	/**Delimiter used in csv*/
	
	/**The method takes in input an array with 4 respectively
	 * @param dataset strings that identifies  the dataset to read
	 * @param fimiOutputFile string that identifies the output fimi file
	 * @param decodeOutputFile string that identifies the output decode file
	 * @param exclude string that identifies the list of attributes name to exclude from the analysis.*/
	public static int createFIMI(String dataset, String fimiOutputFile, String decodeOutputFile, String exclude) {//
		Cronos t = new Cronos();
		t.start();
		String[] itemToExclude = exclude.split(Options.getDelimiter());
		Set<String> toExclude = new HashSet<String>(Arrays.asList(itemToExclude));
		int nrows = 0;
		try {
			// first pass
			BufferedReader input = IOUtil.getReader(dataset);
			HashMap<String, PairIntInt> valuecode = csv2fimi_pass(input, toExclude);
			// System.out.println(ColumnCode.toString(atts));
			input.close();
			// second pass
			input = IOUtil.getReader(dataset);
			PrintWriter output = IOUtil.getWriter(fimiOutputFile);
			nrows = csv2fimi(valuecode, input, output, toExclude);
			input.close();
			output.close();
			// output codes ordered by attribute-id
			PrintWriter outputDecodeWriter = IOUtil.getWriter(decodeOutputFile);
			input = IOUtil.getReader(dataset);
			List<String> attsNames = getCSVattributes(input);
			HashMap<String, Integer> attpos = new HashMap<String, Integer>();
			for (int i = 0; i < attsNames.size(); ++i)
				attpos.put(attsNames.get(i), i);
			ArrayList<String> tmp = new ArrayList<String>(valuecode.keySet());
			tmp.sort((String x, String y) -> {
				int sx = attpos.get(x.substring(0, x.indexOf("=")));
				int sy = attpos.get(y.substring(0, y.indexOf("=")));
				return sx < sy ? -1 : (sx > sy ? 1 : 0);
			});
			for (String item : tmp){
				String line = valuecode.get(item).first + Options.getDelimiter() + item + Options.getDelimiter() + valuecode.get(item).second;
				outputDecodeWriter.println(line);
			}
			outputDecodeWriter.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		t.stop();
		System.out.println("Built .fimi in + " + t.elapsed() + " sec.");
		return nrows;
	}
	
	public static HashMap<String, PairIntInt> csv2fimi_pass(BufferedReader in, Set<String> columnsToExclude)throws IOException{
		List<String> attsNames = getCSVattributes(in);
		HashMap<String, PairIntInt> valuecode = new HashMap<String, PairIntInt>(); // PairIntInt is (code, number of occurrences)
		boolean[] exclude = new boolean[attsNames.size()];	    //The i-th position says if the i-th attribute is to consider or not.
		for (int i = 0; i < attsNames.size(); i++)
			exclude[i] = columnsToExclude.contains(attsNames.get(i));
	    // pass on tokens
	    String[] tokens;
	    final int MAXROWS = 100000;
	    int rowid = 0;
		while (rowid++<MAXROWS && (tokens = IOUtil.readTokens(in, Options.getDelimiter())) != null) {
			if (tokens.length == 0)
				continue;
			for (int i = 0; i < attsNames.size(); ++i) {
				if (!exclude[i]) 
					for(String token : tokens[i].split(";") ) {
						if(token.equals("")) // empty string = missing value
							continue;
						String item = attsNames.get(i)+"="+token;
						PairIntInt val = valuecode.get(item);
						if(val==null) 
							valuecode.put(item, new PairIntInt(0, 1));
						else
							val.second++;
					}
			}
		}
		return valuecode;
	}

	/** FIMI output excluding some columns. */
	public static int csv2fimi(HashMap<String, PairIntInt> valuecode, BufferedReader in,PrintWriter out, Set<String> columnsToExclude) throws IOException {
		// reassign tokenId based on support descending
		ArrayList<String> tmp = new ArrayList<String>(valuecode.keySet());
		tmp.sort((String x, String y) -> {
			int sx = valuecode.get(x).second;
			int sy = valuecode.get(y).second;
			return sx < sy ? 1 : (sx > sy ? -1 : 0); 
			} );
		int tokenId = 1;
		for(String token : tmp) {
			PairIntInt val = valuecode.get(token); 
			val.first = tokenId++;
			val.second = 0;
		}

		List<String> attsNames = getCSVattributes(in);
		boolean[] exclude = new boolean[attsNames.size()];
		for (int i = 0; i < attsNames.size(); i++) 
			exclude[i] = columnsToExclude.contains(attsNames.get(i)); 
		String[] tokens;
		StringBuilder row = new StringBuilder();
		int nrows = 0;
		while ((tokens = IOUtil.readTokens(in)) != null) {
			if (tokens.length == 0)
				continue;
			++nrows;
			row.setLength(0);
			boolean first=true;
			for (int i = 0; i < attsNames.size(); ++i) {
				if (exclude[i]) 
					continue;
				for(String tk : tokens[i].split(";"))
					if(!tk.equals("")) {
						String item = attsNames.get(i)+"="+tk;
						PairIntInt val = valuecode.get(item);
						if(val == null) {
							val = new PairIntInt(tokenId++, 1);
							valuecode.put(item, val);
						} else
							val.second++;
						if(!first)
							row.append( Options.getDelimiter() );
						row.append( val.first );
						first = false;
					}
			}
			out.println(row);
		}
		return nrows;
	}
	
	/** Read attribute names from ARFF header or CSV header. */
	public static ArrayList<String> getCSVattributes(BufferedReader in) throws IOException {
		ArrayList<String> res = new ArrayList<String>();
		String line = in.readLine();
		if (line.toLowerCase().startsWith("@relation")) { // ARFF file
			while ((line = in.readLine()) != null) {
				if (line.toLowerCase().startsWith("@data"))
					break;
				if (line.toLowerCase().startsWith("@attribute")) {
					StringTokenizer tk = new StringTokenizer(line, Options.getDelimiter());
					tk.nextToken();
					String attname = tk.nextToken();
					res.add(attname);
				}
			}
		} else {
			// CSV file with header
			StringTokenizer st = new StringTokenizer(line, Options.getDelimiter());
			while (st.hasMoreTokens())
				res.add(st.nextToken());
		}
		return res;
	}
}
