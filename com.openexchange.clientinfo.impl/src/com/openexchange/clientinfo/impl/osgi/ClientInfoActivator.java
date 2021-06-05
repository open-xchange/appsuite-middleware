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

package com.openexchange.clientinfo.impl.osgi;

import com.openexchange.clientinfo.ClientInfoProvider;
import com.openexchange.clientinfo.ClientInfoService;
import com.openexchange.clientinfo.impl.ClientInfoServiceImpl;
import com.openexchange.clientinfo.impl.MailAppClientInfoProvider;
import com.openexchange.clientinfo.impl.USMEASClientInfoProvider;
import com.openexchange.clientinfo.impl.WebClientInfoProvider;
import com.openexchange.clientinfo.impl.WebDAVClientInfoProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.uadetector.UserAgentParser;


/**
 * {@link ClientInfoActivator}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class ClientInfoActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link ClientInfoActivator}.
     */
    public ClientInfoActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { UserAgentParser.class, SessiondService.class, ServerConfigService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        RankingAwareNearRegistryServiceTracker<ClientInfoProvider> infoProviderTracker = new RankingAwareNearRegistryServiceTracker<>(context, ClientInfoProvider.class);
        rememberTracker(infoProviderTracker);
        openTrackers();

        ClientInfoService service = new ClientInfoServiceImpl(infoProviderTracker);
        registerService(ClientInfoService.class, service);
        registerService(ClientInfoProvider.class, new USMEASClientInfoProvider(), 15);
        registerService(ClientInfoProvider.class, new WebDAVClientInfoProvider(), 15);
        registerService(ClientInfoProvider.class, new WebClientInfoProvider(this), 20);
        registerService(ClientInfoProvider.class, new MailAppClientInfoProvider(this), 25);
    }

}
