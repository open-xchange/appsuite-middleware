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

package com.openexchange.subscribe.yahoo.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.association.spi.OAuthAccountAssociationProvider;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.yahoo.YahooSubscribeService;
import com.openexchange.subscribe.yahoo.oauth.YahooContactsOAuthAccountAssociationProvider;

/**
 * {@link OAuthServiceMetaDataRegisterer}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OAuthServiceMetaDataRegisterer implements ServiceTrackerCustomizer<OAuthServiceMetaData, OAuthServiceMetaData> {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthServiceMetaDataRegisterer.class);

    private final BundleContext context;
    private final String yahooIdentifier;
    private volatile ServiceRegistration<SubscribeService> serviceRegistration;
    private ServiceRegistration<OAuthAccountAssociationProvider> associationProviderRegistration;

    private final ServiceLookup services;

    public OAuthServiceMetaDataRegisterer(final BundleContext context, ServiceLookup services) {
        this.context = context;
        this.services = services;
        yahooIdentifier = KnownApi.YAHOO.getServiceId();
    }

    @Override
    public OAuthServiceMetaData addingService(final ServiceReference<OAuthServiceMetaData> reference) {
        final OAuthServiceMetaData oAuthServiceMetaData = context.getService(reference);
        if (yahooIdentifier.equals(oAuthServiceMetaData.getId())) {
            if (null == serviceRegistration) {
                try {
                    serviceRegistration = context.registerService(SubscribeService.class, new YahooSubscribeService(oAuthServiceMetaData, services), null);
                } catch (OXException e) {
                    LOG.error("Unable to create YahooSubscribeService: " + e.getMessage(), e);
                    return null;
                }
                org.slf4j.LoggerFactory.getLogger(Activator.class).info("YahooSubscribeService was started");
            }

            if (associationProviderRegistration == null) {
                associationProviderRegistration = context.registerService(OAuthAccountAssociationProvider.class, new YahooContactsOAuthAccountAssociationProvider(services), null);
            }
            if (null == serviceRegistration) {
                try {
                    serviceRegistration = context.registerService(SubscribeService.class, new YahooSubscribeService(oAuthServiceMetaData, services), null);
                } catch (OXException e) {
                    LOG.error("Unable to create YahooSubscribeService: " + e.getMessage(), e);
                    return null;
                }
                LoggerFactory.getLogger(Activator.class).info("YahooSubscribeService was started");
            }
        }
        return oAuthServiceMetaData;
    }

    @Override
    public void modifiedService(final ServiceReference<OAuthServiceMetaData> arg0, final OAuthServiceMetaData arg1) {
        //nothing to do here
    }

    @Override
    public void removedService(final ServiceReference<OAuthServiceMetaData> reference, final OAuthServiceMetaData arg1) {
        final OAuthServiceMetaData oAuthServiceMetaData = arg1;
        if (yahooIdentifier.equals(oAuthServiceMetaData.getId())) {
            ServiceRegistration<SubscribeService> serviceRegistration = this.serviceRegistration;
            if (null != serviceRegistration) {
                serviceRegistration.unregister();
                this.serviceRegistration = null;
                LoggerFactory.getLogger(Activator.class).info("YahooSubscribeService was stopped");
            }
            {
                ServiceRegistration<OAuthAccountAssociationProvider> registration = this.associationProviderRegistration;
                if (null != registration) {
                    registration.unregister();
                    this.associationProviderRegistration = null;
                }
            }
            LoggerFactory.getLogger(Activator.class).info("YahooSubscribeService was stopped");
        }
        context.ungetService(reference);
    }
}
