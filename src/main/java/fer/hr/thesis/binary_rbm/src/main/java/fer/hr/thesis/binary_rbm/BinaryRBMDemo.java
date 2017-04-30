package fer.hr.thesis.binary_rbm;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import fer.hr.thesis.binary_rbm.neuron_layer.BinaryNeuronLayer;
import fer.hr.thesis.binary_rbm.trainer.BinaryRBMTrainer;

public class BinaryRBMDemo {

	public static void main(String[] args) {

		Path path = Paths.get("TrainingExamples");

		BinaryRBM rbm = new BinaryRBM(new BinaryNeuronLayer(new int[11664]), new BinaryNeuronLayer(new int[400]));
		
		BinaryRBMTrainer.trainBinaryRBM(rbm, path, 0.001, 1000);
		
		try {
			int[] img = BinaryRBMTrainer.readImageToVector(Paths.get("Example.png"));
			
			rbm.setVisible(img);
			rbm.updateHiddenNeurons();
			rbm.updateVisibleNeurons();
			
			BinaryRBMTrainer.saveAsImage(rbm.getVisible().getAsVector(), "Validation.png", 108, 108);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

}
