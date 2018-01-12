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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
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
import com.openexchange.pns.ApnsConstants;
import com.openexchange.pns.DefaultPushSubscription;
import com.openexchange.pns.EnabledKey;
import com.openexchange.pns.KnownTransport;
import com.openexchange.pns.Message;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushMatch;
import com.openexchange.pns.PushMessageGenerator;
import com.openexchange.pns.PushMessageGeneratorRegistry;
import com.openexchange.pns.PushNotification;
import com.openexchange.pns.PushNotificationTransport;
import com.openexchange.pns.PushNotifications;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.transport.apn.ApnOptions;
import com.openexchange.pns.transport.apn.ApnOptionsPerClient;
import com.openexchange.pns.transport.apn.ApnOptionsProvider;
import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.notification.Payload;
import javapns.notification.PayloadPerDevice;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;

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

    /** The maximum number of simultaneously transported payloads per request */
    private static final int TRANSPORT_CHUNK_SIZE = 100;

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
        ApnOptions options = optHighestRankedApnOptionsFor(client);
        if (null == options) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create("No options found for client: " + client);
        }
        return options;
    }

    private ApnOptions optHighestRankedApnOptionsFor(String client) {
        List<RankedService<ApnOptionsProvider>> list = trackedProviders.getSnapshot();
        for (RankedService<ApnOptionsProvider> rankedService : list) {
            ApnOptions options = rankedService.service.getOptions(client);
            if (null != options) {
                return options;
            }
        }
        return null;
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
            return null != optHighestRankedApnOptionsFor(client);
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
    public void transport(Map<PushNotification, List<PushMatch>> notifications) throws OXException {
        if (null == notifications || notifications.isEmpty()) {
            return;
        }
        /*
         * generate message payloads for all matches of all notifications, associated to targeted client
         */
        Map<String, List<Entry<PushMatch, PayloadPerDevice>>> payloadsPerClient = new HashMap<String, List<Entry<PushMatch, PayloadPerDevice>>>();
        for (Map.Entry<PushNotification, List<PushMatch>> entry : notifications.entrySet()) {
            for (Entry<PushMatch, PayloadPerDevice> payload : getPayloadsPerDevice(entry.getKey(), entry.getValue()).entrySet()) {
                String client = payload.getKey().getClient();
                com.openexchange.tools.arrays.Collections.put(payloadsPerClient, client, payload);
            }
        }
        /*
         * perform transport to devices of each client
         */
        for (Entry<String, List<Entry<PushMatch, PayloadPerDevice>>> entry : payloadsPerClient.entrySet()) {
            transport(entry.getKey(), entry.getValue());
        }
    }

    private static List<PayloadPerDevice> getPayloadsPerDevice(List<Entry<PushMatch, PayloadPerDevice>> payloads) {
        List<PayloadPerDevice> payloadsPerDevice = new ArrayList<PayloadPerDevice>(payloads.size());
        for (Entry<PushMatch, PayloadPerDevice> entry : payloads) {
            payloadsPerDevice.add(entry.getValue());
        }
        return payloadsPerDevice;
    }

    private void transport(String client, List<Entry<PushMatch, PayloadPerDevice>> payloads) {
        List<PushedNotification> notifications = null;
        try {
            notifications = transport(getHighestRankedApnOptionsFor(client), getPayloadsPerDevice(payloads));
        } catch (CommunicationException | KeystoreException | OXException e) {
            LOG.warn("error submitting push notifications", e);
        }
        processNotificationResults(notifications, payloads);
    }

    private List<PushedNotification> transport(ApnOptions options, List<PayloadPerDevice> payloads) throws CommunicationException, KeystoreException {
        List<PushedNotification> results = new ArrayList<PushedNotification>(payloads.size());
        for (int i = 0; i < payloads.size(); i += TRANSPORT_CHUNK_SIZE) {
            int length = Math.min(payloads.size(), i + TRANSPORT_CHUNK_SIZE) - i;
            results.addAll(Push.payloads(options.getKeystore(), options.getPassword(), options.isProduction(), payloads.subList(i, i + length)));
        }
        return results;
    }

    private static PushMatch findMatching(Device device, List<Entry<PushMatch, PayloadPerDevice>> payloads) {
        if (null != device) {
            for (Entry<PushMatch, PayloadPerDevice> entry : payloads) {
                if (device.equals(entry.getValue().getDevice())) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private void processNotificationResults(List<PushedNotification> notifications, List<Entry<PushMatch, PayloadPerDevice>> payloads) {
        if (null == notifications || notifications.isEmpty()) {
            return;
        }
        for (PushedNotification notification : notifications) {
            if (notification.isSuccessful()) {
                LOG.debug("{}", notification);
                continue;
            }
            LOG.info("Unsuccessful push notification: {}", notification);
            if (null != notification.getResponse()) {
                int status = notification.getResponse().getStatus();
                if (STATUS_INVALID_TOKEN == status || STATUS_INVALID_TOKEN_SIZE == status) {
                    PushMatch pushMatch = findMatching(notification.getDevice(), payloads);
                    if (null != pushMatch) {
                        boolean removed = removeSubscription(pushMatch);
                        if (removed) {
                            LOG.info("Removed subscription for device with token: {}.", pushMatch.getToken());
                        }
                        LOG.debug("Could not remove subscriptions for device with token: {}.", pushMatch.getToken());
                    } else {
                        Device device = notification.getDevice();
                        int removed = removeSubscriptions(device);
                        if (0 < removed) {
                            LOG.info("Removed {} subscriptions for device with token: {}.", removed, device.getToken());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void transport(PushNotification notification, Collection<PushMatch> matches) throws OXException {
        if (null != notification && null != matches) {
            List<PushMatch> pushMatches = new ArrayList<PushMatch>(matches);
            transport(Collections.singletonMap(notification, pushMatches));
        }
    }

    private Map<PushMatch, PayloadPerDevice> getPayloadsPerDevice(PushNotification notification, Collection<PushMatch> matches) throws OXException {
        Map<PushMatch, PayloadPerDevice> payloadsPerDevice = new HashMap<PushMatch, PayloadPerDevice>(matches.size());
        for (PushMatch match : matches) {
            PayloadPerDevice payloadPerDevice = getPayloadPerDevice(notification, match);
            if (null != payloadPerDevice) {
                payloadsPerDevice.put(match, payloadPerDevice);
            }
        }
        return payloadsPerDevice;
    }

    private PayloadPerDevice getPayloadPerDevice(PushNotification notification, PushMatch match) throws OXException {
        PushMessageGenerator generator = generatorRegistry.getGenerator(match.getClient());
        if (null == generator) {
            throw PushExceptionCodes.NO_SUCH_GENERATOR.create(match.getClient());
        }
        Message<?> message = generator.generateMessageFor(ID, notification);
        return getPayloadPerDevice(getPayload(message.getMessage()), match);
    }

    private Payload getPayload(Object messageObject) throws OXException {
        if (messageObject instanceof Payload) {
            return (Payload) messageObject;
        }
        if (messageObject instanceof Map) {
            try {
                return toPayload((Map<String, Object>) messageObject);
            } catch (JSONException e) {
                throw PushExceptionCodes.MESSAGE_GENERATION_FAILED.create(e, e.getMessage());
            }
        }
        throw PushExceptionCodes.UNSUPPORTED_MESSAGE_CLASS.create(null == messageObject ? "null" : messageObject.getClass().getName());
    }

    private PayloadPerDevice getPayloadPerDevice(Payload payload, PushMatch match) throws OXException {
        int payloadLength = PushNotifications.getPayloadLength(payload.toString());
        // Check payload length
        if (payloadLength > ApnsConstants.APNS_MAX_PAYLOAD_SIZE) {
            throw PushExceptionCodes.MESSAGE_TOO_BIG.create(ApnsConstants.APNS_MAX_PAYLOAD_SIZE, payloadLength);
        }
        try {
            return new PayloadPerDevice(payload, match.getToken());
        } catch (InvalidDeviceTokenFormatException e) {
            LOG.warn("Invalid device token: '{}', removing from subscription store.", match.getToken(), e);
            try {
                boolean unregistered = subscriptionRegistry.unregisterSubscription(DefaultPushSubscription.instanceFor(match));
                if (false == unregistered) {
                    LOG.error("Failed to remove subscription for invalid token {}", match.getToken());
                }
            } catch (OXException x) {
                LOG.error("Failed to remove subscription for invalid token {}", match.getToken(), x);
            }
        }
        return null;
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
        Map<String, ApnOptions> options = getAllHighestRankedApnOptions();
        if (options.isEmpty()) {
            return;
        }

        LOG.info("Querying APNS feedback service for 'apn'...");
        long start = System.currentTimeMillis();

        for (Map.Entry<String, ApnOptions> entry : options.entrySet()) {
            List<Device> devices = null;
            try {
                ApnOptions apnOptions = entry.getValue();
                devices = Push.feedback(apnOptions.getKeystore(), apnOptions.getPassword(), apnOptions.isProduction());
            } catch (Exception e) {
                LOG.warn("error querying feedback service", e);
            }

            String clientId = entry.getKey();
            if (null != devices && !devices.isEmpty()) {
                for (Device device : devices) {
                    LOG.debug("Got feedback for device with token: {}, last registered: {}", device.getToken(), device.getLastRegister());
                    int numRemoved = removeSubscriptions(device);
                    LOG.info("Removed {} subscriptions associated with client {} for device with token: {}.", numRemoved, clientId, device.getToken());
                }
            } else {
                LOG.info("No devices to unregister received from feedback service for client {}.", clientId);
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

    private boolean removeSubscription(PushMatch match) {
        try {
            DefaultPushSubscription subscription = DefaultPushSubscription.builder()
                .contextId(match.getContextId())
                .token(match.getToken())
                .transportId(ID)
                .userId(match.getUserId())
            .build();
            return subscriptionRegistry.unregisterSubscription(subscription);
        } catch (OXException e) {
            LOG.error("Error removing subscription", e);
        }
        return false;
    }

}
