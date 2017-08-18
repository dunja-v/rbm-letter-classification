package fer.hr.thesis.binary_rbm;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import fer.hr.thesis.binary_rbm.neuron_layer.IBinaryNeuronLayer;

/**
 * <p>
 * The BinaryRBM class represents a Restricted Boltzmann Machine with binary
 * inputs and outputs. It stores all the neurons as well as weights, biases and
 * probabilities necessary for parameter updates. The sizes of visible and
 * hidden layers are immutable.
 * </p>
 * 
 * <p>
 * It includes methods for updating values of the neurons in visible and hidden
 * layer, updating probabilities used to calculate parameter updates and methods
 * for performing the parameter updates.
 * </p>
 * 
 * <p>
 * Probabilities and original input vector values used to calculate parameter
 * updates are not automatically set when the values of the neurons are updated.
 * They must be explicitly set, otherwise and error occurs. This enables the
 * user to determine which probabilities will be used for updating the
 * parameters.
 * </p>
 * 
 * <p>
 * Example of performing one step of the contrastive divergence algorithm and
 * updating the parameters:
 * </p>
 * 
 * <code>rbm.setOriginalData(input);
 * <br>
 * rbm.setVisible(input); <br>
 * rbm.setInitialHiddenProbabilities(rbm.updateHiddenNeurons());
 * <br>
 * rbm.setFinalVisibleProbabilities(rbm.updateFinalVisibleNeurons()); <br>
 * rbm.setFinalHiddenProbabilities(rbm.updateHiddenNeurons()); <br>
 * <br>
 * rbm.updateWeights(learningRate); <br>
 * rbm.updateBiases(learningRate);</code>
 * 
 * <p>
 * An arbitrary number of steps may be performed between setting the initial
 * probabilities and setting the final probabilities.
 * 
 * @author Dunja Vesinger
 * @version 1.0.0
 */
public class BinaryRBM implements Serializable {

	/**
	 * Number used for serialization of the object.
	 */
	private static final long serialVersionUID = 2642695945669173931L;

	/**
	 * Variance of the normal distribution used to initialize the weights.
	 */
	private final static double VARIANCE = 0.01;
	/**
	 * Initial value of the hidden biases.
	 */
	private final static double HIDDEN_INIT = -1;

	/**
	 * Weights of the RBM.
	 */
	private double weights[][];
	/**
	 * Biases of the hidden layer.
	 */
	private double hiddenBiases[];
	/**
	 * Biases of the visible layer.
	 */
	private double visibleBiases[];
	/**
	 * Visible layer.
	 */
	private IBinaryNeuronLayer visible;
	/**
	 * Hidden layer.
	 */
	private IBinaryNeuronLayer hidden;

	/**
	 * Number of hidden neurons.
	 */
	private int numOfHidden;
	/**
	 * Number of visible neurons.
	 */
	private int numOfVisible;

	/**
	 * Probabilities of each hidden neuron being active at the beginning of
	 * calculating contrastive divergence h_j(0).
	 */
	private double[] initialHiddenProbabilities; // h_j(0)
	/**
	 * Probabilities of each hidden neuron being active at the final step of
	 * calculating contrastive divergence h_j(n).
	 */
	private double[] finalHiddenProbabilities; // h_j(n)
	/**
	 * Probabilities of each visible neuron being active at the final step of
	 * calculating contrastive divergence v_i(n).
	 */
	private double[] finalVisibleProbabilities;
	/**
	 * Values of each visible neuron at the beginning of calculating contrastive
	 * divergence v_i(0).
	 */
	private int[] originalData;

	/**
	 * Constructs a new BinaryRBM with the given visible and hidden layers of
	 * neurons. Values of weights and biases are initialized in the constructor.
	 * Weights are initialized to random values using normal distribution with
	 * expectation 0 and variance 0.01. Hidden biases are initialized to -1 and
	 * visible biases to 0.
	 * 
	 * @param visible
	 *            Visible neuron layer
	 * @param hidden
	 *            Hidden neuron layer
	 */
	public BinaryRBM(IBinaryNeuronLayer visible, IBinaryNeuronLayer hidden) {
		if (visible == null || hidden == null) {
			throw new IllegalArgumentException("Layers of the RBM cannot be null.");
		}
		this.visible = visible;
		this.hidden = hidden;
		this.numOfHidden = hidden.size();
		this.numOfVisible = visible.size();

		weights = new double[numOfVisible][numOfHidden];
		initializeWeights();

		hiddenBiases = new double[numOfHidden];
		Arrays.fill(hiddenBiases, HIDDEN_INIT);
		visibleBiases = new double[numOfVisible];
	}

