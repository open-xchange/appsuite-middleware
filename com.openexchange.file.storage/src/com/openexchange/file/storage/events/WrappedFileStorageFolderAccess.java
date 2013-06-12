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

package com.openexchange.file.storage.events;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;

/**
 * {@link WrappedFileStorageFolderAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class WrappedFileStorageFolderAccess implements FileStorageFolderAccess {

    private final FileStorageFolderAccess delegate;

    public WrappedFileStorageFolderAccess(FileStorageFolderAccess delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public boolean exists(String folderId) throws OXException {
        return delegate.exists(folderId);
    }

    @Override
    public FileStorageFolder getFolder(String folderId) throws OXException {
        return delegate.getFolder(folderId);
    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        return delegate.getPersonalFolder();
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        return delegate.getPublicFolders();
    }

    @Override
    public FileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        return delegate.getSubfolders(parentIdentifier, all);
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        return delegate.getRootFolder();
    }

    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        String newId = delegate.createFolder(toCreate);
        //
        return newId;
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        String newId = delegate.updateFolder(identifier, toUpdate);
        //
        return newId;
    }

    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        String newId = delegate.moveFolder(folderId, newParentId);
        //
        return newId;
    }

    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        String newId = delegate.renameFolder(folderId, newName);
        //
        return newId;
    }

    @Override
    public String deleteFolder(String folderId) throws OXException {
        String id = delegate.deleteFolder(folderId);
        //
        return id;
    }

    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        String id = delegate.deleteFolder(folderId, hardDelete);
        //
        return id;
    }

    @Override
    public void clearFolder(String folderId) throws OXException {
        delegate.clearFolder(folderId);
    }

    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {
        delegate.clearFolder(folderId, hardDelete);
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        return delegate.getPath2DefaultFolder(folderId);
    }

    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        return delegate.getStorageQuota(folderId);
    }

    @Override
    public Quota getFileQuota(String folderId) throws OXException {
        return delegate.getFileQuota(folderId);
    }

    @Override
    public Quota[] getQuotas(String folder, Type[] types) throws OXException {
        return delegate.getQuotas(folder, types);
    }

}
