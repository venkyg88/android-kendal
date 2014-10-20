package com.staples.mobile.test;

import com.staples.mobile.cfa.cart.CartBodyGenerator;
import com.staples.mobile.common.access.easyopen.model.cart.OrderItem;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CartGeneratorTest {

    public static final String TAG = "CartGeneratorTest";
    @Test
    public void addBodyGeneratorTest(){

        CartBodyGenerator cbg = new CartBodyGenerator();
        List<OrderItem> testList = new ArrayList<OrderItem>();
        OrderItem item0= new OrderItem();
        item0.setPartNumber_0("partnumber0");
        item0.setQuantity_0(0);
        OrderItem item1 = new OrderItem();
        item1.setPartNumber_0("partnumber1");
        item1.setQuantity_0(1);
        testList.add(item0);
        testList.add(item1);
        String generatedString = cbg.generateAddBody(testList);
        String expected = "{\"orderItem\":["+
                "{\"partNumber_0\":\"partnumber0\" "+
                "\"quantity_0\":\"0\"},"+
                "{\"partNumber_1\":\"partnumber1\" "+
                "\"quantity_1\":\"1\"}"+
                "] }";
        System.out.println("Expected add body:"+expected);
        System.out.println("Generated add body:" + generatedString);
        Assert.assertEquals("Generated json does not match expectations",
                expected.replaceAll("\\s+",""),//removes all whitespaces for easy comparison. JSON isn't sensitive about whitespaces.
                generatedString.replaceAll("\\s+",""));

    }

    @Test
    public void updateBodyGeneratorTest(){

        CartBodyGenerator cbg = new CartBodyGenerator();
        List<OrderItem> testList = new ArrayList<OrderItem>();
        OrderItem item1= new OrderItem();
        item1.setOrderItemId("orderitemid0");
        item1.setPartNumber_0("partnumber0");
        item1.setQuantity_0(0);
        OrderItem item2 = new OrderItem();
        item2.setOrderItemId("orderitemid1");
        item2.setPartNumber_0("partnumber1");
        item2.setQuantity_0(1);
        testList.add(item1);
        testList.add(item2);
        String generatedString = cbg.generateUpdateBody(testList);
        String expected = "{\"orderItem\":["+
                "{\"orderItemId_0\":\"orderitemid0\" "+
                "\"partNumber_0\":\"partnumber0\" "+
                "\"quantity_0\":\"0\"},"+
                "{\"orderItemId_1\":\"orderitemid1\" "+
                "\"partNumber_1\":\"partnumber1\" "+
                "\"quantity_1\":\"1\"}"+
                "] }";
        System.out.println("Expected update body:"+expected);
        System.out.println("Generated update body:" + generatedString);
        Assert.assertEquals("Generated json does not match expectations",
                expected.replaceAll("\\s+",""),//removes all whitespaces for easy comparison. JSON isn't sensitive about whitespaces.
                generatedString.replaceAll("\\s+",""));

    }
}
