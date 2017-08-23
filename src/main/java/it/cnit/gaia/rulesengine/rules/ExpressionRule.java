package it.cnit.gaia.rulesengine.rules;

import io.swagger.sparks.model.SingleResourceMeasurementAPIModel;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import it.cnit.gaia.rulesengine.utils.Exp4JOperators;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.tokenizer.UnknownFunctionOrVariableException;

import java.util.HashMap;
import java.util.Map;

public class ExpressionRule extends GaiaRule{

	/**
	 * The textual expression
	 */
	@LoadMe
	public String expression;

	/**
	 * Mapping from variable name tu resource URI
	 */
	public Map<String, String> variable2uri = new HashMap<>();

	/**
	 * A Key-Value map containings all the numeric fields of the rule
	 */
	@LogMe
	public Map<String, Double> fields = new HashMap<>();

	/**
	 * The compiled expression
	 */
	private Expression expr;

	@Override
	public boolean condition() {
			return evaluate();
	}

	@Override
	public boolean init(){
		return buildExpression();
	}

	public boolean buildExpression() {
		ExpressionBuilder eb = new ExpressionBuilder(expression);
		eb.variables(variable2uri.keySet());
		eb.variables(fields.keySet());
		eb.operator(Exp4JOperators.operators);
		try {
			expr = eb.build();
		}
		catch (UnknownFunctionOrVariableException e){
			warn(e.getMessage());
			return false;
		}
		//expr.setVariables(fields);
		if (expr.validate(false).isValid())
			return true;
		warn(expr.validate().getErrors().toString());
		return false;
	}


	/**
	 * For each variable of the expression, convert is to an URI and get the measurement
	 * @return true if the condition defined by the expression is above 0.5 (1.0 stands for true, 0.0 for false)
	 */
	private boolean evaluate() {
		for (String var : variable2uri.keySet()) {
			String uri = variable2uri.get(var);
			SingleResourceMeasurementAPIModel value = measurements.getLatestFor(uri);
			if(value!=null) {
				Double measurement = value.getReading();
				fields.put(var, measurement);
			}
		}
		expr.setVariables(fields);
		return expr.evaluate() > 0.5;
	}


}
