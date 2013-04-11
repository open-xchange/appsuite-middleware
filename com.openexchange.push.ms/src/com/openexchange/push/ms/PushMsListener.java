/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.push.ms;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.event.EventFactoryService;
import com.openexchange.event.RemoteEvent;
import com.openexchange.groupware.Types;
import com.openexchange.log.LogFactory;
import com.openexchange.ms.Message;
import com.openexchange.ms.MessageListener;
import com.openexchange.server.ServiceLookup;

/**
 * {@link PushMsListener} - The {@link MessageListener message listener} for messaging-based push bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PushMsListener implements MessageListener<Map<String, Object>> {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(PushMsListener.class));

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
        if (!getHostname().equals(pushObj.getHostname())) {
            final ServiceLookup registry = Services.getServiceLookup();
            final EventAdmin eventAdmin = registry.getService(EventAdmin.class);
            final EventFactoryService eventFactoryService = registry.getService(EventFactoryService.class);
            if (eventAdmin != null && eventFactoryService != null) {
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
                    final RemoteEvent remEvent =
                        eventFactoryService.newRemoteEvent(
                            pushObj.getFolderId(),
                            user,
                            pushObj.getContextId(),
                            action,
                            pushObj.getModule(),
                            pushObj.getTimestamp());
                    final Dictionary<String, RemoteEvent> ht = new Hashtable<String, RemoteEvent>();
                    ht.put(RemoteEvent.EVENT_KEY, remEvent);
                    eventAdmin.postEvent(new Event(topicName, ht));
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
                    } catch (final UnknownHostException e) {
                        LOG.error(e.getMessage(), e);
                    }
                    hostName = tmp;
                }
            }
        }
        return tmp;
    }

}
