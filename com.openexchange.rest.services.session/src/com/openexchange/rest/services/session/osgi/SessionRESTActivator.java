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

package com.openexchange.rest.services.session.osgi;

import com.openexchange.ajax.requesthandler.crypto.CryptographicServiceAuthenticationFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rest.services.session.SessionRESTService;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.SessionStorageService;

/**
 * {@link SessionRESTActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SessionRESTActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link SessionRESTActivator}.
     */
    public SessionRESTActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { SessiondService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        // Track optional CryptographicServiceAuthenticationFactory service
        trackService(CryptographicServiceAuthenticationFactory.class);
        trackService(SessionStorageService.class);
        trackService(ObfuscatorService.class);
        trackService(ConfigurationService.class);
        openTrackers();

        // Register session REST end-point
        registerService(SessionRESTService.class, new SessionRESTService(this));
    }

}
