package com.staples.mobile.common.access.lms.model;

import java.util.List;

public class Screen{
   	private List<Item> item;
   	private String name;
   	private String pattern;

 	public List<Item> getItem(){
		return this.item;
	}
	public void setItem(List<Item> item){
		this.item = item;
	}
 	public String getName(){
		return this.name;
	}
	public void setName(String name){
		this.name = name;
	}
 	public String getPattern(){
		return this.pattern;
	}
	public void setPattern(String pattern){
		this.pattern = pattern;
	}
}
