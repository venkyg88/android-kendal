/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.rewards;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.UpdateProfile;
import com.staples.mobile.common.access.easyopen.model.member.UpdateProfileResponse;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RewardsLinkingFragment extends Fragment {
    private static final String TAG = RewardsLinkingFragment.class.getSimpleName();

    public interface LinkRewardsCallback {
        public void onLinkRewardsComplete(String errMsg);
    }

    private MainActivity activity;

    EditText rewardsNumberVw;
    EditText phoneNumberVw;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        activity = (MainActivity)getActivity();

        Log.d(TAG, "onCreateView()");
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.rewards_linking_fragment, container, false);
        rewardsNumberVw = ((EditText)view.findViewById(R.id.rewards_card_number));
        phoneNumberVw = ((EditText)view.findViewById(R.id.rewards_phone_number));

        Button rewardsLinkAcctButton = (Button)view.findViewById(R.id.rewards_link_acct_button);

        rewardsLinkAcctButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(v);
                String rewardsNumber = rewardsNumberVw.getText().toString();
                String phoneNumber = phoneNumberVw.getText().toString();
                showProgressIndicator();
                linkRewardsAccount(rewardsNumber, phoneNumber, new LinkRewardsCallback() {
                    @Override
                    public void onLinkRewardsComplete(String errMsg) {
                        hideProgressIndicator();
                        if (errMsg != null) {
                            makeToast(errMsg);
                        } else {
                            activity.selectRewardsFragment();
                        }
                    }
                });
            }
        });

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
                        @Override public void onProfileRefresh(Member member) {
                            if (linkRewardsCallback != null) {
                                if (ProfileDetails.isRewardsMember()) {
                                    linkRewardsCallback.onLinkRewardsComplete(null);
                                } else {
                                    linkRewardsCallback.onLinkRewardsComplete("Unknown error");
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


    private void showProgressIndicator() {
        if (activity != null) {
            activity.showProgressIndicator();
        }
    }

    private void hideProgressIndicator() {
        if (activity != null) {
            activity.hideProgressIndicator();
        }
    }

    private void makeToast(String msg) {
        if (activity != null) {
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
        }
    }
    private void makeToast(int msgId) {
        if (activity != null) {
            Toast.makeText(activity, msgId, Toast.LENGTH_LONG).show();
        }
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager keyboard = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}
