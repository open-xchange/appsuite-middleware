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

package com.openexchange.rest.client.osgi;

import static com.openexchange.osgi.Tools.generateServiceFilter;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
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
        registerService(EndpointManagerFactory.class, new EndpointManagerFactoryImpl(this));

        HttpClientServiceImpl httpClientService = new HttpClientServiceImpl(context, this);
        this.httpClientService = httpClientService;
        track(generateServiceFilter(context, SpecificHttpClientConfigProvider.class, WildcardHttpClientConfigProvider.class), httpClientService);
        openTrackers();

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
