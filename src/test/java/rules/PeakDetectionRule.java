package rules;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PeakDetectionRule {
	List<Double> values = Arrays
			.asList(
					765720.0,
					764090.0,
					770360.0,
					0.0,
					772220.0,
					771660.0,
					772540.0,
					0.0,
					766270.0,
					767060.0,
					765320.0,
					774040.0,
					0.0,
					2501300.0,
					2499530.0,
					2488860.0,
					0.0,
					2509810.0,
					2509320.0,
					0.0,
					2738710.0,
					3060750.0,
					3072530.0,
					0.0,
					3073310.0,
					3070680.0,
					3075950.0,
					3082090.0,
					0.0,
					3090700.0,
					3106160.0,
					3107400.0,
					0.0,
					3118860.0,
					0.0,
					3183790.0,
					3160430.0,
					3170560.0,
					3154460.0,
					4719970.0,
					4706710.0,
					4719460.0,
					4705210.0,
					4716500.0,
					4675290.0,
					0.0,
					4650850.0,
					4638150.0
			);
	Integer lag = 5;
	Double threshold = 100.0;
	Double influence = 1.0;
	Double[] avgFilter = new Double[values.size()];
	Double[] filteredY = new Double[values.size()];
	Double[] stdFilter = new Double[values.size()];
	Integer[] signal = new Integer[values.size()];


	@Test
	public void test() {
		values = values.stream().filter(x->x>0.000).collect(Collectors.toList());
		avgFilter[lag - 1] = values.subList(0, lag).stream().filter(x -> (x != null)).mapToDouble(Double::doubleValue)
								   .average().getAsDouble();
		stdFilter[lag - 1] = computeSTD(values.subList(0, lag), avgFilter[lag - 1]);
		for (int j = 0; j < lag; j++) {
			signal[j] = 0;
			filteredY[j] = values.get(j);
		}
		for (int i = lag; i < values.size(); i++) {
			if ((Math.abs(values.get(i)) - avgFilter[i - 1]) > threshold * stdFilter[i - 1]) {
				if (values.get(i) > avgFilter[i - 1])
					signal[i] = 1;
				else
					signal[i] = 1;
				filteredY[i] = influence * values.get(i) + (1 - influence) * filteredY[i - 1];
			} else {
				signal[i] = 0;
				filteredY[i] = values.get(i);
			}
			List<Double> filterList = Arrays.asList(filteredY);
			avgFilter[i] = filterList.stream().filter(x -> x != null).mapToDouble(Double::doubleValue).average()
									 .getAsDouble();
			stdFilter[i] = computeSTD(filterList.subList(i - lag, i), avgFilter[i]);
		}
//		System.out.println(Arrays.asList(signal).stream().filter(x -> x > 0).count());
		System.out.println(Arrays.asList(signal));
	}

	@Test
	public void testSTD() {
		double mean = values.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
		System.out.println(mean);
		System.out.println(computeSTD(values, mean));
	}

	@Test
	public void testSubList() {
		System.out.println(values.subList(0, lag));

	}

	private double computeSTD(List<Double> list, double mean) {
		double accumulator = 0.0;
		for (double d : list) {
			accumulator += Math.pow(d - mean, 2);
		}
		return Math.sqrt(accumulator / list.size());
	}

	private double computeMean(List<Double> list) {
		double accumulator = 0.0;
		int counter = 0;
		for (double d : list) {
			counter++;
			accumulator += d;
		}
		if (counter == 0) {
			return 0.0;
		}
		return accumulator / counter;
	}


}
