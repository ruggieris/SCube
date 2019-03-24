package scube;

import javax.swing.JFrame;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Random;
import java.awt.Font;

import javax.swing.JCheckBox;
import javax.swing.JTextArea;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.uci.ics.jung.graph.Graph;
import scube.graph.Edge;
import scube.graph.Node;
import scube.ModuleGraphClustering.Algorithms;
import scube.utils.LabelsGui;
import scube.utils.MInteger;

import java.awt.Color;
import javax.swing.JTextField;

public class Gui {
	private static final JFrame frame = new JFrame();
	private final JPanel panel = new JPanel();
	private final JLabel labelParameters = new JLabel(LabelsGui.getInputParametersLabel());
	private final JButton buttonChooserFolderOutput = new JButton(LabelsGui.getFolderOutputTitle());
	private final JLabel labelChooserFolderOutput = new JLabel(Options.getFolderOutput());
	private final JFileChooser fileChooserFolderOutput = new JFileChooser(Options.getFolderOutput());

	private final JButton buttonChooserCompany = new JButton(LabelsGui.getCompanyChooserTitle());
	private final JLabel labelChooserCompany = new JLabel(Options.getGroupFilePath());
	private final JFileChooser fileChooserCompany = new JFileChooser(Options.getGroupFilePath());
	private final JButton buttonChooserBods = new JButton(LabelsGui.getBodsChooserTitle());
	private final JLabel labelChooserBods = new JLabel(Options.getMembershipFilePath());
	private final JFileChooser fileChooserBods = new JFileChooser(Options.getFolderOutput());

	private final JButton buttonDirectorChooser = new JButton(LabelsGui.getDirectorChooserTitle());
	private final JLabel labelChooserDirector = new JLabel(Options.getIndividualFilePath());
	private final JFileChooser fileChooserDirector = new JFileChooser(Options.getIndividualFilePath());

	private final JCheckBox chckbxConsiderIsolateNode = new JCheckBox(LabelsGui.getConsiderIsolateLabel());
	private final JLabel labelCAattributes = new JLabel(LabelsGui.getLabelCAAttribute());
	private final JTextField	textFieldCAattributes = new JTextField(Options.getModuleSegregationCA());
	
	private final JLabel labelIgnoreFields = new JLabel(LabelsGui.getLabelAttributesToIgnore());
	private final JTextField textFieldAttributesToIgnore = new JTextField(Options.getModuleSegregationIGNORE());
	
	private final JLabel labelDate = new JLabel(LabelsGui.getLabelDate());
	private final JTextField	textFieldDate = new JTextField(Options.getTimeLabel());

	private final JButton btnStartScube = new JButton(LabelsGui.getStartButtonName());
	private final JTextArea txtAreaConsole = new JTextArea();
	private final JScrollPane console = new JScrollPane(txtAreaConsole);
	private final JButton btnClearConsole = new JButton(LabelsGui.getClearConsoleName());
	private JLabel resultFilePath = new JLabel();
	private final JLabel labelConsole = new JLabel(LabelsGui.getLabelConsole());

	private final JButton buttonViewResult = new JButton("View Result");
	public static Thread excCluster;

