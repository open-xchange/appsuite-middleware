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

package com.openexchange.folderstorage.internal;

import java.util.Date;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderFilter;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.internal.actions.AllVisibleFolders;
import com.openexchange.folderstorage.internal.actions.Clear;
import com.openexchange.folderstorage.internal.actions.Create;
import com.openexchange.folderstorage.internal.actions.Delete;
import com.openexchange.folderstorage.internal.actions.Get;
import com.openexchange.folderstorage.internal.actions.List;
import com.openexchange.folderstorage.internal.actions.Path;
import com.openexchange.folderstorage.internal.actions.Update;
import com.openexchange.folderstorage.internal.actions.Updates;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link FolderServiceImpl} - TODO Short description of this class' purpose.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderServiceImpl implements FolderService {

    /**
     * Initializes a new {@link FolderServiceImpl}.
     */
    public FolderServiceImpl() {
        super();
    }

    public void clearFolder(final String treeId, final String folderId, final User user, final Context contex) throws FolderException {
        new Clear(user, contex).doClear(treeId, folderId);
    }

    public void clearFolder(final String treeId, final String folderId, final Session session) throws FolderException {
        try {
            new Clear(new ServerSessionAdapter(session)).doClear(treeId, folderId);
        } catch (final ContextException e) {
            throw new FolderException(e);
        }
    }

    public String createFolder(final Folder folder, final User user, final Context context) throws FolderException {
        return new Create(user, context).doCreate(folder);
    }

    public String createFolder(final Folder folder, final Session session) throws FolderException {
        try {
            return new Create(new ServerSessionAdapter(session)).doCreate(folder);
        } catch (final ContextException e) {
            throw new FolderException(e);
        }
    }

    public void deleteFolder(final String treeId, final String folderId, final Date timeStamp, final User user, final Context context) throws FolderException {
        new Delete(user, context).doDelete(treeId, folderId, timeStamp);

    }

    public void deleteFolder(final String treeId, final String folderId, final Date timeStamp, final Session session) throws FolderException {
        try {
            new Delete(new ServerSessionAdapter(session)).doDelete(treeId, folderId, timeStamp);
        } catch (final ContextException e) {
            throw new FolderException(e);
        }
    }

    public UserizedFolder getDefaultFolder(final User user, final String treeId, final ContentType contentType, final User ruser, final Context context) throws FolderException {
        // TODO Auto-generated method stub
        return null;
    }

    public UserizedFolder getDefaultFolder(final User user, final String treeId, final ContentType contentType, final Session session) throws FolderException {
        // TODO Auto-generated method stub
        return null;
    }

    public UserizedFolder getFolder(final String treeId, final String folderId, final User user, final Context context) throws FolderException {
        return new Get(user, context).doGet(treeId, folderId);
    }

    public UserizedFolder getFolder(final String treeId, final String folderId, final Session session) throws FolderException {
        try {
            return new Get(new ServerSessionAdapter(session)).doGet(treeId, folderId);
        } catch (final ContextException e) {
            throw new FolderException(e);
        }
    }

    public UserizedFolder[] getAllVisibleFolders(final String treeId, final FolderFilter filter, final User user, final Context context) throws FolderException {
        return new AllVisibleFolders(user, context).doAllVisibleFolders(treeId, filter);
    }

    public UserizedFolder[] getAllVisibleFolders(final String treeId, final FolderFilter filter, final Session session) throws FolderException {
        try {
            return new AllVisibleFolders(new ServerSessionAdapter(session)).doAllVisibleFolders(treeId, filter);
        } catch (final ContextException e) {
            throw new FolderException(e);
        }
    }

    public UserizedFolder[] getPath(final String treeId, final String folderId, final User user, final Context context) throws FolderException {
        return new Path(user, context).doPath(treeId, folderId, true);
    }

    public UserizedFolder[] getPath(final String treeId, final String folderId, final Session session) throws FolderException {
        try {
            return new Path(new ServerSessionAdapter(session)).doPath(treeId, folderId, true);
        } catch (final ContextException e) {
            throw new FolderException(e);
        }
    }

    public UserizedFolder[] getSubfolders(final String treeId, final String parentId, final boolean all, final User user, final Context context) throws FolderException {
        return new List(user, context).doList(treeId, parentId, all);
    }

    public UserizedFolder[] getSubfolders(final String treeId, final String parentId, final boolean all, final Session session) throws FolderException {
        try {
            return new List(new ServerSessionAdapter(session)).doList(treeId, parentId, all);
        } catch (final ContextException e) {
            throw new FolderException(e);
        }
    }

    public UserizedFolder[][] getUpdates(final String treeId, final Date timeStamp, final boolean ignoreDeleted, final ContentType[] includeContentTypes, final User user, final Context context) throws FolderException {
        return new Updates(user, context).doUpdates(treeId, timeStamp, ignoreDeleted, includeContentTypes);
    }

    public UserizedFolder[][] getUpdates(final String treeId, final Date timeStamp, final boolean ignoreDeleted, final ContentType[] includeContentTypes, final Session session) throws FolderException {
        try {
            return new Updates(new ServerSessionAdapter(session)).doUpdates(treeId, timeStamp, ignoreDeleted, includeContentTypes);
        } catch (final ContextException e) {
            throw new FolderException(e);
        }
    }

    public void subscribeFolder(final String sourceTreeId, final String folderId, final String targetTreeId, final String targetParentId, final User user, final Context context) throws FolderException {
        // TODO Auto-generated method stub

    }

    public void subscribeFolder(final String sourceTreeId, final String folderId, final String targetTreeId, final String targetParentId, final Session session) throws FolderException {
        // TODO Auto-generated method stub

    }

    public void unsubscribeFolder(final String treeId, final String folderId, final User user, final Context context) throws FolderException {
        // TODO Auto-generated method stub

    }

    public void unsubscribeFolder(final String treeId, final String folderId, final Session session) throws FolderException {
        // TODO Auto-generated method stub

    }

    public void updateFolder(final Folder folder, final Date timeStamp, final User user, final Context context) throws FolderException {
        new Update(user, context).doUpdate(folder, timeStamp);
    }

    public void updateFolder(final Folder folder, final Date timeStamp, final Session session) throws FolderException {
        try {
            new Update(new ServerSessionAdapter(session)).doUpdate(folder, timeStamp);
        } catch (final ContextException e) {
            throw new FolderException(e);
        }
    }

}
