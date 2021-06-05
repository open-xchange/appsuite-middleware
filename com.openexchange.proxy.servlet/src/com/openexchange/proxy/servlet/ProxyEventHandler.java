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

package com.openexchange.proxy.servlet;

import java.util.Map;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;

/**
 * {@link ProxyEventHandler} - The {@link EventHandler event handler} for mail push bundle to track newly created and removed sessions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ProxyEventHandler implements EventHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProxyEventHandler.class);

    public ProxyEventHandler() {
        super();
    }

    @Override
    public void handleEvent(final Event event) {
        final String topic = event.getTopic();
        try {
            if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(topic)) {
                // A single session was removed
                final Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
                if (!session.isTransient()) {
                    ProxyRegistryImpl.getInstance().dropRegistrationsFor(session.getSessionID());
                }
            } else if (SessiondEventConstants.TOPIC_REMOVE_DATA.equals(topic) || SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic)) {
                // A session container was removed
                @SuppressWarnings("unchecked") final Map<String, Session> sessionContainer =
                    (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
                // For each session
                final ProxyRegistryImpl registryImpl = ProxyRegistryImpl.getInstance();
                for (final Session session : sessionContainer.values()) {
                    if (!session.isTransient()) {
                        registryImpl.dropRegistrationsFor(session.getSessionID());
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error while handling SessionD event \"{}\"", topic, e);
        }
    }
}
