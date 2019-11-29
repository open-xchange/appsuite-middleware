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

package com.openexchange.drive.events.apn2.util;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.Subscription;
import com.openexchange.exception.OXException;
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
                if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
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

    private boolean removeSubscription(Subscription subscription, DriveSubscriptionStore subscriptionStore) {
        try {
            return subscriptionStore.removeSubscription(subscription);
        } catch (OXException e) {
            LOG.error("Error removing subscription", e);
        }
        return false;
    }

}
