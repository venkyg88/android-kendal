package com.staples.mobile.common.access.feed;

/**
 * Created by Avinash Dodda
 */

public class FavouriteListDetail {

    private String detailsURL;

    private String listId;

    private String listName;

    private String uniqueItemsInList;

    public String getDetailsURL() {
        return detailsURL;
    }

    public void setDetailsURL(String detailsURL) {
        this.detailsURL = detailsURL;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getUniqueItemsInList() {
        return uniqueItemsInList;
    }

    public void setUniqueItemsInList(String uniqueItemsInList) {
        this.uniqueItemsInList = uniqueItemsInList;
    }
}
