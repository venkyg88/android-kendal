
package com.staples.mobile.common.access.easyopen.model.sku;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Sku{

    //You must have this for JACKSON to work! fr GSON I changed product to Product to work as it does not have annotations. This annotation has no effect on the retrofit options.
    @JsonProperty("Product")
   	private List<Product> Product;
 	public List<Product> getProduct(){
		return this.Product;
	}
	public void setProduct(List<Product> Product){
		this.Product = Product;
	}


}
