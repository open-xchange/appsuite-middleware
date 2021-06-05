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

package com.openexchange.oauth.impl.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.oauth.OAuthAccountInvalidationListener;
import com.openexchange.oauth.impl.internal.InvalidationListenerRegistry;


/**
 * {@link InvalidationListenerServiceTracker}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InvalidationListenerServiceTracker implements ServiceTrackerCustomizer<OAuthAccountInvalidationListener, OAuthAccountInvalidationListener> {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InvalidationListenerServiceTracker.class);

    private final BundleContext context;

    /**
     * Initializes a new {@link InvalidationListenerServiceTracker}.
     */
    public InvalidationListenerServiceTracker(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public OAuthAccountInvalidationListener addingService(final ServiceReference<OAuthAccountInvalidationListener> reference) {
        final OAuthAccountInvalidationListener addedService = context.getService(reference);
        if (InvalidationListenerRegistry.getInstance().addInvalidationListener( addedService)) {
            return addedService;
        }
        LOG.warn("Duplicate invalidation listener \"{}\" is not be added to registry.", addedService.getClass().getName());
        // This service needs not to be tracked, thus return null
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<OAuthAccountInvalidationListener> reference, final OAuthAccountInvalidationListener service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<OAuthAccountInvalidationListener> reference, final OAuthAccountInvalidationListener service) {
        if (null != service) {
            try {
                InvalidationListenerRegistry.getInstance().removeInvalidationListener(service);
            } finally {
                context.ungetService(reference);
            }
        }
    }


}
