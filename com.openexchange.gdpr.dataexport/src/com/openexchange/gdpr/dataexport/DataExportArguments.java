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

package com.openexchange.gdpr.dataexport;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link DataExportArguments} - The arguments for a submitted data export task.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportArguments {

    private List<Module> modules;
    private long maxFileSize;
    private HostInfo hostInfo;

    /**
     * Initializes a new {@link DataExportArguments}.
     */
    public DataExportArguments() {
        super();
    }

    /**
     * Gets the max. file size for file results.
     *
     * @return The max. file size
     */
    public long getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * Sets the max. file size for file results.
     *
     * @param maxFileSize The max. file size to set
     */
    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    /**
     * Gets the modules to export.
     *
     * @return The modules
     */
    public List<Module> getModules() {
        return modules;
    }

    /**
     * Sets the modules to export
     *
     * @param modulesToExport The modules to set
     * @throws IllegalArgumentException If listing contains multiple modules for the same identifier
     */
    public void setModules(List<Module> modulesToExport) {
        if (modulesToExport != null) {
            Set<String> test = new HashSet<String>(modulesToExport.size());
            for (Module module : modulesToExport) {
                if (!test.add(module.getId())) {
                    throw new IllegalArgumentException("Duplicate module identifier: " + module.getId());
                }
            }
        }
        this.modules = modulesToExport;
    }

    /**
     * Gets the host info
     *
     * @return The host info
     */
    public HostInfo getHostInfo() {
        return hostInfo;
    }

    /**
     * Sets the host info
     *
     * @param hostInfo The host info to set
     */
    public void setHostInfo(HostInfo hostInfo) {
        this.hostInfo = hostInfo;
    }

}
