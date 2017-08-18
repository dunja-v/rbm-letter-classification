package fer.hr.thesis.main;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import fer.hr.thesis.binary_rbm.BinaryRBM;
import fer.hr.thesis.binary_rbm.neuron_layer.BinaryNeuronLayer;
import fer.hr.thesis.binary_rbm.trainer.BinaryRBMTrainer;
import fer.hr.thesis.binary_rbm.trainer.TrainingObserver;
import fer.hr.thesis.dataset.ImageEditor;
import fer.hr.thesis.dataset.ImageUtility;

/**
 * <p>
 * Program for training binary RBMs and evaluating their results.
 * </p>
 * <p>
 * The program allows the user to create binary RBMs and choose the number of
 * hidden neurons, save RBMs and load them from files.
 * </p>
 * 
 * <p>
 * The training set can be loaded from a txt file containing binary vectors of
 * the images, their dimensions and the number of classes present.
 * </p>
 * 
 * <p>
 * The training parameters can be chosen by the user. During training, results
 * are printed and standard output and drawn at the chart.
 * </p>
 * 
 * <p>
 * Results can be viewed on concrete examples by loading an image and
 * classifying it using a loaded or trained RBM.
 * </p>
 * 
 * @author Dunja Vesinger
 * @version 1.0.0
 */
public class MainFrame extends JFrame {

	/**
	 * Number used for serialization.
	 */
	private static final long serialVersionUID = 6154029550327855679L;

	/**
	 * Percentage of the dataset used for training.
	 */
	private static final double TRAINING_PERCENTAGE = 0.6;
	/**
	 * Percentage of the dataset used for testing.
	 */
	private static final double TEST_PERCENTAGE = 0.2;
	/**
	 * Percentage of the dataset used for validation.
	 */
	private static final double VALIDATION_PERCENTAGE = 0.2;

	/**
	 * Currently active RBM.
	 */
	private BinaryRBM rbm;
	/**
	 * RBM trainer.
	 */
	private BinaryRBMTrainer trainer;

	/**
	 * Number of classes inside the loaded dataset.
	 */
	private int numOfClasses = -1;
	/**
	 * Width of the images inside the loaded dataset.
	 */
	private int imgWidth;
	/**
	 * Height of the images inside the loaded dataset.
	 */
	private int imgHeight;
	/**
	 * Training set.
	 */
	private List<int[]> trainingSet;
	/**
	 * Test set.
	 */
	private List<int[]> testSet;
	/**
	 * Validation set.
	 */
	private List<int[]> validationSet;

	/**
	 * Panel with the training options.
	 */
	private JTrainingPanel trainingPanel;
	/**
	 * Panel which offers image classification.
	 */
	private JImgDisplayPanel imgDisplay;

	/**
	 * Menu item for loading the dataset.
	 */
	private JMenuItem loadDataset;
	/**
	 * Menu item for creating a new RBM.
	 */
	private JMenuItem newRBM;
	/**
	 * Menu item for loading a RBM.
	 */
	private JMenuItem loadRBM;
	/**
	 * Menu item for saving RBM to file.
	 */
	private JMenuItem saveRBM;

	/**
	 * Menu item for saving generated image to file.
	 */
	private JMenuItem saveImg;
	/**
	 * Menu item for loading image from file.
	 */
	private JMenuItem loadImg;

	/**
	 * Button for classification.
	 */
	private JButton btnClassify;
	/**
	 * Loaded image.
	 */
	private BufferedImage img;
	/**
	 * Label for printing the class of the image.
	 */
	private JLabel lImgClass;

	/**
	 * True if training is currently in progress.
	 */
	private boolean isTrainingActive = false;

	/**
	 * Creates a new MainFrame.
	 */
	public MainFrame() {

		setTitle("Handwritten Letter Classification");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		initGUI();
		pack();
		setVisible(true);

	}

