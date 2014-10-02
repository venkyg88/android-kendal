package com.staples.mobile.common.access.lms.model;

import java.util.List;

public class Lms{
   	private Number build;
   	private String descriptor;
   	private boolean dev;
   	private String endDate;
   	private String product;
   	private String releaseDate;
   	private List<Screen> screen;
   	private String startDate;

 	public Number getBuild(){
		return this.build;
	}
	public void setBuild(Number build){
		this.build = build;
	}
 	public String getDescriptor(){
		return this.descriptor;
	}
	public void setDescriptor(String descriptor){
		this.descriptor = descriptor;
	}
 	public boolean getDev(){
		return this.dev;
	}
	public void setDev(boolean dev){
		this.dev = dev;
	}
 	public String getEndDate(){
		return this.endDate;
	}
	public void setEndDate(String endDate){
		this.endDate = endDate;
	}
 	public String getProduct(){
		return this.product;
	}
	public void setProduct(String product){
		this.product = product;
	}
 	public String getReleaseDate(){
		return this.releaseDate;
	}
	public void setReleaseDate(String releaseDate){
		this.releaseDate = releaseDate;
	}
 	public List<Screen> getScreen(){
		return this.screen;
	}
	public void setScreen(List<Screen> screen){
		this.screen = screen;
	}
 	public String getStartDate(){
		return this.startDate;
	}
	public void setStartDate(String startDate){
		this.startDate = startDate;
	}
}
