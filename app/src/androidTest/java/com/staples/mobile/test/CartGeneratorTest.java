package com.staples.mobile.test;

import com.staples.mobile.cfa.cart.CartBodyGenerator;
import com.staples.mobile.common.access.easyopen.model.cart.OrderItem;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

//For testing the body generator functions for the cart add and update calls
public class CartGeneratorTest {

    @Test
    public void addBodyGeneratorTest(){

        CartBodyGenerator cbg = new CartBodyGenerator();
        List<OrderItem> testList = new ArrayList<OrderItem>();
        OrderItem item0= new OrderItem(null,"partnumber0",0);
        OrderItem item1 = new OrderItem(null,"partnumber1",1);
        testList.add(item0);
        testList.add(item1);
        String generatedString = cbg.generateAddBody(testList);
        String expected = "{\"orderItem\":["+
                "{\"partNumber_0\":\"partnumber0\",\n "+
                "\"quantity_0\":\"0\"},"+
                "{\"partNumber_1\":\"partnumber1\",\n "+
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
        OrderItem item1= new OrderItem("orderitemid0","partnumber0",0);
        OrderItem item2 = new OrderItem("orderitemid1","partnumber1",1);
        testList.add(item1);
        testList.add(item2);
        String generatedString = cbg.generateUpdateBody(testList);
        String expected = "{\"orderItem\":["+
                "{\"orderItemId_0\":\"orderitemid0\",\n "+
                "\"partNumber_0\":\"partnumber0\",\n "+
                "\"quantity_0\":\"0\"},"+
                "{\"orderItemId_1\":\"orderitemid1\",\n "+
                "\"partNumber_1\":\"partnumber1\",\n "+
                "\"quantity_1\":\"1\"}"+
                "] }";
        System.out.println("Expected update body:"+expected);
        System.out.println("Generated update body:" + generatedString);
        Assert.assertEquals("Generated json does not match expectations",
                expected.replaceAll("\\s+",""),//removes all whitespaces for easy comparison. JSON isn't sensitive about whitespaces.
                generatedString.replaceAll("\\s+",""));

    }
}
