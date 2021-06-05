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

package com.openexchange.subscribe.google.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.association.spi.OAuthAccountAssociationProvider;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.google.GoogleContactsSubscribeService;
import com.openexchange.subscribe.google.groupware.GoogleSubscriptionsOAuthAccountDeleteListener;
import com.openexchange.subscribe.google.oauth.GoogleContactsOAuthAccountAssociationProvider;

/**
 * {@link OAuthServiceMetaDataRegisterer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.10.1
 */
public class OAuthServiceMetaDataRegisterer implements ServiceTrackerCustomizer<OAuthServiceMetaData, OAuthServiceMetaData> {

    private final String oauthIdentifier;
    private final ServiceLookup services;
    private final BundleContext context;
    private volatile ServiceRegistration<SubscribeService> contactsRegistration;
    private volatile ServiceRegistration<OAuthAccountDeleteListener> deleteListenerRegistration;
    private volatile ServiceRegistration<OAuthAccountAssociationProvider> associationProviderRegistration;

    /**
     * Initialises a new {@link OAuthServiceMetaDataRegisterer}.
     *
     * @param services The service look-up
     * @param context The bundle context
     */
    public OAuthServiceMetaDataRegisterer(ServiceLookup services, BundleContext context) {
        super();
        this.services = services;
        this.context = context;
        oauthIdentifier = KnownApi.GOOGLE.getServiceId();
    }

    @Override
    public OAuthServiceMetaData addingService(ServiceReference<OAuthServiceMetaData> ref) {
        Logger logger = LoggerFactory.getLogger(OAuthServiceMetaDataRegisterer.class);
        OAuthServiceMetaData oAuthServiceMetaData = context.getService(ref);
        if (oauthIdentifier.equals(oAuthServiceMetaData.getId())) {
            logger.info("Registering Google Contact subscription services.");
            GoogleContactsSubscribeService subscribeService;
            try {
                subscribeService = new GoogleContactsSubscribeService(oAuthServiceMetaData, services);
            } catch (OXException e) {
                logger.error("Unable to create Google Contact subscription services", e);
                return null;
            }
            contactsRegistration = context.registerService(SubscribeService.class, subscribeService, null);
            deleteListenerRegistration = context.registerService(OAuthAccountDeleteListener.class, new GoogleSubscriptionsOAuthAccountDeleteListener(subscribeService, services), null);

            if (associationProviderRegistration == null) {
                associationProviderRegistration = context.registerService(OAuthAccountAssociationProvider.class, new GoogleContactsOAuthAccountAssociationProvider(services, subscribeService), null);
            }
        }
        return oAuthServiceMetaData;
    }

    @Override
    public void modifiedService(ServiceReference<OAuthServiceMetaData> arg0, OAuthServiceMetaData arg1) {
        // nothing
    }

    @Override
    public void removedService(ServiceReference<OAuthServiceMetaData> ref, OAuthServiceMetaData service) {
        Logger logger = LoggerFactory.getLogger(OAuthServiceMetaDataRegisterer.class);
        if (service.getId().equals(oauthIdentifier)) {
            logger.info("Unregistering Google Contacts subscription services.");

            {
                ServiceRegistration<SubscribeService> registration = this.contactsRegistration;
                if (null != registration) {
                    registration.unregister();
                    this.contactsRegistration = null;
                }
            }
            {
                ServiceRegistration<OAuthAccountDeleteListener> registration = this.deleteListenerRegistration;
                if (null != registration) {
                    registration.unregister();
                    this.deleteListenerRegistration = null;
                }
            }
            {
                ServiceRegistration<OAuthAccountAssociationProvider> registration = this.associationProviderRegistration;
                if (null != registration) {
                    registration.unregister();
                    this.associationProviderRegistration = null;
                }
            }
        }
        context.ungetService(ref);
    }
}
