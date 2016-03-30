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
 *    trademarks of the OX Software GmbH group of companies.
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

import java.util.ArrayList;
import java.util.List;
import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import javapns.notification.PayloadPerDevice;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.notification.PushedNotifications;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.DriveEventPublisher;
import com.openexchange.drive.events.apn.APNAccess;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.Subscription;
import com.openexchange.exception.OXException;

/**
 * {@link APNDriveEventPublisher}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class APNDriveEventPublisher implements DriveEventPublisher {

    // https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/CommunicatingWIthAPS.html
    private final int STATUS_INVALID_TOKEN_SIZE = 5;
    private final int STATUS_INVALID_TOKEN = 8;

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(APNDriveEventPublisher.class);

    /**
     * Initializes a new {@link APNDriveEventPublisher}.
     */
    public APNDriveEventPublisher() {
        super();
    }

    protected abstract String getServiceID();

    protected abstract APNAccess getAccess() throws OXException;

    @Override
    public void publish(DriveEvent event) {
        List<Subscription> subscriptions = null;
        try {
            subscriptions = Services.getService(DriveSubscriptionStore.class, true).getSubscriptions(
                event.getContextID(), new String[] { getServiceID() }, event.getFolderIDs());
        } catch (OXException e) {
            LOG.error("unable to get subscriptions for service {}", getServiceID(), e);
        }
        if (null != subscriptions && 0 < subscriptions.size()) {
            List<PayloadPerDevice> payloads = getPayloads(event, subscriptions);
            if (0 < payloads.size()) {
                PushedNotifications notifications = null;
                try {
                    APNAccess access = getAccess();
                    notifications = Push.payloads(access.getKeystore(), access.getPassword(), access.isProduction(), payloads);
                } catch (CommunicationException e) {
                    LOG.warn("error submitting push notifications", e);
                } catch (KeystoreException e) {
                    LOG.warn("error submitting push notifications", e);
                } catch (OXException e) {
                    LOG.warn("error submitting push notifications", e);
                }
                processNotificationResults(notifications);
            }
        }
    }

    private void processNotificationResults(PushedNotifications notifications) {
        if (null != notifications && 0 < notifications.size()) {
            for (PushedNotification notification : notifications) {
                if (notification.isSuccessful()) {
                    LOG.debug("{}", notification);
                } else {
                    LOG.warn("Unsuccessful push notification: {}", notification);
                    if (null != notification.getResponse()) {
                        int status = notification.getResponse().getStatus();
                        if (STATUS_INVALID_TOKEN == status || STATUS_INVALID_TOKEN_SIZE == status) {
                            Device device = notification.getDevice();
                            int removed = removeSubscriptions(device);
                            LOG.info("Removed {} subscriptions for device with token: {}.", removed, device.getToken());
                        }
                    }
                }
            }
        }
    }

    /**
     * Queries the feedback service and processes the received results, removing reported tokens from the subscription store if needed.
     */
    public void queryFeedbackService() {
        LOG.info("Querying APN feedback service for '{}'...", getServiceID());
        long start = System.currentTimeMillis();
        List<Device> devices = null;
        try {
            APNAccess access = getAccess();
            devices = Push.feedback(access.getKeystore(), access.getPassword(), access.isProduction());
        } catch (CommunicationException e) {
            LOG.warn("error querying feedback service", e);
        } catch (KeystoreException e) {
            LOG.warn("error querying feedback service", e);
        } catch (OXException e) {
            LOG.warn("error querying feedback service", e);
        }
        if (null != devices && 0 < devices.size()) {
            for (Device device : devices) {
                LOG.debug("Got feedback for device with token: {}, last registered: {}", device.getToken(), device.getLastRegister());
                int removed = removeSubscriptions(device);
                LOG.info("Removed {} subscriptions for device with token: {}.", removed, device.getToken());
            }
        } else {
            LOG.debug("No devices to unregister received from feedback service.");
        }
        LOG.info("Finished processing APN feedback for ''{}'' after {} ms.", getServiceID(), (System.currentTimeMillis() - start));
    }

    public void notifySilently() {
        LOG.info("Sending silent push notifications for '{}'...", getServiceID());
        long start = System.currentTimeMillis();
        try {
            List<Subscription> subscriptions = Services.getService(DriveSubscriptionStore.class, true).getSubscriptions(getServiceID());
            if (null != subscriptions && 0 < subscriptions.size()) {
                List<PayloadPerDevice> payloads = getSilentNotificationPayloads(subscriptions);
                if (0 < payloads.size()) {
                    APNAccess access = getAccess();
                    PushedNotifications notifications = Push.payloads(access.getKeystore(), access.getPassword(), access.isProduction(), payloads);
                    processNotificationResults(notifications);
                }
            }
        } catch (CommunicationException e) {
            LOG.warn("error sending silent push notifications", e);
        } catch (KeystoreException e) {
            LOG.warn("error sending silent push notifications", e);
        } catch (OXException e) {
            LOG.warn("error sending silent push notifications", e);
        }
        LOG.info("Finished sending silent push notifications for ''{}'' after {} ms.", getServiceID(), (System.currentTimeMillis() - start));
    }

    private List<PayloadPerDevice> getSilentNotificationPayloads(List<Subscription> subscriptions) {
        List<PayloadPerDevice> payloads = new ArrayList<PayloadPerDevice>(subscriptions.size());
        for (Subscription subscription : subscriptions) {
            try {
                PushNotificationPayload payload = new PushNotificationPayload();
                payload.addCustomDictionary("root", subscription.getRootFolderID());
                JSONObject apsObject = payload.getPayload().getJSONObject("aps");
                if (null == apsObject) {
                    apsObject = new JSONObject();
                    payload.getPayload().put("aps", apsObject);
                }
                apsObject.put("content-available", 1);
                payloads.add(new PayloadPerDevice(payload, subscription.getToken()));
            } catch (JSONException e) {
                LOG.warn("error constructing payload", e);
            } catch (InvalidDeviceTokenFormatException e) {
                LOG.warn("Invalid device token: '{}', removing from subscription store.", subscription.getToken(), e);
                removeSubscription(subscription);
            }
        }
        return payloads;
    }

    private List<PayloadPerDevice> getPayloads(DriveEvent event, List<Subscription> subscriptions) {
        String pushTokenReference = event.getPushTokenReference();
        List<PayloadPerDevice> payloads = new ArrayList<PayloadPerDevice>(subscriptions.size());
        for (Subscription subscription : subscriptions) {
            if (null != pushTokenReference && subscription.matches(pushTokenReference)) {
                LOG.trace("Skipping push notification for subscription: {}", subscription);
                continue;
            }
            try {
                PushNotificationPayload payload = new PushNotificationPayload();
                payload.addCustomAlertLocKey("TRIGGER_SYNC");
                payload.addCustomAlertActionLocKey("OK");
                payload.addCustomDictionary("root", subscription.getRootFolderID());
                payload.addCustomDictionary("action", "sync");
                payloads.add(new PayloadPerDevice(payload, subscription.getToken()));
            } catch (JSONException e) {
                LOG.warn("error constructing payload", e);
            } catch (InvalidDeviceTokenFormatException e) {
                LOG.warn("Invalid device token: '{}', removing from subscription store.", subscription.getToken(), e);
                removeSubscription(subscription);
            }
        }
        return payloads;
    }

    private boolean removeSubscription(Subscription subscription) {
        try {
            return Services.getService(DriveSubscriptionStore.class, true).removeSubscription(subscription);
        } catch (OXException e) {
            LOG.error("Error removing subscription", e);
        }
        return false;
    }

    private int removeSubscriptions(Device device) {
        if (null != device && null != device.getToken() && null != device.getLastRegister()) {
            try {
                return Services.getService(DriveSubscriptionStore.class, true).removeSubscriptions(
                    getServiceID(), device.getToken(), device.getLastRegister().getTime());
            } catch (OXException e) {
                LOG.error("Error removing subscription", e);
            }
        } else {
            LOG.warn("Unsufficient device information to remove subscriptions for: {}", device);
        }
        return 0;
    }

    @Override
    public boolean isLocalOnly() {
        return true;
    }

}
