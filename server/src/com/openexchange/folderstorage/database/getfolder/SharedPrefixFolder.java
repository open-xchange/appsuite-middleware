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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.database.getfolder;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import com.openexchange.api2.OXException;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.database.DatabaseFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.Groups;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.iterator.FolderObjectIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;

/**
 * {@link SharedPrefixFolder} - Gets the folder whose identifier starts with shared prefix.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SharedPrefixFolder {

    /**
     * Initializes a new {@link SharedPrefixFolder}.
     */
    private SharedPrefixFolder() {
        super();
    }

    /**
     * Gets the folder whose identifier starts with shared prefix.
     * 
     * @param folderIdentifier The folder identifier starting with shared prefix
     * @param user The user
     * @param userConfiguration The user configuration
     * @param ctx The context
     * @param con The connection
     * @return The corresponding database folder with subfolders set
     * @throws FolderException If returning corresponding database folder fails
     */
    public static DatabaseFolder getSharedPrefixFolder(final String folderIdentifier, final User user, final UserConfiguration userConfiguration, final Context ctx, final Connection con) throws FolderException {
        final int sharedOwner;
        try {
            sharedOwner = Integer.parseInt(folderIdentifier.substring(2));
        } catch (final NumberFormatException exc) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(exc, exc.getMessage());
        }
        String creatorDisplayName;
        try {
            creatorDisplayName = UserStorage.getInstance().getUser(sharedOwner, ctx).getDisplayName();
        } catch (final LdapException e) {
            if (sharedOwner != OCLPermission.ALL_GROUPS_AND_USERS) {
                throw new FolderException(e);
            }
            creatorDisplayName = new StringHelper(user.getLocale()).getString(Groups.ALL_USERS);
        }

        final FolderObject virtualOwnerFolder = FolderObject.createVirtualSharedFolderObject(sharedOwner, creatorDisplayName);
        /*
         * This highly user-specific folder is NOT cacheable
         */
        final DatabaseFolder retval = new DatabaseFolder(virtualOwnerFolder, false);
        retval.setGlobal(false);

        final Queue<FolderObject> q;
        try {
            q = ((FolderObjectIterator) OXFolderIteratorSQL.getVisibleSharedFolders(
                user.getId(),
                user.getGroups(),
                userConfiguration.getAccessibleModules(),
                sharedOwner,
                ctx,
                null,
                con)).asQueue();
        } catch (final SearchIteratorException e) {
            throw new FolderException(e);
        } catch (final OXException e) {
            throw new FolderException(e);
        }
        final int size = q.size();
        final Iterator<FolderObject> iter = q.iterator();
        final List<String> subfolderIds = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            subfolderIds.add(String.valueOf(iter.next().getObjectID()));
        }
        retval.setSubfolderIDs(subfolderIds.toArray(new String[subfolderIds.size()]));
        return retval;
    }

}
