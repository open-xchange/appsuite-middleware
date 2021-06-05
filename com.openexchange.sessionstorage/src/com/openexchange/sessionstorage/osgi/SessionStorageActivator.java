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

package com.openexchange.sessionstorage.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessionstorage.SessionStorageConfiguration;

/**
 * {@link SessionStorageActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SessionStorageActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link SessionStorageActivator}.
     */
    public SessionStorageActivator() {
        super();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        SessionStorageParameterNamesTracker parameterNamesTracker = new SessionStorageParameterNamesTracker(context);
        rememberTracker(parameterNamesTracker);
        openTrackers();

        SessionStorageConfiguration.initInstance(getService(ConfigurationService.class), parameterNamesTracker);
    }

    @Override
    protected void stopBundle() throws Exception {
        SessionStorageConfiguration.releaseInstance();
        super.stopBundle();
    }

}
