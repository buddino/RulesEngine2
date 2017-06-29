package it.cnit.gaia.rulesengine.utils;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OConcurrentResultSet;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.*;
import it.cnit.gaia.buildingdb.BuildingDatabaseService;
import it.cnit.gaia.buildingdb.dto.AreaDTO;
import it.cnit.gaia.buildingdb.dto.BuildingDTO;
import it.cnit.gaia.buildingdb.exceptions.BuildingDatabaseException;
import it.cnit.gaia.rulesengine.model.Area;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BuildingUtils {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	ObjectMapper mapper = new ObjectMapper();
	@Autowired
	OrientGraphFactory graphFactory;
	@Autowired
	BuildingDatabaseService bds;

	public OrientVertex buildTreeFromBuildingDB(Long buildingId) throws IllegalAccessException, BuildingDatabaseException {
		BuildingDTO school = bds.getBuildingStructure(buildingId);
		OrientGraph g = graphFactory.getTx();
		//Check if a school with the same aid already exists
		if (g.getVertices("aid", buildingId).iterator().hasNext()) {
			throw new BuildingDatabaseException("Building already exists");
		}
		//Create the new school vertex
		OrientVertex schoolVertex = g.addVertex("class:School");
		schoolVertex.setProperty("name", school.getName());
		schoolVertex.setProperty("people", school.getPeople());
		schoolVertex.setProperty("sqmt", school.getSqmt());
		schoolVertex.setProperty("country", school.getCountry());
		schoolVertex.setProperty("aid", school.getId());
		schoolVertex.save();
		//Create the structure
		traverseChildren(school, schoolVertex);
		g.commit();
		return schoolVertex;
	}

	private void traverseChildren(AreaDTO root, OrientVertex rootVertex) throws IllegalAccessException {
		LOGGER.debug(root.getChildren().toString());
		Set<AreaDTO> children = root.getChildren();
		OrientBaseGraph g = rootVertex.getGraph();
		for (AreaDTO child : children) {
			OrientVertex childVertex = g.addVertex("class:Area");
			childVertex.setProperty("name", child.getName());
			childVertex.setProperty("description", child.getDescription());
			childVertex.setProperty("aid", child.getId());
			childVertex.setProperty("type", child.getType());
			childVertex.setProperty("json", child.getJson());
			//childVertex.setProperty("json",child.getJson(), OType.EMBEDDEDMAP);
			childVertex.save();
			g.addEdge(null, rootVertex, childVertex, "E").save();
			traverseChildren(child, childVertex);
		}
	}

	private OrientVertex findByName(Iterable<Vertex> schools, String name) {
		Iterator<Vertex> iterator = schools.iterator();
		while (iterator.hasNext()) {
			OrientVertex vertex = (OrientVertex) iterator.next();
			if (vertex.getProperty("name").equals(name)) {
				return vertex;
			}
		}
		return null;
	}

	public OrientVertex getByPath(String path) throws Exception {
		List<String> split = Arrays.asList(path.split("/"));
		OrientGraphNoTx noTx = graphFactory.getNoTx();
		Iterator<String> iterator = split.iterator();
		Iterable<Vertex> vertices = noTx.getVerticesOfClass("School");
		OrientVertex result = null;
		while (iterator.hasNext()) {
			String name = iterator.next();
			result = findByName(vertices, name);
			if (result == null)
				throw new Exception(path + " not found");
			vertices = result.getVertices(Direction.OUT);
		}
		return result;
	}

	public void deleteBuildingTree(Long id) throws BuildingDatabaseException {
		OrientGraph graph = graphFactory.getTx();
		Iterable<Vertex> result = graph.getVertices("aid", id);
		if (!result.iterator().hasNext()) {
			throw new BuildingDatabaseException("Not found");
		}
		OrientVertex v = (OrientVertex) result.iterator().next();
		OCommandSQL command = new OCommandSQL("delete vertex from (traverse * from ?)");
		command.execute(v.getIdentity());
	}

	public List<Area> getSubAreas(Long aid) {
		OrientGraphNoTx db = graphFactory.getNoTx();
		OSQLSynchQuery query = new OSQLSynchQuery("select * from (traverse * from (select from Area where aid = ?)) where @class = \"Area\"");
		query.execute(aid);
		OConcurrentResultSet<ODocument> result = (OConcurrentResultSet<ODocument>) query.getResult();
		List<Area> areas = new ArrayList<>();
		for (ODocument d : result) {
			Area area = new Area();
			area.aid = aid;
			area.name = d.field("name");
			area.type = d.field("type");
			area.rid = d.getIdentity().toString();
			areas.add(area);
		}
		return areas;
	}

}
