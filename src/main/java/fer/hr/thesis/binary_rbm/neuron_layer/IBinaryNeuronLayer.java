package fer.hr.thesis.binary_rbm.neuron_layer;

/**
 * Models a single layer of a Binary Restricted Boltzmann Machine. It saves
 * values of all the neurons inside the layer and enables setting and getting
 * their values.
 * 
 * @author Dunja Vesinger
 * @version 1.0.0
 */
public interface IBinaryNeuronLayer {

	/**
	 * Returns the value of the neuron at the given index.
	 * 
	 * @param index
	 *            Index of the neuron
	 * @return Value of the neuron at the given index
	 */
	public int getElement(int index);

	/**
	 * Sets the value of the neuron at the given index.
	 * 
	 * @param index
	 *            Index of the neuron
	 * @param value
	 *            Value to be set
	 */
	public void setElement(int index, int value);

	/**
	 * Returns a vector containing the values of all the neurons inside this
	 * layer in order in which they are stored.
	 * 
	 * @return Vector containing the values of all the neurons
	 */
	public int[] getAsVector();

	/**
	 * Sets the values of all the neurons inside the layer to the values given
	 * in the vector in order in which they are given.
	 * 
	 * @param values Values of the neurons to be set
	 */
	public void setVector(int[] values);

	/**
	 * Returns the number of neurons in the layer.
	 * @return Number of neurons in the layer
	 */
	public int size();

}
