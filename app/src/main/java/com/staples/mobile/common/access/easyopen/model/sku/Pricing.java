
package com.staples.mobile.common.access.easyopen.model.sku;

public class Pricing{
   	private String buyMoreSaveMoreImage;
   	private String displayRegularPricing;
   	private String displayWasPricing;
   	private String finalPrice;
   	private String listPrice;
   	private String unitOfMeasure;

 	public String getBuyMoreSaveMoreImage(){
		return this.buyMoreSaveMoreImage;
	}
	public void setBuyMoreSaveMoreImage(String buyMoreSaveMoreImage){
		this.buyMoreSaveMoreImage = buyMoreSaveMoreImage;
	}
 	public String getDisplayRegularPricing(){
		return this.displayRegularPricing;
	}
	public void setDisplayRegularPricing(String displayRegularPricing){
		this.displayRegularPricing = displayRegularPricing;
	}
 	public String getDisplayWasPricing(){
		return this.displayWasPricing;
	}
	public void setDisplayWasPricing(String displayWasPricing){
		this.displayWasPricing = displayWasPricing;
	}
 	public String getFinalPrice(){
		return this.finalPrice;
	}
	public void setFinalPrice(String finalPrice){
		this.finalPrice = finalPrice;
	}
 	public String getListPrice(){
		return this.listPrice;
	}
	public void setListPrice(String listPrice){
		this.listPrice = listPrice;
	}
 	public String getUnitOfMeasure(){
		return this.unitOfMeasure;
	}
	public void setUnitOfMeasure(String unitOfMeasure){
		this.unitOfMeasure = unitOfMeasure;
	}
}
