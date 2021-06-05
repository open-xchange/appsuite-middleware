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

package com.openexchange.drive.events.apn2.util;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.Subscription;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.AbstractTask;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.concurrent.PushNotificationFuture;

/**
 * {@link SubscriptionDeliveryTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.4
 */
public abstract class SubscriptionDeliveryTask extends AbstractTask<Void> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SubscriptionDeliveryTask.class);

    private final Subscription subscription;
    private final DriveEvent event;
    private final ApnsHttp2Options options;
    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link ApnsHttp2DriveEventPublisher.SubscriptionDeliveryTask}.
     *
     * @param subscription The subscription
     * @param event The drive event
     * @param options The HTTP/2 options
     * @param services Service lookup
     */
    protected SubscriptionDeliveryTask(Subscription subscription, DriveEvent event, ApnsHttp2Options options, ServiceLookup services) {
        super();
        this.subscription = subscription;
        this.event = event;
        this.options = options;
        this.serviceLookup = services;
    }

    /**
     * Constructs a {@link SimpleApnsPushNotification} based on given {@link DriveEvent}, {@link Subscription} and {@link ApnsHttp2Options} 
     *
     * @param event The drive event
     * @param subscription The subscription
     * @param options The HTTP/2 options
     * @return The {@link SimpleApnsPushNotification} to send to Apple's push servers
     */
    protected abstract SimpleApnsPushNotification getNotification(DriveEvent event, Subscription subscription, ApnsHttp2Options options);

    @Override
    public Void call() throws Exception {
        SimpleApnsPushNotification notification = getNotification(event, subscription, options);
        if (null == notification) {
            return null;
        }

        Optional<ApnsClient> client = ApnsHttp2Utility.getClient(options);
        if (!client.isPresent()) {
            // Nothing to do
            return null;
        }

        // Push the notification & remember response
        NotificationResponseAndSubscription response = new NotificationResponseAndSubscription(client.get().sendNotification(notification), subscription);
        processNotificationResponse(response, serviceLookup);
        return null;
    }

    private void processNotificationResponse(NotificationResponseAndSubscription notificationResponseAndSubscription, ServiceLookup services) throws InterruptedException {
        PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture = notificationResponseAndSubscription.getSendNotificationFuture();
        Subscription subscription = notificationResponseAndSubscription.getSubscription();
        try {
            PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse = sendNotificationFuture.get();
            if (pushNotificationResponse.isAccepted()) {
                LOG.debug("Push notification for drive event accepted by APNs gateway for device token: {}", subscription.getToken());
            } else {
                if (pushNotificationResponse.getTokenInvalidationTimestamp() != null || isInvalidToken(pushNotificationResponse.getRejectionReason())) {
                    LOG.warn("Unsuccessful notification for drive event due to inactive or invalid device token: {}", subscription.getToken());
                    removeSubscription(subscription, services.getService(DriveSubscriptionStore.class));
                } else {
                    LOG.warn("Unsuccessful notification for drive event for device token {}: {}", subscription.getToken(), pushNotificationResponse.getRejectionReason());
                }
            }
        } catch (ExecutionException e) {
            LOG.warn("Failed to send push notification for drive event for device token {}", subscription.getToken(), e.getCause());
        }
    }

    // Checks if rejectionReason is because of invalid token
    // see section 'Understand Error Codes' https://developer.apple.com/documentation/usernotifications/setting_up_a_remote_notification_server/handling_notification_responses_from_apns
    private boolean isInvalidToken(String rejectionReason) {
        if (Strings.isEmpty(rejectionReason)) {
            return false;
        }
        return "BadDeviceToken".equals(rejectionReason) || "Unregistered".equals(rejectionReason);
    }

    private boolean removeSubscription(Subscription subscription, DriveSubscriptionStore subscriptionStore) {
        try {
            return subscriptionStore.removeSubscription(subscription);
        } catch (OXException e) {
            LOG.error("Error removing subscription", e);
        }
        return false;
    }

}
