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

package com.openexchange.file.storage.webdav.osgi;

import static org.slf4j.LoggerFactory.getLogger;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.webdav.generic.GenericWebDAVFileStorageService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.webdav.client.WebDAVClientFactory;

/**
 * {@link WebDAVFileStorageActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.4
 */
public class WebDAVFileStorageActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link WebDAVFileStorageActivator}.
     */
    public WebDAVFileStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {
            FileStorageAccountManagerLookupService.class, WebDAVClientFactory.class, CapabilityService.class
        };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            getLogger(WebDAVFileStorageActivator.class).info("starting bundle {}", context.getBundle());
            /*
             * register genric WebDAV file storage service
             */
            registerService(FileStorageService.class, new GenericWebDAVFileStorageService(this));
        } catch (Exception e) {
            getLogger(WebDAVFileStorageActivator.class).error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        getLogger(WebDAVFileStorageActivator.class).info("stopping bundle {}", context.getBundle());
        super.stopBundle();
    }


}
