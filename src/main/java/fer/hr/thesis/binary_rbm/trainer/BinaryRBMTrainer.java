package fer.hr.thesis.binary_rbm.trainer;

import java.util.ArrayList;
import java.util.List;

import fer.hr.thesis.binary_rbm.BinaryRBM;
import fer.hr.thesis.dataset.ImageUtility;

/**
 * <p>
 * The BinaryRBMTrainer represents a trainer which runs the Contrastive
 * Divergence learning algorithm with one step on {@link BinaryRBM}. It enables
 * suspending and reseting the training process as well as adding and notifying
 * observers when a new training result is produced.
 * </p>
 * 
 * <p>
 * The results on training and validation set are periodically printed on
 * standard output during training. Result on test set is printed after the
 * training is done.
 * </p>
 * *
 * 
 * @author Dunja Vesinger
 * @version 1.0.0
 */
public class BinaryRBMTrainer {
	/**
	 * Value to which visible biases are initialized if the probability of their
	 * activation in learning samples is equal to 0.
	 */
	private static final int BIAS_INIT_0 = -1000;
	/**
	 * Value to which visible biases are initialized if the probability of their
	 * activation in learning samples is equal to 1.
	 */
	private static final int BIAS_INIT_1 = 1000;
	/**
	 * Is the trainer currently paused.
	 */
	private boolean paused;

	/**
	 * List of observers observing the training results.
	 */
	private List<TrainingObserver> observers;

	/**
	 * Creates a new instance of BinaryRBMTrainer.
	 */
	public BinaryRBMTrainer() {
		observers = new ArrayList<>();
	}

	/**
	 * Trains the given BinaryRBM using the Contrastive Divergence learning
	 * algorithm with one step with the given learning parameters and prints the
	 * result on test set on standard output.
	 * 
	 * @param rbm
	 *            RBM to be trained
	 * @param trainingSet
	 *            Training set
	 * @param testSet
	 *            Test set
	 * @param validationSet
	 *            Validation set
	 * @param learningRate
	 *            Learning rate
	 * @param maxNumOfEpochs
	 *            Maximum number of epochs allowed
	 * @param classNum
	 *            Number of classes in the data
	 * @param terminationCond
	 *            Maximal allowed difference in free energies on training set
	 *            and validation set
	 * @param errorPrintInterval
	 *            Number of epochs after which the error is calculated and
	 *            observers notified
	 * 
	 */
	public void trainCD1(BinaryRBM rbm, List<int[]> trainingSet, List<int[]> testSet, List<int[]> validationSet,
			double learningRate, int maxNumOfEpochs, int classNum, int terminationCond, int errorPrintInterval) {

		rbm.setVisibleBiases(claculateInitialBiases(trainingSet));

		trainExamples(rbm, trainingSet, validationSet, learningRate, maxNumOfEpochs, classNum, terminationCond,
				errorPrintInterval);

		System.out.println("Error on test set:");
		calculateError(rbm, testSet, clearLabels(testSet, classNum), classNum);
	}

	/**
	 * Calculates the initial values of visible biases based on the probability
	 * of their activation in the training samples.
	 * 
	 * @param trainingExamples
	 *            Training samples
	 * @return Initial values of visible biases
	 */
	private static double[] claculateInitialBiases(List<int[]> trainingExamples) {
		double[] probabilities = calculateInputVectorProbailities(trainingExamples);

		for (int j = 0; j < probabilities.length; j++) {
			if (probabilities[j] == 0) {
				probabilities[j] = BIAS_INIT_0;
			} else if (probabilities[j] == 1) {
				probabilities[j] = BIAS_INIT_1;
			} else {
				probabilities[j] = Math.log(probabilities[j] / (1 - probabilities[j]));
			}
		}
		return probabilities;
	}

