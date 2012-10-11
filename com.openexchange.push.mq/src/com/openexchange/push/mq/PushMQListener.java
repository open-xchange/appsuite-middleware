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

package com.openexchange.push.mq;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.jms.ObjectMessage;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.event.EventFactoryService;
import com.openexchange.event.RemoteEvent;
import com.openexchange.groupware.Types;
import com.openexchange.mq.topic.MQTopicListener;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.push.mq.registry.PushMQServiceRegistry;

/**
 * {@link PushMQListener}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class PushMQListener implements MQTopicListener {
    
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(PushMQListener.class));

    private EventAdmin eventAdmin;
    private EventFactoryService eventFactoryService;

    /**
     * Initializes a new {@link PushMQListener}.
     */
    public PushMQListener() {
        super();
        ServiceRegistry registry = PushMQServiceRegistry.getServiceRegistry();
        eventAdmin = registry.getService(EventAdmin.class);
        eventFactoryService = registry.getService(EventFactoryService.class);
    }

    @Override
    public void close() {
        eventAdmin = null;
        eventFactoryService = null;
    }

    @Override
    public void onText(String text) {
        LOG.warn("Unsupported operation: TextMessage");
        throw new UnsupportedOperationException();
    }

    @Override
    public void onObjectMessage(ObjectMessage objectMessage) {
        LOG.warn("Unsupported operation: ObjectMessage");
        throw new UnsupportedOperationException();
    }

    @Override
    public void onBytes(byte[] bytes) {
        try {
            Serializable obj = SerializableHelper.readObject(bytes);
            PushMQObject pushObj = (PushMQObject) obj;
            if (!getHostname().equals(pushObj.getHostname())) {
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
                        RemoteEvent remEvent = eventFactoryService.newRemoteEvent(
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
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            LOG.error(e.getMessage(), e);
        }
    }
    
    private String getHostname() {
        String hostname = "";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException e) {
            LOG.error(e.getMessage(), e);
        }
        return hostname;
    }

}
