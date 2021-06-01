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

package com.openexchange.capabilities.json.osgi;

import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.json.actions.CapabilityActionFactory;
import com.openexchange.capabilities.json.converter.Capability2JSON;
import com.openexchange.groupware.userconfiguration.osgi.PermissionRelevantServiceAddedTracker;
import com.openexchange.passwordchange.PasswordChangeService;

/**
 * {@link CapabilitiesJSONActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CapabilitiesJSONActivator extends AJAXModuleActivator {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { CapabilityService.class };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startBundle() throws Exception {
        // Add tracker to identify if a PasswordChangeService was registered. If so, add to PermissionAvailabilityService
        rememberTracker(new PermissionRelevantServiceAddedTracker<PasswordChangeService>(context, PasswordChangeService.class));
        openTrackers();

        registerService(ResultConverter.class, new Capability2JSON());
        registerModule(new CapabilityActionFactory(this), "capabilities");
    }
}
