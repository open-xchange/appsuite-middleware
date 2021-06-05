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

package com.openexchange.webdav.client.osgi;

import java.net.URI;
import java.util.Optional;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rest.client.httpclient.DefaultHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.SpecificHttpClientConfigProvider;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.webdav.client.WebDAVClient;
import com.openexchange.webdav.client.WebDAVClientFactory;
import com.openexchange.webdav.client.jackrabbit.WebDAVClientImpl;

/**
 * {@link WebDAVClientActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class WebDAVClientActivator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(WebDAVClientActivator.class);

    /**
     * Initializes a new {@link WebDAVClientActivator}.
     */
    public WebDAVClientActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HttpClientService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("starting bundle {}", context.getBundle());

            registerService(SpecificHttpClientConfigProvider.class, new DefaultHttpClientConfigProvider(WebDAVClientImpl.HTTP_CLIENT_ID, "Open-Xchange WebDAV client"));

            ServiceLookup services = this;
            registerService(WebDAVClientFactory.class, new WebDAVClientFactory() {

                @Override
                public WebDAVClient create(HttpClient client, URI baseUrl) {
                    return new WebDAVClientImpl(client, baseUrl);
                }

                @Override
                public WebDAVClient create(HttpClient client, HttpContext context, URI baseUrl) {
                    return new WebDAVClientImpl(client, context, baseUrl);
                }

                @Override
                public WebDAVClient create(Session session, String accountId, URI baseUrl, Optional<String> optClientId, HttpContext context) throws OXException {
                    return new WebDAVClientImpl(session, accountId, baseUrl, services, optClientId, context);
                }

            });
        } catch (Exception e) {
            LOG.error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle {}", context.getBundle());
        super.stopBundle();
    }

}