	/**
	 * Calculates the probabilities of each element being activated in the
	 * training set.
	 * 
	 * @param trainingExamples
	 *            Training samples
	 * @return Probabilities of each element being activated
	 */
	private static double[] calculateInputVectorProbailities(List<int[]> trainingExamples) {
		double[] probabilities = new double[trainingExamples.get(0).length];

		for (int i = 0; i < trainingExamples.size(); i++) {
			for (int j = 0; j < probabilities.length; j++) {
				if (trainingExamples.get(i)[j] == 1) {
					probabilities[j]++;
				}
			}
		}
		for (int j = 0; j < probabilities.length; j++) {
			probabilities[j] /= trainingExamples.size();
		}

		return probabilities;
	}

	/**
	 * Trains the given BinaryRBM using the Contrastive Divergence learning
	 * algorithm with one step with the given learning parameters and prints the
	 * result on training and validation set every
	 * <code>errorPrintInterval</code> epochs.
	 * 
	 * @param rbm
	 *            RBM to be trained
	 * 
	 * @param trainingSet
	 *            Training set
	 * @param testSet
	 *            Test set
	 * @param validationSet
	 *            Validation set
	 * @param learningRate
	 *            Learning rate
	 * @param maxNumOfEpochs
	 *            Maximum number of epochs allowed
	 * @param classNum
	 *            Number of classes in the data
	 * @param terminationCond
	 *            Maximal allowed difference in free energies on training set
	 *            and validation set
	 * @param errorPrintInterval
	 *            Number of epochs after which the error is calculated and
	 *            observers notified
	 * 
	 */
	private void trainExamples(BinaryRBM rbm, List<int[]> trainingExamples, List<int[]> validationExamples,
			double learningRate, int numOfEpochs, int classNum, int terminationCond, int errorPrintInterval) {

		List<int[]> unlabeledTrainingExamples = clearLabels(trainingExamples, classNum);
		List<int[]> unlabeledValidationExamples = clearLabels(validationExamples, classNum);
		double delta = 0;

		for (int i = 0; i < numOfEpochs; i++) {

			int exampleNum = trainingExamples.size() / classNum;

			for (int exampleIndex = 0; exampleIndex < exampleNum; exampleIndex++) {
				for (int classIndex = 0; classIndex < classNum; classIndex++) {

					int[] example = trainingExamples.get(exampleIndex + classIndex * exampleNum);

					if (paused) {
						pause();
					}
					trainExample(rbm, example, learningRate);
				}
			}

			if (i % errorPrintInterval == 0) {

				System.out.println("**********************************");
				System.out.println("Epoch " + i);

				System.out.println("Error on training set:");
				double trainingSetFreeEnergy = calculateError(rbm, trainingExamples, unlabeledTrainingExamples,
						classNum);
				System.out.println("Error on validation set:");
				double validationSetFreeEnergy = calculateError(rbm, validationExamples, unlabeledValidationExamples,
						classNum);
				delta = Math.abs(validationSetFreeEnergy - trainingSetFreeEnergy);
				System.out.println("Free energy difference: " + delta);

				notifyTrainingObservers(trainingSetFreeEnergy, validationSetFreeEnergy);

				if (delta > terminationCond) {
					break;
				}
			}

		}
	}

	/**
	 * Runs the given RBM on a single sample and updates its values based on the
	 * result.
	 * 
	 * @param rbm
	 *            RBM to be trained
	 * @param input
	 *            Input sample
	 * @param learningRate
	 *            Learning rate
	 */
	private void trainExample(BinaryRBM rbm, int[] input, double learningRate) {

		rbm.setOriginalData(input);

		rbm.setVisible(input);
		rbm.setInitialHiddenProbabilities(rbm.updateHiddenNeurons());

		rbm.setFinalVisibleProbabilities(rbm.updateFinalVisibleNeurons());
		rbm.setFinalHiddenProbabilities(rbm.updateHiddenNeurons());

		rbm.updateWeights(learningRate);
		rbm.updateBiases(learningRate);

	}

