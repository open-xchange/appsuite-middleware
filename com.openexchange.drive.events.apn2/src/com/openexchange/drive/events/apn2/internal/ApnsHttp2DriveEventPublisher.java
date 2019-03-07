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

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.DriveEventPublisher;
import com.openexchange.drive.events.apn2.ApnsHttp2Options;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.Subscription;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.concurrent.PushNotificationFuture;


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
     * Gets the APNS HTTP/2 options to use for specified context and user.
     *
     * @return The options
     * @throws OXException If options cannot be returned
     */
    protected abstract ApnsHttp2Options getOptions(int contextId, int userId) throws OXException;

    /**
     * Gets the APNS HTTP/2 client for specified options.
     *
     * @param options The APNS HTTP/2 options
     * @return The appropriate client
     */
    private ApnsClient getClient(ApnsHttp2Options options) {
        try {
            return ApnsHttp2Utility.getApnsClient(options);
        } catch (OXException e) {
            LoggerHolder.LOG.error("Unable to create APNS HTTP/2 client for service {}", getServiceID(), e);
        }
        return null;
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

        int numOfSubscriptions = subscriptions.size();
        if (numOfSubscriptions == 0) {
            // Nothing to do
            return;
        }

        // Send push notification & handle response
        ThreadPoolService threadPool = services.getService(ThreadPoolService.class);
        if (null == threadPool || numOfSubscriptions == 1) {
            for (Subscription subscription : subscriptions) {

                // Get the APNS HTTP/2 options to use
                ApnsHttp2Options options;
                try {
                    options = getOptions(subscription.getContextID(), subscription.getUserID());
                    if (options == null) {
                        LoggerHolder.LOG.error("unable to get APNS HTTP/2 options for service {}", getServiceID());
                        return;
                    }
                } catch (OXException e) {
                    LoggerHolder.LOG.error("unable to get APNS HTTP/2 options for service {}", getServiceID(), e);
                    return;
                }

                // Get the APNS HTTP/2 client to use
                ApnsClient client = getClient(options);
                if (null == client) {
                    // Nothing to do
                    return;
                }

                Task<Void> task = new SubscriptionDeliveryTask(subscription, event, options, client, services);
                try {
                    ThreadPools.execute(task);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LoggerHolder.LOG.warn("Interrupted while sending push notification for drive event for device token {}", subscription.getToken(), e);
                    return;
                } catch (Exception e) {
                    LoggerHolder.LOG.warn("Failed sending push notification for drive event to device with token {}", subscription.getToken(), e);
                }
            }
        } else {
            for (Subscription subscription : subscriptions) {

                // Get the APNS HTTP/2 options to use
                ApnsHttp2Options options;
                try {
                    options = getOptions(subscription.getContextID(), subscription.getUserID());
                } catch (OXException e) {
                    LoggerHolder.LOG.error("unable to get APNS HTTP/2 options for service {}", getServiceID(), e);
                    return;
                }

                // Get the APNS HTTP/2 client to use
                ApnsClient client = getClient(options);
                if (null == client) {
                    // Nothing to do
                    return;
                }

                Task<Void> task = new SubscriptionDeliveryTask(subscription, event, options, client, services);
                threadPool.submit(task, CallerRunsBehavior.getInstance());
            }
        }
    }

    static void processNotificationResponse(NotificationResponseAndSubscription notificationResponseAndSubscription, ServiceLookup services) throws InterruptedException {
        PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture = notificationResponseAndSubscription.sendNotificationFuture;
        Subscription subscription = notificationResponseAndSubscription.subscription;
        try {
            PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse = sendNotificationFuture.get();
            if (pushNotificationResponse.isAccepted()) {
                LoggerHolder.LOG.debug("Push notification for drive event accepted by APNs gateway for device token: {}", subscription.getToken());
            } else {
                if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
                    LoggerHolder.LOG.warn("Unsuccessful notification for drive event due to inactive or invalid device token: {}", subscription.getToken());
                    removeSubscription(subscription, services.getService(DriveSubscriptionStore.class));
                } else {
                    LoggerHolder.LOG.warn("Unsuccessful notification for drive event for device token {}: {}", subscription.getToken(), pushNotificationResponse.getRejectionReason());
                }
            }
        } catch (ExecutionException e) {
            LoggerHolder.LOG.warn("Failed to send push notification for drive event for device token {}", subscription.getToken(), e.getCause());
        }
    }

    static SimpleApnsPushNotification getNotification(DriveEvent event, Subscription subscription, ApnsHttp2Options options) {
        String pushTokenReference = event.getPushTokenReference();
        if (null != pushTokenReference && subscription.matches(pushTokenReference)) {
            return null;
        }

        return new ApnsHttp2Notification.Builder(subscription.getToken(), options.getTopic())
            .withCustomField("container-identifier", "NSFileProviderRootContainerItemIdentifier")
            .withExpiration(TimeUnit.HOURS.toMillis(1L))
        .build();
    }

    static boolean removeSubscription(Subscription subscription, DriveSubscriptionStore subscriptionStore) {
        try {
            return subscriptionStore.removeSubscription(subscription);
        } catch (OXException e) {
            LoggerHolder.LOG.error("Error removing subscription", e);
        }
        return false;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class NotificationResponseAndSubscription {

        final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture;
        final Subscription subscription;

        NotificationResponseAndSubscription(PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture, Subscription subscription) {
            super();
            this.sendNotificationFuture = sendNotificationFuture;
            this.subscription = subscription;
        }
    }

    private static class SubscriptionDeliveryTask extends AbstractTask<Void> {

        private final Subscription subscription;
        private final DriveEvent event;
        private final ApnsHttp2Options options;
        private final ApnsClient client;
        private final ServiceLookup serviceLookup;

        /**
         * Initializes a new {@link ApnsHttp2DriveEventPublisher.SubscriptionDeliveryTask}.
         */
        SubscriptionDeliveryTask(Subscription subscription, DriveEvent event, ApnsHttp2Options options, ApnsClient client, ServiceLookup services) {
            super();
            this.subscription = subscription;
            this.event = event;
            this.options = options;
            this.client = client;
            this.serviceLookup = services;
        }

        @Override
        public Void call() throws Exception {
            SimpleApnsPushNotification notification = getNotification(event, subscription, options);
            if (null == notification) {
                return null;
            }

            // Push the notification & remember response
            NotificationResponseAndSubscription response = new NotificationResponseAndSubscription(client.sendNotification(notification), subscription);
            processNotificationResponse(response, serviceLookup);
            return null;
        }
    }

}
