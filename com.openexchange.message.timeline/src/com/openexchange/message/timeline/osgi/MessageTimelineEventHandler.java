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

package com.openexchange.message.timeline.osgi;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.message.timeline.MessageTimelineManagement;
import com.openexchange.sessiond.SessiondEventConstants;

/**
 * {@link MessageTimelineEventHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessageTimelineEventHandler implements EventHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MessageTimelineEventHandler.class);

    /**
     * Initializes a new {@link MessageTimelineEventHandler}.
     */
    public MessageTimelineEventHandler() {
        super();
    }

    @Override
    public void handleEvent(Event event) {
        String topic = event.getTopic();
        if (SessiondEventConstants.TOPIC_LAST_SESSION.equals(topic)) {
            try {
                Integer contextId = (Integer) event.getProperty(SessiondEventConstants.PROP_CONTEXT_ID);
                if (null != contextId) {
                    Integer userId = (Integer) event.getProperty(SessiondEventConstants.PROP_USER_ID);
                    if (null != userId) {
                        // No active session left
                        MessageTimelineManagement.getInstance().dropFor(userId.intValue(), contextId.intValue());
                    }
                }
            } catch (Exception e) {
                LOG.error("Error while handling SessionD event \"{}\"", topic, e);
            }
        }
    }

}
