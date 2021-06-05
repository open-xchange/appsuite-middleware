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

package com.openexchange.filemanagement.osgi;

import java.io.File;
import java.io.FileFilter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;

/**
 * {@link TmpFileCleaner}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class TmpFileCleaner implements ServiceTrackerCustomizer<ConfigurationService, ConfigurationService> {

    /**
     * Initializes a new {@link TmpFileCleaner}.
     */
    public TmpFileCleaner() {
        super();
    }

    @Override
    public ConfigurationService addingService(ServiceReference<ConfigurationService> reference) {
        File dir = ServerConfig.getTmpDir();
        if (dir.isDirectory() && dir.canWrite()) {
            // List files starting with either "open-xchange-" or "openexchange" prefix
            File[] tmpFiles = dir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    String name = pathname.getName();
                    return name.startsWith("open-xchange-") || name.startsWith("openexchange");
                }
            });

            // Delete those remnants
            if (null != tmpFiles) {
                for (File file : tmpFiles) {
                    try {
                        file.delete();
                    } catch (Exception x) {
                        // Ignore
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void modifiedService(ServiceReference<ConfigurationService> reference, ConfigurationService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<ConfigurationService> reference, ConfigurationService service) {
        // Nothing to do.
    }
}
