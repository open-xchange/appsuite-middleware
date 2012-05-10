package com.openexchange.http.client.osgi;

import org.osgi.framework.ServiceReference;

import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.http.client.apache.ApacheHTTPClient;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;


public class HTTPClientActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[]{ManagedFileManagement.class};
	}

	@Override
	protected void startBundle() throws Exception {
		final ApacheHTTPClient client = new ApacheHTTPClient(getService(ManagedFileManagement.class));
		@SuppressWarnings("rawtypes")
		SimpleRegistryListener<HTTPResponseProcessor> listener = new SimpleRegistryListener<HTTPResponseProcessor>() {

			public void added(ServiceReference<HTTPResponseProcessor> ref,
					HTTPResponseProcessor service) {
				client.registerProcessor(service);
			}

			public void removed(ServiceReference<HTTPResponseProcessor> ref,
					HTTPResponseProcessor service) {
				client.forgetProcessor(service);
			}


		};
		track(HTTPResponseProcessor.class, listener );
		
		openTrackers();
		
		registerService(HTTPClient.class, client);
	}
}
