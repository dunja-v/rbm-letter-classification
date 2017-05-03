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

public class BinaryRBM implements Serializable{


	private static final long serialVersionUID = 2642695945669173931L;
	
	private final static double VARIANCE = 0.01;
	private final static int INIT_HIDDEN_BIAS = 0;

	protected double weights[][];
	protected double hiddenBiases[];
	protected double visibleBiases[];
	protected IBinaryNeuronLayer visible;
	protected IBinaryNeuronLayer hidden;

	protected int numOfHidden;
	protected int numOfVisible;

	private double[] initialHiddenProbabilities; // h_j(0)
	private double[] finalHiddenProbabilities; // h_j(n)
	private double[] finalVisibleProbabilities;// v_i(n)

	private int[] originalData;

	public BinaryRBM(IBinaryNeuronLayer visible, IBinaryNeuronLayer hidden) {
		if(visible == null || hidden == null){
			throw new IllegalArgumentException("Layers of the RBM cannot be null.");
		}
		this.visible = visible;
		this.hidden = hidden;
		this.numOfHidden = hidden.size();
		this.numOfVisible = visible.size();

		weights = new double[numOfVisible][numOfHidden];
		initializeWeights();

		hiddenBiases = new double[numOfHidden];
		Arrays.fill(hiddenBiases, INIT_HIDDEN_BIAS);
		visibleBiases = new double[numOfVisible];
	}

	public void setVisibleBiases(double[] biases) {
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
		if (initialHiddenProbabilities == null || finalHiddenProbabilities == null
				|| finalVisibleProbabilities == null) {
			throw new NullPointerException("No Gibbs step was performed. Unable to update biases.");
		}
		for (int i = 0; i < numOfVisible; i++) {
			visibleBiases[i] = visibleBiases[i] + learningRate * (originalData[i] - finalVisibleProbabilities[i]);
		}

		for (int j = 0; j < numOfHidden; j++) {
			hiddenBiases[j] = hiddenBiases[j] + learningRate * (initialHiddenProbabilities[j] - finalHiddenProbabilities[j]);
		}
	}
	
	public double freeEnergy(){
		double freeEnergy = 0;
		
		for (int i = 0; i < numOfVisible; i++) {
			freeEnergy -= visibleBiases[i]*visible.getElement(i);
			for (int j = 0; j < numOfHidden; j++){
				freeEnergy -= Math.log(1 + Math.exp(hiddenBiases[j] + weights[i][j]*visible.getElement(i)));
			}
		}
		
		return freeEnergy;
	}
	
    public static void writeRBMToFile(File outputFile, BinaryRBM rbm) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(
                outputFile.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
            oos.writeObject(rbm);
        } catch (IOException e) {
            throw new IOException("Unable to write RBM to file.");
        }
    }

    public static BinaryRBM readRBMFromFile(File rbmFile) throws IOException {
        try (ObjectInputStream ons = new ObjectInputStream(Files.newInputStream(rbmFile.toPath(),
                StandardOpenOption.READ))) {
            BinaryRBM rbm = (BinaryRBM) ons.readObject();
            return rbm;
        } catch (ClassNotFoundException | IOException e) {
            throw new IOException("Unable to read RBM from file.");
        }
    }

}
