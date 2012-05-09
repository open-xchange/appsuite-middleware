package com.openexchange.http.client.xml.osgi;

import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.xml.JDOMProcessor;
import com.openexchange.server.osgiservice.HousekeepingActivator;
import com.openexchange.xml.jdom.JDOMParser;

public class JDomBasedProcessorActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[]{JDOMParser.class};
	}

	@Override
	protected void startBundle() throws Exception {
		registerService(HTTPResponseProcessor.class, new JDOMProcessor(getService(JDOMParser.class)));
	}

}
