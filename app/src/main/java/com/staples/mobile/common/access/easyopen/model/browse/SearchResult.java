package com.staples.mobile.common.access.easyopen.model.browse;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SearchResult {
    @JsonProperty("fowardUrl")
    private String forwardUrl;
    private String recommendationUrl;
    private String redirectCategoryUrl;
    private String redirectStaticUrl;
    @JsonProperty("Search")
    private List<Search> Search;

    public List<Search> getSearch() {
        return Search;
    }

    public String getForwardUrl() {
        return forwardUrl;
    }

    public String getRecommendationUrl() {
        return recommendationUrl;
    }

    public String getRedirectCategoryUrl() {
        return redirectCategoryUrl;
    }

    public String getRedirectStaticUrl() {
        return redirectStaticUrl;
    }
}
