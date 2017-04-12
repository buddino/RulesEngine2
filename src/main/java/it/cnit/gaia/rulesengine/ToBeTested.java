package it.cnit.gaia.rulesengine;

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ToBeTested {

	@Autowired(required = true)
	OrientGraphFactory gf;

	public String useService(){
		return gf.getConnectionStrategy();
	}

	public OrientGraphFactory getFactory(){
		return gf;
	}

}
