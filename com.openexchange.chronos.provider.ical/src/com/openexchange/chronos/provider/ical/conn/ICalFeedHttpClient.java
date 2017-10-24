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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.chronos.provider.ical.conn;

import org.apache.http.impl.client.CloseableHttpClient;
import com.openexchange.chronos.provider.ical.internal.ICalCalendarProviderProperties;
import com.openexchange.chronos.provider.ical.internal.Services;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.rest.client.httpclient.HttpClients.ClientConfig;

/**
 * {@link ICalFeedHttpClient}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ICalFeedHttpClient {

    protected static CloseableHttpClient httpClient;

    public static CloseableHttpClient getInstance() {
        if (httpClient == null) {
            synchronized (ICalFeedConnector.class) {
                if (httpClient == null) {
                    ClientConfig config = ClientConfig.newInstance();
                    config.setUserAgent("Open-Xchange Calendar Feed Client");
                    init(config);
                    httpClient = HttpClients.getHttpClient(config, Services.getService(SSLSocketFactoryProvider.class), Services.getService(SSLConfigurationService.class));

                }
            }
        }
        return httpClient;
    }

    private static void init(ClientConfig config) {
        LeanConfigurationService leanConfigurationService = Services.getService(LeanConfigurationService.class);
        int maxConnections = leanConfigurationService.getIntProperty(ICalCalendarProviderProperties.maxConnections);
        config.setMaxTotalConnections(maxConnections);

        int maxConnectionsPerRoute = leanConfigurationService.getIntProperty(ICalCalendarProviderProperties.maxConnectionsPerRoute);
        config.setMaxConnectionsPerRoute(maxConnectionsPerRoute);

        int connectionTimeout = leanConfigurationService.getIntProperty(ICalCalendarProviderProperties.connectionTimeout);
        config.setConnectionTimeout(connectionTimeout);

        int socketReadTimeout = leanConfigurationService.getIntProperty(ICalCalendarProviderProperties.socketReadTimeout);
        config.setSocketReadTimeout(socketReadTimeout);
    }
    
    public static void reset() {
        httpClient = null;
    }
}