	/**
	 * Initialized the graphical user interface of the frame.
	 */
	private void initGUI() {

		Container cp = getContentPane();
		JTabbedPane tp = new JTabbedPane();
		cp.add(tp);

		// training panel
		trainingPanel = new JTrainingPanel();
		tp.add("Training", trainingPanel);

		// image panel
		JPanel imgPanel = new JPanel();
		imgPanel.setLayout(new BorderLayout());
		tp.add("Classification", imgPanel);

		imgDisplay = new JImgDisplayPanel();
		imgPanel.add(imgDisplay, BorderLayout.CENTER);

		JPanel imgOptions = new JPanel();

		btnClassify = new JButton("Classify");
		btnClassify.setEnabled(false);
		imgOptions.add(btnClassify);
		imgPanel.add(imgOptions, BorderLayout.PAGE_START);

		lImgClass = new JLabel("No classification");
		imgPanel.add(lImgClass, BorderLayout.PAGE_END);

		addClasifyListener();

		JMenuBar menuBar = new JMenuBar();

		// RBM menu items
		JMenu rbmMenu = new JMenu("RBM");
		menuBar.add(rbmMenu);

		loadDataset = new JMenuItem("Load Dataset...");
		rbmMenu.add(loadDataset);

		newRBM = new JMenuItem("New RBM...");
		newRBM.setEnabled(false);
		rbmMenu.add(newRBM);

		loadRBM = new JMenuItem("Load RBM...");
		rbmMenu.add(loadRBM);

		saveRBM = new JMenuItem("Save RBM as...");
		saveRBM.setEnabled(false);
		rbmMenu.add(saveRBM);

		// Image menu items
		JMenu imgMenu = new JMenu("Image");
		menuBar.add(imgMenu);

		loadImg = new JMenuItem("Load...");
		imgMenu.add(loadImg);

		saveImg = new JMenuItem("Save generated image as...");
		saveImg.setEnabled(false);
		imgMenu.add(saveImg);

		setJMenuBar(menuBar);
		addMenuActionListeners();

	}

