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

import com.openexchange.drive.events.subscribe.Subscription;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.concurrent.PushNotificationFuture;

/**
 * {@link NotificationResponseAndSubscription}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.4
 */
public class NotificationResponseAndSubscription {

    private final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture;
    private final Subscription subscription;

    /**
     * Initializes a new {@link NotificationResponseAndSubscription}.
     *
     * @param sendNotificationFuture The result of the push operation
     * @param subscription The subscription related to the push operation
     */
    public NotificationResponseAndSubscription(PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture, Subscription subscription) {
        super();
        this.sendNotificationFuture = sendNotificationFuture;
        this.subscription = subscription;
    }

    /**
     * Gets the {@link PushNotificationFuture}
     *
     * @return The PushNotificationFuture
     */
    public PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> getSendNotificationFuture() {
        return sendNotificationFuture;
    }

    /**
     * Gets the {@link Subscription}
     *
     * @return The subscription
     */
    public Subscription getSubscription() {
        return subscription;
    }

}
