package com.staples.mobile.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.staples.mobile.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, qualifiers = "port")
public class PortraitLayoutTest {
    private LayoutInflater inflater;

    @Before
    public void setup() {
        inflater = (LayoutInflater) Robolectric.application.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Test
    public void testMain() {
        View view = inflater.inflate(R.layout.main, null);
        Assert.assertNotNull(view);
    }

    @Test
    public void testCategory() {
        View view = inflater.inflate(R.layout.category, null);
        Assert.assertNotNull(view);
    }
}