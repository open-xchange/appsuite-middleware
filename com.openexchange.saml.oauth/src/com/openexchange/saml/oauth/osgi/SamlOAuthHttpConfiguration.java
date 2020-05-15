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

package com.openexchange.saml.oauth.osgi;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import org.slf4j.LoggerFactory;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.httpclient.DefaultHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpBasicConfig;
import com.openexchange.server.ServiceLookup;

/**
 * {@link SamlOAuthHttpConfiguration}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class SamlOAuthHttpConfiguration extends DefaultHttpClientConfigProvider {

    private static final int DEFAULT_READ_TIMEOUT = 6000;
    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int DEFAULT_CONNECTIONS_PER_ROUTE = 100;
    private static final int DEFAULT_TOTAL_CONNECTIONS = 100;

    private static final String MAX_CONNECTIONS = "com.openexchange.saml.oauth.maxConnections";
    private static final String MAX_CONNECTIONS_PER_HOST = "com.openexchange.saml.oauth.maxConnectionsPerHost";
    private static final String CONNECTION_TIMEOUT = "com.openexchange.saml.oauth.connectionTimeout";
    private static final String SOCKET_READ_TIMEOUT = "com.openexchange.saml.oauth.socketReadTimeout";

    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link SamlOAuthHttpConfiguration}.
     *
     * @param serviceLookup The {@link ServiceLookup}
     */
    public SamlOAuthHttpConfiguration(ServiceLookup serviceLookup) {
        super("saml-oauth", "Open-Xchange SAML OAuth Client");
        this.serviceLookup = serviceLookup;
    }

    @Override
    public Interests getAdditionalInterests() {
        return DefaultInterests.builder().propertiesOfInterest(MAX_CONNECTIONS, MAX_CONNECTIONS_PER_HOST, CONNECTION_TIMEOUT, SOCKET_READ_TIMEOUT).build();
    }

    @Override
    public HttpBasicConfig configureHttpBasicConfig(HttpBasicConfig config) {
        try {
            return getFromConfiguration(config);
        } catch (OXException e) {
            LoggerFactory.getLogger(SamlOAuthHttpConfiguration.class).warn("Can't apply HTTP client configuration for SAML OAuth.", e);
        }
        return config.setMaxTotalConnections(DEFAULT_TOTAL_CONNECTIONS).setMaxConnectionsPerRoute(DEFAULT_CONNECTIONS_PER_ROUTE).setConnectTimeout(DEFAULT_TIMEOUT).setSocketReadTimeout(DEFAULT_READ_TIMEOUT);
    }

    private HttpBasicConfig getFromConfiguration(HttpBasicConfig config) throws OXException {
        ConfigView view = serviceLookup.getService(ConfigViewFactory.class).getView();
        Integer value = view.opt(MAX_CONNECTIONS, Integer.class, I(DEFAULT_TOTAL_CONNECTIONS));
        config.setMaxTotalConnections(i(value));

        value = view.opt(MAX_CONNECTIONS_PER_HOST, Integer.class, I(DEFAULT_CONNECTIONS_PER_ROUTE));
        config.setMaxConnectionsPerRoute(i(value));

        value = view.opt(CONNECTION_TIMEOUT, Integer.class, I(DEFAULT_TIMEOUT));
        config.setConnectTimeout(i(value));

        value = view.opt(SOCKET_READ_TIMEOUT, Integer.class, I(DEFAULT_READ_TIMEOUT));
        config.setSocketReadTimeout(i(value));
        return config;
    }

}