	/**
	 * Calculates and returns the average free energies on the given set of
	 * examples for the given RBM. Calculates the number of misclassified
	 * examples for the set and prints the result to standard output.
	 * 
	 * @param rbm
	 *            RBM to be evaluated
	 * @param examples
	 *            Example set with correct labels
	 * @param unlabeledExamples
	 *            Example set with no labels
	 * @param numOfClasses
	 *            Number of classes in dataset
	 * @return Average free energy on dataset
	 */
	private double calculateError(BinaryRBM rbm, List<int[]> examples, List<int[]> unlabeledExamples,
			int numOfClasses) {

		double numOfMissclasified = 0;
		double freeEnergy = 0;

		int examplSize = examples.size();
		for (int i = 0; i < examplSize; i++) {
			rbm.setVisible(unlabeledExamples.get(i));
			rbm.updateHiddenNeurons();
			freeEnergy += rbm.freeEnergy();
			rbm.updateFinalVisibleNeurons();
			int[] generated = rbm.getVisible().getAsVector();

			if (isMissclassified(examples.get(i), generated, numOfClasses)) {
				numOfMissclasified++;
			}
		}

		freeEnergy /= examplSize;

		System.out.println("Number of missclassified examples: " + numOfMissclasified);
		System.out.println("Average free energy on set:" + freeEnergy);

		return freeEnergy;

	}

	/**
	 * Pauses the current thread.
	 */
	private void pause() {
		synchronized (this) {
			while (paused) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/**
	 * Pauses the training of BinaryRBMTrainer.
	 */
	public void suspend() {
		paused = true;
	}

	/**
	 * Resumes the training of BinaryRBMTrainer.
	 */
	public synchronized void resume() {
		paused = false;
		notifyAll();
	}

	/**
	 * Returns a deep copy of the examples with cleared labels (all elements set
	 * to zero).
	 * 
	 * @param examples
	 *            Examples to be cleared
	 * @param numOfClasses
	 *            Number of classes in dataset
	 * @return Deep copy of the examples with cleared labels
	 */
	private static List<int[]> clearLabels(List<int[]> examples, int numOfClasses) {
		List<int[]> clearedExamples = new ArrayList<int[]>();
		for (int[] example : examples) {
			clearedExamples.add(ImageUtility.getClearedLabelCopy(example, numOfClasses));
		}
		return clearedExamples;
	}

	/**
	 * Adds a training observer to the trainer.
	 * 
	 * @param o
	 *            Observer to be added
	 */
	public void addTrainingObserver(TrainingObserver o) {
		observers.add(o);
	}

	/**
	 * Removes a training observer from the trainer.
	 * 
	 * @param o
	 *            Observer to be removed
	 */
	public void removeTrainingObserver(TrainingObserver o) {
		observers.remove(o);
	}

	/**
	 * Notifies all training observers that a training result has been
	 * generated.
	 * 
	 * @param trainingResult
	 *            Result on training set
	 * @param validationResult
	 *            Result on validation set
	 */
	public void notifyTrainingObservers(double trainingResult, double validationResult) {
		for (TrainingObserver o : observers) {
			o.update(trainingResult, validationResult);
		}
	}

	/**
	 * Checks if the label of the generated sample is the same as the label of
	 * the original sample.
	 * 
	 * @param original
	 *            Original sample
	 * @param generated
	 *            Generated sample
	 * @param numOfClasses
	 *            Number of classes in dataset
	 * @return True if the labels differ, False otherwise
	 */
	private static boolean isMissclassified(int[] original, int[] generated, int numOfClasses) {
		for (int i = 1; i <= numOfClasses; i++) {
			int index = generated.length - i;
			if (generated[index] != original[index]) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Generates an output vector based on the given input vector using the RBM.
	 * 
	 * @param rbm
	 *            RBM
	 * @param vectorImg
	 *            Input vector
	 * @return Output vector
	 */
	public static int[] generate(BinaryRBM rbm, int[] vectorImg) {
		rbm.setVisible(vectorImg);
		rbm.updateHiddenNeurons();
		rbm.updateFinalVisibleNeurons();

		return rbm.getVisible().getAsVector();
	}

}
