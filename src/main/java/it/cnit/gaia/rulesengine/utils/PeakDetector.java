package it.cnit.gaia.rulesengine.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PeakDetector {
	private static final double[] kernel = new double[]{-6.9199e-13, -8.4164e-09, -1.5849e-05, -0.0043643, -0.15141, -0.27733, 0.86733, -0.27733, -0.15141, -0.0043643, -1.5849e-05, -8.4164e-09, -6.9199e-13};
	private double[] data;
	private Convolution convolution;
	private List<Integer> peak_indeces;
	private List<Double> peaks = new ArrayList<>();

	public PeakDetector() {
	}

	public PeakDetector(double[] data) {
		this.data = data;
	}

	public void setData(double[] data) {
		this.data = data;
	}

	public void setData(Double[] data) {
		double[] d = new double[data.length];
		for (int i = 0; i < data.length; i++) {
			d[i] = data[i];
		}
		this.data = d;
	}

	public PeakDetector detectPeaks() {
		if (data == null)
			throw new NullPointerException();
		convolution = new Convolution(data, kernel);
		double[] filtered = convolution.colvoltion1D();
		//Calcola la media per soglia
		double mean = computeMean(data);
		//Prendi i valori sopra la soglia

		peak_indeces = threshold(filtered, mean);
		peak_indeces.forEach(i -> peaks.add(data[i]));
		return this;
	}

	public List<Integer> getPeakIndeces() {
		return peak_indeces;
	}

	public List<Double> getPeaks() {
		return peaks;
	}

	private double computeMean(double[] data) {
		double accumulator = 0.0;
		for (int i = 0; i < data.length; i++) {
			accumulator += data[i];
		}
		return accumulator / data.length;
	}

	private List<Integer> threshold(double[] data, double t) {
		List<Double> peaks = new ArrayList<>();
		List<Integer> peak_indeces = new ArrayList<>();
		for (int i = 0; i < data.length; i++) {
			if (data[i] > t) {
				peaks.add(data[i]);
				peak_indeces.add(i);
			}
		}
		return peak_indeces;
	}

	public PeakDetector removeAnomalies() {
		for (int i = 1; i < data.length; i++) {
			if (data[i] == 0.0)
				data[i] = data[i - 1];
		}
		return this;
	}

	public double[] getData() {
		return data;
	}

	public void setData(Collection<Double> data) {
		double[] d = new double[data.size()];
		int i = 0;
		for (Double dd : data) {
			d[i++] = dd;
		}
		this.data = d;
	}


}
