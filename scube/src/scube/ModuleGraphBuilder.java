package scube;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import scube.Options.Projection;
import scube.utils.Cronos;
import scube.utils.Distribution;
import scube.utils.IOUtil;
import scube.utils.MInteger;
import scube.utils.PairIntInt;

/**
 * The Class ModuleGraphBuilder. 
 * Build graph of groups linked by shared individuals by projection.
 */
public class ModuleGraphBuilder {
	
	/**
	 * The main method. Useful for command-line invocation.
	 */
	public static void main(String[] args) throws Exception {
		Options.initialize();
		System.out.println("-- ModuleGraphBuilder starts " + Options.time());
		LocalDateTime[] dates = Options.getDates(Options.getTimeLabel(),Options.getDateSeparator());
		start( (dates != null) ? dates[dates.length-1] : null );
		System.out.println("-- ModuleGraphBuilder ends " + Options.time());
	}

	/**
	 * Start processing.
	 *
	 * @param time DateTime snapshot to consider for edges.
	 */
	public static void start(LocalDateTime time) throws Exception {
		System.out.println("Projecting into " + (Options.getProjectionMode() == Projection.Individual ? "individuals." : "groups."));
		System.out.println("Filtering by date: " + (time != null ? Options.printDate(time) : "none") + ".");
		String filePathIN = Options.getMembershipFilePath();
		String edgesFilePath = Options.getModuleGraphBuilderOutput();
		String isolateNodesFilePath = Options.getIsolateNodesTablePath();
		buildGraph(time, filePathIN, edgesFilePath, isolateNodesFilePath, Options.getProjectionMode());
		System.out.println("Saving projected graph: "+edgesFilePath);
		if(Options.areIsolateNodeConsidered())
			System.out.println("Saving isolated nodes: " +Options.getIsolateNodesTablePath());
	}

	/**
	 * Projection algorithm.
	 *
	 * @param timeInterval the date to consider in the creation of the snapshot
	 * @param filePathIN filepath of graph in CSV format.
	 * @param filePathOUT1 filepath of projected graph.
	 * @param filePathOUT2 filepath of isolated nodes.
	 * @param mode the Projection mode.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void buildGraph(LocalDateTime timeInterval, String filePathIN, String filePathOUT1,String filePathOUT2, Projection mode) throws IOException {
		Map<Integer, List<Integer>> admins = new HashMap<Integer, List<Integer>>();// <dirID,BoDs>
		Map<Integer, List<Integer>> companies = new HashMap<Integer, List<Integer>>();// <comID,directors>
		Cronos crono = new Cronos();
		crono.start();
		int posCom= mode==Projection.Group ? 0 : 1;
		int posDir= 1-posCom;
		try {
			BufferedReader reader = IOUtil.getReader( filePathIN );
			String [] tokens;
			reader.readLine();// header
			while ((tokens = IOUtil.readTokens(reader)) != null) {
				int comID = Integer.parseInt(tokens[posCom]);// company  ID
				int dirID = Integer.parseInt(tokens[posDir]);// director ID
				if (timeInterval!=null && tokens.length >= 4) {
					LocalDateTime start = Options.extractDate(tokens[2],Options.getDateSeparator());
					LocalDateTime end = Options.extractDate(tokens[3],Options.getDateSeparator());
					if (!Options.edgeIsValid(start, end, timeInterval))
						continue;
				}
				List<Integer> dirList = admins.get(dirID);
				if (dirList == null) { // first instance of dirID
					dirList = new ArrayList<Integer>();
					admins.put(dirID, dirList);
				} 
				dirList.add(comID);
				List<Integer> comList = companies.get(comID);
				if (comList == null) { // first instance of comID
					comList = new ArrayList<Integer>();
					companies.put(comID, comList);
				}
				comList.add(dirID);
			}
			reader.close();
			int nCompanies = companies.size();
			System.out.println("Groups/companies: " + nCompanies + "\nIndividuals/directors: " + admins.size());
			
			//Output distributions
			int maxpresence = 0;
			for( List<Integer> ap : admins.values() ) 
				if( ap.size() > maxpresence )
					maxpresence = ap.size();
			Distribution presence = new Distribution(1, maxpresence, 1);
			for( List<Integer> ap : admins.values() ) 
				presence.add(ap.size());
			presence.output(Options.getFolderOutput()+"distributionPresenceDirectors.csv");
			int maxbod = 0;
			for( List<Integer> cb : companies.values() ) 
				if( cb.size() > maxbod )
					maxbod = cb.size();
			Distribution cbods = new Distribution(1, maxbod, 1);
			for( List<Integer> cb : companies.values() ) 
				cbods.add(cb.size());
			cbods.output(Options.getFolderOutput()+"distributionBoDBySize.csv");

			// Output projected graph
			Map<PairIntInt, MInteger> weightedEdges = new TreeMap<PairIntInt, MInteger>();
			BitSet shareDirectors = new BitSet(nCompanies);
			for (int directorID : admins.keySet()) {
				List<Integer> myCompanies = admins.get(directorID);
				if (myCompanies.size() < 2)
					continue;
				// If a company shares at least 1 administrator it isn't isolated
				for (int compID : myCompanies)
					shareDirectors.set(compID);
				int nc1 = myCompanies.size() - 1;
				for (int i = 0; i < nc1; ++i) {
					int cIdi = myCompanies.get(i);
					for (int j = i + 1; j < myCompanies.size(); ++j) {
						int cIdj = myCompanies.get(j);
						PairIntInt edge = new PairIntInt(cIdi, cIdj);
						MInteger count = weightedEdges.get(edge);
						if (count==null) 
							weightedEdges.put(edge, new MInteger(1));
						else
							count.value++;
					}
				}
			}
			PrintWriter out = IOUtil.getWriter(filePathOUT1);
			String nodeID = Options.getNodeMetadata();
			out.println(nodeID + Options.getDelimiter() + nodeID + Options.getDelimiter() + "weight");
			/* Writing edges */
			for (PairIntInt edge : weightedEdges.keySet())
				if(edge.first != edge.second)
					out.println(edge.first + Options.getDelimiter() + edge.second + Options.getDelimiter() + weightedEdges.get(edge).value);
			out.close();
			System.out.println("Projected graph edges: " + weightedEdges.size());
 			// Writing nodes
			out = IOUtil.getWriter(filePathOUT2);
			out.println(nodeID + Options.getDelimiter() + "weight");
			if ( Options.areIsolateNodeConsidered() ){
				int countCompaniesIsolate = 0;
				for (int i = 0; i < nCompanies; ++i){
					if (!shareDirectors.get(i) && companies.get(i) != null) {
						out.println(i + Options.getDelimiter() +companies.get(i).size()); //ISOLATE NODE
						countCompaniesIsolate++;
					}
				}
				System.out.println("Isolated nodes: " + countCompaniesIsolate);
			}
			out.close();
		} catch (IOException ioException) {
			System.out.println("File for building the graph not found.");
		}
	}
}