	/**
	 * Method run on program start.
	 * 
	 * @param args
	 *            Command line arguments are ignored
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(MainFrame::new);
	}

	/**
	 * Adds a listener to the classify button.
	 */
	private void addClasifyListener() {

		btnClassify.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int[] vectorImg = ImageUtility.imageToVector(img);

				// initializing number of classes if uninitialized
				if (numOfClasses < 0) {
					numOfClasses = rbm.getVisible().size() - vectorImg.length;
					if (numOfClasses < 0) {
						JOptionPane.showMessageDialog(MainFrame.this,
								"Incompatible dimensions: image does not match the input size of the RBM.", "Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				vectorImg = ImageUtility.addEmptyEncoding(vectorImg, numOfClasses);

				// checking dimension compatibility
				if (!compatibleDimension(rbm, vectorImg)) {
					JOptionPane.showMessageDialog(MainFrame.this,
							"Incompatible dimensions: image does not match the input size of the RBM.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				int[] generatedImg = BinaryRBMTrainer.generate(rbm, vectorImg);

				BufferedImage generatedBI = ImageUtility.imageFromVector(
						ImageUtility.removeOneHotEncoding(generatedImg, numOfClasses), imgWidth, imgHeight);

				imgDisplay.setGeneratedImg(generatedBI);
				String imgClass = ImageUtility.getLetterClassName(generatedImg, numOfClasses);
				if (imgClass.isEmpty()) {
					imgClass = "Unable to classify";
				}
				lImgClass.setText(imgClass);

				saveImg.setEnabled(true);
			}
		});

	}

	/**
	 * Adds action listeners to all menu items.
	 */
	private void addMenuActionListeners() {

		// loading dataset from file (done in separate thread)
		loadDataset.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File("."));
				int returnVal = chooser.showDialog(MainFrame.this, "Load");

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();

					SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							loadDataset(file);
							return null;
						}

						@Override
						protected void done() {
							newRBM.setEnabled(true);
							isTrainingEnabled();
							isImgCalssifiable();
							super.done();
						}
					};
					sw.execute();
				}
			}
		});

		// creating new rbm
		newRBM.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JCreateRBMDialog jdCreate = new JCreateRBMDialog();
				jdCreate.setModal(true);
				jdCreate.setVisible(true);

				int numOfHidden = jdCreate.getNumOfHidden();
				rbm = new BinaryRBM(new BinaryNeuronLayer(new int[imgWidth * imgHeight + numOfClasses]),
						new BinaryNeuronLayer(new int[numOfHidden]));

				if (rbm != null) {
					saveRBM.setEnabled(true);
					isImgCalssifiable();
					isTrainingEnabled();
				}
			}
		});

		// loading RBM from file
		loadRBM.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File("."));
				int returnVal = chooser.showDialog(MainFrame.this, "Load");

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();

					try {
						rbm = BinaryRBM.readRBMFromFile(file);
						JOptionPane.showMessageDialog(MainFrame.this, "RBM Successfully loaded.", "Message",
								JOptionPane.INFORMATION_MESSAGE);
						saveRBM.setEnabled(true);
						isImgCalssifiable();
						isTrainingEnabled();

					} catch (Exception ex) {
						JOptionPane.showMessageDialog(MainFrame.this, "Unable to load RBM from file.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}

			}
		});

		// saving RBM to file
		saveRBM.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File("."));
				int returnVal = chooser.showDialog(MainFrame.this, "Save");

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();

					try {
						BinaryRBM.writeRBMToFile(file, rbm);
						JOptionPane.showMessageDialog(MainFrame.this, "RBM Successfully saved.", "Message",
								JOptionPane.INFORMATION_MESSAGE);
						isTrainingEnabled();

					} catch (Exception ex) {
						JOptionPane.showMessageDialog(MainFrame.this, "Unable to save RBM to file.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}

			}
		});

		// loading image from file
		loadImg.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File("."));
				int returnVal = chooser.showDialog(MainFrame.this, "Load");

				try {
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						img = ImageUtility.readImage(chooser.getSelectedFile());
						imgWidth = img.getWidth();
						imgHeight = img.getHeight();
						imgDisplay.setImage(img);
						imgDisplay.setGeneratedImg(null);

						saveImg.setEnabled(false);
						imgDisplay.revalidate();
						isImgCalssifiable();
					} else {
						JOptionPane.showMessageDialog(MainFrame.this, "Unable to load image.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(MainFrame.this, "Unable to load image.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}

			}
		});

		// saving image to file
		saveImg.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				BufferedImage img = imgDisplay.getGeneratedImg();

				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File("."));
				int returnVal = chooser.showDialog(MainFrame.this, "Save");

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();

					try {
						ImageIO.write(img, "png", file);
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(MainFrame.this, "Unable to save image to file.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}

			}
		});

	}

	/**
	 * Loads dataset from the given file and divides it into training,
	 * validation and test sets. Prints the number of loaded and divided
	 * examples to standard output.
	 * 
	 * @param source
	 *            File containing the dataset
	 * @throws IOException
	 *             If dataset cannot be loaded
	 */
	protected void loadDataset(File source) throws IOException {

		Scanner sc = new Scanner(source);
		numOfClasses = sc.nextInt();
		imgWidth = sc.nextInt();
		imgHeight = sc.nextInt();

		List<int[]> dataset = new ArrayList<>();
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			line.trim();
			if (!line.isEmpty()) {
				dataset.add(ImageEditor.parseLine(line));
			}
		}
		sc.close();
		try {
			divideDataset(dataset, numOfClasses, TRAINING_PERCENTAGE, TEST_PERCENTAGE, VALIDATION_PERCENTAGE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(MainFrame.this, "Unable to divide dataset in subsets.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}

		System.out.println("Loaded training examples: " + trainingSet.size());
		System.out.println("Loaded test examples: " + testSet.size());
		System.out.println("Loaded validation examples: " + validationSet.size());

	}

	/**
	 * Randomly divides dataset into training, validation and test sets. Equal
	 * number of examples for each class is assured in each subset.
	 * 
	 * @param dataset
	 *            Complete dataset
	 * @param numOfClasses
	 *            Number of classes in dataset
	 * @param trainingPercentage
	 *            Percentage of examples in training set
	 * @param testPercentage
	 *            Percentage of examples in test set
	 * @param validationPercentage
	 *            Percentage of examples in validation set
	 */
	private void divideDataset(List<int[]> dataset, int numOfClasses, double trainingPercentage, double testPercentage,
			double validationPercentage) {
		trainingSet = new ArrayList<>();
		testSet = new ArrayList<>();
		validationSet = new ArrayList<>();

		int examplesPerClass = dataset.size() / numOfClasses;
		int trainingSizePerClass = (int) Math.round(dataset.size() * trainingPercentage / numOfClasses);
		int testSizePerClass = (int) Math.round(dataset.size() * testPercentage / numOfClasses);
		int validationSizePerClass = (int) Math.round(dataset.size() * validationPercentage / numOfClasses);

		for (int currClass = 0; currClass < numOfClasses; currClass++) {

			int currentExamplesInTraining = 0;
			int currentExamplesInTest = 0;
			int currentExamplesInValidation = 0;

			for (int exampleNum = 0; exampleNum < examplesPerClass; exampleNum++) {
				double p = ThreadLocalRandom.current().nextDouble();

				if (p < trainingPercentage) {
					if (currentExamplesInTraining < trainingSizePerClass) {
						trainingSet.add(dataset.get(exampleNum + currClass * examplesPerClass));
						currentExamplesInTraining++;
						continue;
					}

				} else if (p >= trainingPercentage && p < (trainingPercentage + testPercentage)) {
					if (currentExamplesInTest < testSizePerClass) {
						testSet.add(dataset.get(exampleNum + currClass * examplesPerClass));
						currentExamplesInTest++;
						continue;
					}
				} else if (p >= (trainingPercentage + testPercentage)) {
					if (currentExamplesInValidation < validationSizePerClass) {
						validationSet.add(dataset.get(exampleNum + currClass * examplesPerClass));
						currentExamplesInValidation++;
						continue;
					}
				}

				// if the set chosen by the probability was filled for this
				// example, choose another empty set
				if (currentExamplesInTraining < trainingSizePerClass) {
					trainingSet.add(dataset.get(exampleNum + currClass * examplesPerClass));
					currentExamplesInTraining++;

				} else if (currentExamplesInTest < testSizePerClass) {
					testSet.add(dataset.get(exampleNum + currClass * examplesPerClass));
					currentExamplesInTest++;

				} else if (currentExamplesInValidation < validationSizePerClass) {
					validationSet.add(dataset.get(exampleNum + currClass * examplesPerClass));
					currentExamplesInValidation++;
				}

			}
		}

	}

	/**
	 * <p>
	 * The JTariningPanel class enables user to choose parameters for the
	 * training and train the RBM. During training, results are printed and
	 * standard output and drawn at the chart. Training may be paused and
	 * resumed.
	 * </p>
	 * 
	 * @author Dunja Vesinger
	 * @version 1.0.0
	 */
	private class JTrainingPanel extends JPanel {
		/**
		 * Number used for serialization.
		 */
		private static final long serialVersionUID = 8315554344565585343L;
		/**
		 * Button for starting the training.
		 */
		private JButton btnTrain;
		/**
		 * Button for pausing the training.
		 */
		private JButton btnPause;
		/**
		 * Button for resuming the training.
		 */
		private JButton btnResume;

		/**
		 * Text area for inputing learning rate.
		 */
		private JTextArea tLearningRate;
		/**
		 * Text area for inputing number of epochs.
		 */
		private JTextArea tEpochs;
		/**
		 * Text area for inputing termination condition.
		 */
		private JTextArea tTermCond;
		/**
		 * Text area for inputing error check interval.
		 */
		private JTextArea tErrorInterval;

		/**
		 * Chart of the training progress.
		 */
		private FitnessChartPanel graphicalPanel;

		/**
		 * Creates a new JTrainingPanel.
		 */
		public JTrainingPanel() {
			initGUI();
		}

		/**
		 * Sets the enabled state of the training button to the given value.
		 * 
		 * @param isEnabled
		 *            True if the training button should be enabled
		 */
		public void enableTrain(boolean isEnabled) {
			btnTrain.setEnabled(isEnabled);
		}

		/**
		 * Initialized the graphical user interface of the panel.
		 */
		private void initGUI() {

			setLayout(new BorderLayout());

			JPanel optionsPanel = new JPanel();
			add(optionsPanel, BorderLayout.PAGE_START);

			JLabel lLeariningRate = new JLabel("Learning rate:");
			tLearningRate = new JTextArea("0.001");
			optionsPanel.add(lLeariningRate);
			optionsPanel.add(tLearningRate);

			JLabel lEpochs = new JLabel("Maximum number of epochs:");
			tEpochs = new JTextArea("1000");
			optionsPanel.add(lEpochs);
			optionsPanel.add(tEpochs);

			JLabel lTermCond = new JLabel("Maximum free energy difference:");
			tTermCond = new JTextArea("50");
			optionsPanel.add(lTermCond);
			optionsPanel.add(tTermCond);

			JLabel lErrorInterval = new JLabel("Error check interval:");
			tErrorInterval = new JTextArea("50");
			optionsPanel.add(lErrorInterval);
			optionsPanel.add(tErrorInterval);

			btnTrain = new JButton("Train");
			btnTrain.setEnabled(false);
			optionsPanel.add(btnTrain);

			btnPause = new JButton("Pause");
			btnPause.setEnabled(false);
			optionsPanel.add(btnPause);

			btnResume = new JButton("Resume");
			btnResume.setEnabled(false);
			optionsPanel.add(btnResume);

			graphicalPanel = new FitnessChartPanel();
			add(graphicalPanel, BorderLayout.CENTER);

			addTrainActionListener();
			addPauseResume();
		}

		/**
		 * Adds an action listener to the training button.
		 */
		private void addTrainActionListener() {

			btnTrain.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					// checking dimensions
					if (!compatibleDimension(rbm, trainingSet.get(0))) {
						JOptionPane.showMessageDialog(MainFrame.this,
								"Incompatible dimensions: image does not match the input size of the RBM.", "Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}

					// get training parameters from labels
					double learningRate;
					try {
						learningRate = Double.parseDouble(tLearningRate.getText());
						if (learningRate < 0) {
							tLearningRate.setText("Must be a positive decimal number!");
							return;
						}
					} catch (NumberFormatException | NullPointerException ex) {
						tLearningRate.setText("Must be a decimal number!");
						return;
					}
					int epochs = getParameter(tEpochs);
					int termCond = getParameter(tTermCond);
					int errorInterval = getParameter(tErrorInterval);

					if (epochs < 0 || termCond < 0 || errorInterval < 0) {
						return;
					}

					// disable buttons
					trainingPanel.enableTrain(false);
					btnClassify.setEnabled(false);
					isTrainingActive = true;

					SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							try {
								btnPause.setEnabled(true);
								trainer = new BinaryRBMTrainer();

								trainer.addTrainingObserver(new TrainingObserver() {
									@Override
									public void update(double trainingResult, double validationResult) {
										SwingUtilities.invokeLater(() -> graphicalPanel.addValue(trainingResult,
												validationResult, errorInterval));
									}
								});

								trainer.trainCD1(rbm, trainingSet, testSet, validationSet, learningRate, epochs,
										numOfClasses, termCond, errorInterval);

							} catch (Exception e) {
								e.printStackTrace();
							}

							return null;
						}

						@Override
						protected void done() {
							btnPause.setEnabled(false);
							btnResume.setEnabled(false);
							trainingPanel.enableTrain(true);
							isTrainingActive = false;

							super.done();
						}

					};

					graphicalPanel.clearGraph();
					sw.execute();
				}
			});

		}

		/**
		 * Adds an action listeners to pause and resume buttons.
		 */
		private void addPauseResume() {
			btnPause.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (trainer != null) {
						trainer.suspend();
						isTrainingActive = false;
						btnPause.setEnabled(false);
						btnResume.setEnabled(true);
						btnTrain.setEnabled(true);
						btnClassify.setEnabled(true);
					}
				}
			});

			btnResume.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (trainer != null) {
						trainer.resume();
						isTrainingActive = true;
						btnPause.setEnabled(true);
						btnResume.setEnabled(false);
						btnTrain.setEnabled(false);
						btnClassify.setEnabled(false);
					}
				}
			});
		}
	}

	/**
	 * Allows the user to view loaded and generated image on the panel and the
	 * image class generated by the RBM is printed below images.
	 * </p>
	 * 
	 * @author Dunja Vesinger
	 * @version 1.0.0
	 */
	private class JImgDisplayPanel extends JPanel {
		/**
		 * Number used for serialization.
		 */
		private static final long serialVersionUID = -1041464108100452186L;

		/**
		 * Original image.
		 */
		private BufferedImage img;
		/**
		 * Generated image.
		 */
		private BufferedImage generatedImg;

		/**
		 * Sets the original image to the given value.
		 * 
		 * @param img
		 *            Original image
		 */
		public void setImage(BufferedImage img) {
			this.img = img;
			repaint();
		}

		/**
		 * Sets the generated image to the given value.
		 * 
		 * @param img
		 *            Generated image
		 */
		public void setGeneratedImg(BufferedImage generatedImg) {
			this.generatedImg = generatedImg;
			repaint();
		}

		/**
		 * Returns the generated image.
		 * 
		 * @return Generated image
		 */
		public BufferedImage getGeneratedImg() {
			return generatedImg;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			if (img != null) {
				Image dimg = img.getScaledInstance(getWidth() / 2, getHeight(), Image.SCALE_SMOOTH);
				g.drawImage(dimg, 0, 0, null);
			}
			if (generatedImg != null) {
				Image dimg2 = generatedImg.getScaledInstance(getWidth() / 2, getHeight(), Image.SCALE_SMOOTH);
				g.drawImage(dimg2, getWidth() / 2, 0, null);
			}
		}
	}

	/**
	 * Tries to parse a positive integer from the text in the text area. Prints
	 * and error on the text area if the value is invalid.
	 * 
	 * @param t
	 *            Text area containing the parameter value
	 * @return Value of the parameter or -1 if the input is invalid
	 */
	private int getParameter(JTextArea t) {

		try {
			int param = Integer.parseInt(t.getText());

			if (param <= 0) {
				t.setText("Must be a positive integer!");
				return -1;
			}
			return param;

		} catch (NumberFormatException | NullPointerException ex) {
			t.setText("Must be an integer!");
			return -1;
		}

	}

	/**
	 * Checks if training is possible and enables the training button if it is
	 * and disables it otherwise.
	 */
	private void isTrainingEnabled() {
		if (trainingSet != null && rbm != null) {
			trainingPanel.enableTrain(true);
		} else {
			trainingPanel.enableTrain(false);
		}
	}

	/**
	 * Checks if image classification is possible and enables the classify
	 * button if it is and disables it otherwise.
	 */
	private void isImgCalssifiable() {
		if (img != null && rbm != null && !isTrainingActive) {
			btnClassify.setEnabled(true);
		} else {
			btnClassify.setEnabled(false);
		}

	}

	/**
	 * Checks if the dimensions of the example match the length of the visible
	 * vector of the given RBM.
	 * 
	 * @param rbm
	 *            RBM
	 * @param example
	 *            Example
	 * @return True if dimensions are compatible, False otherwise
	 */
	private boolean compatibleDimension(BinaryRBM rbm, int[] example) {
		int inputLen = rbm.getVisible().size();
		return example.length == inputLen;
	}

}
