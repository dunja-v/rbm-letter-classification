package fer.hr.thesis.binary_rbm.neuron_layer;

public interface IBinaryNeuronLayer {

	public int getElement(int index);

	public void setElement(int index, int value);

	public int[] getAsVector();

	public void setVector(int[] values);

	public int size();

}
