package it.cnit.gaia.rulesengine.utils;

import net.objecthunter.exp4j.operator.Operator;

import java.util.Arrays;
import java.util.List;

public class Exp4JOperators {
	static Operator gte = new Operator(">=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
		@Override
		public double apply(double[] values) {
			if (values[0] >= values[1]) {
				return 1d;
			} else {
				return 0d;
			}
		}
	};

	static Operator gt = new Operator(">", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
		@Override
		public double apply(double[] values) {
			if (values[0] > values[1]) {
				return 1d;
			} else {
				return 0d;
			}
		}
	};

	static Operator lte = new Operator("<=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
		@Override
		public double apply(double[] values) {
			if (values[0] <= values[1]) {
				return 1d;
			} else {
				return 0d;
			}
		}
	};

	static Operator lt = new Operator("<", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
		@Override
		public double apply(double[] values) {
			if (values[0] < values[1]) {
				return 1d;
			} else {
				return 0d;
			}
		}
	};

	static Operator eq = new Operator("==", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
		@Override
		public double apply(double[] values) {
			if (values[0] - values[1] < 0.001) {
				return 1d;
			} else {
				return 0d;
			}
		}
	};

	public static List<Operator> operators = Arrays.asList(lt,lte,eq,gt,gte);
}
