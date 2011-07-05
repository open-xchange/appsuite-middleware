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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderFilter;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.internal.performers.AllVisibleFoldersPerformer;
import com.openexchange.folderstorage.internal.performers.ClearPerformer;
import com.openexchange.folderstorage.internal.performers.ConsistencyPerformer;
import com.openexchange.folderstorage.internal.performers.CreatePerformer;
import com.openexchange.folderstorage.internal.performers.DeletePerformer;
import com.openexchange.folderstorage.internal.performers.GetPerformer;
import com.openexchange.folderstorage.internal.performers.ListPerformer;
import com.openexchange.folderstorage.internal.performers.PathPerformer;
import com.openexchange.folderstorage.internal.performers.UpdatePerformer;
import com.openexchange.folderstorage.internal.performers.UpdatesPerformer;
import com.openexchange.folderstorage.internal.performers.VisibleFoldersPerformer;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link FolderServiceImpl} - The {@link FolderService} implementation.
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

    public void checkConsistency(final String treeId, final User user, final Context context) throws OXException {
        new ConsistencyPerformer(user, context).doConsistencyCheck(treeId);
    }
    
    public void checkConsistency(final String treeId, final Session session) throws OXException {
        try {
            new ConsistencyPerformer(new ServerSessionAdapter(session)).doConsistencyCheck(treeId);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    public void clearFolder(final String treeId, final String folderId, final User user, final Context contex) throws OXException {
        new ClearPerformer(user, contex).doClear(treeId, folderId);
    }

    public void clearFolder(final String treeId, final String folderId, final Session session) throws OXException {
        try {
            new ClearPerformer(new ServerSessionAdapter(session)).doClear(treeId, folderId);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    public FolderResponse<String> createFolder(final Folder folder, final User user, final Context context) throws OXException {
        final CreatePerformer createPerformer = new CreatePerformer(user, context);
        return FolderResponseImpl.newFolderResponse(createPerformer.doCreate(folder), createPerformer.getStorageParameters().getWarnings());
    }

    public FolderResponse<String> createFolder(final Folder folder, final Session session) throws OXException {
        try {
            final CreatePerformer createPerformer = new CreatePerformer(new ServerSessionAdapter(session));
            return FolderResponseImpl.newFolderResponse(createPerformer.doCreate(folder), createPerformer.getStorageParameters().getWarnings());
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    public void deleteFolder(final String treeId, final String folderId, final Date timeStamp, final User user, final Context context) throws OXException {
        new DeletePerformer(user, context).doDelete(treeId, folderId, timeStamp);

    }

    public void deleteFolder(final String treeId, final String folderId, final Date timeStamp, final Session session) throws OXException {
        try {
            new DeletePerformer(new ServerSessionAdapter(session)).doDelete(treeId, folderId, timeStamp);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    public UserizedFolder getDefaultFolder(final User user, final String treeId, final ContentType contentType, final User ruser, final Context context, final FolderServiceDecorator decorator) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    public UserizedFolder getDefaultFolder(final User user, final String treeId, final ContentType contentType, final Session session, final FolderServiceDecorator decorator) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    public UserizedFolder getFolder(final String treeId, final String folderId, final User user, final Context context, final FolderServiceDecorator decorator) throws OXException {
        return new GetPerformer(user, context, decorator).doGet(treeId, folderId);
    }

    public UserizedFolder getFolder(final String treeId, final String folderId, final Session session, final FolderServiceDecorator decorator) throws OXException {
        try {
            return new GetPerformer(new ServerSessionAdapter(session), decorator).doGet(treeId, folderId);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    public FolderResponse<UserizedFolder[]> getAllVisibleFolders(final String treeId, final FolderFilter filter, final User user, final Context context, final FolderServiceDecorator decorator) throws OXException {
        final AllVisibleFoldersPerformer performer = new AllVisibleFoldersPerformer(user, context, decorator);
        return FolderResponseImpl.newFolderResponse(performer.doAllVisibleFolders(treeId, filter), performer.getWarnings());
    }

    public FolderResponse<UserizedFolder[]> getAllVisibleFolders(final String treeId, final FolderFilter filter, final Session session, final FolderServiceDecorator decorator) throws OXException {
        try {
            final AllVisibleFoldersPerformer performer = new AllVisibleFoldersPerformer(new ServerSessionAdapter(session), decorator);
            return FolderResponseImpl.newFolderResponse(performer.doAllVisibleFolders(treeId, filter), performer.getWarnings());
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    public FolderResponse<UserizedFolder[]> getPath(final String treeId, final String folderId, final User user, final Context context, final FolderServiceDecorator decorator) throws OXException {
        final PathPerformer performer = new PathPerformer(user, context, decorator);
        return FolderResponseImpl.newFolderResponse(performer.doPath(treeId, folderId, true), performer.getWarnings());
    }

    public FolderResponse<UserizedFolder[]> getPath(final String treeId, final String folderId, final Session session, final FolderServiceDecorator decorator) throws OXException {
        try {
            final PathPerformer performer = new PathPerformer(new ServerSessionAdapter(session), decorator);
            return FolderResponseImpl.newFolderResponse(performer.doPath(treeId, folderId, true), performer.getWarnings());
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    public FolderResponse<UserizedFolder[]> getVisibleFolders(final String treeId, final ContentType contentType, final Type type, final boolean all, final User user, final Context context, final FolderServiceDecorator decorator) throws OXException {
        final VisibleFoldersPerformer performer = new VisibleFoldersPerformer(user, context, decorator);
        return FolderResponseImpl.newFolderResponse(performer.doVisibleFolders(treeId, contentType, type, all), performer.getWarnings());
    }

    public FolderResponse<UserizedFolder[]> getVisibleFolders(final String treeId, final ContentType contentType, final Type type, final boolean all, final Session session, final FolderServiceDecorator decorator) throws OXException {
        try {
            final VisibleFoldersPerformer performer = new VisibleFoldersPerformer(new ServerSessionAdapter(session), decorator);
            return FolderResponseImpl.newFolderResponse(performer.doVisibleFolders(treeId, contentType, type, all), performer.getWarnings());
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    public FolderResponse<UserizedFolder[]> getSubfolders(final String treeId, final String parentId, final boolean all, final User user, final Context context, final FolderServiceDecorator decorator) throws OXException {
        final ListPerformer listPerformer = new ListPerformer(user, context, decorator);
        return FolderResponseImpl.newFolderResponse(listPerformer.doList(treeId, parentId, all), listPerformer.getWarnings());
    }

    public FolderResponse<UserizedFolder[]> getSubfolders(final String treeId, final String parentId, final boolean all, final Session session, final FolderServiceDecorator decorator) throws OXException {
        try {
            final ListPerformer listPerformer = new ListPerformer(new ServerSessionAdapter(session), decorator);
            return FolderResponseImpl.newFolderResponse(listPerformer.doList(treeId, parentId, all), listPerformer.getWarnings());
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    public FolderResponse<UserizedFolder[][]> getUpdates(final String treeId, final Date timeStamp, final boolean ignoreDeleted, final ContentType[] includeContentTypes, final User user, final Context context, final FolderServiceDecorator decorator) throws OXException {
        final UpdatesPerformer performer = new UpdatesPerformer(user, context, decorator);
        return FolderResponseImpl.newFolderResponse(
            performer.doUpdates(treeId, timeStamp, ignoreDeleted, includeContentTypes),
            performer.getWarnings());
    }

    public FolderResponse<UserizedFolder[][]> getUpdates(final String treeId, final Date timeStamp, final boolean ignoreDeleted, final ContentType[] includeContentTypes, final Session session, final FolderServiceDecorator decorator) throws OXException {
        try {
            final UpdatesPerformer performer = new UpdatesPerformer(new ServerSessionAdapter(session), decorator);
            return FolderResponseImpl.newFolderResponse(
                performer.doUpdates(treeId, timeStamp, ignoreDeleted, includeContentTypes),
                performer.getWarnings());
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    public void subscribeFolder(final String sourceTreeId, final String folderId, final String targetTreeId, final String targetParentId, final User user, final Context context) throws OXException {
        // TODO Auto-generated method stub

    }

    public void subscribeFolder(final String sourceTreeId, final String folderId, final String targetTreeId, final String targetParentId, final Session session) throws OXException {
        // TODO Auto-generated method stub

    }

    public void unsubscribeFolder(final String treeId, final String folderId, final User user, final Context context) throws OXException {
        // TODO Auto-generated method stub

    }

    public void unsubscribeFolder(final String treeId, final String folderId, final Session session) throws OXException {
        // TODO Auto-generated method stub

    }

    public void updateFolder(final Folder folder, final Date timeStamp, final User user, final Context context) throws OXException {
        new UpdatePerformer(user, context).doUpdate(folder, timeStamp);
    }

    public void updateFolder(final Folder folder, final Date timeStamp, final Session session) throws OXException {
        try {
            new UpdatePerformer(new ServerSessionAdapter(session)).doUpdate(folder, timeStamp);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    public Map<Integer, ContentType> getAvailableContentTypes() {
        return ContentTypeRegistry.getInstance().getAvailableContentTypes();
    }

}
