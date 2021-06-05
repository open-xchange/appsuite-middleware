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

package com.openexchange.pns;

import java.util.Collection;
import java.util.Collections;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link PushNotificationService} - The service that handles specified push notifications.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
@SingletonService
public interface PushNotificationService {

    /**
     * Handles the specified notification with high priority.
     * <p>
     * Looks up associated subscriptions and delivers the notification using the appropriate {@link PushNotificationTransport transport}.
     *
     * @param notification The push notification to handle
     * @throws OXException If handling push notification fails
     */
    default void handle(PushNotification notification) throws OXException {
        if (notification == null) {
            return;
        }

        handle(notification, PushPriority.HIGH);
    }

    /**
     * Handles the specified notifications with high priority.
     * <p>
     * Looks up associated subscriptions and delivers the notifications using the appropriate {@link PushNotificationTransport transport}.
     *
     * @param notifications The push notifications to handl
     * @throws OXException If handling push notifications fails
     */
    default void handle(Collection<PushNotification> notifications) throws OXException {
        handle(notifications, PushPriority.HIGH);
    }

    /**
     * Handles the specified notification.
     * <p>
     * Looks up associated subscriptions and delivers the notification using the appropriate {@link PushNotificationTransport transport}.
     *
     * @param notification The push notification to handle
     * @throws OXException If handling push notification fails
     */
    default void handle(PushNotification notification, PushPriority priority) throws OXException {
        if (notification == null) {
            return;
        }

        handle(Collections.singletonList(notification), priority);
    }

    /**
     * Handles the specified notifications.
     * <p>
     * Looks up associated subscriptions and delivers the notifications using the appropriate {@link PushNotificationTransport transport}.
     *
     * @param notifications The push notifications to handle
     * @param priority The priority with which the notifications should be handled
     * @throws OXException If handling push notifications fails
     */
    void handle(Collection<PushNotification> notifications, PushPriority priority) throws OXException;

    // ----------------------------------------------------------------------------------------

    /**
     * Gets the number of buffered notifications that are supposed to be transported.
     *
     * @return The number of buffered notifications
     * @throws OXException If number of buffered notifications cannot be returned
     */
    long getNumberOfBufferedNotifications() throws OXException;

    /**
     * Gets the total number of submitted notifications so far.
     * <p>
     * A notification is in submitted state if fetched from buffer and submitted for being transported, but not yet done.
     *
     * @return The total number of submitted notifications
     * @throws OXException If number of submitted notifications cannot be returned
     */
    long getTotalNumberOfSubmittedNotifications() throws OXException;

    /**
     * Gets the total number of notifications that were processed so far.
     * <p>
     * A notification is in processing state if currently transported
     *
     * @return The total number of processed notifications
     * @throws OXException If number of processing notifications cannot be returned
     */
    long getTotalNumberOfProcessedNotifications() throws OXException;

}
