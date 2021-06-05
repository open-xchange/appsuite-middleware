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

package com.openexchange.xing.access.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.oauth.OAuthServiceMetaData;

/**
 * {@link OAuthServiceMetaDataRegisterer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OAuthServiceMetaDataRegisterer implements ServiceTrackerCustomizer<OAuthServiceMetaData, OAuthServiceMetaData> {

    private final BundleContext context;
    private final XingOAuthAccessActivator activator;
    private final String xingIdentifier;

    /**
     * Initializes a new {@link OAuthServiceMetaDataRegisterer}.
     *
     * @param context The bundle context
     * @param activator The activator to track/start services
     */
    public OAuthServiceMetaDataRegisterer(final BundleContext context, final XingOAuthAccessActivator activator) {
        super();
        xingIdentifier = "com.openexchange.oauth.xing";
        this.context = context;
        this.activator = activator;
    }

    @Override
    public OAuthServiceMetaData addingService(final ServiceReference<OAuthServiceMetaData> reference) {
        final OAuthServiceMetaData oAuthServiceMetaData = context.getService(reference);
        if (xingIdentifier.equals(oAuthServiceMetaData.getId())) {
            activator.setOAuthServiceMetaData(oAuthServiceMetaData);
            activator.registerProvider();
            return oAuthServiceMetaData;
        }
        // Not of interest
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<OAuthServiceMetaData> reference, final OAuthServiceMetaData service) {
        // nothing to do here
    }

    @Override
    public void removedService(final ServiceReference<OAuthServiceMetaData> reference, final OAuthServiceMetaData service) {
        if (null != service) {
            final OAuthServiceMetaData oAuthServiceMetaData = service;
            if (xingIdentifier.equals(oAuthServiceMetaData.getId())) {
                activator.setOAuthServiceMetaData(null);
                activator.unregisterProvider();
            }
            context.ungetService(reference);
        }
    }
}
