package com.staples.mobile.cfa.feed;

/**
 * Author: Yongnan Zhou
 */

public class SeenProductsRowItem {
	private String productName;
	private String currentPrice;
	private String reviewCount;
	private String rating;
    private String sku;
    private String unitOfMeasure;
    private String imageUrl;

    public SeenProductsRowItem(String productName, String currentPrice, String reviewCount,
                               String rating, String sku, String unitOfMeasure, String imageUrl) {
        this.productName = productName;
        this.currentPrice = currentPrice;
        this.reviewCount = reviewCount;
        this.rating = rating;
        this.sku = sku;
        this.unitOfMeasure = unitOfMeasure;
        this.imageUrl = imageUrl;
    }

	public String getProduceName() {
		return productName;
	}

	public String getCurrentPrice() {
		return currentPrice;
	}

	public String getReviewCount() {
		return reviewCount;
	}

	public String getRating() {
		return rating;
	}

    public String getSku() {
        return sku;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public String getImageUrl() {
        return imageUrl;
    }

	@Override
	public String toString() {
		return productName;
	}	
}
