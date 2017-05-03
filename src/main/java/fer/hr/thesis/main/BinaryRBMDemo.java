package fer.hr.thesis.main;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import fer.hr.thesis.binary_rbm.BinaryRBM;
import fer.hr.thesis.binary_rbm.neuron_layer.BinaryNeuronLayer;
import fer.hr.thesis.binary_rbm.trainer.BinaryRBMTrainer;

public class BinaryRBMDemo {

	public static void main(String[] args) {

		Path path = Paths.get("binaryImages");

		BinaryRBM rbm = new BinaryRBM(new BinaryNeuronLayer(new int[11690]), new BinaryNeuronLayer(new int[40]));
		
		BinaryRBMTrainer.trainBinaryRBM(rbm, path, 160, 0.001, 500);
		
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
