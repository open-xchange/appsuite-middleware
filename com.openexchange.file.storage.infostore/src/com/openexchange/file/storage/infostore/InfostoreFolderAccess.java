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

package com.openexchange.file.storage.infostore;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link InfostoreFolderAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreFolderAccess implements FileStorageFolderAccess {

    /**
     * Initializes a new {@link InfostoreFolderAccess}.
     * @param session
     */
    public InfostoreFolderAccess(final ServerSession session) {
        super();
        // TODO Auto-generated constructor stub

    }

    @Override
    public void clearFolder(final String folderId) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearFolder(final String folderId, final boolean hardDelete) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public String createFolder(final FileStorageFolder toCreate) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String deleteFolder(final String folderId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String deleteFolder(final String folderId, final boolean hardDelete) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean exists(final String folderId) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Quota getFileQuota(final String folderId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileStorageFolder getFolder(final String folderId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(final String folderId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Quota[] getQuotas(final String folder, final Type[] types) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Quota getStorageQuota(final String folderId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileStorageFolder[] getSubfolders(final String parentIdentifier, final boolean all) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String moveFolder(final String folderId, final String newParentId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String updateFolder(final String identifier, final FileStorageFolder toUpdate) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

}
