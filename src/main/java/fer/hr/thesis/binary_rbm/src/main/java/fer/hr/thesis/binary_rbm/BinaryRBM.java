package fer.hr.thesis.binary_rbm;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import fer.hr.thesis.binary_rbm.neuron_layer.IBinaryNeuronLayer;

public class BinaryRBM {
	
	private final static double VARIANCE = 0.01;

	protected double weights[][];
	protected double hiddenBiases[];
	protected double visibleBiases[];
	protected IBinaryNeuronLayer visible;
	protected IBinaryNeuronLayer hidden;

	protected int numOfHidden;
	protected int numOfVisible;
	
	
	private double[] initialHiddenProbabilities; // h_j(0)
	private double[] finalHiddenProbabilities; // h_j(n)
	private double[] finalVisibleProbabilities;//v_i(n)

	private int[] originalData;

	public BinaryRBM(IBinaryNeuronLayer visible, IBinaryNeuronLayer hidden) {
		this.visible = visible;
		this.hidden = hidden;
		this.numOfHidden = hidden.size();
		this.numOfVisible = visible.size();

		weights = new double[numOfVisible][numOfHidden];
		initializeWeights();

		hiddenBiases = new double[numOfHidden];
		visibleBiases = new double[numOfVisible];
	}
	
	public void setVisibleBiases(double[] biases){
		this.visibleBiases = biases;
	}

	private void initializeWeights() {
		Random rand = ThreadLocalRandom.current();

		for (int i = 0; i < numOfVisible; i++) {
			for (int j = 0; j < numOfHidden; j++) {
				weights[i][j] = rand.nextGaussian() * VARIANCE;
			}
		}
	}

	public void setVisible(int[] visible) {
		this.visible.setVector(visible);
	}

	public IBinaryNeuronLayer getVisible() {
		return visible;
	}

	public IBinaryNeuronLayer getHidden() {
		return hidden;
	}
	
	public void setInitialHiddenProbabilities(double[] initialHiddenProbabilities) {
		this.initialHiddenProbabilities = initialHiddenProbabilities;
	}

	public void setFinalHiddenProbabilities(double[] finalHiddenProbabilities) {
		this.finalHiddenProbabilities = finalHiddenProbabilities;
	}

	public void setFinalVisibleProbabilities(double[] finalVisibleProbabilities) {
		this.finalVisibleProbabilities = finalVisibleProbabilities;
	}

	public void setOriginalData(int[] originalData) {
		this.originalData = originalData;
	}

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

	public void updateWeights(double learningRate) {
		if (initialHiddenProbabilities == null || finalHiddenProbabilities == null) {
			throw new NullPointerException("No Gibbs step was performed. Unable to update weights.");
		}
		for (int i = 0; i < numOfVisible; i++) {
			for (int j = 0; j < numOfHidden; j++)
				weights[i][j] = weights[i][j] + learningRate * (initialHiddenProbabilities[j] * originalData[i]
						- finalHiddenProbabilities[j] * finalVisibleProbabilities[i]);

		}
	}

	public void updateBiases(double learningRate) {
		if (initialHiddenProbabilities == null || finalHiddenProbabilities == null || finalVisibleProbabilities == null) {
			throw new NullPointerException("No Gibbs step was performed. Unable to update biases.");
		}
		for (int i = 0; i < numOfVisible; i++) {
			visibleBiases[i] += learningRate * (originalData[i] - finalVisibleProbabilities[i]);
		}

		for (int j = 0; j < numOfHidden; j++) {
			hiddenBiases[j] += learningRate * (initialHiddenProbabilities[j] - finalHiddenProbabilities[j]);
		}
	}

}
