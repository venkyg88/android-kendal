package app.staples.mobile.cfa.login;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.crittercism.app.Crittercism;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.R;
import com.staples.mobile.common.analytics.Tracker;
import app.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.EmptyResponse;
import com.staples.mobile.common.access.easyopen.model.login.PasswordRecovery;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ResetPasswordFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = ResetPasswordFragment.class.getSimpleName();
    private MainActivity activity;
    EditText recoveryEmail;
    Button resetPasswordBtn;
    EasyOpenApi easyOpenApi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("ResetPasswordFragment:onCreateView(): Displaying the Reset Password screen.");
        activity = (MainActivity)getActivity();
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.reset_password_fragment, container, false);
        recoveryEmail = (EditText)view.findViewById(R.id.emailTV);
        resetPasswordBtn = (Button)view.findViewById(R.id.resetPasswordBtn);
        resetPasswordBtn.setOnClickListener(this);
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);

        return(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.PASSWORD);
        Tracker.getInstance().trackStateForResetPassword(); // Analytics
    }

    @Override
    public void onClick(View view) {
        String emailEntered = recoveryEmail.getText().toString();
        if(!emailEntered.isEmpty() && isEmailValid(emailEntered)) {
            PasswordRecovery recovery = new PasswordRecovery(emailEntered);
            easyOpenApi.recoverPassword(recovery, new Callback<EmptyResponse>() {
                @Override
                public void success(EmptyResponse emptyResponse, Response response) {
                    activity.showNotificationBanner("Please check email for details");
                    activity.selectLoginFragment();
                }

                @Override
                public void failure(RetrofitError error) {
                    activity.showErrorDialog(ApiError.getErrorMessage(error));
                }
            });
        }
        else {
            activity.showErrorDialog("Enter valid email and try again");
        }
    }

    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
}
