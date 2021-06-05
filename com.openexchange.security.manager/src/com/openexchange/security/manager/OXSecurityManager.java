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

package com.openexchange.security.manager;

import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import java.util.List;
import org.osgi.service.condpermadmin.ConditionInfo;
import org.osgi.service.permissionadmin.PermissionInfo;
import com.openexchange.security.manager.impl.FolderPermission;

/**
 * {@link OXSecurityManager} Service to add Permissions to the OX Security Manager
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.3
 */
public interface OXSecurityManager extends Reloadable {

    /**
     * Append a folder permission to the end of the permission list
     * Remember, order matters, as the first rule that matches will be applied
     *
     * @param folderPermission
     * @throws OXException
     */
    void appendFolderPolicy(FolderPermission folderPermission) throws OXException;

    /**
     * Inserts folder permissions to the start of the permission list
     * Remember, order matters, as the first rule that matches will be applied
     *
     * @param folderPermissions
     * @throws OXException
     */
    void insertFolderPolicy(List<FolderPermission> folderPermissions) throws OXException;

    /**
     * Load rules from a policy file specified by the java parameters at startup
     *
     * @throws OXException
     */
    void loadFromPolicyFile() throws OXException;

    /**
     * Loads the configurations from the security-list files and updates permissions
     *
     * @throws OXException
     */
    void updateFromConfiguration() throws OXException;

    /**
     * Add into existing security policy list a new rule
     *
     * @param conditions    The condition info for determining if the rule applies
     * @param permissions   The permissions
     * @param name          Name of the rule
     * @param access        Access
     */
    void insertPolicy(ConditionInfo[] conditions, PermissionInfo[] permissions, String name, String access);

}
