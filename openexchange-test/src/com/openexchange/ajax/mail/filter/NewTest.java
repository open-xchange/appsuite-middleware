package com.openexchange.ajax.mail.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.mail.filter.action.AbstractAction;
import com.openexchange.ajax.mail.filter.action.Stop;
import com.openexchange.ajax.mail.filter.comparison.IsComparison;
import com.openexchange.ajax.mail.filter.test.HeaderTest;

public class NewTest extends AbstractMailFilterTest {

	public static final int[] cols = { Rule.ID };

	public NewTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testDummy() {

	}

	public void testNew() throws Exception {
		final AJAXSession ajaxSession = getSession();
		
		String forUser = null;
		
		deleteAllExistingRules(forUser, ajaxSession);
		
		final Rule rule = new Rule();
		rule.setName("testNew");
		rule.setActioncmds(new AbstractAction[] { new Stop() });
		
		final IsComparison isComp = new IsComparison();
		rule.setTest(new HeaderTest(isComp, new String[] { "testheader" }, new String[] { "testvalue"} ));

		final String id = insertRule(rule, forUser, ajaxSession);

		final String[] idArray = getIdArray(forUser, ajaxSession);		
		
		assertEquals("one rules expected", 1, idArray.length);
		
		final Rule loadRule = loadRules(forUser, id, ajaxSession);
		compareRule(rule, loadRule);
		
		deleteRule(id, forUser, ajaxSession);
	}
	
	public void testNewWithTwoEntries() throws Exception {
		final AJAXSession ajaxSession = getSession();
		final AJAXClient ajaxClient = getClient();
		
		String forUser = null;
		
		deleteAllExistingRules(forUser, ajaxSession);
		
		final Rule rule1 = new Rule();
		rule1.setName("testNewWithTwoEntries1");
		rule1.setActioncmds(new AbstractAction[] { new Stop() });
		
		IsComparison isComp = new IsComparison();
		rule1.setTest(new HeaderTest(isComp, new String[] { "test" }, new String[] { "test"} ));
		
		final String id1 = insertRule(rule1, forUser, ajaxSession);

		final Rule rule2 = new Rule();
		rule2.setName("testNewWithTwoEntries2");
		rule2.setActioncmds(new AbstractAction[] { new Stop() });
		
		isComp = new IsComparison();
		rule2.setTest(new HeaderTest(isComp, new String[] { "test" }, new String[] { "test"} ));
		
		final String id2 = insertRule(rule2, forUser, ajaxSession);

		final String[] idArray = getIdArray(forUser, ajaxSession);		
		
		assertEquals("two rules expected", 2, idArray.length);
		
		Rule loadRule = loadRules(forUser, id1, ajaxSession);
		rule1.setPosition(0);
		compareRule(rule1, loadRule);
		
		loadRule = loadRules(forUser, id2, ajaxSession);
		rule2.setPosition(1);
		compareRule(rule2, loadRule);
		
		deleteRule(id1, forUser, ajaxSession);
		deleteRule(id2, forUser, ajaxSession);
	}
}
