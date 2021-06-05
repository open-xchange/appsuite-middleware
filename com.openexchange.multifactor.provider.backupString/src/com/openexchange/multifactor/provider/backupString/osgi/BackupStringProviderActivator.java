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

package com.openexchange.multifactor.provider.backupString.osgi;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.login.multifactor.MultifactorLoginService;
import com.openexchange.multifactor.MultifactorProvider;
import com.openexchange.multifactor.provider.backupString.impl.MultifactorBackupStringProvider;
import com.openexchange.multifactor.provider.backupString.storage.BackupStringMultifactorDeviceStorage;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link BackupStringProviderActivator}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class BackupStringProviderActivator extends HousekeepingActivator {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(BackupStringProviderActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { LeanConfigurationService.class, BackupStringMultifactorDeviceStorage.class, MultifactorLoginService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = super.context;
        LOG.info("Starting bundle {}", context.getBundle().getSymbolicName());
        final MultifactorBackupStringProvider backupProvider = new MultifactorBackupStringProvider(
            getServiceSafe(LeanConfigurationService.class),
            getServiceSafe(BackupStringMultifactorDeviceStorage.class),
            getServiceSafe(MultifactorLoginService.class));
        registerService(MultifactorProvider.class, backupProvider);
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle {}", context.getBundle().getSymbolicName());
        super.stopBundle();
    }
}
