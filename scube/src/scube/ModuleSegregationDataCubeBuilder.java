package scube;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.googlecode.javaewah.datastructure.BitSet;

import scube.fim.FIM;
import scube.fim.Itemset;
import scube.utils.BitTable;
import scube.utils.Cronos;
import scube.utils.IOUtil;
import scube.utils.MInteger;
import scube.utils.Pair;

/**
 * The Class ModuleSegregationDataCubeBuilder.
 */
public class ModuleSegregationDataCubeBuilder {
	/** Frequent Itemset Mining. */
	public static FIM fim;
	/** A bitmap table. */
	private static BitTable bittable;
	/** The unitID a director belongs to. */
	private static int[] unitIDs;
	/** Number of attributes. */
	private static int ncols;
	
	/**  Segregation measures. */
	public static final int posDissimilarity=0;
	
	/** The Constant posEntropy. */
	public static final int posEntropy=1;
	
	/** The Constant posGini. */
	public static final int posGini=2;
	
	/** The Constant posIsolation. */
	public static final int posIsolation=3;
	
	/** The Constant posInteraction. */
	public static final int posInteraction=4;
	
	/** The Constant posAtkinson. */
	public static final int posAtkinson=5;
	
	/**
	 * The Enum Index.
	 */
	public enum Index {
		
		/** The d. */
		D("Dissimilarity"),
		
		/** The h. */
		H("Entropy"),
		
		/** The g. */
		G("Gini"),
		
		/** The i. */
		I("Isolation"),
		
		/** The Int. */
		Int("Interaction"),
		
		/** The Atkinson. */
		Atkinson("Atkinson");
		
		/** The to string. */
		private final String toString;
		// We want to be able to lookup enum value based on the label property
		/** The lookup. */
		// This map holds all enums keyed on 'label'
		private static Map<String, Index> lookup = new HashMap<String, Index>();
		static {
			// Populate out lookup when enum is created
			for (Index e : Index.values()) {
			lookup.put(e.label(), e);
			}
		}
		
		/**
		 * Gets the.
		 *
		 * @param label the label
		 * @return the index
		 */
		// Provide a method to lookup up enum with matching label
		public static Index get(String label) {
			return lookup.get(label);
		}
		
		/**
		 * Instantiates a new index.
		 *
		 * @param toString the to string
		 */
		private Index(String toString) {
			this.toString = toString;
		}
		
		/**
		 * Label.
		 *
		 * @return the string
		 */
		public String label() {
			return toString;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		public String toString() {
			return label();
		}
	}
	/**The number of segregation indexes.*/
	static public final int nIndex = Index.values().length;
	
	/** Atkinson Parameter. */
	static public double b = Double.NaN;
	

