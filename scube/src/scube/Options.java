package scube;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;

import scube.ModuleGraphClustering.Algorithms;
import scube.Options;
import scube.utils.IOUtil;
import scube.utils.LabelsGui;

public class Options {
	private static String fimiWindowsExecutableProgram = "./lib/fpgrowth.exe";
	private static String fimiLinuxExecutableProgram = "./lib/fpgrowth";
	public static final LocalDateTime maxTime = LocalDateTime.of(2016, 03, 01, 0, 0);

	
	public static Properties props = null;
	public final static String propsFilePath = "varDef.props";
	public static LocalDateTime time;

	public enum Projection {
		Group, Individual
	}

	public static void initialize()  {
		if (props == null) {
			try {
				Options.props = IOUtil.readProps(propsFilePath);
				LabelsGui.initialize(null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public enum OSType {
		Windows, MacOS, Linux, Other
	};
	protected static OSType detectedOS;

	/**
	 * detect the operating system from the os.name System property and cache
	 * the result
	 * 
	 * @returns - the operating system detected
	 */
	public static OSType getOperatingSystemType() {
		if (detectedOS == null) {
			String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
			if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
				detectedOS = OSType.MacOS;
			} else if (OS.indexOf("win") >= 0) {
				detectedOS = OSType.Windows;
			} else if (OS.indexOf("nux") >= 0) {
				detectedOS = OSType.Linux;
			} else {
				detectedOS = OSType.Other;
			}
		}
		return detectedOS;
	}

	public static void initialize(String fileProps) throws IOException {
		if (props == null) {
			Options.props = IOUtil.readProps(fileProps);
			props = IOUtil.readProps(fileProps);
		}
	}

	
	public static String getFolderOutput() {
		return props.getProperty("folderOutput");
	}
	
	public static void setFolderOutput(String s) {
		props.setProperty("folderOutput", s);
	}

//	----------------------------------ETL PARAMETERS----------------------------------
	public static int getModuleDiscretizerBins() {
		return getIntProp("bins");
	}
	
	public static String getExtension() {
		return props.getProperty("extensionFile");
	}
	
	public static int getAgeMax() {
		if (props.getProperty("ageMax") != null)
			return Integer.valueOf(props.getProperty("ageMax"));
		else {
			System.out.println("Error in extracting(" + props.getProperty("ageMax") + ") \"ageMax\" parameter.");
			return -1;
		}
	}
	
	public static String getPlacesIDGeoCoordinates() {
		return Options.getGeolocalizationPathFolder() + props.getProperty("mapIDPlaceGeoCoordinates");
	}

	public static int getAgeMin() {
		if (props.getProperty("ageMin") != null)
			return Integer.valueOf(props.getProperty("ageMin"));
		else {
			System.out.println("Error in extracting(" + props.getProperty("ageMin") + ") \"ageMin\" parameter.");
			return -1;
		}
	}
	
	public static String getGeolocalizationPathFolder() {
		return props.getProperty("folderGeoLocations");
	}
	
	public static String getLabelPlaces() {
		return Options.getGeolocalizationPathFolder() + props.getProperty("mapIDPlaceLabel");
	}


	public static String getAtecoList() {
		return Options.getGeolocalizationPathFolder() + props.getProperty("atecoList");
	}

	public static String[] getPathFolderInputData() {
		return props.getProperty("pathFileInput").split(Options.getDelimiter());
	}

	public static String getModuleETLOutputDirector() {
		return props.getProperty("individualFilePath");
	}
	
	/**Return the bods - companyID,directorID*/
	public static String getModuleETLOutputBods() {
		return props.getProperty("membershipFilePath");
	}
	
	/**Return the company file path*/
	public static String getModuleETLOutputCompany() {
		return props.getProperty("groupFilePath");
	}

	//	----------------------------------GRAPH BUILDER PARAMETERS----------------------------------
	public static String getIsolateNodesTablePath() {
		return getFolderOutput() + props.getProperty("isolateNodeGraph");
	}

	/**Return the director file path*/
	public static String getIndividualFilePath() {
		return props.getProperty("individualFilePath");
	}
	
	/**Return the director file path*/
	public static void setDirectorFilePath(String s) {
		props.setProperty("individualFilePath", s);
	}	
	
	/**Return the bods file path - companyID,directorID*/
	public static String getMembershipFilePath() {
			return props.getProperty("membershipFilePath");
	}
	/**Set the bods file path - companyID,directorID*/
	public static void setBodsFilePath(String s) {
			props.setProperty("membershipFilePath",s);
	}

	/**Return the company file path*/
	public static String getGroupFilePath() {
		return props.getProperty("groupFilePath");
	}
	/**Set the company file path*/
	public static void setCompanyFilePath(String s) {
		props.setProperty("groupFilePath", s);
	}		
	
	public static String getModuleGraphBuilderOutput() {
		return getFolderOutput() + props.getProperty("edgeGraph");
	}
	
	public static Projection getProjectionMode() {
		int mode = getIntProp("projectionMode");
		if(mode==0)
			return Projection.Group;
		if(mode==1)
			return Projection.Individual;
		throw new RuntimeException("Invalid Setting of the variable projectionMode");
	}

	public static void setProjectionMode(String s) {
		props.setProperty("projectionMode", s);
	}

//	----------------------------------GRAPH CLUSTERING PARAMETERS----------------------------------
	/**
	 * Return the right header in relation to the mode
	 * @param mode if 0 project into a graph of groups, otherwise into a graph of individuals.
	 * @throws IOException
	 */
	public static String getNodeMetadata() throws IOException {
		Projection mode = Options.getProjectionMode();
		if (mode == Projection.Group) {
			BufferedReader reader;
			reader = IOUtil.getReader(Options.getGroupFilePath());
			String header = reader.readLine();// header
			reader.close();
			return header.split(Options.getDelimiter())[0];
		}
		if (mode == Projection.Individual) {
			BufferedReader reader = IOUtil.getReader(Options.getMembershipFilePath());
			String header = reader.readLine();// header
			reader.close();
			return header.split(Options.getDelimiter())[0];
		}
		throw new RuntimeException("getNodeMetadata(): Invalid Variable Projection Mode.");
	}
	
	/**Return the nodeUnit file path - companyID,unit*/
	public static String getModuleGraphClusteringOutput() {
		return getFolderOutput() + props.getProperty("nodeUnitFilePath");
	}	

	public static boolean areIsolateNodeConsidered() {
		return (props.getProperty("includeIsolateNode").equals("yes"));
	}

	public static void setIsolateNodeConsidered(String s) {
		props.setProperty("includeIsolateNode", s);
	}

	public static Algorithms getAlgorithm(){
		String algorithm = Options.getPartitioningGraphAlgorithm();
		switch (algorithm) {
		case "filterEdgeGCForWeight":
			return Algorithms.removeEdgesFromGCC;
		case "filterEdges":
			return Algorithms.filterEdgesWeight;
		case "removeGiantComponent":
			return Algorithms.removeGCC;
		case "stoc":
			return Algorithms.stoc;
		case "wccs":
		default:
			return Algorithms.WCCs;
		}
	}

	public static String getPartitioningGraphAlgorithm() {
		return props.getProperty("clusteringAlgorithm");
	}

	public static void setPartitioningGraphAlgorithm(String s) {
		props.setProperty("clusteringAlgorithm", s);
	}

	public static int getMaxDistance() {
		if (testIntValidity("maxDistance"))
			return getIntProp("maxDistance");
		else
			return -1;
	}

	public static int getEdgeWeight() {
		if (testIntValidity("edgeWeight"))
			return getIntProp("edgeWeight");
		else
			return -1;
	}	

	public static void setEdgeWeight(int s) {
		props.setProperty("edgeWeight",String.valueOf(s));
	}
//	----------------------------------TABLE BUILDER PARAMETERS----------------------------------
	
	/**Return the nodeUnit file path - nodeID,unit.
	 * We generalized the approach s.t. bipartite graph can be handled in both directions.*/
	public static String getNodeUnitFilePath() {
		return getFolderOutput() + props.getProperty("nodeUnitFilePath");
	}

	public static String getModuleTableBuilderOutput() {
		return getFolderOutput() +props.getProperty("finalTableFilePath");
	}
//	----------------------------------SEGREGATION PARAMETERS----------------------------------
	public static String getModuleSegregationInput() {
		return getFolderOutput() + props.getProperty("finalTableFilePath");
	}
	
	public static String getModuleSegregationCA() {
		return props.getProperty("moduleSegregationCA");
	}
	
	public static void setModuleSegregationCA(String s) {
		props.setProperty("moduleSegregationCA",s);
	}
	
	
	public static String getModuleSegregationIGNORE() {
		return props.getProperty("moduleSegregationIGNORE");
	}
	
	public static void setModuleSegregationIGNORE(String s) {
		props.setProperty("moduleSegregationIGNORE",s);
	}
	
	
	public static String getModuleSegregationOutput() {
		return getFolderOutput() + props.getProperty("moduleSegregationOutput");
	}
	
	public static double getAtkinsonParameter() {
		double res = 0.5;
		try {
			res = getDoubleProp("atkinsonParameter");
		} catch (Exception e) {
			System.out.println("Error in extracting Atkinson Parameter: it must be a double instead it is "
					+ props.getProperty("atkinsonParameter"));
			System.out.println("Handled using default value: " + res);
		}
		return res;
	}
	
	// ----------FPGROWTH PARAMETERS----------
	public static String getPathExecutableFIMI() {
		OSType os = getOperatingSystemType();
		String result = "";
		switch (os) {
		case Windows:
			result = fimiWindowsExecutableProgram;
			break;
		case Linux:
			result = fimiLinuxExecutableProgram;
			break;
		case MacOS:
		case Other:
		default:
			throw new RuntimeException(
					"There isn't an external library  (FP-growth Algorithm) for this operating system (" + os + "). ");
		}
		return result;
	}
	
	public static void setPathExecutableFIMI(String s) {
		props.setProperty("fimiExecutableProgram", s);
	}
	
	public static int getMinimumSupport() {
			return getIntProp("minimumSupport");
	}

	public static void setMinimumSupport(String s) {
		props.setProperty("minimumSupport", s);
	}
//	----------------------------------VISUALIZER PARAMETERS----------------------------------
	public static String getModuleVisualizerInput(){
		return Options.getFolderOutput() + props.getProperty("moduleVisualizerInput");
	}
	
	public static String getModuleVisualizerOutput(){
		return Options.getFolderOutput() + props.getProperty("moduleVisualizerOutput");
	}
	
//	----------------------------------UTILITY PARAMETERS----------------------------------
	public static int getIntProp(String key) {
		if (props.getProperty(key).equals(""))
			return -1;
		return Integer.parseInt(props.getProperty(key));
	}

	public static double getDoubleProp(String key) {
		return Double.parseDouble(props.getProperty(key));
	}

	public static String getProp(String key) {
		return props.getProperty(key);
	}

	/**Return ","*/
	public static String getDelimiter() {
		return props.getProperty("delimiter");
	}
	
	/**Return ";"*/
	public static String getMultiValuesDelimiter() {
		return props.getProperty("multiValuesDelimiter");
	}
	
	public static boolean testIntValidity(String parameter) {
		if (parameter.equals(""))
			return false;
		try {
			Integer.parseInt(props.getProperty(parameter));
		} catch (NumberFormatException e) {
			System.out.println("Tried to convert parameter " + parameter + "(" + props.getProperty(parameter)
					+ ") in integer failed.");
			return false;
		}
		return true;
	}
	
	/**
	 * Create a date from a String in the format YYYY-MM-DD-Hours-Minutes.
	 * 
	 * @param string the string in input
	 * @param mode   the time granularity (year,month, day).
	 */
	public static LocalDateTime getDate(String string) {
		if (string.isEmpty())
			return null;
		if (string.equals("0000-00-00"))
			return maxTime;
		String[] tokens = string.split("-");
		int year = Integer.parseInt(tokens[0]);
		int month = Integer.parseInt(tokens[1]);
		int day = Integer.parseInt(tokens[2]);
		// System.out.println(string+" --> " +year + "-" + month + "-" + day);
		return LocalDateTime.of(year, month, day, 0, 0);
	}

	public static LocalDateTime[] getDates(String labels, String internalSep) {
		if(labels==null || labels.isEmpty())
			return null;
		String[] time = labels.split(Options.getDelimiter());
		LocalDateTime[] times = new LocalDateTime[time.length];
		for (int i = 0; i < time.length; i++)
			times[i] = extractDate(time[i], internalSep);
		return times;
	}

	public static void setTime(LocalDateTime t) {
		time = t;
	}
	
	/**Return the separator used for defining a date (e.g. in 2016-01-01 is  "-").*/
	public static String getDateSeparator(){
		return "-";
	}
	
	/**
	 * Create a date from a String in the format YYYY-MM-DD-Hours-Minutes where "-" is a parameter.
	 * In case of "0000-00-00" is assigned the {@link Options#maxTime}
	 * @param string the string in input
	 * @param sep  the separator used in sintax YYYY-MM-DD-Hours.
	 */
	public static LocalDateTime extractDate(String string, String sep) {
		if (string.isEmpty())
			return null;
		if (string.equals("0000" + sep + "00" + sep + "00"))
			return maxTime;
		String[] tokens = string.split(sep);
		int year = Integer.parseInt(tokens[0]);
		int month = Integer.parseInt(tokens[1]);
		int day = Integer.parseInt(tokens[2]);
		// System.out.println(string+" --> " +year + "-" + month + "-" + day);
		return LocalDateTime.of(year, month, day, 0, 0);
	}
	
	public static boolean appendResults() {
		return props.getProperty("appendResults").equals("yes");
	}
	
	public static void setAppendResult(String s) {
		props.setProperty("appendResults",s);
	}

	public static boolean existResult(){
		File f = new File(getModuleSegregationOutput());
		boolean c = f.isFile() && !f.isDirectory();
		return c;
	}
	
	/**
	 * Check if the timeInterval is consistent with the start and end date, if
	 * timeInterval is null then return true.
	 */
	public static boolean edgeIsValid(LocalDateTime start, LocalDateTime end, LocalDateTime timeInterval) {
		if(timeInterval == null)
			return true;
		if (start == null || end == null)
			return false;
		if (start.isAfter(end))
			return false;
		// throw new RuntimeException("Edge end cannot be before its start (" +
		// start + "," + end + ")");
		if (start.isBefore(timeInterval) && end.isAfter(timeInterval))
			return true;
		else
			return false;
	}
	
	public static String printDate(LocalDateTime t) {
		if (t == null)
			return "ND";
		return t.getYear() + getDateSeparator() + t.getMonthValue() + getDateSeparator() + t.getDayOfMonth();
	}
	
	public static String timeToString() {
		if (time == null)
			return "";
		return time.getYear() + getDateSeparator() + time.getMonthValue() + getDateSeparator() + time.getDayOfMonth();
	}
	
	
	public static String getTimeLabel(){
		return props.getProperty("date");
	}

	public static LocalDateTime getTime() {
		return time;
	}
	
	public static String getNetClusterAlgorithmOption() {
		Algorithms algorithm = getAlgorithm();
		String alg = "";
		switch (algorithm) {
		case removeEdgesFromGCC:
			alg += Algorithms.removeEdgesFromGCC.toString() + getEdgeWeight();
			break;
		case filterEdgesWeight:
			alg = Algorithms.filterEdgesWeight.toString() + getEdgeWeight();
			break;
		case removeGCC:
			alg = Algorithms.removeGCC.toString();
			break;
		case stoc:
			// TODO: to be added
			// alg = Algorithms.stoc.toString() + "a_s" + getSemanticAttRatio() + "a_t" + getTopologicalAttRatio();
			// break;
		case WCCs:
		default:
			alg = Algorithms.WCCs.toString();
			break;
		}
		return alg;
	}
	
	public static String[] filterHeader(String[] oldHeader) {
		int toRemove = 0;
		HashSet<String> valuesToIgnore = new HashSet<String>(Arrays.asList(Options.getModuleSegregationIGNORE().split(Options.getDelimiter())));
		for (String s : oldHeader)
			if (valuesToIgnore.contains(s))
				toRemove++;
		String[] newHeader = new String[oldHeader.length - toRemove];
		int i = 0;
		int j = 0;
		while (j < newHeader.length) {
			if (!valuesToIgnore.contains(oldHeader[i])) {
				newHeader[j] = oldHeader[i];
				j++;
			}
			i++;
		}
		return newHeader;
	}

	public static String filterHeader(String oldHeader) {
		HashSet<String> valuesToIgnore = new HashSet<String>(Arrays.asList(Options.getModuleSegregationIGNORE().split(Options.getDelimiter())));
		String[] old = oldHeader.split(Options.getDelimiter());
		String newHeader = "";
		boolean first = true;
		for (String s : old)
			if (!valuesToIgnore.contains(s)) {
				newHeader += (first ? "" : Options.getDelimiter()) + s;
				first = false;
			}
		return newHeader;
	}

	/**Return a string filtered out the chars not equal to <code>needle</code>.*/
	public static String filterChar(String haystack, char needle) {
		String h = "";
		for (int i = 0; i < haystack.length(); i++) {
			if (haystack.charAt(i) == needle) {
				h += needle;
			}
		}
		return h;
	}
	
	public static int getThresholdMinWeightGroupIsolated() {
		return getIntProp("minimunWeightIsolated");
	}
	
	public static String time() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd '-' HH:mm:ss");
		String strDate = sdf.format(cal.getTime());
		return "[" + strDate +"]";
	}
}
