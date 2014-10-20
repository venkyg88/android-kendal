package com.staples.mobile.cfa.cart;

import com.staples.mobile.common.access.easyopen.model.cart.OrderItem;

import java.util.List;

/**
 * Generates raw json for the add and update request bodies to be passed to Retrofit calls.
 */
public class CartBodyGenerator {

    public static String generateAddBody(List<OrderItem> orderItemList){
        StringBuffer sb= new StringBuffer("{ \"orderItem\":[\n");
        int index=0;
        for(OrderItem orderItem : orderItemList){
            sb.append("{\"partNumber_"+index+"\":\""+orderItem.getPartNumber_0()+"\", ");
            sb.append("\"quantity_"+index+"\":\""+orderItem.getQuantity_0()+"\" },");
            sb.append("\n");
            index++;
        }
        sb.deleteCharAt(sb.length()-2); //to delete the last comma added
        sb.append("] }");
        return sb.toString();
    }

    public static String generateUpdateBody(List<OrderItem> orderItemList){
        StringBuffer sb= new StringBuffer("{ \"orderItem\":[\n");
        int index=0;
        for(OrderItem orderItem : orderItemList){
            sb.append("{\"orderItemId_"+index+"\":\""+orderItem.getOrderItemId_0()+"\", ");
            sb.append("\"partNumber_"+index+"\":\""+orderItem.getPartNumber_0()+"\", ");
            sb.append("\"quantity_"+index+"\":\""+orderItem.getQuantity_0()+"\" },");
            sb.append("\n");
            index++;
        }
        sb.deleteCharAt(sb.length()-2); //to delete the last comma added
        sb.append("] }");
        return sb.toString();
    }
}
