package fer.hr.thesis.binary_rbm.trainer;

/**
 * Models and observer of the training process in {@link BinaryRBMTrainer}. It
 * is triggered by each training result produced.
 * 
 * @author Dunja Vesinger
 * @version 1.0.0
 */
public interface TrainingObserver {

	/**
	 * Method is called when a new training result is produced by the trainer. 
	 * @param trainingResult Result achieved on training set
	 * @param validationResult Result achieved on validation set
	 */
	public void update(double trainingResult, double validationResult);

}
