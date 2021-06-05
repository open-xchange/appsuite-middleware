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

package com.openexchange.dovecot.doveadm.client.osgi;

import java.util.List;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dovecot.doveadm.client.DoveAdmClient;
import com.openexchange.dovecot.doveadm.client.internal.HttpDoveAdmClient;
import com.openexchange.dovecot.doveadm.client.internal.HttpDoveAdmEndpointAvailableStrategy;
import com.openexchange.dovecot.doveadm.client.internal.HttpDoveAdmEndpointManager;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rest.client.endpointpool.EndpointManagerFactory;
import com.openexchange.rest.client.httpclient.SpecificHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.version.VersionService;

/**
 * {@link DoveAdmClientActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class DoveAdmClientActivator extends HousekeepingActivator {

    private HttpDoveAdmClient client;
    private List<SpecificHttpClientConfigProvider> configProviders;

    /**
     * Initializes a new {@link DoveAdmClientActivator}.
     */
    public DoveAdmClientActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, EndpointManagerFactory.class, ConfigViewFactory.class, HttpClientService.class, VersionService.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        Logger logger = org.slf4j.LoggerFactory.getLogger(DoveAdmClientActivator.class);
        Services.setServiceLookup(this);

        // Check if enabled
        ConfigurationService configurationService = getService(ConfigurationService.class);
        boolean enabled = configurationService.getBoolProperty("com.openexchange.dovecot.doveadm.enabled", false);
        if (false == enabled) {
            logger.info("Connector for Dovecot DoveAdm REST interface is disabled as per property \"com.openexchange.dovecot.doveadm.enabled\". DoveAdm client will not be initialized.");
            return;
        }

        // Check API secret
        String apiSecret = configurationService.getProperty("com.openexchange.dovecot.doveadm.apiSecret");
        if (Strings.isEmpty(apiSecret)) {
            logger.error("Missing API secret from property \"com.openexchange.dovecot.doveadm.apiSecret\". DoveAdm client will not be initialized.");
            return;
        }

        // Initialize the end-point manager
        EndpointManagerFactory factory = getService(EndpointManagerFactory.class);
        HttpDoveAdmEndpointManager endpointManager = new HttpDoveAdmEndpointManager();
        HttpDoveAdmEndpointAvailableStrategy availableStrategy = new HttpDoveAdmEndpointAvailableStrategy(apiSecret);
        List<SpecificHttpClientConfigProvider> configProviders = endpointManager.init(factory, availableStrategy, this);
        if (configProviders.isEmpty()) {
            logger.error("Missing end-points for Dovecot DoveAdm REST interface. DoveAdm client will not be initialized.");
            return;
        }

        // Initialize HTTP client config for the different doveadm calls
        for (SpecificHttpClientConfigProvider configProvider : configProviders) {
            registerService(SpecificHttpClientConfigProvider.class, configProvider);
        }
        this.configProviders = configProviders;

        // Initialize client to Dovecot REST interface
        HttpDoveAdmClient client = new HttpDoveAdmClient(apiSecret, endpointManager, this);
        this.client = client;
        registerService(DoveAdmClient.class, client);

        logger.info("Bundle successfully started: {}", context.getBundle().getSymbolicName());
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        Logger logger = org.slf4j.LoggerFactory.getLogger(DoveAdmClientActivator.class);

        // Clean-up
        super.stopBundle();

        // Shut-down client
        HttpDoveAdmClient client = this.client;
        if (null != client) {
            this.client = null;
            client.shutDown();
        }

        List<SpecificHttpClientConfigProvider> configProviders = this.configProviders;
        if (configProviders != null) {
            this.configProviders = null;
            HttpClientService httpClientService = getService(HttpClientService.class);
            if (httpClientService != null) {
                for (SpecificHttpClientConfigProvider configProvider : configProviders) {
                    httpClientService.destroyHttpClient(configProvider.getClientId());
                }
            }
        }

        logger.info("Bundle successfully stopped: {}", context.getBundle().getSymbolicName());
    }

}
