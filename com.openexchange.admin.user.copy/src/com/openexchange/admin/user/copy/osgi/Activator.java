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

package com.openexchange.admin.user.copy.osgi;

import com.openexchange.admin.daemons.AdminDaemonService;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.copy.UserCopyService;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Activator extends HousekeepingActivator {

    public Activator() {
        super();
    }

    @Override
    public void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Activator.class);
        try {
            AdminCache.compareAndSetBundleContext(null, context);
            ConfigurationService configurationService = getService(ConfigurationService.class);
            AdminCache.compareAndSetConfigurationService(null, configurationService);
            track(UserCopyService.class, new RMIUserCopyRegisterer(context));
            openTrackers();
            log.info("Started bundle: com.openexchange.admin.user.copy");
        } catch (Exception e) {
            log.error("Error starting bundle: com.openexchange.admin.user.copy", e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Activator.class);
        try {
            closeTrackers();
            super.stopBundle();
            log.info("Stopped bundle: com.openexchange.admin.user.copy");
        } catch (Exception e) {
            log.error("Error stopping bundle: com.openexchange.admin.user.copy", e);
            throw e;
        }
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, AdminDaemonService.class };
    }
}
