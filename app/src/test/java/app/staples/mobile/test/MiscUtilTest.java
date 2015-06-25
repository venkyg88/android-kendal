package app.staples.mobile.test;

import org.junit.Assert;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import app.staples.mobile.cfa.util.MiscUtils;

public class MiscUtilTest {

    // Currency formatting

    @Test
    public void testCurrencyFormatting() {
        DecimalFormat format = MiscUtils.getCurrencyFormat();

        Assert.assertEquals("$9.99", format.format(9.9945));
        Assert.assertEquals("$10.00", format.format(9.9955));
        Assert.assertEquals("$1,234,567.89", format.format(1234567.89));
    }

    @Test
    public void testIntegerCurrencyFormatting() {
        DecimalFormat format = MiscUtils.getIntegerCurrencyFormat();

        Assert.assertEquals("$9", format.format(9.45));
        Assert.assertEquals("$10", format.format(9.55));
        Assert.assertEquals("$1,234,568", format.format(1234567.89));
    }

    // Multi strings

    @Test
    public void testListToMultiString() {
        List<String> list;
        String multi;

        multi = MiscUtils.listToMultiString(null);
        Assert.assertNull(multi);

        list = Arrays.asList(new String[] {null, null, null});
        multi = MiscUtils.listToMultiString(list);
        Assert.assertEquals("||", multi);

        list = Arrays.asList(new String[] {"apples", "bananas", "cantaloupes"});
        multi = MiscUtils.listToMultiString(list);
        Assert.assertEquals("apples|bananas|cantaloupes", multi);
    }

    @Test
    public void testMultiStringToList() {
        List<String> list;

        list = MiscUtils.multiStringToList(null);
        Assert.assertNull(list);

        list = MiscUtils.multiStringToList("||");
        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.size());
        Assert.assertNull(list.get(0));
        Assert.assertNull(list.get(1));
        Assert.assertNull(list.get(2));

        list = MiscUtils.multiStringToList("apples|bananas|cantaloupes");
        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("apples", list.get(0));
        Assert.assertEquals("bananas", list.get(1));
        Assert.assertEquals("cantaloupes", list.get(2));
    }

    // JSON boolean parsing

    @Test
    public void testParseBoolean() {
        // Default value
        Assert.assertFalse(MiscUtils.parseBoolean(null, false));
        Assert.assertTrue(MiscUtils.parseBoolean(null, true));
        Assert.assertFalse(MiscUtils.parseBoolean("", false));
        Assert.assertTrue(MiscUtils.parseBoolean("", true));
        Assert.assertFalse(MiscUtils.parseBoolean("Me", false));
        Assert.assertTrue(MiscUtils.parseBoolean("Me", true));

        // False
        Assert.assertFalse(MiscUtils.parseBoolean("n", false));
        Assert.assertFalse(MiscUtils.parseBoolean("n", true));
        Assert.assertFalse(MiscUtils.parseBoolean("FALSE", false));
        Assert.assertFalse(MiscUtils.parseBoolean("FALSE", true));

        // True
        Assert.assertTrue(MiscUtils.parseBoolean("y", false));
        Assert.assertTrue(MiscUtils.parseBoolean("y", true));
        Assert.assertTrue(MiscUtils.parseBoolean("TRUE", false));
        Assert.assertTrue(MiscUtils.parseBoolean("TRUE", true));
    }
}
