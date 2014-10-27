package com.staples.mobile.common.access.easyopen.model.browse;

import java.util.List;

public class Search {
	private String autoSuggest;
    private String blueBoxMessage;
    private String didYouMean;
	private List<FilterGroup> filterGroup;
    private String forwardUrl;
	private int itemCount;
	private List<Product> product;
	private String searchTerm;
	private int totalPages;
	private String uniqueId;

    public String getAutoSuggest() {
        return autoSuggest;
    }

    public String getBlueBoxMessage() {
        return blueBoxMessage;
    }

    public String getDidYouMean() {
        return didYouMean;
    }

    public List<FilterGroup> getFilterGroup() {
        return filterGroup;
    }

    public String getForwardUrl() {
        return forwardUrl;
    }

    public int getItemCount() {
        return itemCount;
    }

    public List<Product> getProduct() {
        return product;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public String getUniqueId() {
        return uniqueId;
    }
}
