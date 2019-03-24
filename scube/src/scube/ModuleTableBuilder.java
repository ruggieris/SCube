package scube;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import scube.Options.Projection;
import scube.utils.IOUtil;
import scube.utils.Pair;

/**
 * The Class ModuleTableBuilder builds a final table ready for segregation discovery.
 */
public class ModuleTableBuilder {

	/**
	 * The main method. Useful for command-line invocation.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		Options.initialize();
		System.out.println("-- ModuleTableBuilder starts " + Options.time());
		LocalDateTime[] dates = Options.getDates(Options.getTimeLabel(),Options.getDateSeparator());
		start( (dates != null) ? dates[dates.length-1] : null );
		System.out.println("-- ModuleTableBuilder ends " + Options.time());
	}

	/**
	 * Start processing.
	 *
	 * @param time the time snapshot.
	 * @throws Exception the exception
	 */
	public static void start(LocalDateTime time) throws Exception {
		System.out.println("Filtering by date: " + (time != null ? Options.printDate(time) : "none") + ".");
		String individualPath = Options.getIndividualFilePath();
		String groupPath = Options.getGroupFilePath();
		if(Options.getProjectionMode() == Projection.Individual) {
			String tmp = individualPath;
			individualPath = groupPath;
			groupPath = tmp;
		}
		Map<Integer, List<Set<String>>> company = readGroups(groupPath);
		Map<Integer, Integer> companyUnit = readNodeUnit(Options.getNodeUnitFilePath());
		Map<Integer, Set<Integer>> directorUnits = new HashMap<Integer, Set<Integer>>();
		Map<Pair<Integer, Integer>, List<Set<String>>> directorUnitAtts = new HashMap<Pair<Integer, Integer>, List<Set<String>>>();

		BufferedReader companyReader = IOUtil.getReader(groupPath);
		String companyHeader = companyReader.readLine();
		int ncompanyAtts = companyHeader.split(Options.getDelimiter()).length - 1;
		companyHeader = companyHeader.substring(companyHeader.indexOf(Options.getDelimiter()) + 1); // remove first
		companyReader.close();

		BufferedReader directorReader = IOUtil.getReader(individualPath);
		String directorHeader = directorReader.readLine();
		directorHeader = directorHeader.substring(directorHeader.indexOf(Options.getDelimiter()) + 1); // remove first
		PrintWriter writer = IOUtil.getWriter( Options.getModuleTableBuilderOutput());
		writer.println(directorHeader + Options.getDelimiter() + companyHeader + Options.getDelimiter() + "unitID");
		BufferedReader bodsReader =IOUtil.getReader(Options.getMembershipFilePath());
		bodsReader.readLine(); // header
		String line;
		int posCom= (Options.getProjectionMode()==Projection.Group) ? 0 : 1;
		int posDir= 1-posCom;
		while ((line = bodsReader.readLine()) != null) {
			String[] tokens = line.split(Options.getDelimiter());
			int companyID = Integer.parseInt(tokens[posCom]);
			int directorID = Integer.parseInt(tokens[posDir]);
			if (tokens.length >= 4) {
				LocalDateTime startd = Options.extractDate(tokens[2],Options.getDateSeparator());
				LocalDateTime end = Options.extractDate(tokens[3],Options.getDateSeparator());
				if (!Options.edgeIsValid(startd, end, time)) {
					continue;
				}
			}
			Integer unit = companyUnit.get(companyID);
			if (unit == null) // could be e.g., isolated
				continue;
			Pair<Integer, Integer> directorUnit = new Pair<Integer, Integer>(directorID, unit);
			if (!directorUnits.containsKey(directorID))
				directorUnits.put(directorID, new HashSet<Integer>());
			directorUnits.get(directorID).add(unit);

			List<Set<String>> atts = directorUnitAtts.get(directorUnit);
			List<Set<String>> companyAtts = company.get(companyID);
			if (atts == null)
				directorUnitAtts.put(directorUnit, companyAtts);
			else
				for (int i = 0; i < ncompanyAtts; i++)
					atts.get(i).addAll(companyAtts.get(i));
		}
		bodsReader.close();
		int nrows = 0;
		String[] tokens = null;
		while ( (line = directorReader.readLine()) != null) {
			tokens = line.split(Options.getDelimiter(), -1);
			int dirID = Integer.parseInt(tokens[0]);
			Set<Integer> units = directorUnits.get(dirID);
			if (units == null) // could be e.g., a director in a isolated
				continue;
			line = line.substring(line.indexOf(Options.getDelimiter()) + 1); // remove first
			for (Integer unitID : units) {
				Pair<Integer, Integer> directorUnit = new Pair<Integer, Integer>(dirID, unitID);
				writer.println(line + Options.getDelimiter()
						+ getCompanyAttributes(directorUnitAtts.get(directorUnit)) + Options.getDelimiter()
						+ unitID);
				++nrows;
			}
		}
		directorReader.close();
		writer.close();
		System.out.println("Rows in output: " + nrows);
		System.out.println("CSV in output: " + Options.getModuleTableBuilderOutput());
	}

	/**
	 * Read groups/company CSV files. Attributes are discrete, possibly multivalued.
	 *
	 * @param pathFile the file path
	 * @return the map
	 */
	public static Map<Integer, List<Set<String>>> readGroups(String pathFile) {
		HashMap<Integer, List<Set<String>>> result = new HashMap<Integer, List<Set<String>>>();
		try {
			BufferedReader reader = IOUtil.getReader(pathFile);
			reader.readLine();// skip header
			String[] companyColumns = null;
			while ((companyColumns = IOUtil.readTokens(reader, Options.getDelimiter())) != null) {
				int companyID = Integer.parseInt(companyColumns[0]);
				if (!result.containsKey(companyID)) {
					List<Set<String>> columns = new ArrayList<Set<String>>(companyColumns.length - 1);// ignore	companyID												// companyID
					for (int i = 1; i < companyColumns.length; i++) {
						Set<String> column = new HashSet<String>();
						String companyColum = companyColumns[i];
						if(!companyColum.equals(""))
							for (String columnValue : companyColum.split(Options.getMultiValuesDelimiter()))
								column.add(columnValue);
						columns.add(column);
					}
					result.put(companyID, columns);
				} else {
					reader.close();
					throw new RuntimeException("Error, in " + pathFile + " duplicated companyIDs.");
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Read mapping from node to unit after clustering.
	 *
	 * @param pathFile the path file
	 * @return the map
	 */
	public static Map<Integer, Integer> readNodeUnit(String pathFile) {
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		try {
			BufferedReader reader = IOUtil.getReader(pathFile);
			reader.readLine(); // skip header
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(Options.getDelimiter());
				int companyID = Integer.parseInt(tokens[0]);
				int unitID = Integer.parseInt(tokens[1]);
				result.put(companyID, unitID);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Gets the company attributes.
	 *
	 * @param atts the atts
	 * @return the company attributes
	 */
	public static String getCompanyAttributes(List<Set<String>> atts) {
		String result = "";
		boolean firstOuter = true;
		for (int i = 0; i < atts.size(); i++) {
			boolean first = true;
			if (!firstOuter)
				result += Options.getDelimiter();
			Set<String> values = atts.get(i);
			int nvalues = 0;
			for (String att : values) {
				result += (first ? "" : Options.getMultiValuesDelimiter()) + att;
				first = false;
				if(++nvalues==3) // TODO: create an option for multi-valued atts
					break;
			}
			firstOuter = false;
		}
		return result;
	}
}
