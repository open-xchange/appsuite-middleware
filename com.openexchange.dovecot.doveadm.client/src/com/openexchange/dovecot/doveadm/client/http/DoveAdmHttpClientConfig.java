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

package com.openexchange.dovecot.doveadm.client.http;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.rest.client.httpclient.HttpClientProperty.CONNTECTION_TIMEOUT_MILLIS;
import static com.openexchange.rest.client.httpclient.HttpClientProperty.MAX_CONNECTIONS_PER_ROUTE;
import static com.openexchange.rest.client.httpclient.HttpClientProperty.MAX_TOTAL_CONNECTIONS;
import static com.openexchange.rest.client.httpclient.HttpClientProperty.SOCKET_READ_TIMEOUT_MILLIS;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.dovecot.doveadm.client.internal.HttpDoveAdmCall;
import com.openexchange.dovecot.doveadm.client.internal.HttpDoveAdmEndpointManager;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.httpclient.DefaultHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpClientProperty;
import com.openexchange.rest.client.httpclient.HttpBasicConfig;
import com.openexchange.server.ServiceLookup;
import com.openexchange.version.VersionService;

/**
 * {@link DoveAdmHttpClientConfig}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class DoveAdmHttpClientConfig extends DefaultHttpClientConfigProvider {

    private static final int READ_TIMEOUT = 3000;
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int TOTAL_CONNECTIONS = 100;
    private static final int TOTAL_CONNECTIONS_PER_ROUTE = 100;

    private static final Logger LOGGER = LoggerFactory.getLogger(DoveAdmHttpClientConfig.class);

    private final ServiceLookup serviceLookup;
    private final HttpDoveAdmCall call;

    /**
     * Initializes a new {@link DoveAdmHttpClientConfig}.
     *
     * @param serviceLookup The {@link ServiceLookup}
     * @param call The {@link HttpDoveAdmCall}^
     */
    public DoveAdmHttpClientConfig(ServiceLookup serviceLookup, HttpDoveAdmCall call) {
        super(getClientId(call), "OX Dovecot Http Client v", Optional.ofNullable(serviceLookup.getService(VersionService.class)));
        this.serviceLookup = serviceLookup;
        this.call = call;
    }

    /**
     * Get the client ID for the {@link HttpDoveAdmCall}
     *
     * @param call The call to get the ID for
     * @return The client ID for the HTTP client
     */
    public static String getClientId(HttpDoveAdmCall call) {
        String name = "doveadm";
        if (call != HttpDoveAdmCall.DEFAULT) {
            name = "doveadm-" + call.getName();
        }
        return name;
    }

    @Override
    public Interests getAdditionalInterests() {
        return DefaultInterests.builder().propertiesOfInterest(HttpDoveAdmEndpointManager.DOVEADM_ENDPOINTS + ".*").build();
    }

    @Override
    public HttpBasicConfig configureHttpBasicConfig(HttpBasicConfig config) {
        try {
            return getFromConfiguration(config);
        } catch (OXException e) {
            LoggerFactory.getLogger(DoveAdmHttpClientConfig.class).warn("Unable to set HTTP client configuration for DoveAdm call {}", call, e);
        }
        return config.setMaxTotalConnections(TOTAL_CONNECTIONS).setMaxConnectionsPerRoute(TOTAL_CONNECTIONS_PER_ROUTE).setConnectionTimeout(CONNECTION_TIMEOUT).setSocketReadTimeout(READ_TIMEOUT);
    }

    /**
     * Configures the {@link HttpBasicConfig} with values fromt he configuration.
     *
     * @param config The {@link HttpBasicConfig} to configure
     * @return The configured {@link HttpBasicConfig}
     * @throws OXException in case the {@link ConfigurationService} is missing
     */
    private HttpBasicConfig getFromConfiguration(HttpBasicConfig config) throws OXException {
        ConfigurationService configService = serviceLookup.getServiceSafe(ConfigurationService.class);

        // Read properties for HTTP connections/pooling
        StringBuilder propPrefix;
        String fallBackName = HttpDoveAdmEndpointManager.DOVEADM_ENDPOINTS;
        String propName = HttpDoveAdmEndpointManager.DOVEADM_ENDPOINTS + "." + call.getName();
        String endPoints = configService.getProperty(propName);
        if (Strings.isEmpty(endPoints)) {
            propPrefix = new StringBuilder(fallBackName.length() + 1).append(fallBackName).append('.');
        } else {
            propPrefix = new StringBuilder(propName.length() + 1).append(propName).append('.');
        }

        int resetLen = propPrefix.length();
        String name = propPrefix.append("totalConnections").toString();
        String value = configService.getProperty(name);
        setValue(name, value, TOTAL_CONNECTIONS, config, MAX_TOTAL_CONNECTIONS);

        propPrefix.setLength(resetLen);
        name = propPrefix.append("maxConnectionsPerRoute").toString();
        value = configService.getProperty(name);
        setValue(name, value, TOTAL_CONNECTIONS_PER_ROUTE, config, MAX_CONNECTIONS_PER_ROUTE);

        propPrefix.setLength(resetLen);
        name = propPrefix.append("readTimeout").toString();
        value = configService.getProperty(name);
        setValue(name, value, READ_TIMEOUT, config, SOCKET_READ_TIMEOUT_MILLIS);

        propPrefix.setLength(resetLen);
        name = propPrefix.append("connectTimeout").toString();
        value = configService.getProperty(name);
        setValue(name, value, CONNECTION_TIMEOUT, config, CONNTECTION_TIMEOUT_MILLIS);
        return config;
    }

    /**
     * Sets the {@link HttpClientProperty} value. If the given value is empty it falls back to the given default value
     *
     * @param propName The name of the property
     * @param value The value
     * @param defaultValue The default value
     * @param config The {@link HttpBasicConfig}
     * @param prop The {@link HttpClientProperty}
     */
    private void setValue(String propName, String value, int defaultValue, HttpBasicConfig config, HttpClientProperty prop) {
        if (Strings.isNotEmpty(value)) {
            try {
                prop.setInConfig(config, Integer.valueOf(value));
            } catch (NumberFormatException ignoree) {
                LOGGER.warn("Unale to parse value for property {}", propName, ignoree);
            }
        }
        prop.setInConfig(config, I(defaultValue));
    }
}
