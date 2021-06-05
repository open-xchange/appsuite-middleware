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

package com.openexchange.oauth.yahoo.access;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.access.OAuthAccessRegistry;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.oauth.yahoo.osgi.Services;
import com.openexchange.sessiond.SessiondEventConstants;

/**
 * {@link YahooAccessEventHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class YahooAccessEventHandler implements EventHandler {

    /**
     * The logger constant.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(YahooAccessEventHandler.class);

    /**
     * Initializes a new {@link YahooAccessEventHandler}.
     */
    public YahooAccessEventHandler() {
        super();
    }

    @Override
    public void handleEvent(Event event) {
        String topic = event.getTopic();
        if (false == SessiondEventConstants.TOPIC_LAST_SESSION.equals(topic)) {
            return;
        }
        try {
            Integer contextId = (Integer) event.getProperty(SessiondEventConstants.PROP_CONTEXT_ID);
            if (null == contextId) {
                return;
            }
            Integer userId = (Integer) event.getProperty(SessiondEventConstants.PROP_USER_ID);
            if (null == userId) {
                return;
            }
            OAuthAccessRegistryService registryService = Services.getService(OAuthAccessRegistryService.class);
            OAuthAccessRegistry registry = registryService.get(KnownApi.YAHOO.getServiceId());
            if (registry.removeIfLast(contextId.intValue(), userId.intValue())) {
                LOG.debug("Yahoo session removed for user {} in context {}", userId, contextId);
            }
        } catch (Exception e) {
            LOG.error("Error while handling SessionD event \"{}\"", topic, e);
        }
    }
}
