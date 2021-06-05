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

package com.openexchange.rest.client.osgi;

import static com.openexchange.osgi.Tools.generateServiceFilter;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rest.client.endpointpool.EndpointManagerFactory;
import com.openexchange.rest.client.endpointpool.internal.EndpointManagerFactoryImpl;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.SpecificHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.WildcardHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.internal.HttpClientServiceImpl;
import com.openexchange.timer.TimerService;
import com.openexchange.version.VersionService;

/**
 * {@link RestClientActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class RestClientActivator extends HousekeepingActivator {

    private HttpClientServiceImpl httpClientService;

    /**
     * Initializes a new {@link RestClientActivator}.
     */
    public RestClientActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { TimerService.class, SSLSocketFactoryProvider.class, SSLConfigurationService.class, ConfigurationService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class[] { VersionService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        RestClientServices.setServices(this);

        HttpClientServiceImpl httpClientService = new HttpClientServiceImpl(context, this);
        this.httpClientService = httpClientService;
        track(generateServiceFilter(context, SpecificHttpClientConfigProvider.class, WildcardHttpClientConfigProvider.class), httpClientService);
        trackService(LeanConfigurationService.class);
        openTrackers();

        registerService(EndpointManagerFactory.class, new EndpointManagerFactoryImpl(httpClientService, this));
        registerService(HttpClientService.class, httpClientService);
        registerService(ForcedReloadable.class, httpClientService);
        // Avoid annoying WARN logging
        //System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.client.protocol.ResponseProcessCookies", "fatal");
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        try {
            // Clear service registry
            RestClientServices.setServices(null);
            // Clean-up registered services and trackers
            cleanUp();
            // Shut-down service
            HttpClientServiceImpl httpClientService = this.httpClientService;
            if (null != httpClientService) {
                this.httpClientService = null;
                httpClientService.shutdown();
            }
            // Call to super...
            super.stopBundle();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(RestClientActivator.class).error("", e);
            throw e;
        }
    }

}
