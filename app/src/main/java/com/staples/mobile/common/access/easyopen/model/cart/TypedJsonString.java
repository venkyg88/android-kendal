package com.staples.mobile.common.access.easyopen.model.cart;

import retrofit.mime.TypedString;

//This is the body type passed to Retrofit for cart add and update calls
public class TypedJsonString extends TypedString {
    public TypedJsonString(String body) {
        super(body);
    }

    @Override public String mimeType() {
        return "application/json";
    }
}