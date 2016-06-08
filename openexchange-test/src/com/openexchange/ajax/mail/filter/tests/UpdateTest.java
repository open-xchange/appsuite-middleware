package com.openexchange.ajax.mail.filter.tests;

import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.AbstractAction;
import com.openexchange.ajax.mail.filter.api.dao.action.Stop;
import com.openexchange.ajax.mail.filter.api.dao.comparison.IsComparison;
import com.openexchange.ajax.mail.filter.api.dao.test.HeaderTest;

public class UpdateTest extends AbstractMailFilterTest {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UpdateTest.class);

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
		final AJAXSession ajaxSession = getSession();
		getClient();

		String forUser = null;

		deleteAllExistingRules(forUser, ajaxSession);

		final Rule rule = new Rule();
		rule.setName("testUpdate");
		rule.setActioncmds(new AbstractAction[] { new Stop() });

		final IsComparison isComp = new IsComparison();
		rule.setTest(new HeaderTest(isComp, new String[] { "testheader" }, new String[] { "testvalue"} ));

		final String id = insertRule(rule, forUser, ajaxSession);
		rule.setId(id);
		rule.setName("testUpdate - 2");

		updateRule(rule, forUser, ajaxSession);

		final String[] idArray = getIdArray(forUser, ajaxSession);

		assertEquals("one rules expected", 1, idArray.length);

		final Rule loadRule = loadRules(forUser, id, ajaxSession);
		compareRule(rule, loadRule);

		deleteRule(id, forUser, ajaxSession);
	}

	public void _notestMove() throws Exception {
		final AJAXSession ajaxSession = getSession();
		getClient();

		String forUser = null;

		deleteAllExistingRules(forUser, ajaxSession);

		final Rule rule = new Rule();
		rule.setName("testMove");
		rule.setActioncmds(new AbstractAction[] { new Stop() });

		final IsComparison isComp = new IsComparison();
		rule.setTest(new HeaderTest(isComp, new String[] { "testheader" }, new String[] { "testvalue"} ));

		final String id1 = insertRule(rule, forUser, ajaxSession);
		final String id2 = insertRule(rule, forUser, ajaxSession);

		final String[] idArray = getIdArray(forUser, ajaxSession);

		assertEquals("one rules expected", 2, idArray.length);

		rule.setId(id2);
		rule.setName("testMove - 2");
		rule.setPosition(0);
		updateRule(rule, forUser, ajaxSession);

		final Rule loadRule = loadRules(forUser, id2, ajaxSession);
		compareRule(rule, loadRule);

		deleteRule(id1, forUser, ajaxSession);
		deleteRule(id2, forUser, ajaxSession);
	}
}
