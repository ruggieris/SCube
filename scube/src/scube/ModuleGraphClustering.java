package scube;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.collections15.Buffer;
import org.apache.commons.collections15.buffer.UnboundedFifoBuffer;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import scube.graph.Edge;
import scube.graph.Node;
import scube.utils.Cronos;
import scube.utils.Distribution;
import scube.utils.IOUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class ModuleGraphClustering. 
 * Cluster a projected graph into units.
 */
public class ModuleGraphClustering {

	/**
	 * The Clustering Algorithms.
	 */
	public enum Algorithms {
		
		/** The remove edges from GCC. */
		removeEdgesFromGCC("Filtering edge weights from Giant Component"),
		
		/** The WC cs. */
		WCCs("Weak connected components"),
		
		/** The remove GCC. */
		removeGCC("Remove giant component"),
		
		/** The filter edges weight. */
		filterEdgesWeight("Filtering edge weight"),
		
		/** The stoc. */
		stoc("Semantic Topological Clustering (SToC)");
		
		/** The to string. */
		private final String toString;
		// We want to be able to lookup enum value based on the label property
		/** The lookup. */
		// This map holds all enums keyed on 'label'
		private static Map<String, Algorithms> lookup = new HashMap<String, Algorithms>();
		static {
			// Populate out lookup when enum is created
			for (Algorithms e : Algorithms.values()) {
			lookup.put(e.label(), e);
			}
		}
		
		/**
		 * Gets the.
		 *
		 * @param label the label
		 * @return the algorithms
		 */
		// Provide a method to lookup up enum with matching label
		public static Algorithms get(String label) {
			return lookup.get(label);
		}
		
		/**
		 * Instantiates a new algorithms.
		 *
		 * @param toString the to string
		 */
		private Algorithms(String toString) {
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

	/**
	 * The main method. Useful for command-line invocation.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		Options.initialize();
		System.out.println("-- ModuleGraphClustering starts " + Options.time());
		Graph<Node, Edge> graph = loadGraph(Options.getModuleGraphBuilderOutput());
		start(graph);
		System.out.println("-- ModuleGraphClustering ends " + Options.time());
	}
	
	/**
	 * Start processing.
	 *
	 * @param graph the graph
	 * @return the list
	 * @throws Exception the exception
	 */
	public static List<List<Node>> start(Graph<Node, Edge> graph) throws Exception {
		System.out.println("Graph edges: " + graph.getEdgeCount());
		System.out.println("Graph vertices: " + graph.getVertexCount());
		List<Node> isolated = new ArrayList<Node>();
		if( Options.areIsolateNodeConsidered() ) {
			isolated = loadIsolateNodes(Options.getIsolateNodesTablePath());
			System.out.println("Graph isolated: " + isolated.size() + " with > " + Options.getThresholdMinWeightGroupIsolated() + " individuals/directors.");
		}
		int nisolated = isolated.size();
		List<List<Node>> setCommunities = clustering(graph, nisolated);
		for(Node isolateNode: isolated){
			List<Node> tmp = new ArrayList<Node>();
			tmp.add(isolateNode);
			setCommunities.add(tmp);
		}
		System.out.println("Units in output: " + setCommunities.size());
		PrintWriter companyUnitWriter = IOUtil.getWriter(Options.getModuleGraphClusteringOutput());
		String nodeID = Options.getNodeMetadata();
		companyUnitWriter.println(nodeID + Options.getDelimiter() + "unitID");
		int unitID = 0;
		for (List<Node> unit : setCommunities) {
			for (Node node : unit) 
				companyUnitWriter.println(node.getID() + Options.getDelimiter() + unitID);
			unitID++;
		}
		companyUnitWriter.close();
		return setCommunities;
	}

	/**
	 * Write distribution to file.
	 *
	 * @param graph the input graph.
	 * @param communities the graph communities.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static int writeDistribution(Graph<Node, Edge> graph, List<List<Node>> setCommunities, int nisolated) throws IOException {
		int maxsize = 1;
		for( List<Node> com :setCommunities ) 
			if( com.size() > maxsize )
				maxsize = com.size();
		Distribution sizeCom = new Distribution(1, maxsize, 1);
		if(Options.areIsolateNodeConsidered())
			sizeCom.add(1, nisolated);	
		else
			sizeCom.add(1, 0);
		for( List<Node> com :setCommunities ) 
			if(com.size()<maxsize)
				sizeCom.add(com.size());
		sizeCom.output(Options.getFolderOutput()+"distributionUnitSize_"+Options.printDate(Options.getTime())+".csv");

		int maxw = 0;
		for (Edge edge : graph.getEdges()) {
			int w = edge.getWeight();
			if(w>maxw)
				maxw = w;
		}				
		Distribution sizeW = new Distribution(0, maxw, 1);
		sizeW.add(0, nisolated);
		for (Edge edge : graph.getEdges())
			sizeW.add( edge.getWeight() );
		sizeW.output(Options.getFolderOutput()+"distributionEdgeWeight_"+Options.printDate(Options.getTime())+".csv");

		int maxd = 0;
		for (Node node : graph.getVertices() ) {
			int d = graph.getNeighborCount(node);
			if( d > maxd)
				maxd = d;
		}				
		Distribution sizeD = new Distribution(0, maxd, 1);
		sizeD.add(0, nisolated);
		for (Node node : graph.getVertices())
			sizeD.add( graph.getNeighborCount(node) );
		sizeD.output(Options.getFolderOutput()+"distributionNodeDegree_"+Options.printDate(Options.getTime())+".csv");
		return maxsize;
	}

	/**
	 * Load isolate nodes.
	 *
	 * @param pathFile the path file
	 * @return the list
	 * @throws Exception the exception
	 */
	public static List<Node> loadIsolateNodes(String pathFile) throws Exception {
		List<Node> result = new ArrayList<Node>();
		StringTokenizer tokenLine;
		String line;
		BufferedReader reader = IOUtil.getReader( pathFile );
		reader.readLine(); // no header
		while ((line = reader.readLine()) != null) {
			tokenLine = new StringTokenizer(line, Options.getDelimiter());
			int companyID = Integer.parseInt(tokenLine.nextToken());
			int weight = Integer.parseInt(tokenLine.nextToken());
			if (weight > Options.getThresholdMinWeightGroupIsolated() ) {
				Node nodeIsolate = new Node(companyID);
				result.add(nodeIsolate);
			}
		}
		reader.close();
		return result;
	}



	/**
	 *  
	 *  Extracts the biggest connected component from a graph and writes her edges on a file .
	 *
	 * @param giantComponent the giant component
	 * @param graph the graph that contains the giant component
	 * @return the giant component as a graph
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Graph<Node, Edge> extractGiantComponent(List<Node> giantComponent, Graph<Node, Edge> graph) throws IOException {
		Graph<Node, Edge> subGraph = new UndirectedSparseGraph<Node, Edge>();
		Set<Edge> edges = new HashSet<Edge>();
		/* Retrieve the edge of the giant component*/
		for (Node node : giantComponent) 
			edges.addAll(graph.getOutEdges(node));
		/* Rebuild the links of the giant component.*/
		for (Edge edge : edges) {
			Pair<Node> edgeTmp = graph.getEndpoints(edge);
			subGraph.addEdge(edge, edgeTmp.getFirst(), edgeTmp.getSecond(),	EdgeType.UNDIRECTED);
		}
		return subGraph;
	}

