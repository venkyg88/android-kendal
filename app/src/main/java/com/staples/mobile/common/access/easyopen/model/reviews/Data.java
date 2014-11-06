package com.staples.mobile.common.access.easyopen.model.reviews;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

public class Data {
    private String bottomline;
    private String comments;
    private String created_date;
    private String created_datetime;
    private String headline;
    private int helpful_score;
    private boolean is_syndicated;
    private String locale;
    private int locale_id;
    private String location;
    private int merchant_group_id;
    private int merchant_id;
    private String merchant_user_email_id;
    private String merchant_user_id;
    private Object msqcs_and_tags; // TODO complicated structure
    private String name;
    private String page_id;
    private int profile_id;
    private int provider_id;
    private int rating;
    private List<HashMap<String, List<String>>> review_tags;
    private String reviewer_type;
    private String shared_review_id;
    private String variant;

    public String getBottomline() {
        return bottomline;
    }

    public String getComments() {
        return comments;
    }

    public String getCreated_date() {
        return created_date;
    }

    public String getCreated_datetime() {
        return created_datetime;
    }

    public String getHeadline() {
        return headline;
    }

    public int getHelpful_score() {
        return helpful_score;
    }

    public boolean isIs_syndicated() {
        return is_syndicated;
    }

    public String getLocale() {
        return locale;
    }

    public int getLocale_id() {
        return locale_id;
    }

    public String getLocation() {
        return location;
    }

    public int getMerchant_group_id() {
        return merchant_group_id;
    }

    public int getMerchant_id() {
        return merchant_id;
    }

    public String getMerchant_user_email_id() {
        return merchant_user_email_id;
    }

    public String getMerchant_user_id() {
        return merchant_user_id;
    }

    public Object getMsqcs_and_tags() {
        return msqcs_and_tags;
    }

    public String getName() {
        return name;
    }

    public String getPage_id() {
        return page_id;
    }

    public int getProfile_id() {
        return profile_id;
    }

    public int getProvider_id() {
        return provider_id;
    }

    public int getRating() {
        return rating;
    }

    public List<HashMap<String, List<String>>> getReview_tags() {
        return review_tags;
    }

    public String getReviewer_type() {
        return reviewer_type;
    }

    public String getShared_review_id() {
        return shared_review_id;
    }

    public String getVariant() {
        return variant;
    }
}
