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

package com.openexchange.drive.events.apn.internal;

import java.util.concurrent.TimeUnit;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.apn2.util.ApnsHttp2Notification;
import com.openexchange.drive.events.apn2.util.ApnsHttp2Options;
import com.openexchange.drive.events.apn2.util.SubscriptionDeliveryTask;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.Subscription;
import com.turo.pushy.apns.DeliveryPriority;
import com.turo.pushy.apns.PushType;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;


/**
 * {@link APNSubscriptionDeliveryTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class APNSubscriptionDeliveryTask extends SubscriptionDeliveryTask {

    /**
     * Initializes a new {@link APNSubscriptionDeliveryTask}.
     *
     * @param subscription The subscription
     * @param event The drive event
     * @param options The HTTP/2 options
     * @param subscriptionStore The subscription store
     */
    public APNSubscriptionDeliveryTask(Subscription subscription, DriveEvent event, ApnsHttp2Options options, DriveSubscriptionStore subscriptionStore) {
        super(subscription, event, options, subscriptionStore);
    }

    @Override
    protected SimpleApnsPushNotification getNotification(DriveEvent event, Subscription subscription, ApnsHttp2Options options) {
        String pushTokenReference = event.getPushTokenReference();
        if (null != pushTokenReference && subscription.matches(pushTokenReference)) {
            return null;
        }
        return new ApnsHttp2Notification.Builder(subscription.getToken(), options.getTopic())
            .withCustomField("root", subscription.getRootFolderID())
            .withCustomField("action", "sync")
            .withContentAvailable(true)
            .withExpiration(TimeUnit.DAYS.toMillis(1L))
            .withPriority(DeliveryPriority.CONSERVE_POWER)
            .withPushType(PushType.BACKGROUND)
            .build();
    }

}
