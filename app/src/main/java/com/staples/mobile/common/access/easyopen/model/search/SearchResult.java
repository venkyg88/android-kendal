package com.staples.mobile.common.access.easyopen.model.search;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Author: Yongnan Zhou
 * Date: 2014 Oct.
 */

public class SearchResult {
	@JsonProperty("Search")
	private com.staples.mobile.common.access.easyopen.model.search.Search[] Search;
	private String recommendationUrl;

	public com.staples.mobile.common.access.easyopen.model.search.Search[] getSearch() {
		return Search;
	}
	
	public String getRecommendationUrl() {
		return recommendationUrl;
	}

}
