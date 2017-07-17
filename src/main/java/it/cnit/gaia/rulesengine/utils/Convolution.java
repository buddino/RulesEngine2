package it.cnit.gaia.rulesengine.utils;


public class Convolution {

	private double[] in;
	private double[] kernal;
	private double[] out;
	public Convolution(double[] _in,double[]_kernal) {
		setIn(_in);
		setKernal(_kernal);
	}
	private void setIn(double[] _in)throws IllegalArgumentException {
		// check the size of the datavector... 
		if(_in.length <= 3) {
			throw new IllegalArgumentException("Data length can't be zero or smaller than zero");
		}

		this.in = _in;
		//Our denoised singal is of same length as of our input raw signal...
		this.out = new double [_in.length];
	}
	private void setKernal(double[] _kernal)throws IllegalArgumentException {

		//Check length of Kernel vector if its greater than zero; or Length is not an odd number. 
		if(_kernal.length <= 0 /*|| (_kernal.length%2) == 0*/ ) {
			throw new IllegalArgumentException("kernal length can't be zero or smaller than zero");
		}

		this.kernal = _kernal;
	}

	public double[] colvoltion1D() {
		int kernalSize = kernal.length;
		int zerosToAppend = (int) Math.ceil(kernalSize/2);
		// Make a new dataVector by appending zeros.
		double[] dataVec = new double [kernalSize-1+in.length];
		// add data in dataVec by compansating the zeropadding
		for(int i = 0; i< in.length;i++) 
		{
			//Add data inbetween the padded zeros...
			dataVec[i+(int) Math.ceil(kernalSize/2)] = in[i];
		}
		// convolution begins here...
		int end = 0;
		while (end < in.length) {
			double sum = 0.0;
			for (int i = 0; i <kernalSize; i++)
			{		
				sum += kernal[i]*dataVec[end+i];
			}
			out[end]= sum;
			end = end+1;
		}
		return out;		
	}

}