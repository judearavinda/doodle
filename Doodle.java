import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Doodle {

	// class of points
	public static class ColoredPoint implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int x;
		private int y;
		private Color color;
		private int stroke;

		public ColoredPoint(int x, int y, Color color, int stroke) {
			this.x = x;
			this.y = y;
			this.color = color;
			this.stroke = stroke;
		}

		public int getStroke() {
			return stroke;
		}

		public void setStroke(int stroke) {
			this.stroke = stroke;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		public Color getColor() {
			return color;
		}

		public void setColor(Color color) {
			this.color = color;
		}
	}

	public class View extends JPanel implements IView, ActionListener {
		private JFrame f = new JFrame("Doodle"); // create Frame

		// this is the model
		private Model model;

		// Menu
		private JMenuBar mb = new JMenuBar();
		private JMenu mnuFile = new JMenu("File");
		private JMenu mnuView = new JMenu("View");

		private JMenuItem mnuItemQuit = new JMenuItem("Quit");

		private JMenuItem mnuItemCreate = new JMenuItem("New Doodle");
		private JMenuItem mnuItemSave = new JMenuItem("Save Doodle");
		private JMenuItem mnuItemLoad = new JMenuItem("Load Doodle");
		private JMenuItem mnuItemAbout = new JMenuItem("About");
		private JCheckBoxMenuItem mnuItemFullSize = new JCheckBoxMenuItem("Full Size");
		private JCheckBoxMenuItem mnuItemFit = new JCheckBoxMenuItem("Fit");

		private JPanel panel;
		private Color currentSelectedColor = Color.BLACK;
		private int currentSelectedStroke = 10;
		private int currentSelectedStrokeIndex;
		private JPanel currentColor;

		private JSlider playbackSlider;

		private CustomCanvas canvas;

		private JButton start;

		private JButton end;

		private JButton play;

		private ArrayList<CustomLabel> strokeButtons;

		// constructor, create the GUI. This is the view
		public View(Model newModel) {

			// set the model
			this.model = newModel;

			// Set menubar
			f.setJMenuBar(mb);

			// Build Menu for file
			mnuFile.add(mnuItemCreate);
			mnuItemCreate.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("create");
					if (model.getStrokes() != null && model.getStrokes().size() > 0) {
						model.promptToSave(f);
					}

				}
			});
			mnuFile.add(mnuItemSave);
			mnuItemSave.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("save");
					model.saveToFile(f);

				}
			});
			mnuFile.add(mnuItemLoad);
			mnuItemLoad.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					model.loadToFile(f);

				}
			});
			mnuFile.add(mnuItemQuit);
			mnuItemQuit.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (model.getStrokes() != null && model.getStrokes().size() > 0) {
						model.promptToSave(f);
					}
					System.exit(0);
				}
			});
			mnuView.add(mnuItemAbout);
			mnuItemAbout.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					model.displayAboutDialog(f);
				}
			});
			mnuView.add(mnuItemFullSize);
			mnuItemFullSize.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("Full Size");
				}
			});
			mnuView.add(mnuItemFit);
			mnuItemFit.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("Fit");
				}
			});

			mb.add(mnuFile);
			mb.add(mnuView);

			// Setup Main Frame
			BorderLayout border = new BorderLayout();
			f.getContentPane().setLayout(border);

			panel = new JPanel(); // use FlowLayout
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

			// initialize colour chooser with buttons
			JPanel colourPanel = new JPanel();
			colourPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
			colourPanel.setLayout(new GridLayout(4, 3, 50, 40));

			Color[] colors = { Color.RED, Color.PINK, Color.GREEN, Color.GRAY, Color.CYAN, Color.DARK_GRAY,
					Color.ORANGE, Color.WHITE, Color.YELLOW, Color.MAGENTA, Color.BLUE, Color.BLACK };

			for (int i = 0; i < colors.length; i++) {
				final JButton button = new JButton();
				button.setBackground(colors[i]);
				button.setOpaque(true);
				button.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						currentSelectedColor = button.getBackground();
						model.setCurrentSelectedColor(currentSelectedColor);

					}
				});
				colourPanel.add(button);
			}

			// Currently Selected Colour
			JPanel currentSelectedColourPanel = new JPanel(); // use FlowLayout
			currentSelectedColourPanel.setLayout(new GridLayout(0, 1));
			currentColor = new JPanel();
			currentColor.setBackground(currentSelectedColor);
			currentColor.setOpaque(true);
			currentSelectedColourPanel.setBorder(BorderFactory.createTitledBorder("Currently Selected Color"));
			currentSelectedColourPanel.add(currentColor);

			// Currently Selected Stroke Width
			JPanel currentSelectedStrokeWidth = new JPanel(); // use FlowLayout
			currentSelectedStrokeWidth.setLayout(new GridLayout(4, 1));
			currentSelectedStrokeWidth.setBorder(BorderFactory.createTitledBorder("Stroke Thickness"));
			strokeButtons = new ArrayList<CustomLabel>();
			for (int i = 10; i < 26; i += 5) {
				final CustomLabel strokeButton = new CustomLabel(currentSelectedColor, i);
				strokeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				strokeButton.addMouseListener(new MouseListener() {

					@Override
					public void mouseClicked(MouseEvent e) {
						currentSelectedStroke = strokeButton.getStroke();
						// reset strokebuttons
						for (int i = 0; i < strokeButtons.size(); i++) {
							strokeButtons.get(i).setBorder(BorderFactory.createLineBorder(Color.WHITE));
						}
						strokeButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
						model.setCurrentSelectedStroke(currentSelectedStroke);

					}

					@Override
					public void mousePressed(MouseEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void mouseReleased(MouseEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void mouseEntered(MouseEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void mouseExited(MouseEvent e) {
						// TODO Auto-generated method stub

					}
				});
				strokeButtons.add(strokeButton);
				currentSelectedStrokeWidth.add(strokeButton);
			}

			// append the button for the color chooser
			JButton choose = new JButton("Choose More Colours...");
			choose.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					Color newColor = JColorChooser.showDialog(panel, "Choose a Custom Color", Color.WHITE);
					if (newColor != null) {
						currentSelectedColor = newColor;
						model.setCurrentSelectedColor(currentSelectedColor);
					}

				}
			});
			panel.add(colourPanel);
			panel.add(choose);
			panel.add(currentSelectedColourPanel);
			panel.add(currentSelectedStrokeWidth);
			panel.setBorder(BorderFactory.createTitledBorder("Choose Color"));

			// initialize the canvas
			canvas = new CustomCanvas();

			// Playback controlPanel
			JPanel playbackPanel = new JPanel();
			playbackPanel.setLayout(new GridLayout(1, 4));
			play = new JButton("Play");
			play.addActionListener(new ActionListener() {

				private int index;
				private Timer timer;

				@Override
				public void actionPerformed(ActionEvent e) {
					// resets the slider to the beginning
					ActionListener al = new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							index = model.getCurrentSelectedStrokeIndex();
							index++;
							if (index > playbackSlider.getMaximum()) {
								timer.stop();
							}
							model.setCurrentSelectedStrokeIndex(index);
						}

					};
					timer = new Timer(1000, al);
					timer.start();

				}

			});
			playbackSlider = new JSlider();
			playbackSlider.setMinimum(0);
			playbackSlider.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					JSlider source = (JSlider) e.getSource();
					if (!source.getValueIsAdjusting()) {
						int fps = (int) source.getValue();
						currentSelectedStrokeIndex = fps;
						model.setCurrentSelectedStrokeIndex(fps);
					}
				}

			});
			start = new JButton("Start");
			start.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// resets the slider to the beginning
					model.setCurrentSelectedStrokeIndex(0);
				}

			});
			end = new JButton("End");
			end.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// resets the slider to the beginning
					model.setCurrentSelectedStrokeIndex(model.getStrokes().size());
				}

			});
			playbackPanel.add(play);
			playbackPanel.add(playbackSlider);
			playbackPanel.add(start);
			playbackPanel.add(end);

			// place colour chooser
			f.add(canvas, BorderLayout.LINE_END);
			f.add(panel, BorderLayout.LINE_START);
			f.add(playbackPanel, BorderLayout.SOUTH);
			// Allows the Swing App to be closed
			f.addWindowListener(new ListenCloseWdw());
		}// end constructor

		public class CustomLabel extends JPanel {
			private Color color = Color.BLACK;
			private int stroke = 5;

			public CustomLabel(Color color2, int stroke2) {
				this.color = color2;
				this.stroke = stroke2;
			}

			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				g2.setColor(this.color);
				g2.setStroke(new BasicStroke(this.stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
				g2.drawLine(0, this.getHeight() / 2, this.getWidth(), this.getHeight() / 2);
			}

			public Color getColor() {
				return color;
			}

			public void setColor(Color color) {
				this.color = color;
				repaint();
			}

			public int getStroke() {
				return stroke;
			}

			public void setStroke(int stroke) {
				this.stroke = stroke;
			}
		}

		public class CustomCanvas extends Canvas implements MouseListener, MouseMotionListener {

			// the main canvas of the program
			private ArrayList<ArrayList<ColoredPoint>> strokes;
			private ArrayList<ColoredPoint> currentStroke;
			private int startX, startY;
			Graphics2D gc;

			public CustomCanvas() {
				// Construct the canvas, and set it to listen for mouse events.
				// Also create an array to hold the points that are displayed on
				// the canvas.
				setBackground(Color.white);
				strokes = new ArrayList<ArrayList<ColoredPoint>>();
				currentStroke = new ArrayList<ColoredPoint>();
				addMouseListener(this);
				addMouseMotionListener(this);
			}

			void doClear() {
				// Clear all the lines from the picture.
				if (strokes.size() > 0) {
					strokes = new ArrayList<ArrayList<ColoredPoint>>();
					repaint();
				}
			}

			public Dimension getPreferredSize() {
				// Say what size this canvas wants to be.
				return new Dimension(500, 400);
			}

			public ArrayList<ArrayList<ColoredPoint>> getStrokes() {
				return strokes;
			}

			public void setStrokes(ArrayList<ArrayList<ColoredPoint>> strokes) {
				this.strokes = strokes;
			}

			public void paint(Graphics g) {
				// Redraw all the lines.
				super.paint(g);
				Graphics2D g2 = (Graphics2D) g;
				int limit = model.getStrokes().size();
				if (model.getCurrentSelectedStrokeIndex() != -1)
					limit = model.getCurrentSelectedStrokeIndex();
				for (int i = 0; i < limit; i++) {
					// draw each stroke
					if (model.getStrokes().size() == 0)
						return;
					ArrayList<ColoredPoint> currentStroke = model.getStrokes().get(i);
					for (int z = 0; z < currentStroke.size(); z++) {
						g2.setColor(currentStroke.get(z).getColor());
						g2.setStroke(new BasicStroke(currentStroke.get(z).getStroke(), BasicStroke.CAP_ROUND,
								BasicStroke.JOIN_MITER));
						g2.drawLine(currentStroke.get(z).getX(), currentStroke.get(z).getY(),
								currentStroke.get(z).getX(), currentStroke.get(z).getY());
					}
				}
				g2.dispose();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// if painting a line from an earlier index
				if (model.getCurrentSelectedStrokeIndex() != model.getStrokes().size()) {
					ArrayList<ArrayList<ColoredPoint>> newStrokes = new ArrayList<ArrayList<ColoredPoint>>();
					ArrayList<ArrayList<ColoredPoint>> currentStrokes = model.getStrokes();
					// we need to reduce the strokes size
					for (int j = 0; j < model.getCurrentSelectedStrokeIndex(); j++) {
						newStrokes.add(currentStrokes.get(j));
					}
					// add it to the model
					model.setStrokes(newStrokes);
					strokes = model.getStrokes();
				}
				currentStroke = new ArrayList<ColoredPoint>();
				startX = e.getX();
				startY = e.getY();
				// clear currentStroke
				gc = (Graphics2D) getGraphics(); // Get a graphics context for
													// use while
				// drawing.
				ColoredPoint currentPoint = new ColoredPoint(startX, startY, currentSelectedColor,
						currentSelectedStroke);
				gc.setColor(currentPoint.getColor());
				gc.setStroke(new BasicStroke(currentSelectedStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
				gc.drawLine(currentPoint.getX(), currentPoint.getY(), currentPoint.getX(), currentPoint.getY());
				currentStroke.add(currentPoint);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				// Make sure that the drag operation has been properly
				// started.
				startX = e.getX();
				startY = e.getY();
				ColoredPoint currentPoint = new ColoredPoint(startX, startY, currentSelectedColor,
						currentSelectedStroke);
				gc.setColor(currentPoint.getColor());
				gc.setStroke(new BasicStroke(currentSelectedStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
				gc.drawLine(currentPoint.getX(), currentPoint.getY(), currentPoint.getX(), currentPoint.getY());
				currentStroke.add(currentPoint);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// When user takes hand off the mouse
				startX = e.getX();
				startY = e.getY();
				ColoredPoint currentPoint = new ColoredPoint(startX, startY, currentSelectedColor,
						currentSelectedStroke);
				gc.setColor(currentPoint.getColor());
				gc.setStroke(new BasicStroke(currentSelectedStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
				// gc.drawLine(currentPoint.getX(), currentPoint.getY(),
				// currentPoint.getX(), currentPoint.getY());
				currentStroke.add(currentPoint);
				strokes.add(currentStroke);
				model.setStrokes(strokes);
				model.setCurrentSelectedStrokeIndex(strokes.size());
				gc.dispose();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

		}

		// IView interface
		public void updateView() {
			// in invalid doodle, disable all file buttons
			if (model.getStrokes() == null || model.getStrokes().size() == 0) {
				mnuItemCreate.setEnabled(false);
				mnuItemSave.setEnabled(false);
			} else {
				mnuItemCreate.setEnabled(true);
				mnuItemSave.setEnabled(true);
			}
			for (int i = 0; i < strokeButtons.size(); i++) {
				strokeButtons.get(i).setColor(model.currentSelectedColor);
			}
			// update view components
			System.out.println("View: updateView");
			currentColor.setBackground(currentSelectedColor);
			if (model.strokes != null && model.strokes.size() > 0) {
			}
			if (model.getStrokes() != null && model.getStrokes().size() > 0) {
				playbackSlider.setMaximum(model.getStrokes().size());
				playbackSlider.setMajorTickSpacing(1);
				playbackSlider.setPaintLabels(true);
				playbackSlider.setPaintTicks(true);
			}
			if (playbackSlider.getValue() != model.getCurrentSelectedStrokeIndex()) {
				playbackSlider.setValue(model.getCurrentSelectedStrokeIndex());
			}
			if (playbackSlider.getValue() > model.getStrokes().size()) {
				playbackSlider.setMaximum(model.getStrokes().size());
			}
			// Disable the panelslider buttons when there are no strokes
			if (model.getStrokes() == null || model.getStrokes().size() == 0
					|| model.getCurrentSelectedStrokeIndex() == model.getStrokes().size())
				end.setEnabled(false);
			else
				end.setEnabled(true);
			if (model.getStrokes() == null || model.getStrokes().size() == 0
					|| model.getCurrentSelectedStrokeIndex() == 0)
				start.setEnabled(false);
			else
				start.setEnabled(true);
			if (model.getStrokes() == null || model.getStrokes().size() == 0)
				play.setEnabled(false);
			else
				play.setEnabled(true);
			canvas.setStrokes(model.getStrokes());
			canvas.repaint();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}
	}

	interface IView {
		public void updateView();
	}

	public class Model {
		// the view
		IView view;
		private Color currentSelectedColor = Color.BLACK;
		private int currentSelectedStroke = 10;

		public Color getCurrentSelectedColor() {
			return currentSelectedColor;
		}

		public void displayAboutDialog(JFrame f) {
			Object[] options = { "OK" };
			int n = JOptionPane.showOptionDialog(f, "Created by Jude Tillekeratne 2016", "About this program",
					JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

		}

		public void promptToSave(JFrame f) {
			Object[] options = { "Yes", "No" };
			int n = JOptionPane.showOptionDialog(f, "Would you like to save the current doodle?", "Save Current Doodle",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);

			if (n == 0) {
				saveToFile(f);
			} else {
				setStrokes(new ArrayList<ArrayList<ColoredPoint>>());
			}
		}

		public void saveToFile(JFrame f) {
			// load all current data to file
			FileDialog fd = new FileDialog(f, "Save to File", FileDialog.SAVE);
			fd.setVisible(true);
			String fileName = fd.getFile();
			if (fileName == null)
				return;
			// by default text file
			if (!fileName.endsWith(""))
				fileName += ".txt";
			// User has canceled.
			String directoryName = fd.getDirectory();
			File file = new File(directoryName, fileName);
			String directory = file.getAbsolutePath();

			if (fileName.endsWith(".txt")) {
				// OutputStream
				PrintWriter out;
				try { // Open the file.
					out = new PrintWriter(new FileWriter(file));
				} catch (IOException e) {
					new Dialog(f, "Error while trying to open file \"" + fileName + "\": " + e.getMessage());
					return;
				}
				// put the strokes in each line
				out.println(this.getStrokes().size());
				ArrayList<ColoredPoint> currentStrokes;
				ColoredPoint currentPoint;
				for (int i = 0; i < this.getStrokes().size(); i++) {
					currentStrokes = this.getStrokes().get(i);
					// the number of points in each stroke
					out.println(currentStrokes.size());
					// iterate over each point
					for (int z = 0; z < currentStrokes.size(); z++) {
						currentPoint = currentStrokes.get(z);
						out.print(currentPoint.getX());
						out.print(" ");
						out.print(currentPoint.getY());
						out.print(" ");
						out.print(currentPoint.getColor().getRGB());
						out.print(" ");
						out.print(currentPoint.getStroke());
						out.println();
					}
				}
				out.close();
				if (out.checkError())
					new Dialog(f, "Some error occured while trying to save data to the file.");
			} else if (fileName.endsWith(".bin")) {
				// make it a binary file, serialize it
				try {
					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(directory));
					ArrayList<ColoredPoint> currentStrokes;
					ColoredPoint currentPoint;
					out.writeObject(getStrokes().size());
					for (int i = 0; i < getStrokes().size(); i++) {
						currentStrokes = getStrokes().get(i);
						out.writeObject(currentStrokes.size());
						for (int z = 0; z < currentStrokes.size(); z++) {
							currentPoint = currentStrokes.get(z);
							out.writeObject(currentPoint);
						}
					}
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (fileName.endsWith(".xml")) {
				// make it an xml file
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db;
				try {
					db = dbf.newDocumentBuilder();
					Document dom = db.newDocument();
					Element rootElement = dom.createElement("Strokes");
					Element strokeElement;
					ArrayList<ColoredPoint> currentStroke;
					ColoredPoint currentPoint;
					Element pointElement;
					for (int i = 0; i < getStrokes().size(); i++) {
						currentStroke = getStrokes().get(i);
						strokeElement = dom.createElement("Points");
						for (int z = 0; z < currentStroke.size(); z++) {
							currentPoint = currentStroke.get(z);
							pointElement = dom.createElement("Point");
							pointElement.setAttribute("x", String.valueOf(currentPoint.getX()));
							pointElement.setAttribute("y", String.valueOf(currentPoint.getY()));
							pointElement.setAttribute("color", String.valueOf(currentPoint.getColor().getRGB()));
							pointElement.setAttribute("stroke", String.valueOf(currentPoint.getStroke()));
							strokeElement.appendChild(pointElement);
						}
						rootElement.appendChild(strokeElement);
					}
					dom.appendChild(rootElement);

					Transformer tr = TransformerFactory.newInstance().newTransformer();
					tr.setOutputProperty(OutputKeys.INDENT, "yes");
					tr.setOutputProperty(OutputKeys.METHOD, "xml");
					tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
					// send DOM to file
					try {
						tr.transform(new DOMSource(dom), new StreamResult(new FileOutputStream(directory)));
						File newFile = new File(directory);
						System.out.println("file created:" + newFile.exists());
						System.out.println("fileName is " + fileName);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TransformerException e) {
						e.printStackTrace();
					}

				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransformerConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransformerFactoryConfigurationError e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				new Dialog(f, "Invalid File Format");
			}
		}

		public void loadToFile(JFrame f) {
			// load all current data from file
			JFileChooser fileChooser = new JFileChooser();
			FileNameExtensionFilter typeText = new FileNameExtensionFilter("Text files", "txt");
			FileNameExtensionFilter typeXML = new FileNameExtensionFilter("XML files", "xml");
			FileNameExtensionFilter typeBinary = new FileNameExtensionFilter("Binary files", "bin");
			fileChooser.addChoosableFileFilter(typeText);
			fileChooser.addChoosableFileFilter(typeXML);
			fileChooser.addChoosableFileFilter(typeBinary);
			int returnValue = fileChooser.showOpenDialog(f);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				String fname = selectedFile.getAbsolutePath();
				if (fname.endsWith(".txt")) {
					// load from a text file
					try {
						BufferedReader br = new BufferedReader(new FileReader(selectedFile));
						int numberOfStrokes = Integer.parseInt(br.readLine());
						ArrayList<ColoredPoint> currentPoints = new ArrayList<ColoredPoint>();
						ArrayList<ArrayList<ColoredPoint>> newStrokes = new ArrayList<ArrayList<ColoredPoint>>();
						ColoredPoint currentPoint;
						int numberOfPoints;
						for (int i = 0; i < numberOfStrokes; i++) {
							numberOfPoints = Integer.parseInt(br.readLine());
							for (int z = 0; z < numberOfPoints; z++) {
								// tokenize the line
								String[] result = (br.readLine()).split(" ");
								currentPoint = new ColoredPoint(Integer.parseInt(result[0]),
										Integer.parseInt(result[1]), new Color(Integer.parseInt(result[2])),
										Integer.parseInt(result[3]));
								currentPoints.add(currentPoint);
							}
							newStrokes.add(currentPoints);
							currentPoints = new ArrayList<ColoredPoint>();
						}

						// set the new strokes
						setStrokes(newStrokes);
						setCurrentSelectedStrokeIndex(newStrokes.size());
						br.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (fname.endsWith(".bin")) {
					// loaf from a binary file, serialize it
					try {
						FileInputStream fis = new FileInputStream(fname);
						ObjectInputStream ois = new ObjectInputStream(fis);
						ArrayList<ColoredPoint> currentPoints = new ArrayList<ColoredPoint>();
						ArrayList<ArrayList<ColoredPoint>> newStrokes = new ArrayList<ArrayList<ColoredPoint>>();
						ColoredPoint currentPoint;
						int numberOfStrokes = (int) ois.readObject();
						int numberOfPoints;
						for (int i = 0; i < numberOfStrokes; i++) {
							numberOfPoints = (int) ois.readObject();
							for (int z = 0; z < numberOfPoints; z++) {
								currentPoint = (ColoredPoint) ois.readObject();
								currentPoints.add(currentPoint);
							}
							newStrokes.add(currentPoints);
							currentPoints = new ArrayList<ColoredPoint>();
						}
						// set the new strokes
						setStrokes(newStrokes);
						setCurrentSelectedStrokeIndex(newStrokes.size());
						ois.close();
						fis.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (fname.endsWith(".xml")) {
					// load it from an xml file
					Document dom;
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					try {
						FileInputStream fis = new FileInputStream(fname);
						DocumentBuilder db = dbf.newDocumentBuilder();
						dom = db.parse(fis);
						Node currentStroke;
						ArrayList<ArrayList<ColoredPoint>> newStrokes = new ArrayList<ArrayList<ColoredPoint>>();
						ArrayList<ColoredPoint> Stroke = new ArrayList<ColoredPoint>();
						ColoredPoint currentPoint;
						NodeList numberOfStrokes = dom.getElementsByTagName("Points");
						// System.out.println((Node)
						// numberOfStrokes.item(0).getChildNodes());
						// iterate over each point
						// System.out.println(numberOfStrokes.getLength());
						for (int i = 0; i < numberOfStrokes.getLength(); i++) {

							NodeList childNodes = numberOfStrokes.item(i).getChildNodes();
							for (int z = 0; z < childNodes.getLength(); z++) {
								// System.out.println(childNodes.item(z).getNodeName());
								if (childNodes.item(z).getNodeType() == Node.ELEMENT_NODE) {
									int xValue = Integer.parseInt(
											childNodes.item(z).getAttributes().getNamedItem("x").getNodeValue());
									int yValue = Integer.parseInt(
											childNodes.item(z).getAttributes().getNamedItem("y").getNodeValue());
									int stroke = Integer.parseInt(
											childNodes.item(z).getAttributes().getNamedItem("stroke").getNodeValue());
									Color color = new Color(Integer.parseInt(
											childNodes.item(z).getAttributes().getNamedItem("color").getNodeValue()));
									currentPoint = new ColoredPoint(xValue, yValue, color, stroke);
									Stroke.add(currentPoint);
								}
							}
							newStrokes.add(Stroke);
							Stroke = new ArrayList<ColoredPoint>();
						}
						// update View
						setStrokes(newStrokes);
						setCurrentSelectedStrokeIndex(newStrokes.size());
					} catch (ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else {
					new Dialog(f, "Invalid File Format");
				}
			}
		}

		public void setCurrentSelectedColor(Color currentSelectedColor) {
			this.currentSelectedColor = currentSelectedColor;
			notifyObserver();
		}

		public int getCurrentSelectedStroke() {
			return currentSelectedStroke;
		}

		public void setCurrentSelectedStroke(int currentSelectedStroke) {
			this.currentSelectedStroke = currentSelectedStroke;
			notifyObserver();
		}

		private int currentSelectedStrokeIndex = -1;
		private ArrayList<ArrayList<ColoredPoint>> strokes;

		// set the view observer
		public void setView(IView view) {
			this.view = view;
			this.strokes = new ArrayList<ArrayList<ColoredPoint>>();
			// update the view to current state of the model
			view.updateView();
		}

		public int getCurrentSelectedStrokeIndex() {
			return currentSelectedStrokeIndex;
		}

		public void setCurrentSelectedStrokeIndex(int currentSelectedStrokeIndex) {
			if (currentSelectedStrokeIndex <= strokes.size()) {
				// 0 means no drawing
				this.currentSelectedStrokeIndex = currentSelectedStrokeIndex;
				notifyObserver();
			}
		}

		public ArrayList<ArrayList<ColoredPoint>> getStrokes() {
			return strokes;
		}

		public void setStrokes(ArrayList<ArrayList<ColoredPoint>> strokes) {
			this.strokes = strokes;
			notifyObserver();
		}

		// notify the IView observer
		private void notifyObserver() {
			System.out.println("Model: notify View");
			view.updateView();
		}

	}

	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

		} catch (Exception e) {
			System.err.println("Look and feel not set.");
		}

		// build
		Doodle demo = new Doodle();
		demo.launchFrame();
	}

	public class ListenMenuQuit implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}

	public class ListenCloseWdw extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}

	public void launchFrame() {
		// create Model and initialize it
		Model model = new Model();
		// Create the View
		View view = new View(model);
		// tell Model about View.
		model.setView(view);

		// create the window
		view.f.getContentPane().add(view);
		view.f.setPreferredSize(new Dimension(800, 600));
		view.f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		view.f.setVisible(true);
		view.f.pack();
	}
}
