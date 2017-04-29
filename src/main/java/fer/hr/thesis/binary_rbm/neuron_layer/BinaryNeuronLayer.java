package fer.hr.thesis.binary_rbm.neuron_layer;

import java.util.Arrays;

public class BinaryNeuronLayer implements IBinaryNeuronLayer{
	
	private int[] values;
	
	public BinaryNeuronLayer(int[] values) {
		if(values == null){
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
		if(this.values != null && this.values.length != values.length){
			throw new UnsupportedOperationException("Trying to change dimensions of a neuron layer.");
		}
		this.values = new int[values.length];
		System.arraycopy(values, 0, this.values, 0, values.length);
	}
	
	
	@Override
	public int size() {
		return values.length;
	}
	
	@Override
	public String toString(){
		return Arrays.toString(values);
	}

}
