package com.staples.mobile.test;

import com.staples.mobile.cfa.IdentifierType;

import org.junit.Assert;
import org.junit.Test;

public class IdentifierTypeTest {

    private class Example {
        IdentifierType type;
        String string;

        private Example(IdentifierType type, String string) {
            this.type = type;
            this.string = string;
        }
    }

    private Example[] examples = {
        new Example(IdentifierType.UNKNOWN,       "-"),
        new Example(IdentifierType.TOPCATEGORY,   "-0"),
        new Example(IdentifierType.TOPCATEGORY,   "-123456"),
        new Example(IdentifierType.UNKNOWN,       "SC"),
        new Example(IdentifierType.SUPERCATEGORY, "SC0"),
        new Example(IdentifierType.SUPERCATEGORY, "SC123456"),
        new Example(IdentifierType.UNKNOWN,       "CG"),
        new Example(IdentifierType.CATEGORY,      "CG0"),
        new Example(IdentifierType.CATEGORY,      "CG123456"),
        new Example(IdentifierType.UNKNOWN,       "DP"),
        new Example(IdentifierType.DEPARTMENT,    "DP0"),
        new Example(IdentifierType.DEPARTMENT,    "DP123456"),
        new Example(IdentifierType.UNKNOWN,       "CL"),
        new Example(IdentifierType.CLASS,         "CL0"),
        new Example(IdentifierType.CLASS,         "CL123456"),
        new Example(IdentifierType.UNKNOWN,       "BI"),
        new Example(IdentifierType.BUNDLE,        "BI0"),
        new Example(IdentifierType.BUNDLE,        "BI123456"),
        new Example(IdentifierType.UNKNOWN,       "SS"),
        new Example(IdentifierType.SKUSET,        "SS0"),
        new Example(IdentifierType.SKUSET,        "SS123456"),
        new Example(IdentifierType.EMPTY,         ""),
        new Example(IdentifierType.SKU,           "0"),
        new Example(IdentifierType.SKU,           "123456"),
        new Example(IdentifierType.EMPTY,         null)
    };

    @Test
    public void testDetect() {
        for(Example example : examples ) {
            IdentifierType type = IdentifierType.detect(example.string);
            if (type!=example.type)
                Assert.fail("\"" + example.string + "\" should have parsed to " + example.type + " but it parsed to " + type);
        }
    }
}