	public static void main(String[] args) {
		Options.initialize();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
					Gui frame = new Gui();
					frame.redirectSystemStreams();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public Gui() throws Exception {
		panel.setAutoscrolls(true);
		frame.getContentPane().add(panel);
		frame.setVisible(true);
		// panel.setResizable(false);
		frame.setTitle(LabelsGui.getMainWindowTitle());
		frame.setFont(new Font(LabelsGui.getFont(), Font.PLAIN, LabelsGui.getFontSize()));
		frame.setBounds(10, 104, 1000, 700);
		fileChooserFolderOutput.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		buttonChooserFolderOutput.setHorizontalAlignment(SwingConstants.LEFT);
		buttonChooserFolderOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnValue = fileChooserFolderOutput.showOpenDialog(frame);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					String folderOutput = fileChooserFolderOutput.getSelectedFile().getAbsolutePath();
					labelChooserFolderOutput.setText(folderOutput);
					Options.setFolderOutput(folderOutput);
				}
			}
		});
		buttonChooserFolderOutput.setBounds(10, 36, 179, 23);
		panel.setLayout(null);

		panel.add(buttonChooserFolderOutput);

		labelChooserFolderOutput.setBounds(196, 36, 357, 22);
		panel.add(labelChooserFolderOutput);

		labelParameters.setBounds(10, 11, 135, 14);
		panel.add(labelParameters);
		buttonChooserCompany.setHorizontalAlignment(SwingConstants.LEFT);
		buttonChooserCompany.setBounds(10, 70, 179, 23);
		panel.add(buttonChooserCompany);
		labelChooserCompany.setBounds(196, 71, 357, 22);
		fileChooserCompany.setFileSelectionMode(JFileChooser.FILES_ONLY);

		buttonChooserCompany.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnValue = fileChooserCompany.showOpenDialog(frame);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					String company = fileChooserCompany.getSelectedFile().getAbsolutePath();
					labelChooserCompany.setText(company);
					Options.setCompanyFilePath(company);
				}
			}
		});

		panel.add(labelChooserCompany);
		buttonChooserBods.setHorizontalAlignment(SwingConstants.LEFT);
		buttonChooserBods.setBounds(10, 104, 179, 23);
		buttonChooserBods.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnValue = fileChooserBods.showOpenDialog(frame);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					String bodsName = fileChooserBods.getSelectedFile().getAbsolutePath();
					labelChooserBods.setText(bodsName);
					Options.setBodsFilePath(bodsName);
				}

			}
		});

		panel.add(buttonChooserBods);
		labelChooserBods.setBounds(196, 105, 357, 22);
		panel.add(labelChooserBods);
		buttonDirectorChooser.setHorizontalAlignment(SwingConstants.LEFT);
		buttonDirectorChooser.setBounds(10, 138, 179, 23);

		panel.add(buttonDirectorChooser);
		buttonDirectorChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnValue = fileChooserDirector.showOpenDialog(frame);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					String directors = fileChooserDirector.getSelectedFile().getAbsolutePath();
					labelChooserDirector.setText(directors);
					Options.setDirectorFilePath(directors);
				}
			}
		});
		labelChooserDirector.setPreferredSize(new Dimension(720, 600));
		labelChooserDirector.setAutoscrolls(true);
		labelChooserDirector.setMinimumSize(new Dimension(640, 400));
		labelChooserDirector.setMaximumSize(new Dimension(1280, 720));
		labelChooserDirector.setBounds(196, 139, 357, 22);
		panel.add(labelChooserDirector);
		
		chckbxConsiderIsolateNode.setFont(new Font("Times New Roman", Font.PLAIN, 12));
		chckbxConsiderIsolateNode.setBounds(660, 11, 291, 40);
		panel.add(chckbxConsiderIsolateNode);
		chckbxConsiderIsolateNode.setSelected(Options.areIsolateNodeConsidered());
		
		btnStartScube.setBounds(10, 197, 179, 23);
		panel.add(btnStartScube);

		console.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		console.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		panel.add(console);
		txtAreaConsole.setForeground(Color.DARK_GRAY);
		txtAreaConsole.setCaretColor(Color.DARK_GRAY);
		txtAreaConsole.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
		txtAreaConsole.setMaximumSize(new Dimension(300, 300));
		txtAreaConsole.setLineWrap(true);
		txtAreaConsole.setEditable(false);

		// txtrAaaa.setEditable(false);
		console.setBounds(10, 300, 870, 300);
		resultFilePath.setHorizontalAlignment(SwingConstants.LEFT);
		resultFilePath.setBounds(134, 253, 543, 23);
		panel.add(resultFilePath);
		labelConsole.setForeground(Color.GRAY);
		labelConsole.setBounds(10, 287, 753, 14);

		buttonViewResult.setBounds(10, 253, 114, 23);
		panel.add(buttonViewResult);
		File f = new File(Options.getModuleVisualizerOutput());
		if (f.exists() && !f.isDirectory()) {
			resultFilePath.setText(LabelsGui.getOldResultLabel() + Options.getModuleVisualizerOutput());
			buttonViewResult.setEnabled(true);
		} else
			buttonViewResult.setEnabled(false);
		panel.add(labelConsole);
		initialize();
		buttonViewResult.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread() {
					@Override
					public void run() {
						try {
							Desktop.getDesktop().open(new File(Options.getModuleVisualizerOutput()));
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							System.out.println("Error in opening the file " + Options.getModuleVisualizerOutput());
						}
					}
				}.start();
			}
		});
		btnStartScube.setHorizontalAlignment(SwingConstants.LEFT);
		textFieldCAattributes.setBounds(560, 107, 291, 20);
		panel.add(textFieldCAattributes);
		textFieldCAattributes.setColumns(10);
		labelCAattributes.setBounds(560, 93, 291, 14);
		panel.add(labelCAattributes);

		btnClearConsole.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtAreaConsole.setText("");				
			}
		});
		btnClearConsole.setBounds(739, 271, 123, 23);
		panel.add(btnClearConsole);
		
		textFieldDate.setBounds(560, 198, 291, 20);
		panel.add(textFieldDate);
		textFieldDate.setColumns(10);
		
		labelDate.setBounds(560, 183, 291, 14);
		panel.add(labelDate);
		
		textFieldAttributesToIgnore.setColumns(10);
		textFieldAttributesToIgnore.setBounds(560, 152, 291, 20);
		panel.add(textFieldAttributesToIgnore);
		labelIgnoreFields.setBounds(560, 138, 291, 14);
		panel.add(labelIgnoreFields);