	/**
	 * Sets visible biases to the given vector of values. A shallow copy is
	 * made.
	 * 
	 * @param biases
	 *            Biases of the visible layer to be set
	 */
	public void setVisibleBiases(double[] biases) {
		if (biases.length != visibleBiases.length) {
			throw new IllegalArgumentException("Invalid length of visible biases vector: " + visibleBiases.length
					+ " expected, but " + biases.length + " given.");
		}
		this.visibleBiases = biases;
	}

	/**
	 * Initializes weights to random values using normal distribution with
	 * expectation 0 and variance 0.01.
	 */
	private void initializeWeights() {
		Random rand = ThreadLocalRandom.current();

		for (int i = 0; i < numOfVisible; i++) {
			for (int j = 0; j < numOfHidden; j++) {
				weights[i][j] = rand.nextGaussian() * VARIANCE;
			}
		}
	}

	/**
	 * Sets the values of the visible neurons to the given vector.
	 * 
	 * @param visible
	 *            Values of visible neurons to be set
	 */
	public void setVisible(int[] visible) {
		this.visible.setVector(visible);
	}

	/**
	 * Returns the current visible neuron layer.
	 * 
	 * @return Visible neuron layer
	 */
	public IBinaryNeuronLayer getVisible() {
		return visible;
	}

	/**
	 * Returns the current hidden neuron layer.
	 * 
	 * @return Hidden neuron layer
	 */
	public IBinaryNeuronLayer getHidden() {
		return hidden;
	}

	/**
	 * Sets the probabilities of each hidden neuron being activated at the first
	 * step of contrastive divergence.
	 * 
	 * @param initialHiddenProbabilities
	 *            Probabilities of hidden neurons to be set
	 */
	public void setInitialHiddenProbabilities(double[] initialHiddenProbabilities) {
		this.initialHiddenProbabilities = initialHiddenProbabilities;
	}

	/**
	 * Sets the probabilities of each hidden neuron being activated at the final
	 * step of contrastive divergence.
	 * 
	 * @param initialHiddenProbabilities
	 *            Probabilities of hidden neurons to be set
	 */
	public void setFinalHiddenProbabilities(double[] finalHiddenProbabilities) {
		this.finalHiddenProbabilities = finalHiddenProbabilities;
	}

	/**
	 * Sets the probabilities of each visible neuron being activated at the
	 * final step of contrastive divergence.
	 * 
	 * @param initialVisibleProbabilities
	 *            Probabilities of visible neurons to be set
	 */
	public void setFinalVisibleProbabilities(double[] finalVisibleProbabilities) {
		this.finalVisibleProbabilities = finalVisibleProbabilities;
	}

	/**
	 * Sets the original vector of the sample.
	 * 
	 * @param originalData
	 *            Original sample to be set
	 */
	public void setOriginalData(int[] originalData) {
		this.originalData = originalData;
	}

	/**
	 * Stochastically updates the values of all hidden neurons according to
	 * their activation probabilities and returns the probabilities of their
	 * activation.
	 * 
	 * @return Probabilities of hidden neurons activation
	 */
	public double[] updateHiddenNeurons() {

		double[] activationProbabilities = new double[numOfHidden];

		for (int j = 0; j < numOfHidden; j++) {
			double energy = 0;
			for (int i = 0; i < numOfVisible; i++) {
				energy += visible.getElement(i) * weights[i][j];
			}
			energy += hiddenBiases[j];

			double probability = 1.0 / (1.0 + Math.exp(-energy));
			activationProbabilities[j] = probability;

			double random = ThreadLocalRandom.current().nextDouble();
			if (random < probability) {
				hidden.setElement(j, 1);
			} else {
				hidden.setElement(j, 0);
			}

		}

		return activationProbabilities;
	}

	/**
	 * Stochastically updates the values of all visible neurons according to
	 * their activation probabilities and returns the probabilities of their
	 * activation.
	 * 
	 * @return Probabilities of visible neurons activation
	 */
	public double[] updateVisibleNeurons() {

		double[] activationProbabilities = new double[numOfVisible];

		for (int i = 0; i < numOfVisible; i++) {
			double energy = 0;
			for (int j = 0; j < numOfHidden; j++) {
				energy += hidden.getElement(j) * weights[i][j];
			}
			energy += visibleBiases[i];

			double probability = 1.0 / (1.0 + Math.exp(-energy));
			activationProbabilities[i] = probability;

			double random = ThreadLocalRandom.current().nextDouble();
			if (random <= probability) {
				visible.setElement(i, 1);
			} else {
				visible.setElement(i, 0);
			}
		}

		return activationProbabilities;
	}

