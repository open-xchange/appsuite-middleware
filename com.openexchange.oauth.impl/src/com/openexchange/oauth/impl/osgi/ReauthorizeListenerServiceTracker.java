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
import com.openexchange.oauth.OAuthAccountReauthorizedListener;
import com.openexchange.oauth.impl.internal.ReauthorizeListenerRegistry;

/**
 * {@link ReauthorizeListenerServiceTracker} - The {@link ServiceTrackerCustomizer} for OAuth account delete listeners.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ReauthorizeListenerServiceTracker implements ServiceTrackerCustomizer<OAuthAccountReauthorizedListener,OAuthAccountReauthorizedListener> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ReauthorizeListenerServiceTracker.class);

    private final BundleContext context;

    /**
     * Initializes a new {@link ReauthorizeListenerServiceTracker}.
     */
    public ReauthorizeListenerServiceTracker(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public OAuthAccountReauthorizedListener addingService(final ServiceReference<OAuthAccountReauthorizedListener> reference) {
        final OAuthAccountReauthorizedListener listener = context.getService(reference);
        if (ReauthorizeListenerRegistry.getInstance().addReauthorizeListener(listener)) {
            return listener;
        }
        LOG.warn("Duplicate re-authorize listener \"{}\" is not added to registry.", listener.getClass().getName());
        // This service needs not to be tracked, thus return null
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<OAuthAccountReauthorizedListener> reference, final OAuthAccountReauthorizedListener listener) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<OAuthAccountReauthorizedListener> reference, final OAuthAccountReauthorizedListener listener) {
        if (null != listener) {
            try {
                ReauthorizeListenerRegistry.getInstance().removeReauthorizeListener(listener);
            } finally {
                context.ungetService(reference);
            }
        }
    }

}
