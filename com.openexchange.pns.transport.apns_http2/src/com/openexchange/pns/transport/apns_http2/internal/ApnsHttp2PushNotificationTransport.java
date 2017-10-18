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

package com.openexchange.pns.transport.apns_http2.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import com.clevertap.apns.ApnsClient;
import com.clevertap.apns.Notification;
import com.clevertap.apns.NotificationRequestError;
import com.clevertap.apns.NotificationResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.SortableConcurrentList;
import com.openexchange.osgi.util.RankedService;
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
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.transport.apns_http2.ApnsHttp2Options;
import com.openexchange.pns.transport.apns_http2.ApnsHttp2OptionsProvider;

/**
 * {@link ApnsHttp2PushNotificationTransport}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ApnsHttp2PushNotificationTransport extends ServiceTracker<ApnsHttp2OptionsProvider, ApnsHttp2OptionsProvider> implements PushNotificationTransport {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ApnsHttp2PushNotificationTransport.class);

    private static final String ID = KnownTransport.APNS_HTTP2.getTransportId();

    // ---------------------------------------------------------------------------------------------------------------

    private final ConfigViewFactory configViewFactory;
    private final PushSubscriptionRegistry subscriptionRegistry;
    private final PushMessageGeneratorRegistry generatorRegistry;
    private final SortableConcurrentList<RankedService<ApnsHttp2OptionsProvider>> trackedProviders;
    private ServiceRegistration<PushNotificationTransport> registration; // non-volatile, protected by synchronized blocks

    /**
     * Initializes a new {@link ApnsHttp2PushNotificationTransport}.
     */
    public ApnsHttp2PushNotificationTransport(PushSubscriptionRegistry subscriptionRegistry, PushMessageGeneratorRegistry generatorRegistry, ConfigViewFactory configViewFactory, BundleContext context) {
        super(context, ApnsHttp2OptionsProvider.class, null);
        this.configViewFactory = configViewFactory;
        this.trackedProviders = new SortableConcurrentList<RankedService<ApnsHttp2OptionsProvider>>();
        this.generatorRegistry = generatorRegistry;
        this.subscriptionRegistry = subscriptionRegistry;
    }

    // ---------------------------------------------------------------------------------------------------------

    @Override
    public synchronized ApnsHttp2OptionsProvider addingService(ServiceReference<ApnsHttp2OptionsProvider> reference) {
        int ranking = RankedService.getRanking(reference);
        ApnsHttp2OptionsProvider provider = context.getService(reference);

        trackedProviders.addAndSort(new RankedService<ApnsHttp2OptionsProvider>(provider, ranking));

        if (null == registration) {
            registration = context.registerService(PushNotificationTransport.class, this, null);
        }

        return provider;
    }

    @Override
    public void modifiedService(ServiceReference<ApnsHttp2OptionsProvider> reference, ApnsHttp2OptionsProvider provider) {
        // Nothing
    }

    @Override
    public synchronized void removedService(ServiceReference<ApnsHttp2OptionsProvider> reference, ApnsHttp2OptionsProvider provider) {
        trackedProviders.remove(new RankedService<ApnsHttp2OptionsProvider>(provider, RankedService.getRanking(reference)));

        if (trackedProviders.isEmpty() && null != registration) {
            registration.unregister();
            registration = null;
        }

        context.ungetService(reference);
    }

    // ---------------------------------------------------------------------------------------------------------

    private ApnsHttp2Options getHighestRankedApnOptionsFor(String client) throws OXException {
        ApnsHttp2Options options = optHighestRankedApnOptionsFor(client);
        if (null == options) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create("No options found for client: " + client);
        }
        return options;
    }

    private ApnsHttp2Options optHighestRankedApnOptionsFor(String client) {
        List<RankedService<ApnsHttp2OptionsProvider>> list = trackedProviders.getSnapshot();
        for (RankedService<ApnsHttp2OptionsProvider> rankedService : list) {
            ApnsHttp2Options options = rankedService.service.getOptions(client);
            if (null != options) {
                return options;
            }
        }
        return null;
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

        String basePropertyName = "com.openexchange.pns.transport.apns_http2.ios.enabled";

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
        Map<String, List<Entry<PushMatch, Notification>>> payloadsPerClient = new HashMap<String, List<Entry<PushMatch, Notification>>>();
        for (Map.Entry<PushNotification, List<PushMatch>> entry : notifications.entrySet()) {
            for (Entry<PushMatch, Notification> payload : getPayloadsPerDevice(entry.getKey(), entry.getValue()).entrySet()) {
                String client = payload.getKey().getClient();
                com.openexchange.tools.arrays.Collections.put(payloadsPerClient, client, payload);
            }
        }
        /*
         * perform transport to devices of each client
         */
        for (Entry<String, List<Entry<PushMatch, Notification>>> entry : payloadsPerClient.entrySet()) {
            transport(entry.getKey(), entry.getValue());
        }
    }

    private static List<Notification> getPayloadsPerDevice(List<Entry<PushMatch, Notification>> payloads) {
        List<Notification> payloadsPerDevice = new ArrayList<Notification>(payloads.size());
        for (Entry<PushMatch, Notification> entry : payloads) {
            payloadsPerDevice.add(entry.getValue());
        }
        return payloadsPerDevice;
    }

    private void transport(String client, List<Entry<PushMatch, Notification>> payloads) {
        List<NotificationResponsePerDevice> notifications = null;
        try {
            notifications = transport(getHighestRankedApnOptionsFor(client), getPayloadsPerDevice(payloads));
        } catch (Exception e) {
            LOG.warn("error submitting push notifications", e);
        }
        processNotificationResults(notifications, payloads);
    }

    private List<NotificationResponsePerDevice> transport(ApnsHttp2Options options, List<Notification> payloads) throws OXException {
        ApnsClient client = options.getApnsClient();
        List<NotificationResponsePerDevice> results = new ArrayList<NotificationResponsePerDevice>(payloads.size());
        for (Notification notification : payloads) {
            results.add(new NotificationResponsePerDevice(client.push(notification), notification.getToken()));
        }
        return results;
    }

    private static PushMatch findMatching(String deviceToken, List<Entry<PushMatch, Notification>> payloads) {
        if (null != deviceToken) {
            for (Entry<PushMatch, Notification> entry : payloads) {
                if (deviceToken.equals(entry.getValue().getToken())) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private void processNotificationResults(List<NotificationResponsePerDevice> notifications, List<Entry<PushMatch, Notification>> payloads) {
        if (null == notifications || notifications.isEmpty()) {
            return;
        }

        for (NotificationResponsePerDevice notificationPerDevice : notifications) {
            NotificationResponse notification = notificationPerDevice.notificationResponse;
            NotificationRequestError error = notification.getError();
            if (null == error) {
                LOG.debug("{}", notification);
                continue;
            }

            if (NotificationRequestError.DeviceTokenInactiveForTopic == error || NotificationRequestError.InvalidProviderToken == error) {
                String deviceToken = notificationPerDevice.deviceToken;
                LOG.warn("Unsuccessful push notification due to inactive or invalid device token: {}", deviceToken);
                PushMatch pushMatch = findMatching(deviceToken, payloads);
                if (null != pushMatch) {
                    boolean removed = removeSubscription(pushMatch);
                    if (removed) {
                        LOG.info("Removed subscription for device with token: {}.", pushMatch.getToken());
                    }
                    LOG.debug("Could not remove subscriptions for device with token: {}.", pushMatch.getToken());
                } else {
                    int removed = removeSubscriptions(deviceToken);
                    if (0 < removed) {
                        LOG.info("Removed {} subscriptions for device with token: {}.", Integer.valueOf(removed), deviceToken);
                    }
                }
            } else {
                LOG.warn("Unsuccessful push notification: {}", notification);
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

    private Map<PushMatch, Notification> getPayloadsPerDevice(PushNotification notification, Collection<PushMatch> matches) throws OXException {
        Map<PushMatch, Notification> payloadsPerDevice = new HashMap<PushMatch, Notification>(matches.size());
        for (PushMatch match : matches) {
            Notification payloadPerDevice = getPayloadPerDevice(notification, match);
            if (null != payloadPerDevice) {
                payloadsPerDevice.put(match, payloadPerDevice);
            }
        }
        return payloadsPerDevice;
    }

    private Notification getPayloadPerDevice(PushNotification notification, PushMatch match) throws OXException {
        PushMessageGenerator generator = generatorRegistry.getGenerator(match.getClient());
        if (null == generator) {
            throw PushExceptionCodes.NO_SUCH_GENERATOR.create(match.getClient());
        }
        Message<?> message = generator.generateMessageFor(ID, notification);
        return getPayload(message.getMessage(), match.getToken());
    }

    private Notification getPayload(Object messageObject, String deviceToken) throws OXException {
        if (messageObject instanceof Notification) {
            return (Notification) messageObject;
        }
        if (messageObject instanceof Map) {
            return toPayload((Map<String, Object>) messageObject, deviceToken);
        }
        throw PushExceptionCodes.UNSUPPORTED_MESSAGE_CLASS.create(null == messageObject ? "null" : messageObject.getClass().getName());
    }

    private Notification toPayload(Map<String, Object> message, String deviceToken) {
        Notification.Builder builder = new Notification.Builder(deviceToken);

        Map<String, Object> source = new HashMap<>(message);
        {
            String sSound = (String) source.remove("sound");
            if (null != sSound) {
                builder.sound(sSound);
            }
        }

        {
            Integer iBadge = (Integer) source.remove("badge");
            if (null != iBadge) {
                builder.badge(iBadge.intValue());
            }
        }

        {
            String sAlert = (String) source.remove("alert");
            if (null != sAlert) {
                builder.alertBody(sAlert);
            }
        }

        {
            String sCategory = (String) source.remove("category");
            if (null != sCategory) {
                builder.category(sCategory);
            }
        }

        // Put remaining as custom dictionary
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object value = entry.getValue();
            if (null == value) {
                LOG.warn("Ignoring unsupported null value");
            } else {
                if (value instanceof Number) {
                    builder.customField(entry.getKey(), value);
                } else if (value instanceof String) {
                    builder.customField(entry.getKey(), value);
                } else {
                    LOG.warn("Ignoring usupported value of type {}: {}", value.getClass().getName(), value.toString());
                }
            }
        }

        return builder.build();
    }

    private int removeSubscriptions(String deviceToken) {
        if (null == deviceToken) {
            LOG.warn("Unsufficient device information to remove subscriptions for: {}", deviceToken);
            return 0;
        }

        try {
            return subscriptionRegistry.unregisterSubscription(deviceToken, ID);
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

    // -------------------------------------------------------------------------------------------------------------------------------

    private static class NotificationResponsePerDevice {

        final NotificationResponse notificationResponse;
        final String deviceToken;

        NotificationResponsePerDevice(NotificationResponse notificationResponse, String deviceToken) {
            super();
            this.notificationResponse = notificationResponse;
            this.deviceToken = deviceToken;
        }
    }

}
