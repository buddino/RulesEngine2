package it.cnit.gaia.rulesengine.utils;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OConcurrentResultSet;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.*;
import io.swagger.sparks.ApiException;
import io.swagger.sparks.model.SiteAPIModel;
import it.cnit.gaia.api.model.SiteInfo;
import it.cnit.gaia.buildingdb.BuildingDatabaseService;
import it.cnit.gaia.buildingdb.dto.AreaDTO;
import it.cnit.gaia.buildingdb.exceptions.BuildingDatabaseException;
import it.cnit.gaia.rulesengine.model.Area;
import it.cnit.gaia.rulesengine.service.MetadataService;
import it.cnit.gaia.rulesengine.service.SparksService;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class BuildingUtils {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	ObjectMapper mapper = new ObjectMapper();
	@Autowired
	OrientGraphFactory graphFactory;
	@Autowired
	BuildingDatabaseService bds;
	@Autowired
	MetadataService metadataService;
	@Autowired
	SparksService sparksService;


	public OrientVertex buildTreeFromBuildingDB(Long buildingId) throws IllegalAccessException, BuildingDatabaseException, ApiException {
		//TODO Use service instead of API directly (LOW)
		//TODO ApiException
		SiteAPIModel school_site = sparksService.getSite(buildingId);
		//BuildingDTO school_meta = bds.getBuildingStructure(buildingId);
		OrientGraph g = graphFactory.getTx();
		//Check if a school with the same aid already exists
		if (g.getVertices("aid", buildingId).iterator().hasNext()) {
			throw new BuildingDatabaseException("Building already exists");
		}
		OrientVertex schoolVertex = g.addVertex("class:School");
		try {
		//Create the new school vertex
		schoolVertex.setProperty("name", school_site.getName());
		schoolVertex.setProperty("lat", school_site.getLatitude());
		schoolVertex.setProperty("lon", school_site.getLongtitude());
		schoolVertex.setProperty("aid", school_site.getId());
		schoolVertex.setProperty("enabled", true);
		SiteInfo siteInfo = metadataService.getSiteInfo(buildingId);
		schoolVertex.setProperty("json", siteInfo.getJson());
		schoolVertex.setProperty("type", siteInfo.getType());
		schoolVertex.setProperty("country", siteInfo.getCountry());
	}
	catch (HttpClientErrorException e){
		if(e.getStatusCode().is4xxClientError())
			LOGGER.warn("No site info for building: "+buildingId);
	}
	catch (IllegalArgumentException e){
		LOGGER.warn("Null property found");
	}
		schoolVertex.save();
		//Create the structure
		traverseChildren(school_site, schoolVertex);
		g.commit();
		return schoolVertex;
	}

	private void traverseChildren(SiteAPIModel root, OrientVertex rootVertex) throws IllegalAccessException, ApiException {
		//TODO ApiException
		//Get subsites
		List<SiteAPIModel> subareas = sparksService.getSubsites(root.getId());
		OrientBaseGraph g = rootVertex.getGraph();
		for (SiteAPIModel area_site : subareas) {
			OrientVertex childVertex = g.addVertex("class:Area");
			//For each get metadata
			try {
				childVertex.setProperty("name", area_site.getName());
				childVertex.setProperty("aid", area_site.getId());
				childVertex.setProperty("enabled", true);
				AreaDTO area_meta = bds.getAreaById(area_site.getId());
				childVertex.setProperty("description", area_meta.getDescription());
				childVertex.setProperty("type", area_meta.getType());
				childVertex.setProperty("json", area_meta.getJson());
				//childVertex.setProperty("json",child.getJson(), OType.EMBEDDEDMAP);
			} catch (BuildingDatabaseException e) {
				LOGGER.warn("Cannot find metadata for area: " + area_site.getId());
			}
			catch (IllegalArgumentException e){
				LOGGER.warn("Null property found");
			}

			childVertex.save();
			g.addEdge(null, rootVertex, childVertex, "E").save();
			traverseChildren(area_site, childVertex);
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

	public String getPath(String rid) {
		OrientGraphNoTx noTx = graphFactory.getNoTx();
		ORID identity = noTx.getVertex(rid).getIdentity();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select unionall(name) as path from (traverse in() from ?)");
		List<ODocument> execute = query.execute(identity);
		List<String> path = execute.get(0).field("path");
		Collections.reverse(path);
		String uri = path.stream().collect(Collectors.joining("/"));
		return uri;
	}

	public void deleteBuildingTreeIncludingTheRules(Long id) throws BuildingDatabaseException {
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
		OrientGraphNoTx db = graphFactory.getNoTx(); //Riguarda
		OSQLSynchQuery query = new OSQLSynchQuery("select * from (traverse * from (select from Area where aid = ?)) where @class = \"Area\"");
		query.execute(aid);
		OConcurrentResultSet<ODocument> result = (OConcurrentResultSet<ODocument>) query.getResult();
		List<Area> areas = new ArrayList<>();
		for (ODocument d : result) {
			Area area = new Area();
			area.aid = d.field("aid");
			area.name = d.field("name");
			area.type = d.field("type");
			area.rid = d.getIdentity().toString();
			areas.add(area);
		}
		return areas;
	}

}
