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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.pns.impl.event;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import com.google.common.collect.ImmutableSet;
import com.openexchange.event.CommonEvent;
import com.openexchange.pns.DefaultPushNotification;
import com.openexchange.pns.KnownTopic;
import com.openexchange.pns.PushNotificationField;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.push.Container;
import com.openexchange.push.PushEventConstants;

/**
 * {@link PushEventHandler} - Handles legacy push events and schedules them into notification service;
 * unless <code>PushEventConstants.PROPERTY_NO_FORWARD</code> is set to <code>true</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class PushEventHandler implements org.osgi.service.event.EventHandler {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PushEventHandler.class);

    private final Set<String> ignorees;
    private final PushNotificationService notificationService;

    /**
     * Initializes a new {@link PushEventHandler}.
     */
    public PushEventHandler(PushNotificationService notificationService) {
        super();
        this.notificationService = notificationService;
        ignorees = ImmutableSet.of(PushEventConstants.PROPERTY_CONTEXT, PushEventConstants.PROPERTY_USER, PushEventConstants.PROPERTY_SESSION,
            PushEventConstants.PROPERTY_FOLDER, CommonEvent.PUBLISH_MARKER, CommonEvent.EVENT_KEY, PushEventConstants.PROPERTY_NO_FORWARD );
    }

    @Override
    public void handleEvent(Event event) {
        try {
            Boolean noForward = (Boolean) event.getProperty(PushEventConstants.PROPERTY_NO_FORWARD);
            if (null != noForward && noForward.booleanValue()) {
                return;
            }

            if (!event.containsProperty(CommonEvent.EVENT_KEY)) {
                return;
            }

            // Required properties
            String folder = requireProperty(PushEventConstants.PROPERTY_FOLDER, event);
            Integer contextId = requireProperty(PushEventConstants.PROPERTY_CONTEXT, event);
            Integer userId = requireProperty(PushEventConstants.PROPERTY_USER, event);

            // Build message data from event's properties
            Map<String, Object> messageData = createMessageData(folder, event);

            // Now schedule as push notification
            DefaultPushNotification notification = DefaultPushNotification.builder()
                .contextId(contextId.intValue())
                .userId(userId.intValue())
                .messageData(messageData)
                .topic(KnownTopic.MAIL_NEW.getName())
                .build();
            notificationService.handle(notification);
        } catch (Exception e) {
            LOG.warn("Failed to handle incoming \"{}\" push event", KnownTopic.MAIL_NEW.getName(), e);
        }
    }

    private <V> V requireProperty(String name, Event event) {
        Object object = event.getProperty(name);
        if (null == object) {
            throw new IllegalArgumentException("Missing required property in event: " + name);
        }
        return (V) object;
    }

    private Map<String, Object> createMessageData(String folder, Event event) {
        String[] propertyNames = event.getPropertyNames();
        Map<String, Object> map = new LinkedHashMap<>(propertyNames.length);
        map.put(PushNotificationField.FOLDER.getId(), folder);
        for (String name : propertyNames) {
            if (!ignorees.contains(name)) {
                if (PushEventConstants.PROPERTY_CONTAINER.equals(name)) {
                    Container<?> container = (Container<?>) event.getProperty(name);
                    map.put(PushNotificationField.ARGS.getId(), container.getList());
                } else {
                    map.put(name, event.getProperty(name));
                }
            }
        }
        return null;
    }

}
