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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.List;
import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.json.JSONException;
import javapns.notification.PayloadPerDevice;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.notification.PushedNotifications;
import org.apache.commons.logging.Log;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.DriveEventPublisher;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.Subscription;
import com.openexchange.exception.OXException;

/**
 * {@link APNDriveEventPublisher}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class APNDriveEventPublisher implements DriveEventPublisher {

    private static final String SERIVCE_ID = "apn";
    private static final Log LOG = com.openexchange.log.Log.loggerFor(APNDriveEventPublisher.class);

    private final APNAccess access;

    public APNDriveEventPublisher(APNAccess access) {
        super();
        this.access = access;
    }

    @Override
    public void publish(DriveEvent event) {
        List<Subscription> subscriptions = null;
        try {
            subscriptions = Services.getService(DriveSubscriptionStore.class, true).getSubscriptions(
                event.getContextID(), SERIVCE_ID, event.getFolderIDs());
        } catch (OXException e) {
            LOG.error("unable to get subscriptions for service " + SERIVCE_ID, e);
        }
        if (null != subscriptions && 0 < subscriptions.size()) {
            List<PayloadPerDevice> payloads = getPayloads(event, subscriptions);
            PushedNotifications notifications = null;
            try {
                notifications = Push.payloads(access.getKeystore(), access.getPassword(), access.isProduction(), payloads);
            } catch (CommunicationException e) {
                LOG.warn("error submitting push notifications", e);
            } catch (KeystoreException e) {
                LOG.warn("error submitting push notifications", e);
            }
            if (LOG.isDebugEnabled() && null != notifications) {
                for (PushedNotification notification : notifications) {
                    LOG.debug(notification);
                }
            }
        }
    }

    private static List<PayloadPerDevice> getPayloads(DriveEvent event, List<Subscription> subscriptions) {
        List<PayloadPerDevice> payloads = new ArrayList<PayloadPerDevice>(subscriptions.size());
        for (Subscription subscription : subscriptions) {
            try {
                PushNotificationPayload payload = new PushNotificationPayload();
                payload.addCustomAlertLocKey("TRIGGER_SYNC");
                payload.addCustomAlertActionLocKey("OK");
                payload.addCustomDictionary("root", subscription.getRootFolderID());
                payload.addCustomDictionary("action", "sync");
//                payload.addCustomDictionary("folders", event.getFolderIDs().toString());
                payloads.add(new PayloadPerDevice(payload, subscription.getToken()));
            } catch (JSONException e) {
                LOG.warn("error constructing payload", e);
            } catch (InvalidDeviceTokenFormatException e) {
                LOG.warn("error constructing payload", e);
            }
        }
        return payloads;
    }

    @Override
    public boolean isLocalOnly() {
        return true;
    }

}
