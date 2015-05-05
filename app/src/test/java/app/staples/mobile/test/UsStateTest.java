package app.staples.mobile.test;

import org.junit.Assert;
import org.junit.Test;

import app.staples.mobile.cfa.profile.UsState;

public class UsStateTest {
    @Test
    public void testFindByAbbr() {
        // Test for things it should find
        for(UsState goal : UsState.values()) {
            UsState found = UsState.findByAbbr(goal.abbr);
            if (found!=goal) {
                Assert.fail(goal.abbr+" was parsed findByAbbr incorrectly to "+found);
            }
        }

        // Test for things it should not find
        for(UsState goal : UsState.values()) {
            UsState found = UsState.findByAbbr(goal.name);
            if (found!=null) {
                Assert.fail(goal.name+" was parsed by findByAbbr incorrectly to "+found);
            }
        }
    }

    @Test
    public void testFindByName() {
        // Test for things it should find
        for(UsState goal : UsState.values()) {
            UsState found = UsState.findByName(goal.name);
            if (found!=goal) {
                Assert.fail(goal.name+" was parsed by findByName incorrectly to "+found);
            }
        }

        // Test for things it should not find
        for(UsState goal : UsState.values()) {
            UsState found = UsState.findByName(goal.abbr);
            if (found!=null) {
                Assert.fail(goal.name+" was parsed by findByName incorrectly to "+found);
            }
        }
    }
}
