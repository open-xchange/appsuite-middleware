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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.osgi.Tools.requireService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.DriveEventPublisher;
import com.openexchange.drive.events.apn.APNAccess;
import com.openexchange.drive.events.apn.APNCertificateProvider;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.Subscription;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.strings.TimeSpanParser;
import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.notification.PayloadPerDevice;
import javapns.notification.PushNotificationBigPayload;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.notification.PushedNotifications;

/**
 * {@link APNDriveEventPublisher}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class APNDriveEventPublisher implements DriveEventPublisher {

    /** The logger constant */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(APNDriveEventPublisher.class);

    // https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/CommunicatingWIthAPS.html
    private final int STATUS_INVALID_TOKEN_SIZE = 5;
    private final int STATUS_INVALID_TOKEN = 8;

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final ConcurrentMap<APNAccess, Boolean> initializedFeedbackQueries;
    private final ServiceLookup services;
    private final String serviceId;
    private final Class<? extends APNCertificateProvider> certifcateProviderClass;
    private final Map<String, String> optionals;

    /**
     * Initializes a new {@link APNDriveEventPublisher}.
     *
     * @param services The service lookup reference
     * @param serviceId The service identifier used for push subscriptions, i.e. <code>apn</code> or <code>apn.macos</code>
     * @param type The operation system type, this publisher is responsible for.
     * @param certifcateProviderClass The class under which the fallback APN certificate provider gets registered.
     */
    public APNDriveEventPublisher(ServiceLookup services, String serviceId, OperationSystemType type, Class<? extends APNCertificateProvider> certifcateProviderClass) {
        super();
        this.services = services;
        this.serviceId = serviceId;
        this.certifcateProviderClass = certifcateProviderClass;
        HashMap<String, String> map = new HashMap<>(1);
        map.put(DriveEventsAPNProperty.OPTIONAL_FIELD, type.getName());
        optionals = Collections.unmodifiableMap(map);
        this.initializedFeedbackQueries = new ConcurrentHashMap<>();
    }

    private APNAccess getAccess(int contextId, int userId) {
        LeanConfigurationService configService = services.getService(LeanConfigurationService.class);
        if (null == configService) {
            LOG.warn("Unable to get configuration service to determine push options via {} for user {} in context {}.", serviceId, I(userId), I(contextId));
            return null;
        }
        /*
         * check if push via APN is enabled
         */
        if (false == configService.getBooleanProperty(userId, contextId, DriveEventsAPNProperty.enabled, optionals)) {
            LOG.trace("Push via {} is disabled for user {} in context {}.", serviceId, I(userId), I(contextId));
            return null;
        }
        /*
         * get APN options via config cascade if configured
         */
        String keystore = configService.getProperty(userId, contextId, DriveEventsAPNProperty.keystore, optionals);
        if (Strings.isNotEmpty(keystore)) {
            String password = configService.getProperty(userId, contextId, DriveEventsAPNProperty.password, optionals);
            boolean production = configService.getBooleanProperty(userId, contextId, DriveEventsAPNProperty.production, optionals);
            LOG.trace("Using configured keystore {}, {} service for push via {} for user {} in context {}.", keystore, production ? "production" : "sandbox", serviceId, I(userId), I(contextId));
            return new APNAccess(keystore, password, production);
        }
        /*
         * check for a registered APN options provider as fallback, otherwise
         */
        APNCertificateProvider certificateProvider = services.getOptionalService(certifcateProviderClass);
        if (null != certificateProvider) {
            LOG.trace("Using fallback certificate provider for push via {} for user {} in context {}.", serviceId, I(userId), I(contextId));
            return certificateProvider.getAccess();
        }
        LOG.trace("No configuration for push via {} found for user {} in context {}.", serviceId, I(userId), I(contextId));
        return null;
    }

    @Override
    public void publish(DriveEvent event) {
        /*
         * get subscriptions from storage
         */
        List<Subscription> subscriptions = null;
        try {
            subscriptions = requireService(DriveSubscriptionStore.class, services).getSubscriptions(
                event.getContextID(), new String[] { serviceId }, event.getFolderIDs());
        } catch (OXException e) {
            LOG.error("unable to get subscriptions for service {}", serviceId, e);
        }
        if (null == subscriptions || subscriptions.isEmpty()) {
            return;
        }
        /*
         * build payloads per APN access
         */
        Map<APNAccess, List<PayloadPerDevice>> payloadsPerAccess = new HashMap<APNAccess, List<PayloadPerDevice>>();
        for (Subscription subscription : subscriptions) {
            PayloadPerDevice payload = getPayload(event, subscription);
            if (null == payload) {
                LOG.debug("No payload constructed for subscription {}, skipping", subscription);
                continue;
            }
            APNAccess access = getAccess(subscription.getContextID(), subscription.getUserID());
            if (null == access) {
                LOG.debug("No APN configuration for subscription {}, skipping", subscription);
                continue;
            }
            com.openexchange.tools.arrays.Collections.put(payloadsPerAccess, access, payload);
        }
        /*
         * send payloads per access & setup feedback query task unless already done
         */
        PushedNotifications notifications = new PushedNotifications(subscriptions.size());
        for (Map.Entry<APNAccess, List<PayloadPerDevice>> entry : payloadsPerAccess.entrySet()) {
            APNAccess access = entry.getKey();
            try {
                notifications.addAll(Push.payloads(access.getKeystore(), access.getPassword(), access.isProduction(), entry.getValue()));
                setupFeedbackQueriesIfNeeded(access);
            } catch (CommunicationException e) {
                LOG.warn("error submitting push notifications", e);
            } catch (KeystoreException e) {
                LOG.warn("error submitting push notifications", e);
            }
        }
        /*
         * process notification results afterwards
         */
        processNotificationResults(notifications);
    }

    private void setupFeedbackQueriesIfNeeded(final APNAccess access) {
        Boolean marker = initializedFeedbackQueries.get(access);
        if (null == marker) {
            marker = initializedFeedbackQueries.putIfAbsent(access, Boolean.TRUE);
            if (null == marker) {
                // This thread set the marker and thus is supposed to setup feedback queries
                boolean error = true; // pessimistic
                try {
                    setupFeedbackQueries(access);
                    error = false;
                } catch (OXException e) {
                    LOG.warn("Error setting up feedback queries for {}.", serviceId, e);
                } finally {
                    if (error) {
                        initializedFeedbackQueries.remove(access);
                    }
                }
            }
        }
    }

    private void setupFeedbackQueries(APNAccess access) throws OXException {
        LeanConfigurationService configService = requireService(LeanConfigurationService.class, services);
        String feedbackQueryInterval = configService.getProperty(DriveEventsAPNProperty.feedbackQueryInterval, optionals);
        if (Strings.isNotEmpty(feedbackQueryInterval)) {
            long interval = TimeSpanParser.parseTimespan(feedbackQueryInterval).longValue();
            if (60 * 1000 <= interval) {
                requireService(TimerService.class, services).scheduleWithFixedDelay(new Runnable() {

                    @Override
                    public void run() {
                        queryFeedbackService(access);
                    }
                }, interval, interval);
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
                            LOG.info("Removed {} subscriptions for device with token: {}.", I(removed), device.getToken());
                        }
                    }
                }
            }
        }
    }

    /**
     * Queries the feedback service and processes the received results, removing reported tokens from the subscription store if needed.
     *
     * @param access The underlying APN access to use
     */
    public void queryFeedbackService(APNAccess access) {
        LOG.info("Querying APN feedback service for '{}'...", serviceId);
        long start = System.currentTimeMillis();
        List<Device> devices = null;
        try {
            if (null != access) {
                devices = Push.feedback(access.getKeystore(), access.getPassword(), access.isProduction());
            }
        } catch (CommunicationException e) {
            LOG.warn("error querying feedback service", e);
        } catch (KeystoreException e) {
            LOG.warn("error querying feedback service", e);
        }
        if (null != devices && 0 < devices.size()) {
            for (Device device : devices) {
                LOG.debug("Got feedback for device with token: {}, last registered: {}", device.getToken(), device.getLastRegister());
                int removed = removeSubscriptions(device);
                LOG.info("Removed {} subscriptions for device with token: {}.", I(removed), device.getToken());
            }
        } else {
            LOG.debug("No devices to unregister received from feedback service.");
        }
        LOG.info("Finished processing APN feedback for ''{}'' after {} ms.", serviceId, L((System.currentTimeMillis() - start)));
    }

    private PayloadPerDevice getSilentNotificationPayload(Subscription subscription) {
        try {
            PushNotificationPayload payload = new PushNotificationBigPayload();
            payload.addCustomDictionary("root", subscription.getRootFolderID());
            JSONObject apsObject = payload.getPayload().getJSONObject("aps");
            if (null == apsObject) {
                apsObject = new JSONObject();
                payload.getPayload().put("aps", apsObject);
            }
            apsObject.put("content-available", 1);
            return new PayloadPerDevice(payload, subscription.getToken());
        } catch (JSONException e) {
            LOG.warn("error constructing payload", e);
        } catch (InvalidDeviceTokenFormatException e) {
            LOG.warn("Invalid device token: '{}', removing from subscription store.", subscription.getToken(), e);
            removeSubscription(subscription);
        }
        return null;
    }

    private PayloadPerDevice getPayload(DriveEvent event, Subscription subscription) {
        String pushTokenReference = event.getPushTokenReference();
        if (null != pushTokenReference && subscription.matches(pushTokenReference)) {
            LOG.trace("Skipping push notification for subscription: {}", subscription);
            return null;
        }
        try {
            PushNotificationPayload payload = new PushNotificationBigPayload();
            payload.addCustomAlertLocKey("TRIGGER_SYNC");
            payload.addCustomAlertActionLocKey("OK");
            payload.addCustomDictionary("root", subscription.getRootFolderID());
            payload.addCustomDictionary("action", "sync");
            return new PayloadPerDevice(payload, subscription.getToken());
        } catch (JSONException e) {
            LOG.warn("error constructing payload", e);
            return null;
        } catch (InvalidDeviceTokenFormatException e) {
            LOG.warn("Invalid device token: '{}', removing from subscription store.", subscription.getToken(), e);
            removeSubscription(subscription);
            return null;
        }
    }

    private boolean removeSubscription(Subscription subscription) {
        try {
            return requireService(DriveSubscriptionStore.class, services).removeSubscription(subscription);
        } catch (OXException e) {
            LOG.error("Error removing subscription", e);
        }
        return false;
    }

    private int removeSubscriptions(Device device) {
        if (null != device && null != device.getToken() && null != device.getLastRegister()) {
            try {
                return requireService(DriveSubscriptionStore.class, services).removeSubscriptions(
                    serviceId, device.getToken(), device.getLastRegister().getTime());
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
