package hr.fer.irg.RBM;

import org.junit.Assert;
import org.junit.Test;

import fer.hr.thesis.rbm.layer.NeuronLayer;

public class NeuronLayerTest {
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorGivenNull(){
		NeuronLayer l = new NeuronLayer(null);
	}
	
	@Test
	public void testSize(){
		NeuronLayer<Integer> l = new NeuronLayer<>(new Integer[]{0, 1, 2});
		Assert.assertEquals(3, l.size());
	}
	
	@Test
	public void testGetFirst(){
		NeuronLayer<Integer> l = new NeuronLayer<>(new Integer[]{0, 1, 2});
		Assert.assertEquals(Integer.valueOf(0), l.getElement(0));
	}
	
	@Test
	public void testGetLast(){
		NeuronLayer<Integer> l = new NeuronLayer<>(new Integer[]{0, 1, 2});
		Assert.assertEquals(Integer.valueOf(2), l.getElement(2));
	}
	
	@Test
	public void testSet(){
		NeuronLayer<Integer> l = new NeuronLayer<>(new Integer[]{0, 1, 2});
		l.setElement(0, 5);
		Assert.assertEquals(Integer.valueOf(5), l.getElement(0));
	}
	
	@Test
	public void testGetAsVector(){
		Integer[] elements = new Integer[]{0, 1, 2};
		NeuronLayer<Integer> l = new NeuronLayer<>(elements);
		Assert.assertArrayEquals(elements, l.getAsVector());
	}

}
