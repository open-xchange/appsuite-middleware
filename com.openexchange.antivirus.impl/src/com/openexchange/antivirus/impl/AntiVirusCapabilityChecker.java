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

package com.openexchange.antivirus.impl;

import com.openexchange.antivirus.AntiVirusProperty;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link AntiVirusCapabilityChecker}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class AntiVirusCapabilityChecker implements CapabilityChecker {

    public static final String CAPABILITY = "antivirus";
    private final ServiceLookup services;

    /**
     * Initialises a new {@link AntiVirusCapabilityChecker}.
     */
    public AntiVirusCapabilityChecker(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public boolean isEnabled(String capability, Session session) throws OXException {
        if (false == CAPABILITY.equals(capability)) {
            return true;
        }
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        if (serverSession.isAnonymous()) {
            return false;
        }
        LeanConfigurationService lcService = services.getService(LeanConfigurationService.class);
        return lcService.getBooleanProperty(session.getUserId(), session.getContextId(), AntiVirusProperty.enabled);
    }
}