	/**
	 * Clustering algorithm.
	 *
	 * @param graph the graph
	 * @param nisolated the number of isolated
	 * @return the list
	 * @throws Exception the exception
	 */
	public static List<List<Node>> clustering(Graph<Node, Edge> graph, int nisolated) throws Exception {
		List<List<Node>> setCommunities = null;
		List<Node> giantComponent=null;
		Graph<Node,Edge> subgraphGiantCC=null;
		List<List<Node>> setCommunitiesFromGC=null;
		switch (Options.getAlgorithm()) {
		case removeEdgesFromGCC:
			System.out.println("Algorithm: filterEdgeGCForWeight");
			int maxEdgeWeight = Options.getEdgeWeight();
			setCommunities =  extractWCCs(graph);
			int maxsize = writeDistribution(graph, setCommunities, nisolated);
			System.out.println("Communities before: "  + (nisolated + setCommunities.size()));
			giantComponent = returnGCC(setCommunities);
			System.out.println("GC nodes: " + giantComponent.size());
			setCommunities.remove(giantComponent);
			subgraphGiantCC = extractGiantComponent(giantComponent, graph);
			int totGiantEdges = subgraphGiantCC.getEdgeCount();
			System.out.println("GC edges: " + totGiantEdges);
			int rmv = removeEdgeFilteringWeight(subgraphGiantCC, maxEdgeWeight);
			System.out.println("Removing from GC edges with weight <= "+maxEdgeWeight);
			System.out.println("Removed: "+rmv+" edges.");
			setCommunitiesFromGC = extractWCCs(subgraphGiantCC);
			System.out.println("GC split into: " + setCommunitiesFromGC.size() + " CCs.");
			setCommunities.addAll(setCommunitiesFromGC);
			// Output distribution after
			Distribution sizeComAfter = new Distribution(1, maxsize, 1);
			for( List<Node> com :setCommunities ) 
				sizeComAfter.add(com.size());
			sizeComAfter.add(1, nisolated);
			sizeComAfter.output(Options.getFolderOutput()+"distributionUnitSizeNoGiant_"+Options.printDate(Options.getTime())+".csv");			
			break;
		case filterEdgesWeight:
			System.out.println("Algorithm: filterEdges");
			/* Filter the edges of the graph with a lower threshold passed by parameter.*/
			maxEdgeWeight = Options.getEdgeWeight();
			removeEdgeFilteringWeight(graph, maxEdgeWeight);			
			/* Extract the WCCs from the graph */
			setCommunities = extractWCCs(graph);
			writeDistribution(graph, setCommunities, nisolated);
			break;
		case removeGCC:
			System.out.println("Algorithm: removeGiantComponent");
			setCommunities =  extractWCCs(graph);
			giantComponent = returnGCC(setCommunities);
			setCommunities.remove(giantComponent);
			writeDistribution(graph, setCommunities, nisolated);
			break;
		case WCCs:
		default:
			System.out.println("Algorithm: wccs");
			/* Weak connected component algorithm*/
			setCommunities = extractWCCs(graph);
			writeDistribution(graph, setCommunities, nisolated);
			break;	
		}
		return setCommunities;
	}
	
