package fer.hr.thesis.binary_rbm.neuron_layer;

import java.io.Serializable;
import java.util.Arrays;

/**
 * The BinaryNeuronLayer class represents one visible or hidden layer of a
 * Restricted Boltzmann Machine. It saves values of all the neurons inside the
 * layer and enables setting and getting their values. The number of neurons is
 * determined on creation and immutable after.
 * 
 * @author Dunja Vesinger
 * @version 1.0.0
 */
public class BinaryNeuronLayer implements IBinaryNeuronLayer, Serializable {

	/**
	 * Number used for serialization.
	 */
	private static final long serialVersionUID = 5548095782618667836L;

	/**
	 * Values of all the neurons inside the layer.
	 */
	private int[] values;

	/**
	 * Creates a new BinaryNeuronLayer with the given values.
	 * @param values Values of the neurons
	 */
	public BinaryNeuronLayer(int[] values) {
		if (values == null) {
			throw new IllegalArgumentException("Values of the layer cannot be null.");
		}
		this.values = new int[values.length];
		System.arraycopy(values, 0, this.values, 0, values.length);
	}

	@Override
	public int getElement(int index) {
		return values[index];
	}

	@Override
	public void setElement(int index, int value) {
		values[index] = value;

	}

	@Override
	public int[] getAsVector() {
		int[] copyOfValues = new int[values.length];
		System.arraycopy(values, 0, copyOfValues, 0, values.length);
		return values;
	}

	@Override
	public void setVector(int[] values) {
		if (this.values != null && this.values.length != values.length) {
			throw new UnsupportedOperationException("Trying to change dimensions of a neuron layer from "
					+ this.values.length + " to " + values.length + ".");
		}
		this.values = new int[values.length];
		System.arraycopy(values, 0, this.values, 0, values.length);
	}

	@Override
	public int size() {
		return values.length;
	}

	@Override
	public String toString() {
		return Arrays.toString(values);
	}

}
