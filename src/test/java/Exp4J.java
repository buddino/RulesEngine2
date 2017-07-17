import it.cnit.gaia.rulesengine.utils.Exp4JOperators;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.junit.Test;

public class Exp4J {
	@Test
	public void test() {
		String expression = "x*2 > 9 || x+2.0<2.0";
		ExpressionBuilder eb = new ExpressionBuilder(expression);
		eb.variable("x");
		eb.operator(Exp4JOperators.operators);
		Expression build = eb.build();
		build.setVariable("x", 3.0);
		System.out.println(build.evaluate());
	}

	@Test
	public void testComplexExpr() {
		String expression = "humid*c1 + temp*humid*c3 - temp/humid + temp*(c2-humid)";
		ExpressionBuilder eb = new ExpressionBuilder(expression);
		eb.variable("threshold").variable("temp").variable("humid").variable("c1").variable("c2").variable("c3");
		eb.operator(Exp4JOperators.operators);
		Expression build = eb.build();
		build.setVariable("threshold",35.0);
		build.setVariable("temp",31.0);
		build.setVariable("humid",85.0);
		build.setVariable("c1",355.0);
		build.setVariable("c2",35.20);
		build.setVariable("c3",3.0);
		System.out.println(build.evaluate());
	}

	@Test
	public void testComplexExprBool() {
		String expression = "humid*c1 + temp*humid*c3 - temp/humid + temp*(c2-humid) > threshold";
		ExpressionBuilder eb = new ExpressionBuilder(expression);
		eb.variable("threshold").variable("temp").variable("humid").variable("c1").variable("c2").variable("c3");
		eb.operator(Exp4JOperators.operators);
		Expression build = eb.build();
		build.setVariable("threshold",35.0);
		build.setVariable("temp",31.0);
		build.setVariable("humid",85.0);
		build.setVariable("c1",355.0);
		build.setVariable("c2",35.20);
		build.setVariable("c3",3.0);
		System.out.println(build.evaluate());
	}

	@Test
	public void testComplexExprBoolNegate() {
		String expression = "!(humid*c1 + temp*humid*c3 - temp/humid + temp*(c2-humid) > threshold)";
		ExpressionBuilder eb = new ExpressionBuilder(expression);
		eb.variable("threshold").variable("temp").variable("humid").variable("c1").variable("c2").variable("c3");
		eb.operator(Exp4JOperators.operators);
		Expression build = eb.build();
		build.setVariable("threshold",35.0);
		build.setVariable("temp",31.0);
		build.setVariable("humid",85.0);
		build.setVariable("c1",355.0);
		build.setVariable("c2",35.20);
		build.setVariable("c3",3.0);
		System.out.println(build.evaluate());
	}
}
