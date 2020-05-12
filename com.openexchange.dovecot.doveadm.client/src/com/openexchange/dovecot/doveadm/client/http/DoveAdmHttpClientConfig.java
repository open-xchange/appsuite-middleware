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

import java.util.Optional;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.dovecot.doveadm.client.internal.ClientConfig;
import com.openexchange.dovecot.doveadm.client.internal.HttpDoveAdmCall;
import com.openexchange.dovecot.doveadm.client.internal.HttpDoveAdmEndpointManager;
import com.openexchange.rest.client.httpclient.DefaultHttpClientConfigProvider;
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

    /**
     * Generates the client identifier for the DoveAdm call.
     *
     * @param call The call to get the identifier for
     * @return The client identifier for the HTTP client
     */
    public static String generateClientId(HttpDoveAdmCall call) {
        String name = "doveadm";
        if (call != HttpDoveAdmCall.DEFAULT) {
            name = "doveadm-" + call.getName();
        }
        return name;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final ClientConfig clientConfig;

    /**
     * Initializes a new {@link DoveAdmHttpClientConfig}.
     *
     * @param httpClientId The identifier for the HTTP client
     * @param clientConfig The client configuration
     * @param services The service look-up
     */
    public DoveAdmHttpClientConfig(String httpClientId, ClientConfig clientConfig, ServiceLookup services) {
        super(httpClientId, "OX Dovecot Http Client v", Optional.ofNullable(services.getService(VersionService.class)));
        this.clientConfig = clientConfig;
    }

    @Override
    public Interests getAdditionalInterests() {
        return DefaultInterests.builder().propertiesOfInterest(HttpDoveAdmEndpointManager.DOVEADM_ENDPOINTS + ".*").build();
    }

    @Override
    public HttpBasicConfig configureHttpBasicConfig(HttpBasicConfig config) {
        config.setConnectTimeout(clientConfig.getConnectTimeout());
        config.setMaxConnectionsPerRoute(clientConfig.getMaxConnectionsPerRoute());
        config.setMaxTotalConnections(clientConfig.getTotalConnections());
        config.setSocketReadTimeout(clientConfig.getReadTimeout());
        return config;
    }

}
