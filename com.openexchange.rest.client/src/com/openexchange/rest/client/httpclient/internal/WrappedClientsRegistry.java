/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.rest.client.httpclient.internal;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.rest.client.httpclient.HttpClients.ClientConfig;

/**
 * {@link WrappedClientsRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WrappedClientsRegistry {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(WrappedClientsRegistry.class);

    private static final WrappedClientsRegistry INSTANCE = new WrappedClientsRegistry();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static WrappedClientsRegistry getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------------

    private final List<ClientAndConfig> wrappers; // guarded by synchronized
    private SSLSocketFactoryProvider factoryProvider; // guarded by synchronized
    private SSLConfigurationService sslConfig; // guarded by synchronized

    /**
     * Initializes a new {@link WrappedClientsRegistry}.
     */
    private WrappedClientsRegistry() {
        super();
        wrappers = new LinkedList<>();
    }

    /**
     * Creates an appropriate <code>DefaultHttpClient</code> instance
     *
     * @param httpClient The wrapping instance
     * @param config The configuration with which the <code>DefaultHttpClient</code> instance has been initialized
     * @return The resulting <code>DefaultHttpClient</code> instance, which is either wrapped or not
     */
    public DefaultHttpClient createWrapped(ClientConfig config) {
        SSLSocketFactoryProvider factoryProvider;
        SSLConfigurationService sslConfig;
        synchronized (wrappers) {
            factoryProvider = this.factoryProvider;
            sslConfig = this.sslConfig;
            if (null == factoryProvider) {
                DefaultHttpClient fallbackHttpClient = HttpClients.getFallbackHttpClient(config);
                WrappingDefaultHttpClient wrapper = new WrappingDefaultHttpClient(fallbackHttpClient);
                wrappers.add(new ClientAndConfig(wrapper, config));
                return wrapper;
            }
        }

        // Return unmanaged instance
        return HttpClients.getHttpClient(config, factoryProvider, sslConfig);
    }


    /**
     * Sets the SSL services
     *
     * @param factoryProvider The SSL socket factory provider to set
     * @param sslConfig The SSL configuration service to set
     */
    public void setSSLServices(SSLSocketFactoryProvider factoryProvider, SSLConfigurationService sslConfig) {
        synchronized (wrappers) {
            this.factoryProvider = factoryProvider;
            this.sslConfig = sslConfig;
            if (null != factoryProvider) {
                for (Iterator<ClientAndConfig> iter = wrappers.iterator(); iter.hasNext();) {
                    // Create unmanaged instance
                    ClientAndConfig clientEntry = iter.next();
                    DefaultHttpClient newHttpClient = null;
                    try {
                        newHttpClient = HttpClients.getHttpClient(clientEntry.config, factoryProvider, sslConfig);
                        clientEntry.wrapper.replaceHttpClient(newHttpClient);
                        newHttpClient = null; // Avoid preliminary shut-down
                        iter.remove();
                    } catch (Exception e) {
                        LOG.warn("Failed to replace/exchange fall-back HttpClient instance using the following configuration: {}", clientEntry.config, e);
                    } finally {
                        if (null != newHttpClient) {
                            HttpClients.shutDown(newHttpClient);
                        }
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------

    private static final class ClientAndConfig {

        final WrappingDefaultHttpClient wrapper;
        final ClientConfig config;

        ClientAndConfig(WrappingDefaultHttpClient client, ClientConfig config) {
            super();
            this.wrapper = client;
            this.config = config;
        }
    }

}
