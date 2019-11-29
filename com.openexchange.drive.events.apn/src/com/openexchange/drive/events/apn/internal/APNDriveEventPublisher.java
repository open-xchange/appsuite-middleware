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
import static com.openexchange.osgi.Tools.requireService;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.DriveEventPublisher;
import com.openexchange.drive.events.apn.APNAccess;
import com.openexchange.drive.events.apn.APNCertificateProvider;
import com.openexchange.drive.events.apn2.util.ApnsHttp2Options;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.Subscription;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link APNDriveEventPublisher}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class APNDriveEventPublisher implements DriveEventPublisher {

    /** The logger constant */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(APNDriveEventPublisher.class);

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
            String topic = configService.getProperty(userId, contextId, DriveEventsAPNProperty.topic, optionals);
            LOG.trace("Using configured keystore {}, {} service for push via {} for user {} in context {}.", keystore, production ? "production" : "sandbox", serviceId, I(userId), I(contextId));
            return new APNAccess(keystore, password, production, topic);
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
            LOG.trace("No subscriptions found for service {} for folder {} in context {}", serviceId, event.getFolderIDs(), I(event.getContextID()));
            return;
        }

        for (Subscription subscription : subscriptions) {
            APNAccess access = getAccess(subscription.getContextID(), subscription.getUserID());
            if (null == access) {
                LOG.debug("No APN configuration for subscription {}, skipping", subscription);
                continue;
            }

            /*
             * send notification via APN HTTP/2 for iOS devices
             */
            ApnsHttp2Options options = getApn2Options(access);
            Task<Void> task = new APNSubscriptionDeliveryTask(subscription, event, options, services);
            try {
                ThreadPools.execute(task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.warn("Interrupted while sending push notification for drive event for device token {}", subscription.getToken(), e);
                return;
            } catch (Exception e) {
                LOG.warn("Failed sending push notification for drive event to device with token {}", subscription.getToken(), e);
            }
        }
    }

    @Override
    public boolean isLocalOnly() {
        return true;
    }

    private ApnsHttp2Options getApn2Options(APNAccess access) {
        Object store = access.getKeystore();
        String password = access.getPassword();
        boolean production = access.isProduction();
        String topic = access.getTopic();
        if (store instanceof byte[]) {
            return new ApnsHttp2Options((byte[]) store, password, production, topic);
        }
        if (store instanceof String) {
            FileInputStream in = null;
            try {
                in = new FileInputStream((String) store);
                byte[] data = IOUtils.toByteArray(in);
                return new ApnsHttp2Options(data, password, production, topic);
            } catch (Exception e) {
                LOG.error("Error loading keystore", e);
            } finally {
                Streams.close(in);
            }
        }
        return null;
    }

}
