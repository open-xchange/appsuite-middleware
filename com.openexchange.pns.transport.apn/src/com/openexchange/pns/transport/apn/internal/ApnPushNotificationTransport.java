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

package com.openexchange.pns.transport.apn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushNotification;
import com.openexchange.pns.PushNotificationField;
import com.openexchange.pns.PushNotificationTransport;
import com.openexchange.pns.PushNotifications;
import com.openexchange.pns.PushSubscription;
import com.openexchange.pns.PushSubscriptionDescription;
import com.openexchange.pns.PushSubscriptionRegistry;
import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.json.JSONException;
import javapns.notification.NewsstandNotificationPayload;
import javapns.notification.Payload;
import javapns.notification.PayloadPerDevice;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.notification.PushedNotifications;

/**
 * {@link ApnPushNotificationTransport}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ApnPushNotificationTransport implements PushNotificationTransport {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ApnPushNotificationTransport.class);

    private static final String ID = "apn";

    // https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/CommunicatingWIthAPS.html
    private static final int STATUS_INVALID_TOKEN_SIZE = 5;

    private static final int STATUS_INVALID_TOKEN = 8;

    private static final int MAX_PAYLOAD_SIZE = 256;

    // ---------------------------------------------------------------------------------------------------------------

    private final ApnOptions options;
    private final PushSubscriptionRegistry subscriptionRegistry;

    /**
     * Initializes a new {@link ApnPushNotificationTransport}.
     */
    public ApnPushNotificationTransport(ApnOptions options, PushSubscriptionRegistry subscriptionRegistry) {
        super();
        this.options = options;
        this.subscriptionRegistry = subscriptionRegistry;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void transport(PushNotification notification, Collection<PushSubscription> subscriptions) throws OXException {
        if (null != subscriptions && 0 < subscriptions.size()) {
            // Create payloads for each subscription
            List<PayloadPerDevice> payloads = getPayloads(notification, subscriptions);

            // Transport them
            if (!payloads.isEmpty()) {
                PushedNotifications notifications = null;
                try {
                    notifications = Push.payloads(options.getKeystore(), options.getPassword(), options.isProduction(), payloads);
                } catch (CommunicationException e) {
                    LOG.warn("error submitting push notifications", e);
                } catch (KeystoreException e) {
                    LOG.warn("error submitting push notifications", e);
                }

                processNotificationResults(notification, notifications);
            }
        }
    }

    private void processNotificationResults(PushNotification notification, PushedNotifications pushedNotifications) {
        if (null != pushedNotifications && !pushedNotifications.isEmpty()) {
            for (PushedNotification pushedNotification : pushedNotifications) {
                if (pushedNotification.isSuccessful()) {
                    LOG.debug("{}", pushedNotification);
                } else {
                    LOG.warn("Unsuccessful push notification: {}", pushedNotification);
                    if (null != pushedNotification.getResponse()) {
                        int status = pushedNotification.getResponse().getStatus();
                        if (STATUS_INVALID_TOKEN == status || STATUS_INVALID_TOKEN_SIZE == status) {
                            Device device = pushedNotification.getDevice();
                            boolean removed = removeSubscription(notification, device);
                            if (removed) {
                                LOG.info("Removed subscription for device with token: {}.", device.getToken());
                            }
                            LOG.debug("Could not remove subscriptions for device with token: {}.", device.getToken());
                        }
                    }
                }
            }
        }
    }

    private List<PayloadPerDevice> getPayloads(PushNotification notification, Collection<PushSubscription> subscriptions) throws OXException {
        try {
            Payload payload;
            if (false == PushNotifications.isRefresh(notification)) {
                payload = constructPayload(notification);
                int payloadLength = PushNotifications.getPayloadLength(payload.toString());
                // Check payload length
                if (payloadLength > MAX_PAYLOAD_SIZE) {
                    int bytesToCut = payloadLength - MAX_PAYLOAD_SIZE;
                    PushNotifications.cutNotification(notification, bytesToCut);
                    payload = constructPayload(notification);
                }
            } else {
                payload = NewsstandNotificationPayload.contentAvailable();
                payload.addCustomDictionary("message", "refresh");
                payload.addCustomDictionary("SYNC_EVENT", "MAIL");
            }

            List<PayloadPerDevice> payloads = new ArrayList<PayloadPerDevice>(subscriptions.size());
            for (PushSubscription subscription : subscriptions) {
                try {
                    payloads.add(new PayloadPerDevice(payload, subscription.getToken()));
                } catch (InvalidDeviceTokenFormatException e) {
                    LOG.warn("Invalid device token: '{}', removing from subscription store.", subscription.getToken(), e);
                    try {
                        subscriptionRegistry.unregisterSubscription(PushSubscriptionDescription.instanceFor(subscription));
                    } catch (OXException x) {
                        LOG.error("Failed to remove subscription for invalid token {}", subscription.getToken(), x);
                    }
                }
            }
            return payloads;
        } catch (JSONException e) {
            throw PushExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private PushNotificationPayload constructPayload(PushNotification notification) throws JSONException {
        switch (notification.getAffiliation()) {
            case MAIL:
                return constructMailPayload(notification);
            default:
                break;
        }
        return null;
    }

    private PushNotificationPayload constructMailPayload(PushNotification notification) throws JSONException {
        PushNotificationPayload payload = new PushNotificationPayload();
        payload.addSound("beep.wav");

        String subject = PushNotifications.getValueFor(PushNotificationField.MAIL_SUBJECT, notification);
        String sender = PushNotifications.getValueFor(PushNotificationField.MAIL_SENDER, notification);
        String path = PushNotifications.getValueFor(PushNotificationField.MAIL_PATH, notification);
        Integer unread = PushNotifications.getValueFor(PushNotificationField.MAIL_UNREAD, notification);

        payload.addAlert(new StringBuilder(sender).append("\n").append(subject).toString());
        if (null != unread) {
            payload.addBadge(unread.intValue());
        }
        payload.addCustomDictionary(PushNotificationField.MAIL_PATH.getId(), path);
        return payload;
    }

    /**
     * Queries the feedback service and processes the received results, removing reported tokens from the subscription store if needed.
     */
    public void queryFeedbackService() {
        LOG.info("Querying APN feedback service for 'apn'...");
        long start = System.currentTimeMillis();

        List<Device> devices = null;
        try {
            devices = Push.feedback(options.getKeystore(), options.getPassword(), options.isProduction());
        } catch (Exception e) {
            LOG.warn("error querying feedback service", e);
        }

        if (null != devices && !devices.isEmpty()) {
            for (Device device : devices) {
                LOG.debug("Got feedback for device with token: {}, last registered: {}", device.getToken(), device.getLastRegister());
                int numRemoved = removeSubscriptions(device);
                LOG.info("Removed {} subscriptions for device with token: {}.", numRemoved, device.getToken());
            }
        } else {
            LOG.info("No devices to unregister received from feedback service.");
        }

        LOG.info("Finished processing APN feedback for 'apn' after {} ms.", (System.currentTimeMillis() - start));
    }

    private int removeSubscriptions(Device device) {
        if (null == device || null == device.getToken()) {
            LOG.warn("Unsufficient device information to remove subscriptions for: {}", device);
            return 0;
        }

        try {
            return subscriptionRegistry.unregisterSubscription(device.getToken(), ID);
        } catch (OXException e) {
            LOG.error("Error removing subscription", e);
        }
        return 0;
    }

    private boolean removeSubscription(PushNotification notification, Device device) {
        if (null == device || null == device.getToken() || null == device.getLastRegister()) {
            LOG.warn("Unsufficient device information to remove subscriptions for: {}", device);
            return false;
        }

        try {
            PushSubscriptionDescription subscriptionDesc = new PushSubscriptionDescription();
            subscriptionDesc.setAffiliation(notification.getAffiliation());
            subscriptionDesc.setContextId(notification.getContextId());
            subscriptionDesc.setToken(device.getToken());
            subscriptionDesc.setUserId(notification.getUserId());
            return subscriptionRegistry.unregisterSubscription(subscriptionDesc);
        } catch (OXException e) {
            LOG.error("Error removing subscription", e);
        }
        return false;
    }

}
