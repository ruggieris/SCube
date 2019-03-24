package scube.utils;

import java.io.IOException;
import java.util.Properties;

import scube.Options;


public class LabelsGui {

	public static Properties props = null;
	public final static String propsFilePath = "labelGUI.props";
	public final static String frameworkName = "SCube v. 1.1";

	public static void initialize(String fileProps) throws IOException {
		if (props == null)
			if (fileProps == null)
				LabelsGui.props = IOUtil.readProps(propsFilePath);
			else
				LabelsGui.props = IOUtil.readProps(fileProps);
	}

	public static String getMainTabName() {
		return props.getProperty("mainTabName");

	}

	public static String getFont() {
		return props.getProperty("font");
	}

	public static int getFontSize() {
		return Integer.parseInt(props.getProperty("fontSize"));
	}

	public static String getMainWindowTitle() {
		return getFrameworkName() + " " + props.getProperty("mainWindowTitle");
	}

	public static String getFrameworkName() {
		return frameworkName;
	}

	public static String getPivotTableName() {
		return props.getProperty("frameworkName")+" Pivot Table";
	}
	
	public static String getSecondIterationPivotTableName(String date) {
		return props.getProperty("frameworkName") + date + " Pivot Table";
	}
	public static String getBodsChooserTitle() {
		return props.getProperty("bodChooserTitle");
	}

	public static String getDirectorChooserTitle() {
		return props.getProperty("directorChooserTitle");
	}

	public static String getCompanyChooserTitle() {
		return props.getProperty("companyChooserTitle");
	}

	public static String getStartButtonName() {
		return props.getProperty("startButtonName") + getFrameworkName();
	}

	public static String getFolderOutputTitle() {
		return props.getProperty("folderOutputChooserName");
	}
	public static String getConsiderIsolateLabel() {
		return props.getProperty("considerIsolate");
	}
	
	public static String getConsideringIsolateNodesLabel() {
		return props.getProperty("consideringIsolateNodesLabel");
	}

	public static String getEdgeWeightWindowName() {
		return props.getProperty("edgeWeightWindowName");
	}
	public static String getEdgeWeightLabel() {
		return props.getProperty("edgeWeightLabel");
	}
	
	public static String getEdgeWeightDynamicLabel() {
		return props.getProperty("edgeWeightDynamicLabel");
	}
	
	public static String getMinSuppWindowName() {
		return props.getProperty("minSuppWindowName");
	}

	public static String getMinSupportLabel() {
		return props.getProperty("minSupportLabel");
	}

	public static String getLabelConsole() {
		return props.getProperty("labelConsole")+" "+getFrameworkName();
	}

	public static String getClusteringFrameName() {
		return props.getProperty("clusteringFrameName");
	}
	public static String getClusteringLabelComboBox() {
		return props.getProperty("clusteringLabelComboBox");
	}

	public static String getOldResultLabel() {
		return props.getProperty("oldResultLabel");
	}

	public static String getInputParametersLabel() {
		return props.getProperty("inputParametersLabel");
	}

	public static String getLabelCAAttribute() {
		return props.getProperty("labelCAattributes") +"\"" + Options.getDelimiter()+"\"";
	}

	public static Object getQuestionResultView() {
		return props.getProperty("questionResult");
		}

	public static String getSuccesfullyTerminated() {
		return props.getProperty("successTermination");
	}


	public static String getClearConsoleName() {
		return props.getProperty("clearConsoleName");
	}

	public static String getLabelDate() {
		return props.getProperty("labelDate")  +"\"" + Options.getDelimiter()+"\"";
	}

	public static String getLabelAttributesToIgnore() {
		return props.getProperty("attributeToIgnore")  +"\"" + Options.getDelimiter()+"\"";
	}
}
