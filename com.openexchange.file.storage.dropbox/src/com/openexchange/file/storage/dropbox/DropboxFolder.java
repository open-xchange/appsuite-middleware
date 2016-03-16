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

package com.openexchange.file.storage.dropbox;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.RESTUtility;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.TypeAware;

/**
 * {@link DropboxFolder}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DropboxFolder extends DefaultFileStorageFolder implements TypeAware {

    private FileStorageFolderType type;

    /**
     * Initializes a new {@link DropboxFolder}.
     *
     * @param entry The dropbox entry representing the folder
     * @param userID The identifier of the user to use as created-/modified-by information
     * @param accountDisplayName The display name of the dropbox account
     * @throws OXException
     */
    public DropboxFolder(Entry entry, int userID, String accountDisplayName) throws OXException {
        this(userID);
        parseDirEntry(entry, accountDisplayName);
    }

    /**
     * Initializes a new {@link DropboxFolder}.
     */
    public DropboxFolder(final int userId) {
        super();
        type = FileStorageFolderType.NONE;
        holdsFiles = true;
        b_holdsFiles = true;
        holdsFolders = true;
        b_holdsFolders = true;
        exists = true;
        subscribed = true;
        b_subscribed = true;
        final DefaultFileStoragePermission permission = DefaultFileStoragePermission.newInstance();
        permission.setEntity(userId);
        permissions = Collections.<FileStoragePermission> singletonList(permission);
        ownPermission = permission;
        createdBy = userId;
        modifiedBy = userId;
    }

    @Override
    public FileStorageFolderType getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type The type to set
     * @return This folder with type applied
     */
    public DropboxFolder setType(FileStorageFolderType type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return id == null ? super.toString() : id;
    }

    /**
     * Parses specified Dropbox entry.
     *
     * @param entry The Dropbox entry denoting the directory
     * @param accountDisplayName The account'S display name
     * @throws OXException If parsing Dropbox entry fails
     */
    public DropboxFolder parseDirEntry(final com.dropbox.client2.DropboxAPI.Entry entry, String accountDisplayName) throws OXException {
        if (null != entry) {
            try {
                final String path = entry.path;
                id = path;
                {
                    if ("/".equals(path)) {
                        rootFolder = true;
                        id = FileStorageFolder.ROOT_FULLNAME;
                        setParentId(null);
                        setName(null == accountDisplayName ? entry.root : accountDisplayName);
                    } else {
                        rootFolder = false;
                        final int pos = path.lastIndexOf('/');
                        setParentId(pos < 0 ? FileStorageFolder.ROOT_FULLNAME : path.substring(0, pos));
                        setName(pos < 0 ? path : path.substring(pos + 1));
                    }
                    b_rootFolder = true;
                }
                {
                    final String modified = entry.modified;
                    if (null != modified) {
                        final Date date;
                        synchronized (RESTUtility.class) {
                            date = RESTUtility.parseDate(modified);
                        }
                        creationDate = new Date(date.getTime());
                        lastModifiedDate = new Date(date.getTime());
                    }
                }
                {
                    final boolean hasSubfolders = hasSubfolder(entry);
                    setSubfolders(hasSubfolders);
                    setSubscribedSubfolders(hasSubfolders);
                }
            } catch (final RuntimeException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return this;
    }

    private boolean hasSubfolder(com.dropbox.client2.DropboxAPI.Entry entry) {
        List<com.dropbox.client2.DropboxAPI.Entry> contents = entry.contents;
        if (contents == null || contents.isEmpty()) {
            return false;
        }

        for (com.dropbox.client2.DropboxAPI.Entry subEntry : contents) {
            if (subEntry.isDir) {
                return true;
            }
        }
        return false;
    }

}
