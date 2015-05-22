package app.staples.mobile.cfa.profile;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import app.staples.mobile.cfa.DrawerItem;
import app.staples.mobile.cfa.MainActivity;
import app.staples.R;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.EmptyResponse;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;
import com.staples.mobile.common.access.easyopen.model.member.Member;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CreditCardArrayAdapter extends ArrayAdapter<CCDetails> implements View.OnClickListener{

    private Context context;
    private LayoutInflater inflater;
    private String selectedCreditCardId;

    public CreditCardArrayAdapter(Context context, List<CCDetails> values, String selectedCreditCardId) {
        super(context, R.layout.profile_listview_row);
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.selectedCreditCardId = selectedCreditCardId;
        if (values!=null) {
            addAll(values);
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null) {
            view = inflater.inflate(R.layout.profile_listview_row, parent, false);
        }

        CCDetails creditCard = getItem(position);
        String cardNumber;
        if (creditCard.getCardNumber().length() > 4) {
            cardNumber = creditCard.getCardNumber().substring(creditCard.getCardNumber().length() - 4);
        } else {
            cardNumber = creditCard.getCardNumber();
        }

        Resources r = context.getResources();
        String tmpCard = r.getString(R.string.card_ending_in) + " " + cardNumber;
        String expDate = "Exp. " + creditCard.getExpirationMonth() + "/" + creditCard.getExpirationYear().substring(2,4);
        TextView ccText = (TextView) view.findViewById(R.id.rowItemText);
        TextView expText = (TextView) view.findViewById(R.id.secondItemText);
        View itemLayout = view.findViewById(R.id.item_layout);
        ccText.setText(tmpCard);
        expText.setText(expDate);
        itemLayout.setOnClickListener(this);
        itemLayout.setTag(creditCard);

        ImageView cardTypeImg = (ImageView)view.findViewById(R.id.cardTypeImg);
        cardTypeImg.setImageResource(CreditCard.Type.matchOnApiName(creditCard.getCardType()).getImageResource());
        ImageButton optionButton = (ImageButton) view.findViewById(R.id.listOptions);
        optionButton.setTag(creditCard);
        optionButton.setOnClickListener(this);

        if (selectedCreditCardId != null) {
            View selectionImageView = view.findViewById(R.id.selectionImage);
            if (selectedCreditCardId.equals(creditCard.getCreditCardId())) {
                selectionImageView.setVisibility(View.VISIBLE); // visible
            } else {
                selectionImageView.setVisibility(View.INVISIBLE); // invisible but taking up space
            }
        }
        return view;
    }
    @Override
    public void onClick(View view) {
        final CCDetails creditCard;

        switch (view.getId()) {
            case R.id.item_layout:
                creditCard = (CCDetails) view.getTag();
                String paymentMethodId = creditCard.getCreditCardId();
                if (ProfileDetails.paymentMethodSelectionListener != null) {
                    ProfileDetails.paymentMethodSelectionListener.onPaymentMethodSelected(paymentMethodId);
                }
                break;
            case R.id.listOptions:
                creditCard = (CCDetails) view.getTag();
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(context, view);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.list_menu_options, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.updateListItem:
                                updateCreditCard(creditCard);
                                return true;
                            case R.id.deleteListItem:
                                deleteCreditCard(creditCard);
                                return true;
                            default:
                                return true;
                        }
                    }
                });
                popup.show(); //showing popup menu
                break;
        }
    }

    public void updateCreditCard(final CCDetails creditCard) {
        Fragment creditFragment = Fragment.instantiate(context, CreditCardFragment.class.getName());
        Bundle bundle = new Bundle();
        bundle.putSerializable("creditCardData", creditCard);
        creditFragment.setArguments(bundle);
        ((MainActivity)context).selectFragment(DrawerItem.CARD, creditFragment, MainActivity.Transition.RIGHT);
    }

    public void deleteCreditCard(final CCDetails creditCard) {
        String creditCardId = creditCard.getCreditCardId();
        ((MainActivity)context).showProgressIndicator();
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(true);
        easyOpenApi.deleteMemberCreditCard(creditCardId, new Callback<EmptyResponse>() {

            @Override
            public void success(EmptyResponse empty, Response response) {
                (new ProfileDetails()).refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
                    @Override
                    public void onProfileRefresh(Member member, String errMsg) {
                        ((MainActivity) context).hideProgressIndicator();

                        remove(creditCard);
                        notifyDataSetChanged();
                        ((MainActivity) context).showNotificationBanner(R.string.cc_deleted);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                ((MainActivity)context).hideProgressIndicator();
                ((MainActivity) context).showErrorDialog(ApiError.getErrorMessage(error));
            }
        });
    }
}