/*		comboBox.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	

		    }
		});
*/
		btnStartScube.addActionListener(new ActionListener() {		
			public void actionPerformed(ActionEvent e) {
				txtAreaConsole.setText("");
				LocalDateTime[] dates = Options.getDates(textFieldDate.getText(),Options.getDateSeparator());
				if (chckbxConsiderIsolateNode.isSelected())
					Options.setIsolateNodeConsidered("yes");
				else
					Options.setIsolateNodeConsidered("no");
				Options.setModuleSegregationCA(textFieldCAattributes.getText());
				Options.setModuleSegregationIGNORE(textFieldAttributesToIgnore.getText());
				MInteger minWeight = new MInteger(1);
				MInteger maxWeight = new MInteger(10);
				Algorithms inputClusterAlgorithm = getAlgoritm(panel);
				if (inputClusterAlgorithm != null) {
					switch (inputClusterAlgorithm) {
					case WCCs:
					case removeGCC:
						break;
					case filterEdgesWeight:
					case removeEdgesFromGCC:
					default:
						createAndShowClusterParameters(minWeight, maxWeight);
					}
					createAndShowFimiPanel(1, 1000);
					boolean append = true;
					if(dates==null) {
						dates = new LocalDateTime[1]; // null element
						append = false;
					}
					for (LocalDateTime t : dates) {
						Options.setTime(t);
						try {
							ModuleGraphBuilder.start(t);
							Graph<Node, Edge> graph = ModuleGraphClustering.loadGraph(Options.getModuleGraphBuilderOutput());
							ModuleGraphClustering.start(graph);
							ModuleTableBuilder.start(t);
							ModuleSegregationDataCubeBuilder.start(append);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
					ModuleVisualizer.start();
					resultFilePath.setText(LabelsGui.getOldResultLabel() + Options.getModuleVisualizerOutput());
					buttonViewResult.setEnabled(true);
					int n = JOptionPane.showConfirmDialog(null, LabelsGui.getQuestionResultView(),
							LabelsGui.getFrameworkName() + " " + LabelsGui.getSuccesfullyTerminated(),
							JOptionPane.YES_NO_OPTION);
					if (n == JOptionPane.YES_OPTION) {
						try {
							Desktop.getDesktop().open(new File(Options.getModuleVisualizerOutput()));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
	}

	public static Algorithms getAlgoritm(JPanel panel) {
		return (Algorithms) JOptionPane.showInputDialog(panel, LabelsGui.getClusteringLabelComboBox(),
				"Clustering Algorithm Choice", JOptionPane.QUESTION_MESSAGE, null, Algorithms.values(),
				Algorithms.removeEdgesFromGCC);
	}
	public void initialize() {
		Font font = new Font(LabelsGui.getFont(), Font.PLAIN, LabelsGui.getFontSize());
		setFont(frame, font);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	public void setFont(Component component, Font font) {
		component.setFont(font);
		if (component instanceof Container)
			for (Component child : ((Container) component).getComponents()) {
				// child.setVisible(true);
				setFont(child, font);
			}
	}

	private void redirectSystemStreams() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b));
			}
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len));
			}
			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};
		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}

	private void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				txtAreaConsole.append(text);
			}
		});
	}

	public static void createAndShowClusterParameters(MInteger minEdgeWeight, MInteger maxEdgeWeight) {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(423, 229));
		panel.setLayout(null);
		// Show a label every 10% of values
		int offset = (int) ((maxEdgeWeight.value - minEdgeWeight.value) * 0.1);
		if (offset < 1)
			offset = 1;
		int min = minEdgeWeight.value == Integer.MAX_VALUE ? 1 : minEdgeWeight.value;
		int max = maxEdgeWeight.value == Integer.MIN_VALUE ? 1 : maxEdgeWeight.value;
		offset = offset > 0 ? offset : 1;
		int start = Options.getEdgeWeight();
		if (start > max || start < min) {
			Random rn = new Random();
			int n = max - min + 1;
			int i = rn.nextInt() % n;
			int randomNum = min + i;
			start = randomNum;
		}
		JSlider slider = new JSlider(min, max, start);
		slider.setBounds(49, 98, 335, 59);
		panel.add(slider);
		slider.setVisible(true);
		JLabel labelCurrentEdgeWeight = new JLabel(LabelsGui.getEdgeWeightDynamicLabel() + start);
		labelCurrentEdgeWeight.setHorizontalAlignment(SwingConstants.CENTER);
		labelCurrentEdgeWeight.setBounds(62, 189, 322, 14);
		panel.add(labelCurrentEdgeWeight);
		JLabel labelEdgeWeight = new JLabel(LabelsGui.getEdgeWeightWindowName());
		labelEdgeWeight.setHorizontalAlignment(SwingConstants.CENTER);
		labelEdgeWeight.setBounds(49, 49, 335, 14);
		panel.add(labelEdgeWeight);
		slider.setMajorTickSpacing(offset);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				JSlider theSlider = (JSlider) changeEvent.getSource();
				if (!theSlider.getValueIsAdjusting()) {
					labelCurrentEdgeWeight.setText(LabelsGui.getEdgeWeightDynamicLabel() + theSlider.getValue());
				}
			}
		};
		slider.addChangeListener(changeListener);
		int result = JOptionPane.showConfirmDialog(frame, panel, LabelsGui.getEdgeWeightWindowName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.YES_OPTION) {
			Options.setEdgeWeight(slider.getValue());
		}
	}
	

	public static void createAndShowFimiPanel(int minSupp, int maxSupp) {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(423, 229));
		panel.setLayout(null);
		//Show a label every 20% of values
		int offset = (int) ((maxSupp - minSupp) * 0.2);
		if (offset < 1)
			offset = 1;
		int min = minSupp == Integer.MAX_VALUE ? 1 : minSupp;
		int max = maxSupp == Integer.MIN_VALUE ? 1 : maxSupp;
		offset = offset > 0 ? offset : 1;
		int start = Options.getMinimumSupport();
		if (start > max || start < min) {
			Random rn = new Random();
			int n = max - min + 1;
			int i = rn.nextInt() % n;
			int randomNum = min + i;
			start = randomNum;
		}
