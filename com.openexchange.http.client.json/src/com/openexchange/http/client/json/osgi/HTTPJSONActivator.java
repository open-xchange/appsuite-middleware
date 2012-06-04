package com.openexchange.http.client.json.osgi;

import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.json.JSONArrayResponseProcessor;
import com.openexchange.http.client.json.JSONObjectResponseProcessor;
import com.openexchange.osgi.HousekeepingActivator;

public class HTTPJSONActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return null;
	}

	@Override
	protected void startBundle() throws Exception {
		registerService(HTTPResponseProcessor.class, new JSONObjectResponseProcessor());
		registerService(HTTPResponseProcessor.class, new JSONArrayResponseProcessor());
	}

}
