package app.staples.mobile.cfa.rewards;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.staples.mobile.common.widget.Code128CBarcode;

import app.staples.R;
import app.staples.mobile.cfa.widget.ActionBar;

public class BarcodeFragment extends Fragment {
    private static final String TAG = BarcodeFragment.class.getSimpleName();

    private static final String TITLE = "title";
    private static final String BARCODE = "barcode";
    private static final String AMOUNT = "amount";
    private static final String EXPIRES = "expires";


    private String title;
    private String barcode;
    private String amount;
    private String expires;

    public void setArguments(String title, String barcode, String amount, String expires) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(BARCODE, barcode);
        args.putString(AMOUNT, amount);
        args.putString(EXPIRES, expires);
        setArguments(args);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(TITLE);
            barcode = args.getString(BARCODE);
            amount = args.getString(AMOUNT);
            expires = args.getString(EXPIRES);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("BarcodeFragment:onCreateView(): Displaying the Barcode screen.");
        View view = inflater.inflate(R.layout.barcode_fragment, container, false);
        ((TextView) view.findViewById(R.id.reward_coupon_label)).setText(R.string.rewards_list_tabtitle);
        ((TextView) view.findViewById(R.id.reward_coupon_value)).setText(amount);
        ((TextView) view.findViewById(R.id.reward_coupon_expire)).setText("expires " + expires);
        ((Code128CBarcode) view.findViewById(R.id.barcode)).setText(barcode);
        ((TextView) view.findViewById(R.id.caption)).setText(barcode);
        return(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.BARCODE, title);
    }
}
