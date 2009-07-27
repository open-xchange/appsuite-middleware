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

package com.openexchange.folderstorage.virtual;

import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StoragePriority;

/**
 * {@link VirtualFolderStorage} - TODO Short description of this class' purpose.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class VirtualFolderStorage implements FolderStorage {

    private final String treeId;

    /**
     * Initializes a new {@link VirtualFolderStorage}.
     * 
     * @param treeId The tree identifier
     */
    public VirtualFolderStorage(final String treeId) {
        super();
        this.treeId = treeId;
    }

    public void commitTransaction(final StorageParameters params) throws FolderException {
        
    }

    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        
    }

    public void deleteFolder(final String folderId, final StorageParameters storageParameters) throws FolderException {
        
    }

    public Folder getDefaultFolder(final int entity, final ContentType contentType, final StorageParameters storageParameters) throws FolderException {
        // TODO Auto-generated method stub
        return null;
    }

    public Folder getFolder(final String folderId, final StorageParameters storageParameters) throws FolderException {
        // TODO Auto-generated method stub
        return null;
    }

    public FolderType getFolderType() {
        // TODO Auto-generated method stub
        return null;
    }

    public StoragePriority getStoragePriority() {
        // TODO Auto-generated method stub
        return null;
    }

    public SortableId[] getSubfolders(final String parentId, final StorageParameters storageParameters) throws FolderException {
        // TODO Auto-generated method stub
        return null;
    }

    public void rollback(final StorageParameters params) {
        // TODO Auto-generated method stub
        
    }

    public StorageParameters startTransaction(final StorageParameters parameters, final boolean modify) throws FolderException {
        // TODO Auto-generated method stub
        return null;
    }

    public void updateFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        // TODO Auto-generated method stub
        
    }

    
    
}
