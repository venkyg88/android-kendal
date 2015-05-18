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

    private String title;
    private String barcode;

    public void setArguments(String title, String identifier) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(BARCODE, identifier);
        setArguments(args);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(TITLE);
            barcode = args.getString(BARCODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("BarcodeFragment:onCreateView(): Displaying the Barcode screen.");
        View view = inflater.inflate(R.layout.barcode_fragment, container, false);
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
