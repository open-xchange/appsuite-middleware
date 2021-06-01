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

package com.openexchange.messaging.twitter.session;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.sessiond.SessiondEventConstants;

/**
 * {@link TwitterEventHandler} - The {@link EventHandler event handler} for mail push bundle to track newly created and removed sessions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterEventHandler implements EventHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TwitterEventHandler.class);

    public TwitterEventHandler() {
        super();
    }

    @Override
    public void handleEvent(final Event event) {
        final String topic = event.getTopic();
        try {
            if (SessiondEventConstants.TOPIC_LAST_SESSION.equals(topic)) {
                Integer contextId = (Integer) event.getProperty(SessiondEventConstants.PROP_CONTEXT_ID);
                if (null != contextId) {
                    Integer userId = (Integer) event.getProperty(SessiondEventConstants.PROP_USER_ID);
                    if (null != userId) {
                        if (TwitterAccessRegistry.getInstance().removeAccessIfLast(contextId.intValue(), userId.intValue())) {
                            LOG.debug("Twitter access removed for user {} in context {}", userId, contextId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error while handling SessionD event \"{}\"", topic, e);
        }
    }
}
