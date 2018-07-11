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

package com.openexchange.drive.events.apn2.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.clevertap.apns.ApnsClient;
import com.clevertap.apns.Notification;
import com.clevertap.apns.NotificationRequestError;
import com.clevertap.apns.NotificationResponse;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.DriveEventPublisher;
import com.openexchange.drive.events.apn2.ApnsHttp2Options;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.Subscription;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;


/**
 * {@link ApnsHttp2DriveEventPublisher} - The event publisher using APNS HTTP/2.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public abstract class ApnsHttp2DriveEventPublisher implements DriveEventPublisher {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ApnsHttp2DriveEventPublisher.class);
    }

    /** The tracked OSGi services */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link ApnsHttp2DriveEventPublisher}.
     *
     * @param services The tracked OSGi services
     */
    protected ApnsHttp2DriveEventPublisher(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public boolean isLocalOnly() {
        return true;
    }

    /**
     * Gets the service identifier for this APNS HTTP/2 event publisher
     *
     * @return The service identifier
     */
    protected abstract String getServiceID();

    /**
     * Gets the APNS HTTP/2 options to use.
     *
     * @return The options
     * @throws OXException If options cannot be returned
     */
    protected abstract ApnsHttp2Options getOptions() throws OXException;

    private boolean removeSubscription(Subscription subscription) {
        try {
            DriveSubscriptionStore subscriptionStore = services.getService(DriveSubscriptionStore.class);
            return subscriptionStore.removeSubscription(subscription);
        } catch (OXException e) {
            LoggerHolder.LOG.error("Error removing subscription", e);
        }
        return false;
    }

    @Override
    public void publish(DriveEvent event) {
        // Query available subscriptions
        List<Subscription> subscriptions = null;
        try {
            DriveSubscriptionStore subscriptionStore = services.getService(DriveSubscriptionStore.class);
            subscriptions = subscriptionStore.getSubscriptions( event.getContextID(), new String[] { getServiceID() }, event.getFolderIDs());
        } catch (Exception e) {
            LoggerHolder.LOG.error("unable to get subscriptions for service {}", getServiceID(), e);
        }

        if (null == subscriptions) {
            // Nothing to do
            return;
        }

        // Compile appropriate notifications/payloads for available subscriptions
        List<NotificationAndSubscription> notifications = getNotifications(event, subscriptions);
        if (notifications.isEmpty()) {
            // Nothing to do
            return;
        }

        // Get the APNS HTTP/2 client to use
        ApnsClient client = getClient();
        if (null == client) {
            // Nothing to do
            return;
        }

        // Push the notifications & remember responses
        List<NotificationResponseAndSubscription> responses = new ArrayList<>(notifications.size());
        for (NotificationAndSubscription notification : notifications) {
            responses.add(new NotificationResponseAndSubscription(client.push(notification.notification), notification.subscription));
        }

        processNotificationResponses(responses);
    }

    private void processNotificationResponses(List<NotificationResponseAndSubscription> notificationResponses) {
        if (null == notificationResponses || notificationResponses.isEmpty()) {
            return;
        }

        for (NotificationResponseAndSubscription notificationResponseAndSubscription : notificationResponses) {
            NotificationResponse notificationResponse = notificationResponseAndSubscription.notificationResponse;
            NotificationRequestError error = notificationResponse.getError();
            if (null == error) {
                LoggerHolder.LOG.debug("{}", notificationResponse);
                continue;
            }

            Subscription subscription = notificationResponseAndSubscription.subscription;
            if (NotificationRequestError.DeviceTokenInactiveForTopic == error || NotificationRequestError.InvalidProviderToken == error) {
                LoggerHolder.LOG.warn("Unsuccessful notification for drive event due to inactive or invalid device token: {}", subscription.getToken());
                removeSubscription(subscription);
            } else {
                LoggerHolder.LOG.warn("Unsuccessful notification for drive event for device token {}: {}", subscription.getToken(), error);
            }
        }
    }

    private ApnsClient getClient() {
        try {
            return ApnsHttp2Utility.getApnsClient(getOptions());
        } catch (OXException e) {
            LoggerHolder.LOG.error("unable to create APNS HTTP/s client for service {}", getServiceID(), e);
        }
        return null;
    }

    private List<NotificationAndSubscription> getNotifications(DriveEvent event, List<Subscription> subscriptions) {
        int size = subscriptions.size();
        if (size <= 0) {
            return Collections.emptyList();
        }

        List<NotificationAndSubscription> notifications = new ArrayList<NotificationAndSubscription>(size);
        for (Subscription subscription : subscriptions) {
            Notification notification = getNotification(event, subscription);
            if (null != notification) {
                notifications.add(new NotificationAndSubscription(notification, subscription));
            }
        }
        return notifications;
    }

    private Notification getNotification(DriveEvent event, Subscription subscription) {
        String pushTokenReference = event.getPushTokenReference();
        if (null != pushTokenReference && subscription.matches(pushTokenReference)) {
            return null;
        }

        return new ApnsHttp2Notification.Builder(subscription.getToken())
            .withCustomAlertLocKey("TRIGGER_SYNC")
            .withCustomAlertActionLocKey("OK")
            .withCustomField("root", subscription.getRootFolderID())
            .withCustomField("action", "sync")
            .build();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class NotificationAndSubscription {

        final Notification notification;
        final Subscription subscription;

        NotificationAndSubscription(Notification notification, Subscription subscription) {
            super();
            this.notification = notification;
            this.subscription = subscription;
        }
    }

    private static class NotificationResponseAndSubscription {

        final NotificationResponse notificationResponse;
        final Subscription subscription;

        NotificationResponseAndSubscription(NotificationResponse notificationResponse, Subscription subscription) {
            super();
            this.notificationResponse = notificationResponse;
            this.subscription = subscription;
        }
    }

}
