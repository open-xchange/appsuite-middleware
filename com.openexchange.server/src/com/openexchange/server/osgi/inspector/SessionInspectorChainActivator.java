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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.session.inspector.SessionInspectorChain;

/**
 * Activator to start {@link ServiceTracker} to listen for {@link AutoLoginAuthenticationService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionInspectorChainActivator implements BundleActivator {

    private ServiceTracker<SessionInspectorChain, SessionInspectorChain> tracker;

    /**
     * Initializes a new {@link SessionInspectorChainActivator}.
     */
    public SessionInspectorChainActivator() {
        super();
    }

    @Override
    public void start(final BundleContext context) {
        ServiceTracker<SessionInspectorChain, SessionInspectorChain> tracker = new ServiceTracker<SessionInspectorChain, SessionInspectorChain>(context, SessionInspectorChain.class, new SessionInspectorChainCustomizer(context));
        this.tracker = tracker;
        tracker.open();
    }

    @Override
    public void stop(BundleContext context) {
        ServiceTracker<SessionInspectorChain, SessionInspectorChain> tracker = this.tracker;
        if (null != tracker) {
            tracker.close();
            this.tracker = null;
        }
    }

}
