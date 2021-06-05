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

package com.openexchange.event.impl;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link LoginEvent}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class LoginEvent {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoginEvent.class);

    public static final String TOPIC = "com/openexchange/login";

    private static final String USER_KEY = "USER";
    private static final String CONTEXT_KEY = "CONTEXT";
    private static final String SESSION_KEY = "SESSION";


    private final int userId;
    private final int contextId;
    private final String sessionId;

    public LoginEvent(int userId, int contextId, String sessionId) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.sessionId = sessionId;
    }

    public LoginEvent(Event event) {
        if (!TOPIC.equals(event.getTopic())) {
            throw new IllegalArgumentException("Can only handle events with topic "+TOPIC);
        }
        this.userId = (Integer) event.getProperty(USER_KEY);
        this.contextId = (Integer) event.getProperty(CONTEXT_KEY);
        this.sessionId = (String) event.getProperty(SESSION_KEY);

    }


    public int getUserId() {
        return userId;
    }


    public int getContextId() {
        return contextId;
    }


    public String getSessionId() {
        return sessionId;
    }

    public void post() {
        final EventAdmin eventAdmin = ServerServiceRegistry.getInstance().getService(EventAdmin.class);
        if (eventAdmin == null) {
            LOG.debug("Event Admin is disabled, so skipping LoginEvent");
            return;
        }
        Dictionary ht = new Hashtable();
        ht.put(USER_KEY, userId);
        ht.put(CONTEXT_KEY, contextId);
        ht.put(SESSION_KEY, sessionId);

        Event event = new Event(TOPIC, ht);

        eventAdmin.postEvent(event);
    }

}
