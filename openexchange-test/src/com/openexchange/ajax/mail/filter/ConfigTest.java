package com.openexchange.ajax.mail.filter;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.mail.filter.actions.ConfigRequest;
import com.openexchange.ajax.mail.filter.actions.ConfigResponse;

public class ConfigTest extends AbstractMailFilterTest {

	protected static final String HOSTNAME = "hostname";
	
	protected String hostname = null;
	
	public ConfigTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testDummy() {

	}

	public void testConfig() throws Exception {
		final AJAXClient client = getClient();
		
		final ConfigRequest configRequest = new ConfigRequest(true);
		final ConfigResponse configResponse = client.execute(configRequest);
		
		final ConfigTestHolder[] configTests = configResponse.getConfigTests();
		final String[] actions = configResponse.getActionCommands();
		
		assertTrue("config tests not > 0", configTests.length > 0);
		assertTrue("actions not > 0", actions.length > 0);
	}
}
