package it.cnit.gaia.rulesengine.utils;

import net.objecthunter.exp4j.operator.Operator;

import java.util.Arrays;
import java.util.List;

public class Exp4JOperators {
	static Operator gte = new Operator(">=", 2, true, Operator.PRECEDENCE_ADDITION - 10) {
		@Override
		public double apply(double[] values) {
			if (values[0] >= values[1]) {
				return 1d;
			} else {
				return 0d;
			}
		}
	};

	static Operator gt = new Operator(">", 2, true, Operator.PRECEDENCE_ADDITION - 10) {
		@Override
		public double apply(double[] values) {
			if (values[0] > values[1]) {
				return 1d;
			} else {
				return 0d;
			}
		}
	};

	static Operator lte = new Operator("<=", 2, true, Operator.PRECEDENCE_ADDITION - 10) {
		@Override
		public double apply(double[] values) {
			if (values[0] <= values[1]) {
				return 1d;
			} else {
				return 0d;
			}
		}
	};

	static Operator lt = new Operator("<", 2, true, Operator.PRECEDENCE_ADDITION - 10) {
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

	static Operator and = new Operator("&&", 2, true, Operator.PRECEDENCE_ADDITION - 100) {
		@Override
		public double apply(double[] values) {
			if((values[0]==0.0 || values[0]==1.0) && (values[1]==0.0 || values[1]==1.0)){
				if(values[0]==0.0 || values[1]==0.0)
					return 0d;
				return 1d;
			}
			System.err.println("Operator && works only on boolean (1.0 or 0.0) values");
			return 0d;
		}
	};

	static Operator or = new Operator("||", 2, true, Operator.PRECEDENCE_ADDITION - 100) {
		@Override
		public double apply(double[] values) {
			if((values[0]==0.0 || values[0]==1.0) && (values[1]==0.0 || values[1]==1.0)){
				if(values[0]==0.0 && values[1]==0.0)
					return 0d;
				return 1d;
			}
			System.err.println("Operator && works only on boolean (1.0 or 0.0) values");
			return 0d;
		}
	};

	static Operator not = new Operator("!", 1, true, Operator.PRECEDENCE_ADDITION - 500) {
		@Override
		public double apply(double[] values) {
			if(values[0]==0.0 || values[0]==1.0){
				if(values[0]==0.0)
					return 1d;
				return 0d;
			}
			System.err.println("Operator && works only on boolean (1.0 or 0.0) values");
			return 0d;
		}
	};

	public static List<Operator> operators = Arrays.asList(lt,lte,eq,gt,gte,and,or,not);
}
