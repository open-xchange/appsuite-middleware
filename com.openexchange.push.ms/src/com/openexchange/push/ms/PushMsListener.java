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

package com.openexchange.push.ms;

import static com.openexchange.java.Autoboxing.I;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.event.EventFactoryService;
import com.openexchange.event.RemoteEvent;
import com.openexchange.groupware.Types;
import com.openexchange.ms.Message;
import com.openexchange.ms.MessageListener;
import com.openexchange.server.ServiceLookup;

/**
 * {@link PushMsListener} - The {@link MessageListener message listener} for messaging-based push bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PushMsListener implements MessageListener<Map<String, Object>> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushMsListener.class);

    private volatile String hostName;

    /**
     * Initializes a new {@link PushMsListener}.
     */
    public PushMsListener() {
        super();
    }

    @Override
    public void onMessage(final Message<Map<String, Object>> message) {
        final PushMsObject pushObj = PushMsObject.valueFor(message.getMessageObject());
        if (null == pushObj) {
            LOG.debug("Received null from topic. Ignore...");
            return;
        }

        if (getHostname().equals(pushObj.getHostname())) {
            LOG.debug("Recieved PushMsObject's host name is equal to this listener's host name: {}. Ignore...", getHostname());
            return;
        }

        LOG.debug("{} received PushMsObject: {}", getHostname(), pushObj);
        final ServiceLookup registry = Services.getServiceLookup();
        final EventAdmin eventAdmin = registry.getService(EventAdmin.class);
        if (eventAdmin != null) {
            final EventFactoryService eventFactoryService = registry.getService(EventFactoryService.class);
            if (eventFactoryService != null) {
                final int action;
                final String topicName;
                if (pushObj.getModule() == Types.FOLDER) {
                    action = RemoteEvent.FOLDER_CHANGED;
                    topicName = "com/openexchange/remote/folderchanged";
                } else {
                    action = RemoteEvent.FOLDER_CONTENT_CHANGED;
                    topicName = "com/openexchange/remote/foldercontentchanged";
                }
                for (final int user : pushObj.getUsers()) {
                    /*-
                     * Post event to current user
                     *
                     *       See com.openexchange.usm.ox_event.impl.OXEventManagerImpl.handleEvent(Event)
                     *       See com.openexchange.usm.session.impl.SessionManagerImpl.folderContentChanged(int, int, String, long)
                     */
                    final RemoteEvent remEvent = eventFactoryService.newRemoteEvent(
                        pushObj.getFolderId(),
                        user,
                        pushObj.getContextId(),
                        action,
                        pushObj.getModule(),
                        pushObj.getTimestamp());
                    final Map<String, RemoteEvent> ht = Collections.singletonMap(RemoteEvent.EVENT_KEY, remEvent);
                    eventAdmin.postEvent(new Event(topicName, ht));
                    LOG.debug("Posted remote event to user {} in context {}: {}", I(user), I(pushObj.getContextId()), remEvent);
                }
            }
        }
    }

    private String getHostname() {
        String tmp = hostName;
        if (null == tmp) {
            synchronized (this) {
                tmp = hostName;
                if (null == tmp) {
                    tmp = "";
                    try {
                        tmp = InetAddress.getLocalHost().getHostName();
                    } catch (UnknownHostException e) {
                        LOG.error("", e);
                    }
                    hostName = tmp;
                }
            }
        }
        return tmp;
    }

}
