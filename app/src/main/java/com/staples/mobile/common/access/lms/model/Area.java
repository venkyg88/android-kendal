package com.staples.mobile.common.access.lms.model;

import java.util.List;

public class Area{
   	private String name;
   	private List<String> skuItem;
   	private String skuList;
   	private String viewMore;

 	public String getName(){
		return this.name;
	}
	public void setName(String name){
		this.name = name;
	}
 	public List<String> getSkuItem(){
		return this.skuItem;
	}
	public void setSkuItem(List<String> skuItem){
		this.skuItem = skuItem;
	}
 	public String getSkuList(){
		return this.skuList;
	}
	public void setSkuList(String skuList){
		this.skuList = skuList;
	}
 	public String getViewMore(){
		return this.viewMore;
	}
	public void setViewMore(String viewMore){
		this.viewMore = viewMore;
	}
}
