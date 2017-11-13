package it.cnit.gaia.rulesengine.utils;

import io.swagger.sparks.ApiException;
import io.swagger.sparks.model.CollectionOfResourceAPIModel;
import io.swagger.sparks.model.ResourceAPIModel;
import it.cnit.gaia.rulesengine.service.SparksService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RuleCreateHelper {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	//Static hard coded map
	//TODO Use the DB to read the map





	@Autowired
	SparksService sparksService;

	public List<ResourceAPIModel> getAllSuggestedResources(@NotNull String property, @NotNull Long aid) throws ApiException {
		CollectionOfResourceAPIModel collectionOfResourceAPIModel = sparksService.getResApi()
																				 .retrieveSiteResources(aid);
		if (collectionOfResourceAPIModel.getResources() != null) {
			List<ResourceAPIModel> filtered = collectionOfResourceAPIModel.getResources().stream()
																		  .filter(r -> r.getProperty().equals(property))
																		  .collect(Collectors.toList());
			return filtered;
		}
		return null;
	}

	public ResourceAPIModel getSuggestedResource(@NotNull String property, @NotNull Long aid) throws ApiException {
		List<ResourceAPIModel> filtered = getAllSuggestedResources(property, aid);
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





}
