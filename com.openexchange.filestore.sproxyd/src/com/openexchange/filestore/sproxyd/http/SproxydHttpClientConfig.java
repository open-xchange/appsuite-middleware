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

package com.openexchange.filestore.sproxyd.http;

import static com.openexchange.filestore.utils.PropertyNameBuilder.optIntProperty;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.utils.PropertyNameBuilder;
import com.openexchange.rest.client.httpclient.AbstractHttpClientModifer;
import com.openexchange.rest.client.httpclient.GenericHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpBasicConfig;
import com.openexchange.server.ServiceLookup;

/**
 * {@link SproxydHttpClientConfig}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class SproxydHttpClientConfig extends AbstractHttpClientModifer implements GenericHttpClientConfigProvider {

    private static final int DEF_READ_TIMEOUT = 15000;
    private static final int DEF_CON_TIMEOUT = 5000;
    private static final int DEF_MAX_CONNECTIONS_PER_HOST = 100;
    private static final int DEF_MAX_CONNECTIONS = 100;
    private ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link SproxydHttpClientConfig}.
     * 
     * @param serviceLookup The {@link ServiceLookup}
     */
    public SproxydHttpClientConfig(ServiceLookup serviceLookup) {
        super(null);
        this.serviceLookup = serviceLookup;
    }

    @Override
    public @NonNull String getClientIdPattern() {
        return "sproxyd*";
    }

    @Override
    public HttpBasicConfig configureHttpBasicConfig(String clientId, HttpBasicConfig config) {
        try {
            return getFromConfiguration(clientId, config);
        } catch (OXException e) {
            LoggerFactory.getLogger(SproxydHttpClientConfig.class).error("Unable to load correct properties for sproxyd HTTP client. Falling back to defaults", e);
        }
        // Fallback to default values
        return config.setMaxTotalConnections(DEF_MAX_CONNECTIONS).setMaxConnectionsPerRoute(DEF_MAX_CONNECTIONS_PER_HOST).setConnectionTimeout(DEF_CON_TIMEOUT).setSocketReadTimeout(DEF_READ_TIMEOUT);
    }

    @Override
    public Interests getAdditionalInterests() {
        return DefaultInterests.builder().propertiesOfInterest("com.openexchange.filestore.sproxyd.*").build();
    }

    /**
     * Get the {@link HttpBasicConfig} from the configuration 
     *
     * @param clientId The client id
     * @param config The {@link HttpBasicConfig}
     * @return The configured {@link HttpBasicConfig}
     * @throws OXException in case the configuration service is missing
     */
    private HttpBasicConfig getFromConfiguration(String clientId, HttpBasicConfig config) throws OXException {
        PropertyNameBuilder nameBuilder = new PropertyNameBuilder("com.openexchange.filestore.sproxyd.");
        ConfigurationService configService = serviceLookup.getServiceSafe(ConfigurationService.class);

        config.setMaxTotalConnections(optIntProperty(clientId, "maxConnections", DEF_MAX_CONNECTIONS, nameBuilder, configService));
        config.setMaxConnectionsPerRoute(optIntProperty(clientId, "maxConnectionsPerHost", DEF_MAX_CONNECTIONS_PER_HOST, nameBuilder, configService));
        config.setConnectionTimeout(optIntProperty(clientId, "connectionTimeout", DEF_CON_TIMEOUT, nameBuilder, configService));
        config.setSocketReadTimeout(optIntProperty(clientId, "socketReadTimeout", DEF_READ_TIMEOUT, nameBuilder, configService));
        return config;
    }

}