	/**
	 * The main method. Useful for command-line invocation.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		Options.initialize();
		System.out.println("-- ModuleSegregationDataCubeBuilder starts " + Options.time());
		start(false);
		System.out.println("-- ModuleSegregationDataCubeBuilder ends " + Options.time());
	}

	/**
	 * Start processing.
	 *
	 * @throws Exception the exception
	 */
	public static void start(boolean append) throws Exception {
		b = Options.getAtkinsonParameter(); // set atkinsonParameter
		String ignore = Options.getModuleSegregationIGNORE();
		if (ignore.isEmpty())
			ignore = "unitID";
		else
			ignore = "unitID" + Options.getDelimiter() + ignore;
		System.out.println("Ignoring attributes: " + ignore);
		fim = new FIM(Options.getModuleSegregationInput(), ignore);
		String excludeAtts = Options.getModuleSegregationCA(); // unitID already excluded in .fimi
		System.out.println("Excluding attributes: " + excludeAtts);
		CollectionItemsets itemsetsSA = new CollectionItemsets(fim, excludeAtts, Options.getMinimumSupport());
		int nrows = fim.getNoRows();
		ncols = fim.getNoAtts();
		unitIDs = new int[nrows]; // unit of a rowID
		BufferedReader tb = IOUtil.getReader( Options.getModuleSegregationInput() );
		String line;
		int pos = 0;
		String header = tb.readLine(); // skip header
		int nunits = 0;
		// count (max) number of units
		while ((line = tb.readLine()) != null) {
			String last = line.substring(line.lastIndexOf(Options.getDelimiter())+1);
			int unit = Integer.parseInt(last);
			unitIDs[pos++] = unit;
			if (unit >= nunits)
				nunits = unit + 1;
		}
		tb.close();
		// all the CA attributes
		bittable = new BitTable(fim.dec.numberOfCodes(), nrows);
		bittable.loadFromFimi(fim.getFimiFile(), Options.getDelimiter());			
		fim.extractItemsets(Options.getPathExecutableFIMI(), excludeAtts, Options.getMinimumSupport(), true, false);
		fim.initScanFrequent();
		int []mA = new int[nunits];
		int npairs = 0;
		Itemset itemset;
		PrintWriter w = IOUtil.getWriter(Options.getModuleSegregationOutput(), append);
		if (!append)
			writeHeader(w, Options.filterHeader(header));
		int processed=0;
		Cronos crono = new Cronos();
		crono.start();
		while ((itemset = fim.nextFrequent()) != null) {
			if( processed++ % 100 == 99) {
				System.out.println("Processed: "+ processed + " in " + crono.elapsedSoFar() + " secs.");
				System.out.flush();
			}
			BitSet coverB = bittable.cover(itemset.getItems());
			int []tB = new int[nunits];
			for (int bitsetpos : coverB)
				tB[unitIDs[bitsetpos]]++;
			int T = coverB.cardinality();
			ArrayList<Integer> nonZero = new ArrayList<Integer>();
			for (int i = 0; i < nunits; i++)
				if(tB[i]>0)
					nonZero.add(i);
			for(Pair<Itemset,BitSet> it : itemsetsSA.getClosed(coverB, bittable)) {
				Itemset itA = it.first;
				BitSet coverA = it.second;
				int M = itA.getSupport();
				if (M == T) // no majority individual
					continue;
				npairs++;
				for(int i : nonZero)
					mA[i] = 0;
				for (int bitsetpos : coverA) 
					mA[unitIDs[bitsetpos]]++;
				// sort nonZero by mA/tB (ie., p_i) DESC 
				final int []tB2 = tB;
				nonZero.sort((Integer x, Integer y) -> { 
					double px = mA[x] / (double)tB2[x];
					double py = mA[y] / (double)tB2[y];
					return px < py ? 1 : (px > py ? -1 : 0); 
					} ); 
				double[] results = getIndex(tB, mA, nonZero, M, T);
				// TODO: add an option to restrict to iceberg cube
				writeRow(w, itemset.getItems(), itA.getItems(), M, T, results);
			}
		}
		fim.endScanFrequent();
		w.close();
		System.out.println("Segregation cube size: "+ npairs);
	}
	
	/**
	 * Gets segregation indexes.
	 *
	 * @return the array of segregation indexes.
	 */
	private static double[] getIndex(int []tB, int []mA, ArrayList<Integer> nonZero, int M, int T) {
		double[] sum = new double[nIndex];
		int X = 0;
		int Y = 0;
		double P = M / (double) T;
		for (int pos : nonZero ) {
			// compute summation
			int m_i = mA[pos];
			int t_i = tB[pos];
			// D
			double p_i = m_i / (double) t_i;
			sum[posDissimilarity] += t_i * Math.abs(p_i - P);
			// H
			sum[posEntropy] += t_i * entropy(p_i);
			// G
			X += m_i;
			Y += t_i - m_i;
			sum[posGini] += (X - m_i) * Y - X * (Y - t_i + m_i);			
			// I
			sum[posIsolation] += m_i * p_i;
			sum[posAtkinson] += Math.pow(1 - p_i, 1 - b) * Math.pow(p_i, b) * t_i;
		}
		// normalization
		// D
		sum[posDissimilarity] = sum[posDissimilarity] / (T - M) / (2 * P);
		// H
		sum[posEntropy] = 1 - sum[posEntropy] / (T * entropy(P));
		// G
		sum[posGini] =  sum[posGini] / M / (T - M);
		// I
		sum[posIsolation] =  sum[posIsolation] / M;
		// Int - more efficient computation
		sum[posInteraction] = 1 - sum[posIsolation];
		sum[posAtkinson] = 1 -( (M / (double) (T - M)))	* Math.pow(Math.abs(sum[posAtkinson])/M, 1 / (1 - b));
		return sum;
	}

