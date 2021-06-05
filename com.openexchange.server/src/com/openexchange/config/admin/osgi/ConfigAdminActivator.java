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

package com.openexchange.config.admin.osgi;

import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.config.admin.HideAdminService;
import com.openexchange.config.admin.internal.HideAdminServiceImpl;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.user.UserService;

/**
 *
 * {@link ConfigAdminActivator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public final class ConfigAdminActivator extends AJAXModuleActivator {

    /**
     * Initializes a new {@link ConfigAdminActivator}.
     */
    public ConfigAdminActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { LeanConfigurationService.class, ContextService.class, UserService.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void startBundle() throws Exception {
        HideAdminService hideAdminService = new HideAdminServiceImpl(getServiceSafe(LeanConfigurationService.class), getServiceSafe(ContextService.class), getServiceSafe(UserService.class));
        registerService(HideAdminService.class, hideAdminService);
        ServerServiceRegistry.getInstance().addService(HideAdminService.class, hideAdminService);
    }

    @Override
    protected void stopBundle() throws Exception {
        ServerServiceRegistry.getInstance().removeService(HideAdminService.class);
        super.stopBundle();
    }

}
