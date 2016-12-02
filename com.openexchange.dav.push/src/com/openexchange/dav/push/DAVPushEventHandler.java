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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.dav.push;

import static org.slf4j.LoggerFactory.getLogger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.pns.DefaultPushNotification;
import com.openexchange.pns.PushNotification;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.session.Session;

/**
 * {@link DAVPushEventHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class DAVPushEventHandler implements EventHandler {

    /**
     * The topics handled by the PUSH event handler
     */
    public static final String[] TOPICS = {
        "com/openexchange/groupware/folder/*",
        "com/openexchange/groupware/contact/*",
        "com/openexchange/groupware/appointment/*",
        "com/openexchange/groupware/task/*",
    };

    private final PushNotificationService notificationService;

    /**
     * Initializes a new {@link DAVPushEventHandler}.
     *
     * @param notificationService The push notification service to use
     */
    public DAVPushEventHandler(PushNotificationService notificationService) {
        super();
        this.notificationService = notificationService;
    }

    @Override
    public void handleEvent(Event event) {
        if (null != event && event.containsProperty(CommonEvent.EVENT_KEY)) {
            Object commonEvent = event.getProperty(CommonEvent.EVENT_KEY);
            if (null != commonEvent && CommonEvent.class.isInstance(commonEvent)) {
                try {
                    handle((CommonEvent) commonEvent);
                } catch (Exception e) {
                    getLogger(DAVPushEventHandler.class).error("error handling event", e);
                }
            }
        }
    }

    private void handle(CommonEvent event) throws OXException {
        Map<Integer, Set<Integer>> affectedUsers = event.getAffectedUsersWithFolder();
        if (null == affectedUsers || 0 == affectedUsers.size()) {
            return;
        }
        String clientId = getClientId(event);
        if (null == clientId) {
            return;
        }
        List<PushNotification> pushNotifications = new ArrayList<PushNotification>();
        String rootTopic = DAVPushUtility.getRootTopic(clientId);
        Long timestamp = Long.valueOf(getTimestamp(event).getTime());
        Integer priority = determinePriority(event);
        String clientToken = determineClientToken(event);
        int contextId = event.getContextId();
        for (Map.Entry<Integer, Set<Integer>> entry : affectedUsers.entrySet()) {
            int userId = entry.getKey().intValue();
            pushNotifications.add(getPushNotification(contextId, userId, rootTopic, timestamp, priority, clientToken));
            for (Integer folderId : entry.getValue()) {
                String folderTopic = DAVPushUtility.getFolderTopic(clientId, folderId.toString());
                pushNotifications.add(getPushNotification(contextId, userId, folderTopic, timestamp, priority, clientToken));
            }
        }

        notificationService.handle(pushNotifications);
    }

    private static DefaultPushNotification getPushNotification(int contextId, int userId, String topic, Long timestamp, Integer priority, String clientToken) {
        String pushKey = DAVPushUtility.getPushKey(topic, contextId, userId);
        return DefaultPushNotification.builder()
            .contextId(contextId)
            .userId(userId)
            .topic(topic)
            .messageData(getMessageData(timestamp, pushKey, priority, clientToken))
        .build();
    }

    private static Map<String, Object> getMessageData(Long timestamp, String pushKey, Integer priority, String clientToken) {
        Map<String, Object> messageData = new HashMap<String, Object>(3);
        messageData.put(DAVPushUtility.PARAMETER_TIMESTAMP, timestamp);
        messageData.put(DAVPushUtility.PARAMETER_PUSHKEY, pushKey);
        messageData.put(DAVPushUtility.PARAMETER_PRIORITY, priority);
        if (null != clientToken) {
            messageData.put(DAVPushUtility.PARAMETER_CLIENTTOKEN, clientToken);
        }
        return messageData;
    }

    private static String getClientId(CommonEvent event) {
        if (Types.CONTACT == event.getModule()) {
            return DAVPushUtility.CLIENT_CARDDAV;
        }
        if (Types.APPOINTMENT == event.getModule() || Types.TASK == event.getModule()) {
            return DAVPushUtility.CLIENT_CALDAV;
        }
        if (Types.FOLDER == event.getModule()) {
            Object object = event.getActionObj();
            if (null == object) {
                object = event.getOldObj();
            }
            if (null != object && FolderObject.class.isInstance(object)) {
                int module = ((FolderObject) object).getModule();
                if (FolderObject.CONTACT == module) {
                    return DAVPushUtility.CLIENT_CARDDAV;
                }
                if (FolderObject.CALENDAR == module || FolderObject.TASK == module) {
                    return DAVPushUtility.CLIENT_CALDAV;
                }
            }
        }
        return null;
    }

    private static Date getTimestamp(CommonEvent event) {
        Date timestamp = null;
        Object object = event.getActionObj();
        if (null != object && DataObject.class.isInstance(object)) {
            timestamp = ((DataObject) object).getLastModified();
        }
        if (null == timestamp) {
            object = event.getOldObj();
            if (null != object && DataObject.class.isInstance(object)) {
                timestamp = ((DataObject) object).getLastModified();
            }
        }
        return null == timestamp ? new Date() : timestamp;
    }

    private static int determinePriority(CommonEvent event) {
        //TODO
        // - is user attendee?
        // - is event in past or far in future?
        return Integer.valueOf(50);
    }

    private static String determineClientToken(CommonEvent event) {
        Session session = event.getSession();
        if (null != session) {
            return (String) session.getParameter("com.openexchange.dav.push.clientToken");
        }
        return null;
    }

}
