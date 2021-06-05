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

package com.openexchange.filemanagement.distributed.servlet.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.filemanagement.DistributedFileUtils;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.filemanagement.distributed.servlet.DistributedFileServlet;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.service.http.HttpServices;

/**
 * Activator for "com.openexchange.filemanagement.distributed.servlet" bundle.
 */
public class Activator extends HousekeepingActivator {

    private String alias;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HttpService.class, ManagedFileManagement.class, ConfigurationService.class, DistributedFileUtils.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        HttpService service = getService(HttpService.class);
        String alias = com.openexchange.filemanagement.DistributedFileManagement.PATH;
        service.registerServlet(alias, new DistributedFileServlet(this), null, null);
        this.alias = alias;
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        HttpService service = getService(HttpService.class);
        if (null != service) {
            String alias = this.alias;
            if (null != alias) {
                this.alias = null;
                HttpServices.unregister(alias, service);
            }
        }
        super.stopBundle();
    }

}
