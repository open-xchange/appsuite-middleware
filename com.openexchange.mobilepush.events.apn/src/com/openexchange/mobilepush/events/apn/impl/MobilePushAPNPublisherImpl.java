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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mobilepush.events.apn.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.json.JSONException;
import javapns.notification.PayloadPerDevice;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.notification.PushedNotifications;
import com.openexchange.exception.OXException;
import com.openexchange.mobilepush.events.MobilePushEvent;
import com.openexchange.mobilepush.events.MobilePushPublisher;
import com.openexchange.mobilepush.events.apn.APNAccess;
import com.openexchange.mobilepush.events.apn.osgi.Services;
import com.openexchange.mobilepush.events.storage.ContextUsers;
import com.openexchange.mobilepush.events.storage.MobilePushStorageService;
import com.openexchange.mobilepush.events.storage.PushUtility;
import com.openexchange.mobilepush.events.storage.Subscription;

/**
 * {@link MobilePushAPNPublisherImpl}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class MobilePushAPNPublisherImpl implements MobilePushPublisher {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MobilePushAPNPublisherImpl.class);

    // https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/CommunicatingWIthAPS.html
    private static final int STATUS_INVALID_TOKEN_SIZE = 5;

    private static final int STATUS_INVALID_TOKEN = 8;

    private static final String SERVICE_ID = "apn";

    private final APNAccess apnAccess;
    /**
     * Initializes a new {@link MobilePushAPNPublisherImpl}.
     */
    public MobilePushAPNPublisherImpl(APNAccess apnAccess) {
        super();
        this.apnAccess = apnAccess;
    }

    private APNAccess getAccess() throws OXException {
        return apnAccess;
    }

    @Override
    public void multiPublish(MobilePushEvent loginEvent) {
        List<String> tokens = null;
        try {
            MobilePushStorageService mnss = Services.getService(MobilePushStorageService.class);
            tokens = mnss.getTokens(loginEvent.getContextUsers(), SERVICE_ID, loginEvent.getProvider());
        } catch (OXException e) {
            LOG.debug("Could not get subscription: {}", SERVICE_ID, e);
        }
        if (null != tokens && 0 < tokens.size()) {
            List<PayloadPerDevice> payloads = getPayloadsForLoginRequest(loginEvent, tokens);
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
                processNotificationResults(loginEvent, notifications);
            }
        }
    }

    @Override
    public void publish(MobilePushEvent event) {
        List<Subscription> subscriptions = null;
        try {
            MobilePushStorageService mnss = Services.getService(MobilePushStorageService.class);
            subscriptions = mnss.getSubscriptions(event.getContextId(), event.getUserId(), SERVICE_ID, event.getProvider());
        } catch (OXException e) {
            LOG.debug("Could not get subscription: {}", SERVICE_ID, e);
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
                processNotificationResults(event, notifications);
            }
        }
    }

    private void processNotificationResults(MobilePushEvent event, PushedNotifications notifications) {
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
                            boolean removed = removeSubscriptions(event, device);
                            if (removed) {
                                LOG.info("Removed subscriptions for device with token: {}.", device.getToken());
                            }
                            LOG.debug("Could not remove subscriptions for device with token: {}.", device.getToken());
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
        LOG.info("Querying APN feedback service for '{}'...", "apn");
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
                // int removed = removeSubscriptions(device.getDeviceId());
                // LOG.info("Removed {} subscriptions for device with token: {}.", removed, device.getToken());
            }
        } else {
            LOG.debug("No devices to unregister received from feedback service.");
        }
        LOG.info("Finished processing APN feedback for ''{}'' after {} ms.", "apn", (System.currentTimeMillis() - start));
    }

    private List<PayloadPerDevice> getPayloadsForLoginRequest(MobilePushEvent event, List<String> tokens) {
        List<PayloadPerDevice> payloads = new ArrayList<PayloadPerDevice>(tokens.size());
        for (String token : tokens) {
            try {
                //TODO: notify silently
                PushNotificationPayload payload = new PushNotificationPayload();
                payload.addAlert("LOGIN");
                payloads.add(new PayloadPerDevice(payload, token));
            } catch (JSONException e) {
                LOG.warn("error constructing payload", e);
            } catch (InvalidDeviceTokenFormatException e) {
                LOG.warn("Invalid device token: '{}', removing from subscription store.", token, e);
                removeSubscription(event, token);
            }
        }
        return payloads;
    }

    private List<PayloadPerDevice> getPayloads(MobilePushEvent event, List<Subscription> subscriptions) {
        List<PayloadPerDevice> payloads = new ArrayList<PayloadPerDevice>(subscriptions.size());
        for (Subscription subscription : subscriptions) {
            try {
                PushNotificationPayload payload = new PushNotificationPayload();
                payload.addSound("beep.wav");
                Map<String, String> messageData = event.getMessageData();
                if (messageData.containsKey("subject") && messageData.containsKey("received_from")) {
                    String subject = messageData.get("subject");
                    String receivedFrom = messageData.get("received_from");
                    StringBuffer sb = new StringBuffer(receivedFrom);
                    sb.append("\n");
                    sb.append(subject);
                    payload.addAlert(sb.toString());
                }
                payloads.add(new PayloadPerDevice(payload, subscription.getToken()));
            } catch (JSONException e) {
                LOG.warn("error constructing payload", e);
            } catch (InvalidDeviceTokenFormatException e) {
                LOG.warn("Invalid device token: '{}', removing from subscription store.", subscription.getToken(), e);
                removeSubscription(event, subscription.getToken());
            }
        }
        return payloads;
    }

    private boolean removeSubscription(MobilePushEvent event, String token) {
        try {
            List<ContextUsers> contextUsers = event.getContextUsers();
            if (contextUsers != null && contextUsers.isEmpty()) {
                int contextId = PushUtility.getContextIdForToken(contextUsers, token);
                return Services.getService(MobilePushStorageService.class, true).deleteSubscription(contextId, token, SERVICE_ID);
            } else {
                return Services.getService(MobilePushStorageService.class, true).deleteSubscription(event.getContextId(), token, SERVICE_ID);
            }
        } catch (OXException e) {
            LOG.error("Error removing subscription", e);
        }
        return false;
    }

    private boolean removeSubscriptions(MobilePushEvent event, Device device) {
        if (null != device && null != device.getToken() && null != device.getLastRegister()) {
            try {
                return Services.getService(MobilePushStorageService.class, true).deleteSubscription(event.getContextId(), device.getToken(), SERVICE_ID);
            } catch (OXException e) {
                LOG.error("Error removing subscription", e);
            }
        } else {
            LOG.warn("Unsufficient device information to remove subscriptions for: {}", device);
        }
        return false;
    }
}
