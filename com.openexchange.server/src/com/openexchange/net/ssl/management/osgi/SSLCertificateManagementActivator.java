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

package com.openexchange.net.ssl.management.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.net.ssl.management.SSLCertificateManagementService;
import com.openexchange.net.ssl.management.internal.SSLCertificateManagementServiceImpl;
import com.openexchange.net.ssl.management.storage.AddHashHostColumnUpdateTask;
import com.openexchange.net.ssl.management.storage.CreateSSLCertificateManagementTable;
import com.openexchange.net.ssl.management.storage.CreateSSLCertificateManagementTableTask;
import com.openexchange.net.ssl.management.storage.SSLCertificateManagementTableUtf8Mb4UpdateTask;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link SSLCertificateManagementActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SSLCertificateManagementActivator extends HousekeepingActivator {

    /**
     * Initialises a new {@link SSLCertificateManagementActivator}.
     */
    public SSLCertificateManagementActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(CreateTableService.class, new CreateSSLCertificateManagementTable());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new CreateSSLCertificateManagementTableTask(getService(DatabaseService.class)), new AddHashHostColumnUpdateTask(), new SSLCertificateManagementTableUtf8Mb4UpdateTask()));
        registerService(SSLCertificateManagementService.class, new SSLCertificateManagementServiceImpl(this));

        Logger logger = LoggerFactory.getLogger(SSLCertificateManagementActivator.class);
        logger.info("SSLCertificateManagementService registered successfully");
    }

    @Override
    protected void stopBundle() throws Exception {
        unregisterService(SSLCertificateManagementService.class);
        Logger logger = LoggerFactory.getLogger(SSLCertificateManagementActivator.class);
        logger.info("SSLCertificateManagementService unregistered successfully");
        super.stopBundle();
    }

}
