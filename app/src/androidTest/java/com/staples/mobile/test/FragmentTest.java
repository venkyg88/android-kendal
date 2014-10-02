package com.staples.mobile.test;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

import com.staples.mobile.cfa.ToBeDoneFragment;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.FragmentTestUtil;

/**
 * Created by pyhre001 on 9/15/14.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, qualifiers = "port")
public class FragmentTest {
    @Test
    public void testToBeDoneFragment() {
        System.out.println("testToBeDoneFragment");
        Fragment fragment = new ToBeDoneFragment();
        Bundle args = new Bundle();
        args.putString("title", "FragmentTest");
        fragment.setArguments(args);

        FragmentTestUtil.startFragment(fragment);
        View view = fragment.getView();
        Assert.assertNotNull("ToBeDoneFragment should have a View", view);
    }
}
