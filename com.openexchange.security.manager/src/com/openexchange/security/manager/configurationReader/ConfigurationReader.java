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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.security.manager.configurationReader;

import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.security.manager.OXSecurityManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.security.manager.impl.FolderPermission;
import com.openexchange.security.manager.impl.FolderPermission.Allow;
import com.openexchange.config.Reloadables;

/**
 * {@link ConfigurationReader}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.3
 */
public class ConfigurationReader {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigurationReader.class);

    private final OXSecurityManager securityManager;
    private final ConfigurationService configService;
    private ArrayList<String> reloadableConfigurationPaths;

    /**
     * Private class that specifies which configuration should be loaded, and what permission applies
     *
     * {@link SecurityAddition}
     *
     * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
     * @since v7.10.3
     */
    private static class SecurityAddition {
        private final String path;
        private final boolean file;
        private final Allow allow;

        public SecurityAddition (String path, boolean file, Allow allow) {
            this.path = path;
            this.file = file;
            this.allow = allow;
        }

        public String getPath() {
            return path;
        }

        public boolean isFile() {
            return file;
        }

        public Allow getAllow() {
            return allow;
        }
    }

    public ConfigurationReader(OXSecurityManager securityManager, ConfigurationService configService) {
        this.securityManager = securityManager;
        this.configService = configService;
        this.reloadableConfigurationPaths = new ArrayList<String>(1);
    }

    /**
     * Parse the configuration line for permissions and type
     *
     * @param add  The configuration to add
     * @return
     */
    private SecurityAddition getSecurityAddition (String add) {
        Allow allow = getAllow(add);
        boolean isFile = add.startsWith("file:");
        add = cleanup(add);
        if (isFile) {
            return new SecurityAddition (add, true, allow);
        }
        return new SecurityAddition(add, false, allow);
    }

    /**
     * Validate that the path is a valid property name
     * Add to reloadable list for the security manager
     *
     * @param config  Configuration
     */
    private void addReloadConfigurationPath(String config) {
        try {
            Reloadables.validatePropertyName(config);
            reloadableConfigurationPaths.add(config);
        } catch (IllegalArgumentException e) {
            // NTD.  Not a valid reloadable config path
        }
    }

    /**
     * Gets Allow type from the end of a configuration
     * @param config
     * @return
     */
    private Allow getAllow (String config) {
        if (config == null) {
            return null;
        }
        Allow allow = Allow.READ;
        if (config.endsWith(":RW")) {
            config = config.replace(":RW", "");
            allow = Allow.READ_WRITE;
        }
        if (config.endsWith(":W")) {
            config = config.replace(":W", "");
            allow = Allow.WRITE;
        }
        config = config.replace(":R", "");
        return allow;
    }

    /**
     * Removes file prefix and write permission suffixes
     * @param config
     * @return
     */
    private String cleanup(String config) {
        if (config == null) {
            return null;
        }
        return config.replace(":RW", "").replace(":R", "").replace(":W", "").replace("file:", "").trim();
    }

    /**
     * Adds a system variable containg directories, separated with ":"
     * Example is sun.boot.class.path
     * @param folderPermissions
     * @param config
     */
    private void addVariable(ArrayList<FolderPermission> folderPermissions, String var) {
        if (var == null) {
            return;
        }
        Allow allow = getAllow(var);
        var = cleanup(var);
        String permissionString = System.getProperty(var.substring(2, var.length() -1));
        if (permissionString != null) {
            String[] perms = permissionString.split(":");
            for (String perm: perms) {
                FolderPermission folder =
                    new FolderPermission(perm,
                        perm,
                        FolderPermission.Decision.ALLOW,
                        allow,
                        FolderPermission.Type.DIRECTORY);
                folderPermissions.add(folder);
            }
        }
    }

    /**
     * Add a configuration from the list file
     * Loads the config from configuration service and adds
     *
     * @param folderPermissions
     * @param config  Config to pull from the config file
     */
    private void addConfiguration(ArrayList<FolderPermission> folderPermissions, String config) {
        SecurityAddition toAdd = getSecurityAddition(config);  // Parse the configuration requirement for permissions
        String directory = configService.getProperty(toAdd.getPath());  // Get the configured value from configuration service
        addReloadConfigurationPath(toAdd.getPath());  // Keep track of configuration paths
        if (directory != null && !directory.isEmpty()) {
            // Create the folder permission
            FolderPermission folder =
                new FolderPermission(config,
                    directory,
                    FolderPermission.Decision.ALLOW,
                    toAdd.getAllow(),
                    toAdd.isFile() ? FolderPermission.Type.FILE : FolderPermission.Type.RECURSIVE);
            folderPermissions.add(folder);
        } else {
            LOG.debug("Security manager: Missing configuration for " + toAdd.getPath());
        }
    }

    /**
     * Add individual folder from the list file
     *
     * @param folderPermissions
     * @param folder  The folder to add
     */
    private void addFolder(ArrayList<FolderPermission> folderPermissions, String folder) {
        Allow allow = getAllow(folder);
        folder = cleanup(folder);
        FolderPermission folderToAdd =
            new FolderPermission(folder,
                folder,
                FolderPermission.Decision.ALLOW,
                allow,
                FolderPermission.Type.DIRECTORY);
        folderPermissions.add(folderToAdd);
    }


    /**
     * Reads the list of configuration values we need from the security files.
     * Pulls the values from config service, and if directory or file
     *
     * @throws OXException
     */
    public List<FolderPermission> readConfigFolders () throws OXException {
        List<String> configurations = null;
        try {
            configurations = new ConfigurationFileParser(configService).getConfigList();
        } catch (IOException e) {
            LOG.error("Problem loading list of configuration settings from config file for security settings", e);
        }
        if (configurations != null) {
            reloadableConfigurationPaths = new ArrayList<String>(configurations.size());
            ArrayList<FolderPermission> folderPermissions = new ArrayList<FolderPermission>(configurations.size());
            for (String config : configurations) {
                if (config.startsWith("$")) {
                    addVariable(folderPermissions, config);
                } else {
                    if (config.startsWith(File.separator)) {
                        addFolder(folderPermissions, config);
                    } else {
                        addConfiguration(folderPermissions, config);
                    }

                }
            }
            return folderPermissions;
        }
        return null;
    }


    /**
     * Return a list of reloadable configuration paths that were loaded.
     *
     * @return  List of configuration paths
     */
    public String[] getReloadableConfigurationPaths() {
        return this.reloadableConfigurationPaths.toArray(new String[reloadableConfigurationPaths.size()]);
    }

}
