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

package com.openexchange.file.storage.infostore.folder;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageGuestPermission;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.infostore.osgi.Services;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.ContentTypeDiscoveryService;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;

/**
 * {@link FolderParser}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderParser {

    private static final String REAL_TREE_ID = FolderStorage.REAL_TREE_ID;

    private static final String INFOSTORE = "infostore";

    private static volatile ContentType contentType;

    /**
     * Initializes a new {@link FolderParser}.
     */
    private FolderParser() {
        super();
    }

    /**
     * Parses a folder from given file storage folder.
     *
     * @param fsFolder The file storage folder
     * @return The parsed folder
     * @throws OXException If parsing folder fails
     */
    public static Folder parseFolder(final FileStorageFolder fsFolder) throws OXException {
        if (null == fsFolder) {
            return null;
        }
        try {
            final ParsedFolder folder = new ParsedFolder();
            folder.setTreeID(REAL_TREE_ID);
            {
                final String str = fsFolder.getId();
                if (!com.openexchange.java.Strings.isEmpty(str)) {
                    folder.setID(str);
                }
            }
            {
                final String str = fsFolder.getParentId();
                if (!com.openexchange.java.Strings.isEmpty(str)) {
                    folder.setParentID(str);
                }
            }
            {
                final String str = fsFolder.getName();
                if (!com.openexchange.java.Strings.isEmpty(str)) {
                    folder.setName(str);
                }
            }
            {
                folder.setContentType(getContentType());
            }
            folder.setSubscribed(fsFolder.isSubscribed());
            {
                final List<FileStoragePermission> permissions = fsFolder.getPermissions();
                if (null != permissions && !permissions.isEmpty()) {
                    folder.setPermissions(parsePermission(permissions));
                }
            }

            folder.setMeta(fsFolder.getMeta());

            return folder;
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public static ContentType getContentType() throws OXException {
        ContentType ct = contentType;
        if (null == ct) {
            synchronized (FolderParser.class) {
                ct = contentType;
                if (null == ct) {
                    try {
                        final ContentTypeDiscoveryService discoveryService = Services.getService(ContentTypeDiscoveryService.class);
                        ct = discoveryService.getByString(INFOSTORE);
                        if (null == ct) {
                            throw FolderExceptionErrorMessage.UNKNOWN_CONTENT_TYPE.create(INFOSTORE);
                        }
                        contentType = ct;
                    } catch (final OXException e) {
                        throw e;
                    }
                }
            }
        }
        return ct;
    }

    /**
     * Parses a list of file storage permissions and transforms them into permissions used by the folder service.
     *
     * @param fileStoragePermissions The file storage permissions to parse
     * @return The parsed permissions
     */
    private static Permission[] parsePermission(List<FileStoragePermission> fileStoragePermissions) throws OXException {
        if (null == fileStoragePermissions) {
            return null;
        }
        List<Permission> permissions = new ArrayList<Permission>(fileStoragePermissions.size());
        for (FileStoragePermission fileStoragePermission : fileStoragePermissions) {
            ParsedPermission permission;
            if (FileStorageGuestPermission.class.isInstance(fileStoragePermission)) {
                ParsedGuestPermission guestPermission = new ParsedGuestPermission();
                guestPermission.setRecipient(((FileStorageGuestPermission) fileStoragePermission).getRecipient());
                permission = guestPermission;
            } else {
                permission = new ParsedPermission();
            }
            permission.setEntity(fileStoragePermission.getEntity());
            permission.setGroup(fileStoragePermission.isGroup());
            permission.setAdmin(fileStoragePermission.isAdmin());
            permission.setAllPermissions(fileStoragePermission.getFolderPermission(), fileStoragePermission.getReadPermission(),
                fileStoragePermission.getWritePermission(), fileStoragePermission.getDeletePermission());
            permissions.add(permission);
        }
        return permissions.toArray(new Permission[fileStoragePermissions.size()]);
    }
}
