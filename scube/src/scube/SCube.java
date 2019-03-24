package scube;

import java.time.LocalDateTime;

import edu.uci.ics.jung.graph.Graph;
import scube.graph.Edge;
import scube.graph.Node;

/**
 * The Class SCube automatize the process of segregation discovery.
 */
public class SCube {
	
	/**
	 * The main method. Useful for command-line invocation.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) throws Exception {
		Options.initialize();
		System.out.println("-- SCube starts " + Options.time());
		start(null);
		System.out.println("-- SCube ends " + Options.time());
	}

	/**
	 * Start.
	 *
	 * @param t the time snapshot
	 */
	public static void start(LocalDateTime[] dates) throws Exception {
		if(dates==null)
			dates = Options.getDates(Options.getTimeLabel(),Options.getDateSeparator());
		if (dates != null) {
			boolean append = false;
			for (LocalDateTime date : dates) {
				execution(date, append);
				append = true;
			}
		}
		else
			execution(null, false);
		ModuleVisualizer.start();
	}
	
	/**
	 * Execute the process for a given date snapshot.
	 *
	 * @param d the date snapshot
	 */
	public static void execution(LocalDateTime d, boolean append ) throws Exception {
		Options.setTime(d);
		ModuleGraphBuilder.start(d);
		Graph<Node, Edge> graph = ModuleGraphClustering.loadGraph(Options.getModuleGraphBuilderOutput());
		ModuleGraphClustering.start(graph);
		ModuleTableBuilder.start(d);
		ModuleSegregationDataCubeBuilder.start(append);
	}
}
