package scube;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFPivotTable;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import scube.utils.IOUtil;
import scube.utils.LabelsGui;

/**
 * The Class ModuleVisualizer generates .xlsx style sheets and Pivot tables.
 */
public class ModuleVisualizer {
	
	/** The wb. */
	private static XSSFWorkbook wb;
	
	/** The format. */
	private static DataFormat format;
	
	/** The max num characters. */
	public static int maxNumCharacters=0;
	
	/** The font. */
	static String font = "Courier New";
	
	/**
	 * The main method. Useful for command-line invocation.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		Options.initialize();
		System.out.println("-- ModuleVisualizer starts " + Options.time());
		start();
		System.out.println("-- ModuleVisualizer ends " + Options.time());
	}

	/**
	 * Initialize.
	 */
	public static void initialize() {
		wb = new XSSFWorkbook();
		format = wb.createDataFormat();
	}
	
	/**
	 * Start.
	 */
	public static void start() {
		initialize();
		int sheetIndex = wb.getSheetIndex(LabelsGui.getFrameworkName());
		XSSFSheet sheet = null;
		if (sheetIndex == -1){
			System.out.println("Creating Excel file.");
			sheet = wb.createSheet(LabelsGui.getFrameworkName());
		}
		else{
			System.out.println("Problem on overwriting information on the same file ");
		}
		Row row;
		Cell cell;
		try {
			BufferedReader reader = IOUtil.getReader( Options.getModuleVisualizerInput() );
			// header
			String[] header = reader.readLine().split(Options.getDelimiter());// header
//			String header[] = Configurator.filterHeader(tmp);
			CellStyle csHeader = getStyleHeader(wb);
			row = sheet.createRow(0);
			for (int col = 0; col < header.length; col++) {
				cell = row.createCell(col);
				cell.setCellValue(header[col]);
				cell.setCellStyle(csHeader);
			}

			String line;
			int rows = 1;
			while ((line = reader.readLine()) != null) {
				String[] values = line.split(Options.getDelimiter(),-1);
//				System.out.println(Arrays.toString(values));
				row = sheet.createRow(rows);
				for (int col = 0; col < header.length; col++) {
					cell = row.createCell(col);
					if (isIndexPos(col, header.length)) {
						cell.setCellValue(Double.parseDouble(values[col]));
						cell.setCellStyle(getIndexStyleCells(wb));
					} else if (isIntegerParseInt(values[col])) {
						cell.setCellValue(Integer.parseInt(values[col]));
						cell.setCellStyle(getIntStyleCells(wb));
					} else if (isDoubleParseDouble(values[col])) {
						cell.setCellValue(Double.parseDouble(values[col]));
						cell.setCellStyle( getDoubleStyleCells(wb));
					} else {
						cell.setCellValue(values[col]);
						cell.setCellStyle(getInternalStyleCell(wb));
					}
					if(isDatePos(col, header.length)){
						cell.setCellStyle(getDateStyleCells(wb));
					}
				}
				rows++;
				if (rows % 100000 == 0)
					System.gc();
			}
			sheet.createFreezePane(0,1);//block the first row
			String autoFilter = CellReference.convertNumToColString(0) + "1:" + CellReference.convertNumToColString(header.length-1) + "1";
			sheet.setAutoFilter(CellRangeAddress.valueOf(autoFilter));
			insertPivotTable(wb, sheet, header.length, rows);
			System.gc();
			FileOutputStream fileOut = new FileOutputStream(Options.getModuleVisualizerOutput());
			wb.write(fileOut);
			fileOut.close();
			reader.close();
			wb.close();
			wb = null;
			headerCel = internalCel = doubleCel = indexCel = intCel = dateCel = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	


	/**
	 * Gets the style Header.
	 *
	 * @param wb the wb
	 * @return the style H eader
	 */
	public static CellStyle getStyleHeader(Workbook wb) {
		if (headerCel == null) {
			// Cell style for header row
			CellStyle cs = wb.createCellStyle();
			cs.setFillForegroundColor(HSSFColor.AQUA.index);
			cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			Font f = wb.createFont();			
			f.setFontName(font);
			f.setBold(true);
			f.setFontHeightInPoints((short) 12);
			cs.setFont(f);
			headerCel = cs;
		}
		return headerCel;
	}

	/**
	 * Gets the internal style cell.
	 *
	 * @param wb the wb
	 * @return the internal style cell
	 */
	public static CellStyle getInternalStyleCell(Workbook wb) {
		if (internalCel == null) {
			// Cell style for header row
			CellStyle cs = wb.createCellStyle();
			formatCell(cs);
			internalCel = cs;
		}
		return internalCel;
	}
	
	/**
	 * Format cell.
	 *
	 * @param cs the cs
	 */
	public static void formatCell(CellStyle cs){
		cs.setFillForegroundColor(HSSFColor.WHITE.index);
		cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		Font f = wb.createFont();
		f.setBold(true);
		f.setFontHeightInPoints((short) 10);
		cs.setFont(f);		
	}
	
	/**
	 * Gets the int style cells.
	 *
	 * @param wb the wb
	 * @return the int style cells
	 */
	public static CellStyle getIntStyleCells(Workbook wb) {
		if (intCel == null) {
			CellStyle cs = wb.createCellStyle();
			formatCell(cs);
			cs.setDataFormat(format.getFormat("#,##0"));
			intCel = cs;
		}
		return intCel;
	}

	/**
	 * Gets the double style cells.
	 *
	 * @param wb the wb
	 * @return the double style cells
	 */
	public static CellStyle getDoubleStyleCells(Workbook wb) {
		if (doubleCel == null) {
			CellStyle cs = wb.createCellStyle();
			formatCell(cs);
			cs.setDataFormat(format.getFormat("#,##0.000"));
			doubleCel = cs;
		}
		return doubleCel;
	}

	/**
	 * Gets the index style cells.
	 *
	 * @param wb the wb
	 * @return the index style cells
	 */
	public static CellStyle getIndexStyleCells(Workbook wb) {
		if (indexCel == null) {
			CellStyle cs = wb.createCellStyle();
			formatCell(cs);
			cs.setDataFormat(format.getFormat("0.00"));
			indexCel = cs;
		}
		return indexCel;
	}
	
	/**
	 * Gets the date style cells.
	 *
	 * @param wb the wb
	 * @return the date style cells
	 */
	public static CellStyle getDateStyleCells(Workbook wb) {
		if (dateCel == null) {
			CellStyle cs = wb.createCellStyle();
			formatCell(cs);
			cs.setDataFormat(format.getFormat("YYYY-MM-DD"));
			dateCel = cs;
		}
		return dateCel;
	}

	/**
	 * Checks if is integer parse int.
	 *
	 * @param str the str
	 * @return true, if is integer parse int
	 */
	public static boolean isIntegerParseInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException nfe) {
		}
		return false;
	}

