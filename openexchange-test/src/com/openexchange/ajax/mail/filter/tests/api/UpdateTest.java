
package com.openexchange.ajax.mail.filter.tests.api;

import java.util.List;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.AbstractAction;
import com.openexchange.ajax.mail.filter.api.dao.action.Stop;
import com.openexchange.ajax.mail.filter.api.dao.comparison.IsComparison;
import com.openexchange.ajax.mail.filter.api.dao.test.HeaderTest;
import com.openexchange.ajax.mail.filter.tests.AbstractMailFilterTest;

public class UpdateTest extends AbstractMailFilterTest {

    public static final int[] cols = { Rule.ID };

    public UpdateTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testDummy() {

    }

    public void testUpdate() throws Exception {
        final Rule rule = new Rule();
        rule.setName("testUpdate");
        rule.setActioncmds(new AbstractAction[] { new Stop() });

        final IsComparison isComp = new IsComparison();
        rule.setTest(new HeaderTest(isComp, new String[] { "testheader" }, new String[] { "testvalue" }));

        final int id = mailFilterAPI.createRule(rule);
        rule.setId(id);
        rule.setName("testUpdate - 2");

        mailFilterAPI.updateRule(rule);

        List<Rule> rules = mailFilterAPI.listRules();
        assertEquals("One rule was expected", 1, rules.size());

        final Rule loadRule = rules.get(0);
        assertRule(rule, loadRule);

        mailFilterAPI.deleteRule(id);
    }

    public void _notestMove() throws Exception {
        final Rule rule = new Rule();
        rule.setName("testMove");
        rule.setActioncmds(new AbstractAction[] { new Stop() });

        final IsComparison isComp = new IsComparison();
        rule.setTest(new HeaderTest(isComp, new String[] { "testheader" }, new String[] { "testvalue" }));

        final int id1 = mailFilterAPI.createRule(rule);
        final int id2 = mailFilterAPI.createRule(rule);

        List<Rule> rules = mailFilterAPI.listRules();
        assertEquals("Two rules were expected", 2, rules.size());

        rule.setId(id2);
        rule.setName("testMove - 2");
        rule.setPosition(0);
        mailFilterAPI.updateRule(rule);

        rules = mailFilterAPI.listRules();
        assertEquals("Two rules were expected", 2, rules.size());

        Rule loadRule = rules.get(1);
        assertRule(rule, loadRule);

        mailFilterAPI.deleteRule(id1);
        mailFilterAPI.deleteRule(id2);
    }
}
