/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
