package com.staples.mobile.cfa.search;

/**
 * Author: Yongnan Zhou
 */

// Used to save meta data from search result
public class SearchResultRowItem {
	private int imageId;
	private String productName;
	private String previousPrice;
	private String currentPrice;
	private String reviewAmount;
	private String rating;
    private String sku;
    private String unitOfMeasure;
    private String imageUrl;

	public SearchResultRowItem(int imageId, String productName, String previousPrice, String currentPrice, 
			String reviewAmount, String rating) {
		this.imageId = imageId;
		this.productName = productName;
		this.previousPrice = previousPrice;
		this.currentPrice = currentPrice;
		this.reviewAmount = reviewAmount;
		this.rating = rating;
	}

    public SearchResultRowItem(int imageId, String productName, String previousPrice, String currentPrice,
                               String reviewAmount, String rating, String sku, String unitOfMeasure, String imageUrl) {
        this.imageId = imageId;
        this.productName = productName;
        this.previousPrice = previousPrice;
        this.currentPrice = currentPrice;
        this.reviewAmount = reviewAmount;
        this.rating = rating;
        this.sku = sku;
        this.unitOfMeasure = unitOfMeasure;
        this.imageUrl = imageUrl;
    }

	public int getImageId() {
		return imageId;
	}

	public void setImageId(int imageId) {
		this.imageId = imageId;
	}

	public String getProduceName() {
		return productName;
	}

	public void setProduceName(String productName) {
		this.productName = productName;
	}

	public String getPreviousPrice() {
		return previousPrice;
	}

	public void setPreviousPrice(String previousPrice) {
		this.previousPrice = previousPrice;
	}

	public String getCurrentPrice() {
		return currentPrice;
	}

	public void setCurrentPrice(String currentPrice) {
		this.currentPrice = currentPrice;
	}

	public String getReviewAmount() {
		return reviewAmount;
	}

	public void setReviewAmount(String reviewAmount) {
		this.reviewAmount = reviewAmount;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

	@Override
	public String toString() {
		return productName;
	}	
}
