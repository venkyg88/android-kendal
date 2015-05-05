package app.staples.mobile.test;

import com.crittercism.app.Crittercism;
import com.staples.mobile.common.access.easyopen.model.cart.OrderItem;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import app.staples.mobile.cfa.cart.CartApiManager;

//For testing the body generator functions for the cart add and update calls
public class CartGeneratorTest {
    @Test
    public void updateBodyGeneratorTest(){
        Method method = null;
        String generatedString = null;

        // Find method
        try {
            method = CartApiManager.class.getDeclaredMethod("generateAddUpdateBody", List.class);
            method.setAccessible(true);
        } catch(NoSuchMethodException e) {
            Crittercism.logHandledException(e);
            Assert.fail("Could not find generateAddUpdateBody in CartApiManager");
        }

        // Make update list
        List<OrderItem> list = new ArrayList<OrderItem>();
        list.add(new OrderItem("orderitemid0","partnumber0",0));
        list.add(new OrderItem("orderitemid1","partnumber1",1));

        // Invoke
        try {
            generatedString = (String) method.invoke(null, list);
        } catch(Exception e) {
            Crittercism.logHandledException(e);
            Assert.fail("Could not invoke generateAddUpdateBody on CartApiManager ");
        }

        // Compare
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
