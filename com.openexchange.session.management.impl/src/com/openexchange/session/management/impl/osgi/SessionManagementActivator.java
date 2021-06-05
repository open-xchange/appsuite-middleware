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

package com.openexchange.session.management.impl.osgi;

import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.inspector.SessionInspectorService;
import com.openexchange.session.management.SessionManagementService;
import com.openexchange.session.management.impl.LocalLastActiveTimestampSetter;
import com.openexchange.session.management.impl.SessionManagementProperty;
import com.openexchange.session.management.impl.SessionManagementServiceImpl;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.user.UserService;

/**
 * {@link SessionManagementActivator}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class SessionManagementActivator extends HousekeepingActivator implements Reloadable {

    private SessionManagementServiceImpl sessionManagementImpl;

    /**
     * Initializes a new {@link SessionManagementActivator}.
     */
    public SessionManagementActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { SessiondService.class, LeanConfigurationService.class, UserService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        trackService(GeoLocationService.class);
        trackService(HazelcastInstance.class);
        openTrackers();

        registerService(SessionInspectorService.class, new LocalLastActiveTimestampSetter());

        SessionManagementServiceImpl sessionManagementImpl = new SessionManagementServiceImpl(this);
        this.sessionManagementImpl = sessionManagementImpl;
        registerService(SessionManagementService.class, sessionManagementImpl);

        registerService(Reloadable.class, this);
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        super.stopBundle();
        this.sessionManagementImpl = null;
    }

    @Override
    public synchronized void reloadConfiguration(ConfigurationService configService) {
        SessionManagementServiceImpl sessionManagementImpl = this.sessionManagementImpl;
        if (null != sessionManagementImpl) {
            sessionManagementImpl.reinitBlacklistedClients();
        }
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(
            SessionManagementProperty.GLOBAL_LOOKUP.getFQPropertyName(),
            SessionManagementProperty.CLIENT_BLACKLIST.getFQPropertyName());
    }

}
