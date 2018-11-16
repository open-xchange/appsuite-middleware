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

package com.openexchange.config.admin.internal;

import java.util.Arrays;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.internal.UserizedFolderImpl;

/**
 * {@link PermissionFilterUserizedFolderImpl} overrides {@link UserizedFolderImpl} to filter out administrators permission
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class PermissionFilterUserizedFolderImpl extends UserizedFolderImpl implements UserizedFolder {

    private static final long serialVersionUID = 3941871306938103932L;
    private final int adminUserId;

    /**
     * Initializes a new {@link PermissionFilterUserizedFolderImpl} from specified folder.
     * 
     * @param adminUserId The user id of the context admin
     * @param userizedFolder The requested origin {@link UserizedFolder}
     *
     * @throws IllegalArgumentException If folder is <code>null</code>
     */
    public PermissionFilterUserizedFolderImpl(int adminUserId, UserizedFolder userizedFolder) {
        super(userizedFolder);
        this.adminUserId = adminUserId;
    }

    /**
     * {@inheritDoc}
     * 
     * The returned {@link Permission}s will not contain one for the administrator even she actually has got {@link Permission}s for the given {@link UserizedFolder}. So this implementation should be used to view permissions only.
     */
    @Override
    public Permission[] getPermissions() {
        Permission[] permissions = super.getPermissions();
        if (permissions == null || permissions.length == 0) {
            return permissions;
        }
        return Arrays.stream(permissions).filter(x -> x.getEntity() != adminUserId).toArray(Permission[]::new);
    }
}
