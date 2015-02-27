package com.staples.mobile.cfa.profile;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.SkuDetails;
import com.staples.mobile.common.access.easyopen.model.member.ShipmentSKU;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Created by Avinash Dodda.
 */
public class ShipmentArrayAdapter extends ArrayAdapter<ShipmentSKU> {
    private LayoutInflater inflater;
    private TextView skuTitle;
    private TextView skuPrice;
    private TextView skuQuantity;
    private ImageView skuImage;
    EasyOpenApi easyOpenApi;
    Context activity;


    public ShipmentArrayAdapter(Context context) {
        super(context, R.layout.shipment_listitem);
        this.activity = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        View rowView = inflater.inflate(R.layout.shipment_listitem, parent, false);
        skuTitle = (TextView)rowView.findViewById(R.id.shipmentTitle);
        skuPrice = (TextView)rowView.findViewById(R.id.shipmentPrice);
        skuQuantity = (TextView)rowView.findViewById(R.id.shipmentQty);
        skuImage = (ImageView)rowView.findViewById(R.id.skuImage);

        final ShipmentSKU shipment = getItem(position);
        easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getSkuDetails(shipment.getSkuNumber(), null, 50, new Callback<SkuDetails>() {
            @Override
            public void success(SkuDetails skuDetails, Response response) {
                Picasso.with(activity).load(skuDetails.getProduct().get(0).getImage().get(0).getUrl()).error(R.drawable.no_photo).into(skuImage);
                skuTitle.setText(shipment.getSkuDescription());
                skuPrice.setText(shipment.getLineTotal());
                skuQuantity.setText(shipment.getQtyOrdered());
            }

            @Override
            public void failure(RetrofitError error) {
                ((MainActivity)activity).showErrorDialog(ApiError.getErrorMessage(error));
            }
        });




        return rowView;
    }
}
