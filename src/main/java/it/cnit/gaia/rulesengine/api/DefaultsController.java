package it.cnit.gaia.rulesengine.api;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import io.swagger.annotations.*;
import it.cnit.gaia.rulesengine.api.dto.DefaultsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Consumes;

@RestController
@Consumes({MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_JSON_UTF8_VALUE})
@Api(tags = "Defaults API",
		description = "API for managing default values for the rule classes",
		authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "read")})},
		produces = MediaType.APPLICATION_JSON_VALUE)
public class DefaultsController {
	@Autowired
	private OrientGraphFactory ogf;


	//Add a default
	@ApiOperation(value = "ADD a rule to area", notes = "Add a custom rule according to the object passed in the body")
	@PostMapping(value = "rules/{classname}/default", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> addDefaultForClass(
			@ApiParam(value = "TODO", example = "PowerFactor", required = true)
			@PathVariable String classname, @RequestBody DefaultsDTO defaultsDTO) {
		//TODO Move out
		ODatabaseDocumentTx database = ogf.getDatabase();
		ODocument d = new ODocument("GaiaDefaults");
		d.field("classname",classname);
		//TODO Add control on the classname
		d.field("fields",defaultsDTO.getFields());
		d.field("suggestion", defaultsDTO.getSuggestion());
		d.save();
		System.out.println(defaultsDTO);
		return null;

	}
	//Delete default
	//Patch a default
	//Put a default
}