	/**
	 * Finds all weak components in a graph as sets of vertex sets. A weak component
	 * is defined as a maximal subgraph in which all pairs of vertices in the
	 * subgraph are reachable from one another in the underlying undirected
	 * subgraph.
	 * <p>
	 * This implementation identifies components as sets of vertex sets. To create
	 * the induced graphs from any or all of these vertex sets, see
	 * <code>algorithms.filters.FilterUtils</code>.
	 * <p>
	 * Running time: O(|V| + |E|) where |V| is the number of vertices and |E| is the
	 * number of edges.
	 *
	 * @author Scott White
	 * Extracts the weak components from a graph.
	 * @param <N> the number type
	 * @param <E> the element type
	 * @param graph the graph
	 * @return 	the ordered (by dimension  - DECR - in position 0 the biggest)
	 * 			list of connected components
	 */
	public static <N, E> List<List<N>> extractWCCs( Graph<N, E> graph ) {
		Cronos crono = new Cronos();
		crono.start();
		List<List<N>> result = new ArrayList<List<N>>();
		HashSet<N> unvisitedVertices = new HashSet<N>(graph.getVertices());
		/* Until every node is visited*/
		while (!unvisitedVertices.isEmpty()) {
			List<N> cluster = new ArrayList<N>();
			N root = unvisitedVertices.iterator().next();
			unvisitedVertices.remove(root);
			cluster.add(root);
			Buffer<N> queue = new UnboundedFifoBuffer<N>();
			queue.add(root);
			while (!queue.isEmpty()) {
				N currentMyNode = queue.remove();
				Collection<N> neighbors = graph.getNeighbors(currentMyNode);
				for (N neighbor : neighbors) {
					if (unvisitedVertices.contains(neighbor)) {
						queue.add(neighbor);
						unvisitedVertices.remove(neighbor);
						cluster.add(neighbor);
					}
				}
			}
			result.add(cluster);
		}
		crono.stop();
		System.out.println("WCCs found in " + crono.elapsed() + " secs.");
		return result;
	}

	/**
	 * Return Giant Component.
	 *
	 * @param <N> the number type
	 * @param clusters the clusters
	 * @return the list
	 */
	public static <N> List<N> returnGCC(List<List<N>> clusters){
		int pos=0;
		int max=clusters.get(0).size();
		for(int i = 1; i<clusters.size();i++) {
			List<N> com = clusters.get(i);
			if(com.size()>max) {
				pos = i;
				max = com.size();
			}
		}
		return clusters.get(pos);
	}
	
	/**
	 * Removes the edge filtering weight.
	 *
	 * @param graph the graph
	 * @param maxEdgeWeight the max edge weight
	 * @return the int
	 */
	public static int removeEdgeFilteringWeight(Graph<Node, Edge> graph, int maxEdgeWeight){
		int count=0;
		for (Edge edge : new ArrayList<>(graph.getEdges()))
			if ( edge.getWeight() <= maxEdgeWeight) {
				graph.removeEdge(edge);
				count++;
			}
		return count;
	}
	
	/**
	 * Load the files that contain entities (checking the type) for build (and
	 * return) the object graph. If type < 0 it skips the check.
	 *
	 * @param filePath the file path
	 * @return the graph
	 */
	public static Graph<Node, Edge> loadGraph(String filePath) {
		Graph<Node, Edge> graph = new UndirectedSparseGraph<Node, Edge>();
		String line;
		StringTokenizer tokenLine;
		try {
			//row structure <source,destination,weight>
			BufferedReader reader = IOUtil.getReader( filePath );
			// Discard the file headers
			reader.readLine();
			/* Insert every edges loaded in the graph */
			int edgeID=0;
			while ((line = reader.readLine()) != null) {
				/* Read the node ID and the activity code (ateco) */
				tokenLine = new StringTokenizer(line, Options.getDelimiter());
				int sourceID 		= Integer.parseInt(tokenLine.nextToken());
				int destinationID 	= Integer.parseInt(tokenLine.nextToken());
				int weight 			= Integer.parseInt(tokenLine.nextToken());
				if(sourceID==destinationID || weight <= 0)
					throw new RuntimeException("");
				Edge edge = new Edge(edgeID, weight);
				Node source = new Node(sourceID);
				Node destination = new Node(destinationID);
				/* Check the number of shared directors for load an edge between two companies*/
				graph.addVertex(source);
				graph.addVertex(destination);
				graph.addEdge(edge, source, destination,EdgeType.UNDIRECTED);
				edgeID++;
			}
			//System.out.println(nlines + " edges inserted.");
			reader.close();
		} catch (IOException ioException) {
			System.out.println("File for building the graph not found.");
		}
		System.out.println("Graph loaded ( "+filePath+")");
		return graph;
	}
}
