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

package com.openexchange.server.osgi.inspector;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.server.services.SessionInspector;
import com.openexchange.session.inspector.SessionInspectorChain;

/**
 * {@link SessionInspectorChain} service tracker putting the service into the static {@link SessionInspector} class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SessionInspectorChainCustomizer implements ServiceTrackerCustomizer<SessionInspectorChain, SessionInspectorChain> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SessionInspectorChainCustomizer.class);

    private final BundleContext context;

    /**
     * Initializes a new {@link SessionInspectorChainCustomizer}.
     *
     * @param context The associated bundle context
     */
    public SessionInspectorChainCustomizer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public SessionInspectorChain addingService(final ServiceReference<SessionInspectorChain> reference) {
        final SessionInspectorChain auth = context.getService(reference);
        if (SessionInspector.getInstance().setService(auth)) {
            return auth;
        }
        LOG.error("Several session inspector chains found. Remove all except one!");
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<SessionInspectorChain> reference, final SessionInspectorChain service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<SessionInspectorChain> reference, final SessionInspectorChain service) {
        if (!SessionInspector.getInstance().dropService(service)) {
            LOG.error("Removed session inspector chain was not active!");
        }
        context.ungetService(reference);
    }
}
