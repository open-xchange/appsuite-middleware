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

package com.openexchange.passwordchange.history.impl.events;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.passwordchange.history.PasswordChangeRecorder;
import com.openexchange.passwordchange.history.PasswordChangeRecorderRegistryService;
import com.openexchange.session.Session;

/**
 * {@link PasswordChangeEventListener} Listens to password change event created in {@link com.openexchange.passwordchange.BasicPasswordChangeService#perform(com.openexchange.passwordchange.PasswordChangeEvent)} (in propagate)
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordChangeEventListener implements EventHandler {

    /** The topic the event listens to */
    private static final String TOPIC = "com/openexchange/passwordchange";

    private final PasswordChangeRecorderRegistryService registry;

    /**
     * Initializes a new {@link PasswordChangeEventListener}.
     *
     * @param registry The {@link PasswordChangeRecorderRegistryService} to get the {@link PasswordChangeRecorder} from
     */
    public PasswordChangeEventListener(PasswordChangeRecorderRegistryService registry) {
        super();
        this.registry = registry;
    }

    /**
     * Gets the topic of interest.
     *
     * @return The topic
     */
    public String getTopic() {
        return TOPIC;
    }

    @Override
    public void handleEvent(Event event) {
        if (false == TOPIC.equals(event.getTopic())) {
            return;
        }

        // Read user/context identifier
        int contextId = ((Integer) event.getProperty("com.openexchange.passwordchange.contextId")).intValue();
        int userId = ((Integer) event.getProperty("com.openexchange.passwordchange.userId")).intValue();
        String ipAddress = String.valueOf(event.getProperty(("com.openexchange.passwordchange.ipAddress")));
        Session session = (Session) event.getProperty("com.openexchange.passwordchange.session");

        PasswordChangeHelper.recordChangeSafe(contextId, userId, ipAddress, session.getClient(), registry);
    }
}
