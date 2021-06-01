/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.drive.events.apn.APNAccess.AuthType;
import com.openexchange.drive.events.apn.APNCertificateProvider;
import com.openexchange.drive.events.apn2.util.ApnsHttp2Options;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.Subscription;
import com.openexchange.exception.OXException;
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
        String sType = configService.getProperty(userId, contextId, DriveEventsAPNProperty.authtype, optionals);
        AuthType type = AuthType.authTypeFor(sType);
        if (null == type) {
            LOG.debug("Missing or invalid authentication type for push via {} for user {} in context {}. Using fall-back \"certificate\".", serviceId, I(userId), I(contextId));
            type = AuthType.CERTIFICATE;
        }
        switch (type) {
            case CERTIFICATE:
                /*
                 * APN with certificate in keystore is configured
                 */
                String keystore = configService.getProperty(userId, contextId, DriveEventsAPNProperty.keystore, optionals);
                if (Strings.isNotEmpty(keystore)) {
                    String password = configService.getProperty(userId, contextId, DriveEventsAPNProperty.password, optionals);
                    boolean production = configService.getBooleanProperty(userId, contextId, DriveEventsAPNProperty.production, optionals);
                    String topic = configService.getProperty(userId, contextId, DriveEventsAPNProperty.topic, optionals);
                    LOG.trace("Using configured keystore {}, {} service for push via {} for user {} in context {}.", keystore, production ? "production" : "sandbox", serviceId, I(userId), I(contextId));
                    return new APNAccess(keystore, password, production, topic);
                }
                break;
            case JWT:
                /*
                 * APN with private key is configured
                 */
                String privateKey = configService.getProperty(userId, contextId, DriveEventsAPNProperty.privatekey, optionals);
                if (Strings.isNotEmpty(privateKey)) {
                    String keyId = configService.getProperty(userId, contextId, DriveEventsAPNProperty.keyid, optionals);
                    String teamId = configService.getProperty(userId, contextId, DriveEventsAPNProperty.teamid, optionals);
                    String topic = configService.getProperty(userId, contextId, DriveEventsAPNProperty.topic, optionals);
                    boolean production = configService.getBooleanProperty(userId, contextId, DriveEventsAPNProperty.production, optionals);
                    LOG.trace("Using configured private key {}, {} service for push via {} for user {} in context {}.", privateKey, production ? "production" : "sandbox", serviceId, I(userId), I(contextId));
                    return new APNAccess(privateKey, keyId, teamId, topic, production);
                }
                break;
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
            subscriptions = requireService(DriveSubscriptionStore.class, services).getSubscriptions(event.getContextID(), new String[] { serviceId }, event.getFolderIDs());
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
        switch (access.getAuthType()) {
            case CERTIFICATE: {
                Object store = access.getKeystore();
                String password = access.getPassword();
                boolean production = access.isProduction();
                String topic = access.getTopic();
                if (store instanceof byte[]) {
                    return new ApnsHttp2Options((byte[]) store, password, production, topic);
                }
                if (!(store instanceof String)) {
                    break;
                }

                try (FileInputStream in = new FileInputStream((String) store)) {
                    return new ApnsHttp2Options(IOUtils.toByteArray(in), password, production, topic);
                } catch (Exception e) {
                    LOG.error("Error loading keystore", e);
                }
                break;
            }
            case JWT: {
                Object privateKey = access.getPrivateKey();
                String keyId = access.getKeyId();
                String teamId = access.getTeamId();
                String topic = access.getTopic();
                boolean production = access.isProduction();
                if (privateKey instanceof byte[]) {
                    return new ApnsHttp2Options((byte[]) privateKey, keyId, teamId, production, topic);
                }
                if (!(privateKey instanceof String)) {
                    break;
                }

                try (FileInputStream in = new FileInputStream((String) privateKey)) {
                    return new ApnsHttp2Options(IOUtils.toByteArray(in), keyId, teamId, production, topic);
                } catch (Exception e) {
                    LOG.error("Error loading private key", e);
                }
                break;
            }
        }
        return null;
    }

}
