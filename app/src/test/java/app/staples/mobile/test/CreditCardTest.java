package com.staples.mobile.test;

import app.staples.mobile.cfa.profile.CreditCard;

import org.junit.Assert;
import org.junit.Test;

public class CreditCardTest {
    private CreditCard[] examples = {
        new CreditCard(CreditCard.Type.AMERICAN_EXPRESS, "373953146391007"),
        new CreditCard(CreditCard.Type.MASTERCARD,       "5204730000001003"),
        new CreditCard(CreditCard.Type.MASTERCARD,       "5204730000002449"),
        new CreditCard(CreditCard.Type.MASTERCARD,       "5204730000002811"),
        new CreditCard(CreditCard.Type.MASTERCARD,       "5463050000001010"),
        new CreditCard(CreditCard.Type.VISA,             "4895390000000005"),
        new CreditCard(CreditCard.Type.VISA,             "4487970000000008"),
        new CreditCard(CreditCard.Type.VISA,             "4895400000000002"),
        new CreditCard(CreditCard.Type.VISA,             "4895240000000002"),
        new CreditCard(CreditCard.Type.VISA,             "4859180000000004"),
        new CreditCard(CreditCard.Type.VISA,             "4895260000000000"),
        new CreditCard(CreditCard.Type.STAPLES,          "7972000000000009")
    };

    @Test
    public void testDetect() {
        for(CreditCard example : examples) {
            CreditCard.Type type = CreditCard.Type.detect(example.getNumber());
            if (type!=example.getType())
                Assert.fail("\"" + example.getNumber() + "\" should have parsed to " + example.getType() + " but it parsed to " + type);
        }
    }

    @Test
    public void testGoodChecksum() {
        for(CreditCard example : examples) {
            boolean valid = example.isChecksumValid();
            Assert.assertTrue("\""+ example.getNumber() + "\" should have validated ok", valid);
        }
    }

    @Test
    public void testBadChecksum() {
        for(CreditCard example : examples) {
            int n = example.getNumber().length();
            for(int i=0;i<n;i++) {
                for(int j=1;j<10;j++) {
                    byte[] seq = example.getNumber().getBytes();
                    seq[i] = (byte) ((seq[i]-'0'+j)%10+'0');
                    CreditCard cc = new CreditCard(CreditCard.Type.UNKNOWN, new String(seq));
                    boolean valid = cc.isChecksumValid();
                    Assert.assertFalse("\"" + cc.getNumber() + "\" should not have validated ok", valid);
                }
            }
        }
    }
}
