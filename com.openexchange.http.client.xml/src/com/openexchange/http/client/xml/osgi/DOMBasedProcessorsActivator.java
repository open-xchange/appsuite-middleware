package com.openexchange.http.client.xml.osgi;

import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.xml.DOMProcessor;
import com.openexchange.http.client.xml.JOOXProcessor;
import com.openexchange.server.osgiservice.HousekeepingActivator;

public class DOMBasedProcessorsActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return null;
	}

	@Override
	protected void startBundle() throws Exception {
		registerService(HTTPResponseProcessor.class, new DOMProcessor());
		registerService(HTTPResponseProcessor.class, new JOOXProcessor());
	}

}
