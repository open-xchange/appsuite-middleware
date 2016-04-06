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

package com.openexchange.groupware.infostore.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.tx.DBService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.CapabilityUserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationCodes;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;

public class DelUserFolderDiscoverer extends DBService {

    public DelUserFolderDiscoverer() {
        super();
    }

    public DelUserFolderDiscoverer(final DBProvider provider) {
        super(provider);
    }

    public List<FolderObject> discoverFolders(final int userId, final Context ctx, boolean includeShared) throws OXException {
        final List<FolderObject> discovered = new ArrayList<FolderObject>();
        final User user = UserStorage.getInstance().getUser(userId, ctx);
        int[] accessibleModules;
        try {
            accessibleModules = CapabilityUserConfigurationStorage.loadUserConfiguration(userId, ctx).getAccessibleModules();
        } catch (final OXException e) {
            if (!UserConfigurationCodes.NOT_FOUND.equals(e)) {
                throw e;
            }
            accessibleModules = null;
        }

        final Queue<FolderObject> queue = ((FolderObjectIterator) OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(
            userId,
            user.getGroups(),
            accessibleModules,
            FolderObject.INFOSTORE,
            ctx)).asQueue();
        folder: for (final FolderObject fo : queue) {
            if (isVirtual(fo)) {
                continue folder;
            }
            for (final OCLPermission perm : fo.getPermissionsAsArray()) {
                if (!includeShared && someoneElseMayReadInfoitems(perm, userId)) {
                    continue folder;
                }
            }
            discovered.add(fo);
        }

        return discovered;
    }

    private boolean isVirtual(final FolderObject fo) {
        final int id = fo.getObjectID();
        return id == FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID || id == FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID;
    }

    private boolean someoneElseMayReadInfoitems(final OCLPermission perm, final int userId) {
       return (perm.isGroupPermission() || perm.getEntity() != userId) && (perm.canReadAllObjects() || perm.canReadOwnObjects());
    }
}
