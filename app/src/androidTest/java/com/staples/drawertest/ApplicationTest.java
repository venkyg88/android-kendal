package com.staples.drawertest;

import android.app.Application;
import android.test.ApplicationTestCase;

public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

   // TopCategoryResponse tests

    public void testInvalidURL() {
        TopCategoryFiller.TopCategoryResponse response = (TopCategoryFiller.TopCategoryResponse) JSONResponse.getResponse("cow", TopCategoryFiller.TopCategoryResponse.class);
        assertEquals(992, response.httpStatusCode);
    }

    public void testURLDoesNotExist() {
        TopCategoryFiller.TopCategoryResponse response = (TopCategoryFiller.TopCategoryResponse) JSONResponse.getResponse("http://abcde/", TopCategoryFiller.TopCategoryResponse.class);
        assertEquals(995, response.httpStatusCode);
    }
}