	/**
	 * Deterministically updates the values of all visible neurons according to
	 * their activation probabilities by rounding the probability to the nearest
	 * integer and returns the probabilities of their activation.
	 * 
	 * @return Probabilities of visible neurons activation
	 */
	public double[] updateFinalVisibleNeurons() {

		double[] activationProbabilities = new double[numOfVisible];

		for (int i = 0; i < numOfVisible; i++) {
			double energy = 0;
			for (int j = 0; j < numOfHidden; j++) {
				energy += hidden.getElement(j) * weights[i][j];
			}
			energy += visibleBiases[i];

			double probability = 1.0 / (1.0 + Math.exp(-energy));
			activationProbabilities[i] = probability;

			visible.setElement(i, (int) Math.round(probability));

		}
		return activationProbabilities;
	}

	/**
	 * Updates the values of all weights based on previously saved activation
	 * probabilities of hidden and visible neurons and original sample given.
	 * 
	 * @param learningRate Learning rate
	 * @throws NullPointerException If any of the data necessary for the update is missing
	 */
	public void updateWeights(double learningRate) {
		if (initialHiddenProbabilities == null || finalHiddenProbabilities == null) {
			throw new NullPointerException("No Gibbs step was performed. Unable to update weights.");
		}
		for (int i = 0; i < numOfVisible; i++) {
			for (int j = 0; j < numOfHidden; j++) {
				weights[i][j] = weights[i][j] + learningRate * (initialHiddenProbabilities[j] * originalData[i]
						- finalHiddenProbabilities[j] * finalVisibleProbabilities[i]);
			}
		}
	}

	/**
	 * Updates the values of all hidden and visible biases based on previously saved activation
	 * probabilities of hidden and visible neurons and original sample given.
	 * 
	 * @param learningRate Learning rate
	 * @throws NullPointerException If any of the data necessary for the update is missing
	 */
	public void updateBiases(double learningRate) {
		if (initialHiddenProbabilities == null || finalHiddenProbabilities == null
				|| finalVisibleProbabilities == null) {
			throw new NullPointerException("No Gibbs step was performed. Unable to update biases.");
		}
		for (int i = 0; i < numOfVisible; i++) {
			visibleBiases[i] = visibleBiases[i] + learningRate * (originalData[i] - finalVisibleProbabilities[i]);
		}

		for (int j = 0; j < numOfHidden; j++) {
			hiddenBiases[j] = hiddenBiases[j]
					+ learningRate * (initialHiddenProbabilities[j] - finalHiddenProbabilities[j]);
		}
	}

	/**
	 * Calculates the free energy of the currently set visible vector.
	 * @return Free energy of the currently set visible vector
	 */
	public double freeEnergy() {

		double freeEnergy = 0;

		for (int i = 0; i < numOfVisible; i++) {
			freeEnergy += visibleBiases[i] * visible.getElement(i);
		}
		for (int j = 0; j < numOfHidden; j++) {
			double xj = hiddenBiases[j];
			for (int i = 0; i < numOfVisible; i++) {
				xj += weights[i][j] * visible.getElement(i);
			}
			freeEnergy += Math.log(1 + Math.exp(xj));
		}

		return -freeEnergy;
	}

	/**
	 * Saves the given BinaryRBM to binary file.
	 * @param outputFile File to be saved in
	 * @param rbm RBM to be saved
	 * @throws IOException In case of being unable to write to given file
	 */
	public static void writeRBMToFile(File outputFile, BinaryRBM rbm) throws IOException {
		try (ObjectOutputStream oos = new ObjectOutputStream(
				Files.newOutputStream(outputFile.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
			oos.writeObject(rbm);
		} catch (IOException e) {
			throw new IOException("Unable to write RBM to file.");
		}
	}

	/**
	 * Loads a BinaryRBM from the given file.
	 * @param rbmFile File to be loaded from 
	 * @return BinaryRBM loaded
	 * @throws IOException In case of being unable to read BinaryRBM from given file
	 */
	public static BinaryRBM readRBMFromFile(File rbmFile) throws IOException {
		try (ObjectInputStream ons = new ObjectInputStream(
				Files.newInputStream(rbmFile.toPath(), StandardOpenOption.READ))) {
			BinaryRBM rbm = (BinaryRBM) ons.readObject();
			return rbm;
		} catch (ClassNotFoundException | IOException e) {
			throw new IOException("Unable to read RBM from file.");
		}
	}

}