//		System.out.println("min"+min+" start "+start+" max "+max);
		JSlider slider = new JSlider(min, max, start);
		slider.setBounds(49, 98, 335, 59);
		panel.add(slider);
		slider.setVisible(true);
		JLabel labelCurrMinSupp = new JLabel(LabelsGui.getMinSupportLabel() + start);
		labelCurrMinSupp.setHorizontalAlignment(SwingConstants.CENTER);
		labelCurrMinSupp.setBounds(62, 189, 322, 14);
		panel.add(labelCurrMinSupp);
		JLabel labelEdgeWeight = new JLabel(LabelsGui.getMinSuppWindowName());
		labelEdgeWeight.setHorizontalAlignment(SwingConstants.CENTER);
		labelEdgeWeight.setBounds(49, 49, 335, 14);
		panel.add(labelEdgeWeight);
		slider.setMajorTickSpacing(offset);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				JSlider theSlider = (JSlider) changeEvent.getSource();
				if (!theSlider.getValueIsAdjusting()) {
					labelCurrMinSupp.setText(LabelsGui.getMinSupportLabel() + theSlider.getValue());
				}
			}
		};
		slider.addChangeListener(changeListener);
		int result = JOptionPane.showConfirmDialog(frame, panel, LabelsGui.getMinSuppWindowName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.YES_OPTION) {
			Options.setMinimumSupport(String.valueOf(slider.getValue()));
		}
	}
}

