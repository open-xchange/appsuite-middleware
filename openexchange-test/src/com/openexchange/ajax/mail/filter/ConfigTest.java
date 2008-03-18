package com.openexchange.ajax.mail.filter;

import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.filter.actions.ConfigRequest;
import com.openexchange.ajax.mail.filter.actions.ConfigResponse;
import com.openexchange.mail.filter.ConfigTestHolder;

public class ConfigTest extends AbstractMailFilterTest {

	public ConfigTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testDummy() {

	}

	public void testConfig() throws Exception {
		final AJAXSession ajaxSession = getSession();
		
		final ConfigRequest configRequest = new ConfigRequest(true);
		final ConfigResponse configResponse = (ConfigResponse) Executor.execute(ajaxSession,
				configRequest);
		
		final ConfigTestHolder[] configTests = configResponse.getConfigTests();
		final String[] actions = configResponse.getActionCommands();
		
		assertTrue("config tests not > 0", configTests.length > 0);
		assertTrue("actions not > 0", actions.length > 0);
	}
}
