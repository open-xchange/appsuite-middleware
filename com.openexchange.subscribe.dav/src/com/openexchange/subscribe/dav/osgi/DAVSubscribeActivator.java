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

package com.openexchange.subscribe.dav.osgi;

import static com.openexchange.subscribe.dav.AbstractDAVSubscribeService.CLIENT_ID;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rest.client.httpclient.DefaultHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpBasicConfig;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.SpecificHttpClientConfigProvider;
import com.openexchange.subscribe.SubscriptionExecutionService;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link DAVSubscribeActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class DAVSubscribeActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link DAVSubscribeActivator}.
     */
    public DAVSubscribeActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ThreadPoolService.class, ContextService.class, VCardService.class, HttpClientService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServices(this);

        registerService(SpecificHttpClientConfigProvider.class, new DefaultHttpClientConfigProvider(CLIENT_ID, "Open-Xchange DAV Http Client") {

            @Override
            public HttpBasicConfig configureHttpBasicConfig(HttpBasicConfig config) {
                return config.setMaxTotalConnections(10).setMaxConnectionsPerRoute(5).setConnectTimeout(5000).setSocketReadTimeout(10000);
            }
        });

        trackService(SubscriptionExecutionService.class);
        trackService(FolderUpdaterRegistry.class);
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServices(null);
        super.stopBundle();
    }

}
