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

package com.openexchange.sessiond.osgi;

import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.sessiond.impl.SessionHandler;
import com.openexchange.sessiond.impl.SessionImpl;
import com.openexchange.sessiond.impl.container.ShortTermSessionControl;
import com.openexchange.sessionstorage.SessionStorageService;

/**
 * {@link SessionStorageServiceTracker}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class SessionStorageServiceTracker implements ServiceTrackerCustomizer<SessionStorageService, SessionStorageService> {

    private final BundleContext context;
    private final SessiondActivator activator;

    /**
     * Initializes a new {@link SessionStorageServiceTracker}.
     */
    public SessionStorageServiceTracker(SessiondActivator activator, BundleContext context) {
        super();
        this.activator = activator;
        this.context = context;
    }

    @Override
    public SessionStorageService addingService(final ServiceReference<SessionStorageService> reference) {
        final SessionStorageService service = context.getService(reference);
        activator.addService(SessionStorageService.class, service);
        final List<ShortTermSessionControl> sessionControls = SessionHandler.getSessions();
        if (!sessionControls.isEmpty()) {
            final List<SessionImpl> sessions = new ArrayList<SessionImpl>(sessionControls.size());
            for (final ShortTermSessionControl sessionControl : sessionControls) {
                if (false == sessionControl.getSession().isTransient()) {
                    sessions.add(sessionControl.getSession());
                }
            }
            SessionHandler.storeSessions(sessions, service);
        }
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<SessionStorageService> reference, final SessionStorageService service) {
        // nothing to do
    }

    @Override
    public void removedService(final ServiceReference<SessionStorageService> reference, final SessionStorageService service) {
        activator.removeService(SessionStorageService.class);
        context.ungetService(reference);
    }

}
