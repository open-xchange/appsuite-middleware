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

package com.openexchange.api.client.impl.osgi;

import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.api.client.ApiClientService;
import com.openexchange.api.client.impl.ApiClientServiceImpl;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.SpecificHttpClientConfigProvider;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.user.UserService;
import com.openexchange.version.VersionService;

/**
 * {@link ApiClientActivator}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class ApiClientActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { HttpClientService.class, UserService.class, LeanConfigurationService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class[] { VersionService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        ApiClientServiceImpl apiClientService = new ApiClientServiceImpl(this);
        registerService(ApiClientService.class, apiClientService);
        registerService(EventHandler.class, apiClientService, singletonDictionary(EventConstants.EVENT_TOPIC, new String[] { 
            SessiondEventConstants.TOPIC_REMOVE_SESSION, SessiondEventConstants.TOPIC_REMOVE_CONTAINER }));
        registerService(SpecificHttpClientConfigProvider.class, new ApiClientConfigConfigProvider());
    }

}
