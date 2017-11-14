package it.cnit.gaia.rulesengine.utils;

import io.swagger.sparks.ApiException;
import io.swagger.sparks.model.CollectionOfResourceAPIModel;
import io.swagger.sparks.model.ResourceAPIModel;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.service.SparksService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RuleCreationHelper {
	//Static hard coded map
	//TODO Use the DB to read the map
	private static HashMap<String, String> parametersMap = new HashMap<>();

	static {
		parametersMap.put("pwf_uri", "Power Factor");
		parametersMap.put("temperature_uri", "Temperature");
		parametersMap.put("humidity_uri", "Relative Humidity");
	}

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	Pattern external = Pattern.compile("ext_");
	Pattern root = Pattern.compile("root_");
	Pattern parent = Pattern.compile("parent_");
	@Autowired
	SparksService sparksService;

	@Autowired
	RulesLoader rulesLoader;


	public ResourceAPIModel getSuggestedResourceByFieldname(@NotNull String uri, @NotNull Long aid, @Nullable String property) throws ApiException {
		String tag = null;
		if (needsRoot(uri) != null) {
			uri = needsRoot(uri);
			//Get root
			try {
				aid = rulesLoader.getAreaMap().getOrDefault(aid, null).getSchool().getAid();
			} catch (NullPointerException e) {
				LOGGER.warn("Error while retrieving root area", e);
			}

		} else if (needsExternal(uri) != null) {
			uri = needsExternal(uri);
			//Get root
			try {
				aid = rulesLoader.getAreaMap().getOrDefault(aid, null).getSchool().getAid();
				tag = "External";
			} catch (NullPointerException e) {
				LOGGER.warn("Error while retrieving root area", e);
			}
		}

		if (property == null)
			property = parametersMap.getOrDefault(uri, null);
		if (property == null)
			return null;
		return getSuggestedResourceByProperty(property, aid, tag);
	}


	public List<ResourceAPIModel> getAllSuggestedResources(@NotNull String property, @NotNull Long
			aid, @Nullable String tag) throws ApiException {
		CollectionOfResourceAPIModel collectionOfResourceAPIModel = sparksService.getResApi()
																				 .retrieveSiteResources(aid);
		if (collectionOfResourceAPIModel.getResources() != null) {
			Stream<ResourceAPIModel> filteredStream = collectionOfResourceAPIModel.getResources().stream()
																				  .filter(r -> r.getProperty()
																								.equals(property));
			if (tag != null) {
				filteredStream = filteredStream.filter(r -> r.getTags().contains(tag));
			}
			List<ResourceAPIModel> filtered = filteredStream.collect(Collectors.toList());
			return filtered;
		}
		return null;
	}


	public ResourceAPIModel getSuggestedResourceByProperty(@NotNull String property, @NotNull Long aid, @Nullable String tag) throws ApiException {
		List<ResourceAPIModel> filtered = getAllSuggestedResources(property, aid, tag);
		if (filtered == null || filtered.size() == 0)
			return null;

		if (filtered.size() == 1)
			return filtered.get(0);

		//Return the resource with the shortest path in the URI
		if (filtered.size() > 1) {
			ResourceAPIModel resource = null;
			for (ResourceAPIModel currentResource : filtered) {
				int newLength = currentResource.getUri().split("/").length;
				if (resource == null)
					resource = currentResource;
				else {
					if (newLength < resource.getUri().split("/").length)
						resource = currentResource;
				}
			}
			return resource;
		}
		return null;
	}

	private String needsRoot(String uri) {
		if (root.matcher(uri).find()) {
			String s = uri.replaceFirst(root.pattern(), "");
			return s;
		}
		return null;
	}

	private String needsExternal(String uri) {
		if (external.matcher(uri).find()) {
			String s = uri.replaceFirst(external.pattern(), "");
			return s;
		}
		return null;
	}

	private String needsParent(String uri) {
		if (parent.matcher(uri).find()) {
			String s =uri.replaceFirst(root.pattern(), "");
			return s;
		}
		return null;
	}

}
