/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */


package com.openexchange.http.client.osgi;

import org.osgi.framework.ServiceReference;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.http.client.apache.ApacheHTTPClient;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;


public class HTTPClientActivator extends HousekeepingActivator {

	@Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ManagedFileManagement.class, SSLSocketFactoryProvider.class };
	}

	@Override
	protected void startBundle() throws Exception {
	    Services.setServiceLookup(this);
		final ApacheHTTPClient client = new ApacheHTTPClient(getService(ManagedFileManagement.class));
		SimpleRegistryListener<HTTPResponseProcessor> listener = new SimpleRegistryListener<HTTPResponseProcessor>() {

			@Override
            public void added(ServiceReference<HTTPResponseProcessor> ref,
					HTTPResponseProcessor service) {
				client.registerProcessor(service);
			}

			@Override
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