	/**
	 * Binary entropy.
	 *
	 * @param p the fraction of one of the two values.
	 * @return entropy.
	 */
	private static double entropy(double p) {
		final double Log2 = Math.log(2);
		if (p == 0 || p == 1)
			return 0;
		return -p * Math.log(p) / Log2 - (1 - p) * Math.log(1 - p) / Log2;
	}

	/**
	 * Write header of output CSV.
	 *
	 * @param w the output.
	 * @param header the header of finaltable.csv
	 */
	public static void writeHeader(PrintWriter w, String header) {
		String[] cols = header.split(Options.getDelimiter());
		String r = "";
		boolean first = true;
		for (int i = 0; i < cols.length - 1; i++) {
			r += (first ? "" : Options.getDelimiter()) + cols[i];
			first = false;
		}
		r += Options.getDelimiter() + "M" + Options.getDelimiter() + "T";
		
		for(Index i : Index.values())
			r += Options.getDelimiter() + i;		
		
		//add graph partition algorithm and time interval
		r += 	Options.getDelimiter() //+ "partitionGraphAlgorithm" + Options.getDelimiter()	
					+ "timeUnit";
		w.println(r);
	}

	/**
	 * Remove attribute ma,e.
	 *
	 * @param string the string
	 * @return the string
	 */
	private static String removeatt(String string) {
		return string.substring(string.indexOf("=") + 1);
	}

	/**
	 * Write row to the output.
	 *
	 */
	public static void writeRow(PrintWriter w, List<Integer> itemsB, List<Integer> itonlyAitems, int M, int T, double[] indexes) {
		String[] res = new String[ncols];
		for (Integer ca : itemsB) {
			int colPos = fim.dec.getAttribute(ca);
			String colVal = removeatt(fim.dec.decode(ca));
			if (res[colPos] == null)
				res[colPos] = colVal;
			else
				if(!res[colPos].contains(colVal))
					res[colPos] = res[colPos] + Options.getMultiValuesDelimiter() + colVal;
		}
		for (Integer sa : itonlyAitems) {
			int colPos = fim.dec.getAttribute(sa);
			String colVal = removeatt(fim.dec.decode(sa));
			if (res[colPos] == null)
				res[colPos] = colVal;
			else
				if(!res[colPos].contains(colVal))
					res[colPos] = res[colPos] + Options.getMultiValuesDelimiter() + colVal;
		}

		boolean first = true;
		String r = "";
		for (String col : res) {
			if (col == null)
				col = "";
			r += (first ? "" : Options.getDelimiter()) + col;
			first = false;
		}
		r += Options.getDelimiter() + M + Options.getDelimiter() + T;
		for (double index : indexes)
			r += Options.getDelimiter() + index;

		r += //Options.getDelimiter() + Options.getNetClusterAlgorithmOption() + 
				Options.getDelimiter() + Options.timeToString();
		w.println(r);
	}
}

class ItemsetSearchTree {
	public Pair<Itemset,BitSet> itemset = null;
	public int splitItem = -1;
	public ItemsetSearchTree include = null;
	public ItemsetSearchTree includeNot = null;
	
	public ItemsetSearchTree(List<Pair<Itemset,BitSet>> set) {
		int sz = set.size();
		if(sz<=1) {
			if(sz==1)
				itemset = set.get(0);
			return;
		}
		// compute frequency
		HashMap<Integer, MInteger> freq = new HashMap<Integer, MInteger>();
		for(Pair<Itemset, BitSet> el : set)
			for(Integer item : el.first.getItems()) {
				MInteger fitem = freq.get(item);
				if(fitem==null)
					freq.put(item,  new MInteger(1));
				else
					fitem.value++;
			}
		// maximal frequency not meaningful
		ArrayList<Entry<Integer, MInteger>> freqlist = new ArrayList<Entry<Integer, MInteger>>();
		int nmaxfreq = 0;
		for(Entry<Integer, MInteger> e : freq.entrySet())
			if(e.getValue().value < sz)
				freqlist.add(e);
			else
				nmaxfreq++;
		// sort by frequency
		freqlist.sort( (Entry<Integer,MInteger> e1, Entry<Integer,MInteger> e2) -> {
			return e1.getValue().compareTo(e2.getValue());
		});
		// take median item
		splitItem = freqlist.get( freqlist.size() / 2).getKey();
		// recursive split
		List<Pair<Itemset,BitSet>> setInclude = new ArrayList<Pair<Itemset,BitSet>>();
		List<Pair<Itemset,BitSet>> setIncludeNot = new ArrayList<Pair<Itemset,BitSet>>();
		for(Pair<Itemset,BitSet> el : set)
			if( !el.first.include(splitItem) )
				setIncludeNot.add(el);
			else
				if( el.first.getItems().size() == nmaxfreq+1 )
					itemset = el;
				else
					setInclude.add(el);
		includeNot = new ItemsetSearchTree(setIncludeNot);	
		if( setInclude.size()  > 0 ) 
			include = new ItemsetSearchTree(setInclude);
	}

