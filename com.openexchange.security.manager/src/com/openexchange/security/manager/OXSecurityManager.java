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
