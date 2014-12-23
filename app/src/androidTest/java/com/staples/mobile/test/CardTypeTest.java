package com.staples.mobile.test;

import com.staples.mobile.cfa.profile.CardType;

import org.junit.Assert;
import org.junit.Test;

public class CardTypeTest {
    private class Example {
        CardType type;
        String string;

        private Example(CardType type, String string) {
            this.type = type;
            this.string = string;
        }
    }

    private Example[] examples = {
        new Example(CardType.AMERICAN_EXPRESS, "373953146391007"),
        new Example(CardType.MASTERCARD,       "5204730000001003"),
        new Example(CardType.MASTERCARD,       "5204730000002449"),
        new Example(CardType.MASTERCARD,       "5204730000002811"),
        new Example(CardType.MASTERCARD,       "5463050000001010"),
        new Example(CardType.VISA,             "4895390000000005"),
        new Example(CardType.VISA,             "4487970000000008"),
        new Example(CardType.VISA,             "4895400000000002"),
        new Example(CardType.VISA,             "4895240000000002"),
        new Example(CardType.VISA,             "4859180000000004"),
        new Example(CardType.VISA,             "4895260000000000"),
        new Example(CardType.STAPLES,          "7972000000000009")
    };

    @Test
    public void testDetect() {
        for(Example example : examples ) {
            CardType type = CardType.detect(example.string);
            if (type!=example.type)
                Assert.fail("\"" + example.string + "\" should have parsed to " + example.type + " but it parsed to " + type);
        }
    }
}