	public void setSupport(BitSet cover, BitTable bittable, HashSet<Integer> seen) {
		itemset.second = bittable.cover(cover, itemset.first.getFilteredItems( i -> !seen.contains(i) ) );  // cover in B // filter items wrt already trasversed
		int supp = itemset.second.cardinality();
		itemset.first.setSupport(supp); // support in B
		if(splitItem<0)
			return;
		includeNot.setSupport(cover,  bittable, seen);
		if(include != null) {
			seen.add(splitItem);
			include.setSupport(bittable.cover(cover, splitItem),  bittable, seen);
			seen.remove(splitItem);
		}
	}
	
}

class CollectionItemsets {
	private ArrayList<Pair<Itemset,BitSet>> frequent;
	private int minsupp;
	ItemsetSearchTree search;
	
	public CollectionItemsets(FIM fim, String excludeAtts, int minsupp) throws Exception {
		this.minsupp = minsupp;
		fim.extractItemsets(Options.getPathExecutableFIMI(), excludeAtts, minsupp, false, true);
		fim.initScanFrequent();
		Itemset itemset;
		frequent = new ArrayList<Pair<Itemset,BitSet>>();
		while ((itemset = fim.nextFrequent()) != null)
			if(itemset.size()>0) {
				frequent.add(new Pair<Itemset,BitSet>(itemset, null));
//				System.out.println( itemset.toString(fim.dec) );
			}
		fim.endScanFrequent();
		search = new ItemsetSearchTree(frequent);
	}

	public List<Pair<Itemset,BitSet>> getClosed(BitSet cover, BitTable bittable) {
		search.setSupport(cover.clone(), bittable, new HashSet<Integer>());
		frequent.sort( (Pair<Itemset,BitSet> p1, Pair<Itemset,BitSet> p2) -> 
			{   // sort by (supp, number of items) 
				int s1 = p1.first.getSupport();
				int s2 = p2.first.getSupport();
				if(s1<s2)
					return -1;
				if(s1>s2)
					return 1;
				int sz1 = p1.first.getItems().size();
				int sz2 = p2.first.getItems().size();
				if(sz1<sz2)
					return -1;
				if(sz1>sz2)
					return 1;
				return 0;
			}  );
		int sz = frequent.size();
		int posStart = 0;
		ArrayList<Pair<Itemset,BitSet>> closed = new ArrayList<Pair<Itemset,BitSet>>();
		while(posStart < sz) {
			// find end of same supp
			int supp = frequent.get(posStart).first.getSupport();
			int posEnd = posStart+1;
			while(posEnd < sz)
				if( supp == frequent.get(posEnd).first.getSupport() )
					++posEnd;
				else
					break;
			// check frequent
			if( supp >= minsupp ) 
				// check closed in [posStart, posEnd)
				for(int i=posStart; i<posEnd; ++i) {
					Itemset iset = frequent.get(i).first;
					int isz = iset.getItems().size();
					boolean isClosed = true;
					for(int j=i+1; j<posEnd; ++j) {
						Itemset jset = frequent.get(j).first;
						int jsz = jset.getItems().size();
						if(jsz==isz)
							continue;
						if(jsz>isz+1)
							break;
						if( iset.isIncludedIn(jset) ) {
							isClosed = false;
							break;
						}
					}
					if(isClosed)
						closed.add(frequent.get(i));
				}
			posStart = posEnd;			
		}
		return closed;
	}	
	
}
