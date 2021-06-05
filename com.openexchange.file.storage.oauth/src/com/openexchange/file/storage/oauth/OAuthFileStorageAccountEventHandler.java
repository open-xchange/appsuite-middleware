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

package com.openexchange.file.storage.oauth;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.access.OAuthAccessRegistry;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.sessiond.SessiondEventConstants;

/**
 * {@link OAuthFileStorageAccountEventHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OAuthFileStorageAccountEventHandler implements EventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthFileStorageAccountEventHandler.class);

    private final KnownApi api;
    private final ServiceLookup services;

    /**
     * Initialises a new {@link OAuthFileStorageAccountEventHandler}.
     * 
     * @param services The service lookup instance
     * @param api the API
     */
    public OAuthFileStorageAccountEventHandler(ServiceLookup services, KnownApi api) {
        super();
        this.services = services;
        this.api = api;
    }

    @Override
    public void handleEvent(Event event) {
        String topic = event.getTopic();
        if (false == SessiondEventConstants.TOPIC_LAST_SESSION.equals(topic)) {
            return;
        }
        try {
            Integer contextId = Integer.class.cast(event.getProperty(SessiondEventConstants.PROP_CONTEXT_ID));
            if (null == contextId) {
                return;
            }
            Integer userId = Integer.class.cast(event.getProperty(SessiondEventConstants.PROP_USER_ID));
            if (null == userId) {
                return;
            }
            OAuthAccessRegistryService registryService = services.getService(OAuthAccessRegistryService.class);
            OAuthAccessRegistry registry = registryService.get(api.getServiceId());
            if (registry.removeIfLast(contextId.intValue(), userId.intValue())) {
                LOG.debug("{} access removed for user {} in context {}", api.getDisplayName(), userId, contextId);
            }
        } catch (Exception e) {
            LOG.error("Error while handling SessionD event '{}'", topic, e);
        }
    }
}
