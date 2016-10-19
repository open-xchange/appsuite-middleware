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

package com.openexchange.pns.transport.apn.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.SortableConcurrentList;
import com.openexchange.osgi.util.RankedService;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushMatch;
import com.openexchange.pns.PushMessageGenerator;
import com.openexchange.pns.PushMessageGeneratorRegistry;
import com.openexchange.pns.PushNotification;
import com.openexchange.pns.PushNotificationTransport;
import com.openexchange.pns.PushNotifications;
import com.openexchange.pns.DefaultPushSubscription;
import com.openexchange.pns.EnabledKey;
import com.openexchange.pns.KnownTransport;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.Message;
import com.openexchange.pns.transport.apn.ApnOptions;
import com.openexchange.pns.transport.apn.ApnOptionsPerClient;
import com.openexchange.pns.transport.apn.ApnOptionsProvider;
import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.json.JSONException;
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
public class ApnPushNotificationTransport extends ServiceTracker<ApnOptionsProvider, ApnOptionsProvider> implements PushNotificationTransport {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ApnPushNotificationTransport.class);

    private static final String ID = KnownTransport.APNS.getTransportId();

    // https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/CommunicatingWIthAPS.html
    private static final int STATUS_INVALID_TOKEN_SIZE = 5;

    private static final int STATUS_INVALID_TOKEN = 8;

    private static final int MAX_PAYLOAD_SIZE = 256;

    // ---------------------------------------------------------------------------------------------------------------

    private final ConfigViewFactory configViewFactory;
    private final PushSubscriptionRegistry subscriptionRegistry;
    private final PushMessageGeneratorRegistry generatorRegistry;
    private final SortableConcurrentList<RankedService<ApnOptionsProvider>> trackedProviders;
    private ServiceRegistration<PushNotificationTransport> registration; // non-volatile, protected by synchronized blocks

    /**
     * Initializes a new {@link ApnPushNotificationTransport}.
     */
    public ApnPushNotificationTransport(PushSubscriptionRegistry subscriptionRegistry, PushMessageGeneratorRegistry generatorRegistry, ConfigViewFactory configViewFactory, BundleContext context) {
        super(context, ApnOptionsProvider.class, null);
        this.configViewFactory = configViewFactory;
        this.trackedProviders = new SortableConcurrentList<RankedService<ApnOptionsProvider>>();
        this.generatorRegistry = generatorRegistry;
        this.subscriptionRegistry = subscriptionRegistry;
    }

    // ---------------------------------------------------------------------------------------------------------

    @Override
    public synchronized ApnOptionsProvider addingService(ServiceReference<ApnOptionsProvider> reference) {
        int ranking = RankedService.getRanking(reference);
        ApnOptionsProvider provider = context.getService(reference);

        trackedProviders.addAndSort(new RankedService<ApnOptionsProvider>(provider, ranking));

        if (null == registration) {
            registration = context.registerService(PushNotificationTransport.class, this, null);
        }

        return provider;
    }

    @Override
    public void modifiedService(ServiceReference<ApnOptionsProvider> reference, ApnOptionsProvider provider) {
        // Nothing
    }

    @Override
    public synchronized void removedService(ServiceReference<ApnOptionsProvider> reference, ApnOptionsProvider provider) {
        trackedProviders.remove(new RankedService<ApnOptionsProvider>(provider, RankedService.getRanking(reference)));

        if (trackedProviders.isEmpty() && null != registration) {
            registration.unregister();
            registration = null;
        }

        context.ungetService(reference);
    }

    // ---------------------------------------------------------------------------------------------------------

    private ApnOptions getHighestRankedApnOptionsFor(String client) throws OXException {
        List<RankedService<ApnOptionsProvider>> list = trackedProviders.getSnapshot();
        for (RankedService<ApnOptionsProvider> rankedService : list) {
            ApnOptions options = rankedService.service.getOptions(client);
            if (null != options) {
                return options;
            }
        }
        throw PushExceptionCodes.UNEXPECTED_ERROR.create("No options found for client: " + client);
    }

    private Map<String, ApnOptions> getAllHighestRankedApnOptions() {
        List<RankedService<ApnOptionsProvider>> list = trackedProviders.getSnapshot();
        Collections.reverse(list);
        Map<String, ApnOptions> options = new LinkedHashMap<>();
        for (RankedService<ApnOptionsProvider> rankedService : list) {
            Collection<ApnOptionsPerClient> availableOptions = rankedService.service.getAvailableOptions();
            for (ApnOptionsPerClient ao : availableOptions) {
                options.put(ao.getClient(), ao.getOptions());
            }
        }
        return options;
    }

    @Override
    public boolean servesClient(String client) throws OXException {
        try {
            return null != getHighestRankedApnOptionsFor(client);
        } catch (OXException x) {
            return false;
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    private static final Cache<EnabledKey, Boolean> CACHE_AVAILABILITY = CacheBuilder.newBuilder().maximumSize(65536).expireAfterWrite(30, TimeUnit.MINUTES).build();

    /**
     * Invalidates the <i>enabled cache</i>.
     */
    public static void invalidateEnabledCache() {
        CACHE_AVAILABILITY.invalidateAll();
    }

    @Override
    public boolean isEnabled(String topic, String client, int userId, int contextId) throws OXException {
        EnabledKey key = new EnabledKey(topic, client, userId, contextId);
        Boolean result = CACHE_AVAILABILITY.getIfPresent(key);
        if (null == result) {
            result = Boolean.valueOf(doCheckEnabled(topic, client, userId, contextId));
            CACHE_AVAILABILITY.put(key, result);
        }
        return result.booleanValue();
    }

    private boolean doCheckEnabled(String topic, String client, int userId, int contextId) throws OXException {
        ConfigView view = configViewFactory.getView(userId, contextId);

        String basePropertyName = "com.openexchange.pns.transport.apn.ios.enabled";

        ComposedConfigProperty<Boolean> property;
        property = null == topic || null == client ? null : view.property(basePropertyName + "." + client + "." + topic, boolean.class);
        if (null != property && property.isDefined()) {
            return property.get().booleanValue();
        }

        property = null == client ? null : view.property(basePropertyName + "." + client, boolean.class);
        if (null != property && property.isDefined()) {
            return property.get().booleanValue();
        }

        property = view.property(basePropertyName, boolean.class);
        if (null != property && property.isDefined()) {
            return property.get().booleanValue();
        }

        return false;
    }

    @Override
    public void transport(PushNotification notification, Collection<PushMatch> matches) throws OXException {
        if (null != notification && null != matches) {
            // Create payloads for each match
            Map<String, List<PayloadPerDevice>> clientPayloads = getPayloads(notification, matches);

            // Transport them
            for (Map.Entry<String, List<PayloadPerDevice>> clientPayload : clientPayloads.entrySet()) {
                PushedNotifications notifications = null;
                try {
                    // Send to devices
                    ApnOptions options = getHighestRankedApnOptionsFor(clientPayload.getKey());
                    final List<PayloadPerDevice> payloads = clientPayload.getValue();
                    notifications = Push.payloads(options.getKeystore(), options.getPassword(), options.isProduction(), payloads);

                    // Log it
                    Object ostr = new Object() {

                        @Override
                        public String toString() {
                            StringBuilder sb = new StringBuilder(payloads.size() * 16);
                            for (PayloadPerDevice payloadPerDevice : payloads) {
                                sb.append(payloadPerDevice.getDevice().getToken()).append(", ");
                            }
                            sb.setLength(sb.length() - 2);
                            return sb.toString();
                        }
                    };
                    LOG.info("Sent notification \"{}\" via transport '{}' for user {} in context {} to device(s): {}", notification.getTopic(), ID, I(notification.getUserId()), I(notification.getContextId()), ostr);
                } catch (CommunicationException e) {
                    LOG.warn("error submitting push notifications", e);
                } catch (KeystoreException e) {
                    LOG.warn("error submitting push notifications", e);
                } catch (OXException e) {
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

    private Map<String, List<PayloadPerDevice>> getPayloads(PushNotification notification, Collection<PushMatch> matches) throws OXException {
        Map<String, List<PayloadPerDevice>> clientPayloads = new LinkedHashMap<>(matches.size());
        for (PushMatch match : matches) {
            String client = match.getClient();
            PushMessageGenerator generator = generatorRegistry.getGenerator(client);
            if (null == generator) {
                throw PushExceptionCodes.NO_SUCH_GENERATOR.create(client);
            }

            Message<?> message = generator.generateMessageFor(ID, notification);

            List<PayloadPerDevice> payloads = clientPayloads.get(client);
            if (null == payloads) {
                payloads = new LinkedList<>();
                clientPayloads.put(client, payloads);
            }

            Object object = message.getMessage();
            if (object instanceof Payload) {
                addCheckPayload((Payload) object, match, payloads);
            } else if (object instanceof Map) {
                try {
                    addCheckPayload(toPayload((Map<String, Object>) object), match, payloads);
                } catch (JSONException e) {
                    throw PushExceptionCodes.MESSAGE_GENERATION_FAILED.create(e, e.getMessage());
                }
            } else {
                throw PushExceptionCodes.UNSUPPORTED_MESSAGE_CLASS.create(null == object ? "null" : object.getClass().getName());
            }
        }
        return clientPayloads;
    }

    private void addCheckPayload(Payload payload, PushMatch match, List<PayloadPerDevice> payloads) throws OXException {
        int payloadLength = PushNotifications.getPayloadLength(payload.toString());
        // Check payload length
        if (payloadLength > MAX_PAYLOAD_SIZE) {
            throw PushExceptionCodes.MESSAGE_TOO_BIG.create(MAX_PAYLOAD_SIZE, payloadLength);
        }

        try {
            payloads.add(new PayloadPerDevice(payload, match.getToken()));
        } catch (InvalidDeviceTokenFormatException e) {
            LOG.warn("Invalid device token: '{}', removing from subscription store.", match.getToken(), e);
            try {
                subscriptionRegistry.unregisterSubscription(DefaultPushSubscription.instanceFor(match));
            } catch (OXException x) {
                LOG.error("Failed to remove subscription for invalid token {}", match.getToken(), x);
            }
        }
    }

    private Payload toPayload(Map<String, Object> message) throws JSONException {
        PushNotificationPayload payload = new PushNotificationPayload();

        Map<String, Object> source = new HashMap<>(message);
        {
            String sSound = (String) source.remove("sound");
            if (null != sSound) {
                payload.addSound(sSound);
            }
        }

        {
            Integer iBadge = (Integer) source.remove("badge");
            if (null != iBadge) {
                payload.addBadge(iBadge.intValue());
            }
        }

        {
            String sAlert = (String) source.remove("alert");
            if (null != sAlert) {
                payload.addAlert(sAlert);
            }
        }

        {
            String sCategory = (String) source.remove("category");
            if (null != sCategory) {
                payload.addCategory(sCategory);
            }
        }

        // Put remaining as custom dictionary
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object value = entry.getValue();
            if (null == value) {
                LOG.warn("Ignoring usupported null value");
            } else {
                if (value instanceof Number) {
                    payload.addCustomDictionary(entry.getKey(), ((Number) value).intValue());
                } else if (value instanceof String) {
                    payload.addCustomDictionary(entry.getKey(), value.toString());
                } else {
                    LOG.warn("Ignoring usupported value of type {}: {}", value.getClass().getName(), value.toString());
                }
            }
        }

        return payload;
    }


    /**
     * Queries the feedback service and processes the received results, removing reported tokens from the subscription store if needed.
     */
    public void queryFeedbackService() {
        LOG.info("Querying APNS feedback service for 'apn'...");
        long start = System.currentTimeMillis();

        Map<String, ApnOptions> options = getAllHighestRankedApnOptions();
        for (ApnOptions apnOptions : options.values()) {
            List<Device> devices = null;
            try {
                devices = Push.feedback(apnOptions.getKeystore(), apnOptions.getPassword(), apnOptions.isProduction());
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
        }

        LOG.info("Finished processing APNS feedback after {} ms.", (System.currentTimeMillis() - start));
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
            DefaultPushSubscription.Builder builder = DefaultPushSubscription.builder()
                .contextId(notification.getContextId())
                .token(device.getToken())
                .transportId(ID)
                .userId(notification.getUserId());

            DefaultPushSubscription subscriptionDesc = builder.build();

            return subscriptionRegistry.unregisterSubscription(subscriptionDesc);
        } catch (OXException e) {
            LOG.error("Error removing subscription", e);
        }
        return false;
    }

}
