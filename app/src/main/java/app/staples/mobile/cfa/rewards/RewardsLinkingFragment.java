package app.staples.mobile.cfa.rewards;

import android.app.Fragment;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.crittercism.app.Crittercism;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.UpdateProfile;
import com.staples.mobile.common.access.easyopen.model.member.UpdateProfileResponse;

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.profile.ProfileDetails;
import app.staples.mobile.cfa.widget.ActionBar;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RewardsLinkingFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = RewardsLinkingFragment.class.getSimpleName();

    public interface LinkRewardsCallback {
        public void onLinkRewardsComplete(String errMsg);
    }

    private MainActivity activity;
    PhoneNumberFormattingTextWatcher phoneNumberFormattingTextWatcher;

    EditText rewardsNumberVw;
    EditText phoneNumberVw;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("RewardsLinkingFragment:onCreateView(): Displaying the Rewards Linking screen.");
        activity = (MainActivity)getActivity();
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.rewards_linking_fragment, container, false);
        rewardsNumberVw = ((EditText)view.findViewById(R.id.rewards_card_number));
        phoneNumberVw = ((EditText)view.findViewById(R.id.rewards_phone_number));
        phoneNumberFormattingTextWatcher = new PhoneNumberFormattingTextWatcher();
        phoneNumberVw.addTextChangedListener(phoneNumberFormattingTextWatcher);

        View rewardsLinkAcctButton = view.findViewById(R.id.rewards_link_acct_button);
        rewardsLinkAcctButton.setOnClickListener(this);
        return(view);
     }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.LINK);
    }

    public static void linkRewardsAccount(String rewardsNumber, String phoneNumber, final LinkRewardsCallback linkRewardsCallback) {
        if (!TextUtils.isEmpty(rewardsNumber) && !TextUtils.isEmpty(phoneNumber)) {
            EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(true);
            UpdateProfile updateProfile = new UpdateProfile();
            updateProfile.setFieldName("rewardsNumber");
            updateProfile.setRewardsMemberOption("alreadyRewardsMember");
            updateProfile.setRewardsNumber(rewardsNumber);
            updateProfile.setRewardsPhoneNumber(phoneNumber);
            easyOpenApi.updateProfile(updateProfile, new Callback<UpdateProfileResponse>() {
                @Override
                public void success(UpdateProfileResponse updateProfileResponse, Response response) {
                    new ProfileDetails().refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
                        @Override
                        public void onProfileRefresh(Member member, String errMsg) {
                            if (linkRewardsCallback != null) {
                                if (ProfileDetails.isRewardsMember()) {
                                    linkRewardsCallback.onLinkRewardsComplete(null);
                                } else {
                                    linkRewardsCallback.onLinkRewardsComplete((errMsg != null) ? errMsg : "Unknown Error");
                                }
                            }
                        }
                    });
                }

                @Override
                public void failure(RetrofitError error) {
                    if (linkRewardsCallback != null) {
                        linkRewardsCallback.onLinkRewardsComplete(ApiError.getErrorMessage(error));
                    }
                }
            });
        }
    }

    private boolean validateFields(String rewardsNumber, String phoneNumber) {
        if(TextUtils.isEmpty(rewardsNumber) || TextUtils.isEmpty(phoneNumber)) {
            return false;
        }
        return true;
    }

    private String stripPhoneNumber(String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            return phoneNumber.replaceAll("[^0-9]", "");
        }
        return phoneNumber;
    }

    @Override
    public void onClick(View view) {
        if(activity == null) return;
        switch(view.getId()) {
            case R.id.rewards_link_acct_button:
                activity.hideSoftKeyboard();
                String rewardsNumber = rewardsNumberVw.getText().toString();
                String phoneNumber = stripPhoneNumber(phoneNumberVw.getText().toString());
                if(validateFields(rewardsNumber, phoneNumber)) {
                    if(phoneNumber.length()<10) {
                        activity.showErrorDialog(R.string.invalid_phone_number);
                        return;
                    }
                    activity.showProgressIndicator();
                    linkRewardsAccount(rewardsNumber, phoneNumber, new LinkRewardsCallback() {
                        @Override
                        public void onLinkRewardsComplete(String errMsg) {
                            activity.hideProgressIndicator();
                            if (errMsg != null) {
                                activity.showErrorDialog(errMsg, false);
                            } else {
                                activity.selectRewardsFragment();
                            }
                        }
                    });
                } else{
                    activity.showErrorDialog(R.string.empty_rewards_linking_msg);
                }
        }
    }
}
