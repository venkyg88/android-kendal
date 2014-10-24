package com.staples.mobile.common.access.easyopen.model.browse;

import java.util.List;

/**
 * Author: Yongnan Zhou
 * Date: 2014 Oct.
 */

public class Search {
	private String autoSuggest;
	private FilterGroup[] filterGroup;
	private int itemCount;
	private List<Product> product;
	private String searchTerm;
	private int totalPages;
	private String uniqueId;

    public String getAutoSuggest() {
        return autoSuggest;
    }

    public FilterGroup[] getFilterGroup() {
        return filterGroup;
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
