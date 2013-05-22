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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.file.storage.infostore.folder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;

/**
 * {@link FolderWriter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderWriter {

    /**
     * Initializes a new {@link FolderWriter}.
     */
    private FolderWriter() {
        super();
    }

    /**
     * Writes a folder.
     *
     * @param folder The folder
     * @return The written folder
     * @throws OXException If writing folder fails
     */
    public static FileStorageFolder writeFolder(final UserizedFolder folder) throws OXException {
        if (null == folder) {
            return null;
        }
        try {
            final DefaultFileStorageFolder ret = new DefaultFileStorageFolder();
            ret.setCreationDate(folder.getCreationDateUTC());
            ret.setDefaultFolder(folder.isDefault());
            ret.setExists(true);
            ret.setId(folder.getID());
            ret.setLastModifiedDate(folder.getLastModifiedUTC());
            ret.setName(folder.getName());
            ret.setParentId(folder.getParentID());
            ret.setPermissions(parsePermission(folder.getPermissions()));
            ret.setOwnPermission(parsePermission(folder.getOwnPermission()));
            ret.setRootFolder(folder.getParentID() == null);
            ret.setSubscribed(folder.isSubscribed());
            {
                final String[] subfolderIDs = folder.getSubfolderIDs();
                ret.setSubfolders(subfolderIDs != null && subfolderIDs.length > 0);
            }
            return ret;
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Parses given permission.
     *
     * @param permission The permission to parse
     * @return The parsed permission
     * @throws OXException If parsing fails
     */
    public static FileStoragePermission parsePermission(final Permission permission) throws OXException {
        if (null == permission) {
            return null;
        }
        try {
            final int entity = permission.getEntity();
            final DefaultFileStoragePermission oclPerm = DefaultFileStoragePermission.newInstance();
            oclPerm.setEntity(entity);
            oclPerm.setGroup(permission.isGroup());
            oclPerm.setAdmin(permission.isAdmin());
            oclPerm.setAllPermissions(
                permission.getFolderPermission(),
                permission.getReadPermission(),
                permission.getWritePermission(),
                permission.getDeletePermission());
                return (oclPerm);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Parses given permissions.
     *
     * @param permissions The permissions to parse
     * @return The parsed permissions
     * @throws OXException If parsing fails
     */
    public static List<FileStoragePermission> parsePermission(final Permission[] permissions) throws OXException {
        if (null == permissions) {
            return Collections.emptyList();
        }
        try {
            final int numberOfPermissions = permissions.length;
            final List<FileStoragePermission> perms = new ArrayList<FileStoragePermission>(numberOfPermissions);
            for (int i = 0; i < numberOfPermissions; i++) {
                final Permission elem = permissions[i];
                final int entity = elem.getEntity();
                final DefaultFileStoragePermission oclPerm = DefaultFileStoragePermission.newInstance();
                oclPerm.setEntity(entity);
                oclPerm.setGroup(elem.isGroup());
                oclPerm.setAdmin(elem.isAdmin());
                oclPerm.setAllPermissions(
                    elem.getFolderPermission(),
                    elem.getReadPermission(),
                    elem.getWritePermission(),
                    elem.getDeletePermission());
                perms.add(oclPerm);
            }
            return perms;
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /** Checks for an empty string */
    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