	/**
	 * Checks if is double parse double.
	 *
	 * @param str the str
	 * @return true, if is double parse double
	 */
	public static boolean isDoubleParseDouble(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException nfe) {
		}
		return false;
	}

	/**
	 * Checks if is index pos.
	 *
	 * @param i the i
	 * @param end the end
	 * @return true, if is index pos
	 */
	public static boolean isIndexPos(int i, int end) {
		boolean result = (i >= end -  ModuleSegregationDataCubeBuilder.nIndex - 1) && i < end-1;
//		System.out.println(i +" end " + end + " indexes : "+ indexes +" isIndexPos:"+result);
		return (result);
	}
	
	/**
	 * Checks if is date pos.
	 *
	 * @param i the i
	 * @param end the end
	 * @return true, if is date pos
	 */
	public static boolean isDatePos(int i, int end) {
		return (i == end - 1);
	}	

	/**
	 * Insert pivot table.
	 *
	 * @param wb the wb
	 * @param sourceSheet the source sheet
	 * @param cols the cols
	 * @param rows the rows
	 */
	public static void insertPivotTable(XSSFWorkbook wb, XSSFSheet sourceSheet, int cols, int rows) {
		/* Create Pivot Table on a separate worksheet */
		/* Add a new sheet to create Pivot Table */
		int sheetIndex = wb.getSheetIndex(LabelsGui.getPivotTableName());
		XSSFSheet pivot_sheet = null;
		if (sheetIndex == -1) {
			System.out.println("Sheet and pivot table created.");
			pivot_sheet = wb.createSheet(LabelsGui.getPivotTableName());
		} else {
			System.out.println("Problem on overwriting information over the same file ");
		}
		AreaReference a = new AreaReference(new CellReference(CellReference.convertNumToColString(0) + 1),
				new CellReference(CellReference.convertNumToColString(cols - 1) + rows));
		CellReference b = new CellReference(CellReference.convertNumToColString(0) + "1");
		/* Create Pivot Table on a separate worksheet */
		XSSFPivotTable pivotTable = pivot_sheet.createPivotTable(a, b, sourceSheet);
		// Configure the pivot table
		pivotTable.addRowLabel(1);
		int index =0;
		for (int i = 0; i < cols; i++) {
			if (isIndexPos(i, cols)) {
				pivotTable.addColumnLabel( (i== cols-3) ? DataConsolidateFunction.MIN : DataConsolidateFunction.MAX, 
						i, ModuleSegregationDataCubeBuilder.Index.values()[index].toString());
				index++;
			}
		}
	}
	
	/**
	 * Sorts (A-Z) rows by String column.
	 *
	 * @param sheet - sheet to sort
	 * @param column - String column to sort by
	 * @param rowStart - sorting from this row down
	 */
	@SuppressWarnings("unused")
	private static void sortSheet(XSSFSheet sheet, int column, int rowStart) {
	    boolean sorting = true;
	    int lastRow = sheet.getLastRowNum();
	    while (sorting == true) {
	        sorting = false;
	        for (Row row : sheet) {
	            // skip if this row is before first to sort
	            if (row.getRowNum()<rowStart) continue;
	            // end if this is last row
	            if (lastRow==row.getRowNum()) break;
	            Row row2 = sheet.getRow(row.getRowNum()+1);
	            if (row2 == null) continue;
	            double firstValue = (row.getCell(column) != null) ? row.getCell(column).getNumericCellValue() : 0;
	            double secondValue = (row2.getCell(column) != null) ? row2.getCell(column).getNumericCellValue() : 0;
	            //compare cell from current row and next row - and switch if secondValue should be before first
	            if (secondValue > firstValue) {                    
	                sheet.shiftRows(row2.getRowNum(), row2.getRowNum(), -1);
	                sheet.shiftRows(row.getRowNum(), row.getRowNum(), 1);
	                sorting = true;
	            }
	        }
	    }
	}
	
	/** The int cel. */
	private static CellStyle intCel;
	
	/** The header cel. */
	private static CellStyle headerCel;

	/** The internal cel. */
	private static CellStyle internalCel;
	
	/** The double cel. */
	private static CellStyle doubleCel;
	
	/** The index cel. */
	private static CellStyle indexCel;
	
	/** The date cel. */
	private static CellStyle dateCel;
}
