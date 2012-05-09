package com.openexchange.http.client.osgi;

import org.osgi.framework.ServiceReference;

import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.http.client.apache.ApacheHTTPClient;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.server.osgiservice.HousekeepingActivator;
import com.openexchange.server.osgiservice.SimpleRegistryListener;

public class HTTPClientActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[]{ManagedFileManagement.class};
	}

	@Override
	protected void startBundle() throws Exception {
		final ApacheHTTPClient client = new ApacheHTTPClient(getService(ManagedFileManagement.class));
		
		track(HTTPResponseProcessor.class, new SimpleRegistryListener<HTTPResponseProcessor<?, ?>>() {

			public void added(ServiceReference ref,
					HTTPResponseProcessor<?, ?> thing) {
				client.registerProcessor(thing);
			}

			public void removed(ServiceReference ref,
					HTTPResponseProcessor<?, ?> thing) {
				client.forgetProcessor(thing);
			}
		});
		
		openTrackers();
		
		registerService(HTTPClient.class, client);
	}


}
