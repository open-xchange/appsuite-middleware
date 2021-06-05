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

package com.openexchange.drive.events.apn2.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.openexchange.drive.events.DriveContentChange;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.apn2.util.ApnsHttp2Notification;
import com.openexchange.drive.events.apn2.util.ApnsHttp2Options;
import com.openexchange.drive.events.apn2.util.SubscriptionDeliveryTask;
import com.openexchange.drive.events.subscribe.Subscription;
import com.openexchange.drive.events.subscribe.SubscriptionMode;
import com.openexchange.server.ServiceLookup;
import com.turo.pushy.apns.DeliveryPriority;
import com.turo.pushy.apns.PushType;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;

/**
 * {@link ApnsSubscriptionDeliveryTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.4
 */
public class ApnsSubscriptionDeliveryTask extends SubscriptionDeliveryTask {

    /**
     * Initializes a new {@link ApnsSubscriptionDeliveryTask}.
     *
     * @param subscription The subscription
     * @param event The drive event
     * @param options The HTTP/2 options
     * @param services Service lookup
     */
    public ApnsSubscriptionDeliveryTask(Subscription subscription, DriveEvent event, ApnsHttp2Options options, ServiceLookup services) {
        super(subscription, event, options, services);
    }

    @Override
    protected SimpleApnsPushNotification getNotification(DriveEvent event, Subscription subscription, ApnsHttp2Options options) {
        String pushTokenReference = event.getPushTokenReference();
        if (null != pushTokenReference && subscription.matches(pushTokenReference)) {
            return null;
        }
        if (null == options) {
            return null;
        }
        ApnsHttp2Notification.Builder builder = new ApnsHttp2Notification.Builder(subscription.getToken(), options.getTopic())
            .withCustomField("container-identifier", "NSFileProviderRootContainerItemIdentifier")
            .withExpiration(TimeUnit.DAYS.toMillis(1L))
        ;
        if (event.isContentChangesOnly() && SubscriptionMode.SEPARATE.equals(subscription.getMode())) {
            List<String> folderIds = new ArrayList<String>();
            for (DriveContentChange contentChange : event.getContentChanges()) {
                if (contentChange.isSubfolderOf(subscription.getRootFolderID())) {
                    folderIds.add(contentChange.getFolderId());
                }
            }
            if (false == folderIds.isEmpty()) {
                builder.withCustomField("folderIds", folderIds);
            }
        }
        builder.withPriority(DeliveryPriority.CONSERVE_POWER);
        builder.withPushType(PushType.FILEPROVIDER);
        return builder.build();
    }

}
