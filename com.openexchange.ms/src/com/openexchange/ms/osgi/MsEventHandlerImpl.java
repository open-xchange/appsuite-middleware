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

package com.openexchange.ms.osgi;

import java.util.Map;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.java.Strings;
import com.openexchange.ms.MsEventConstants;
import com.openexchange.ms.MsService;
import com.openexchange.ms.Topic;
import com.openexchange.ms.internal.HzMsService;

/**
 * {@link MsEventHandlerImpl} - The event handler for {@link MsService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public final class MsEventHandlerImpl implements EventHandler {

    private final HzMsService msService;

    /**
     * Initializes a new {@link MsEventHandlerImpl}.
     *
     * @param msService The associated service
     */
    public MsEventHandlerImpl(final HzMsService msService) {
        this.msService = msService;
    }

    @Override
    public void handleEvent(final Event event) {
        final String topic = event.getTopic();
        if (MsEventConstants.TOPIC_REMOTE_REPUBLISH.equals(topic)) {
            handleForRemotePublication(msService, event);
        }
    }

    private void handleForRemotePublication(final HzMsService msService, final Event event) {
        try {
            final String topicName = (String) event.getProperty(MsEventConstants.PROPERTY_TOPIC_NAME);
            if (Strings.isNotEmpty(topicName)) {
                final Map<String, Object> map = (Map<String, Object>) event.getProperty(MsEventConstants.PROPERTY_DATA_MAP);
                if (null != map && !map.isEmpty()) {
                    final Topic<Map<String, Object>> msTopic = msService.getTopic(topicName);
                    msTopic.publish(map);
                }
            }
        } catch (Exception e) {
            // Ignore
            final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MsActivator.class);
            logger.warn("Could not handle event with topic ''{}''", event.getTopic(), e);
        }
    }

}
