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

package com.openexchange.folderstorage.internal;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderFilter;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
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
import com.openexchange.folderstorage.internal.performers.ReinitializePerformer;
import com.openexchange.folderstorage.internal.performers.SubscribePerformer;
import com.openexchange.folderstorage.internal.performers.UnsubscribePerformer;
import com.openexchange.folderstorage.internal.performers.UpdatePerformer;
import com.openexchange.folderstorage.internal.performers.UpdatesPerformer;
import com.openexchange.folderstorage.internal.performers.UserSharedFoldersPerformer;
import com.openexchange.folderstorage.internal.performers.VisibleFoldersPerformer;
import com.openexchange.folderstorage.outlook.OutlookFolderStorage;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
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

    @Override
    public void reinitialize(String treeId, Session session) throws OXException {
        ReinitializePerformer performer = new ReinitializePerformer(ServerSessionAdapter.valueOf(session));
        performer.doReinitialize(treeId);
    }

    @Override
    public FolderResponse<Void> checkConsistency(final String treeId, final User user, final Context context) throws OXException {
        final ConsistencyPerformer performer = new ConsistencyPerformer(user, context);
        performer.doConsistencyCheck(treeId);
        return FolderResponseImpl.newFolderResponse(null, performer.getWarnings());
    }

    @Override
    public FolderResponse<Void> checkConsistency(final String treeId, final Session session) throws OXException {
        final ConsistencyPerformer performer = new ConsistencyPerformer(ServerSessionAdapter.valueOf(session));
        performer.doConsistencyCheck(treeId);
        return FolderResponseImpl.newFolderResponse(null, performer.getWarnings());
    }

    @Override
    public FolderResponse<Void> clearFolder(final String treeId, final String folderId, final User user, final Context contex) throws OXException {
        final ClearPerformer performer = new ClearPerformer(user, contex);
        performer.doClear(treeId, folderId);
        return FolderResponseImpl.newFolderResponse(null, performer.getWarnings());
    }

    @Override
    public FolderResponse<Void> clearFolder(final String treeId, final String folderId, final Session session) throws OXException {
        final ClearPerformer performer = new ClearPerformer(ServerSessionAdapter.valueOf(session));
        performer.doClear(treeId, folderId);
        return FolderResponseImpl.newFolderResponse(null, performer.getWarnings());
    }

    @Override
    public FolderResponse<String> createFolder(final Folder folder, final User user, final Context context, final FolderServiceDecorator decorator) throws OXException {
        final CreatePerformer createPerformer = new CreatePerformer(user, context, decorator);
        return FolderResponseImpl.newFolderResponse(createPerformer.doCreate(folder), createPerformer.getStorageParameters().getWarnings());
    }

    @Override
    public FolderResponse<String> createFolder(final Folder folder, final Session session, final FolderServiceDecorator decorator) throws OXException {
        final CreatePerformer createPerformer = new CreatePerformer(ServerSessionAdapter.valueOf(session), decorator);
        return FolderResponseImpl.newFolderResponse(createPerformer.doCreate(folder), createPerformer.getStorageParameters().getWarnings());
    }

    @Override
    public FolderResponse<Void> deleteFolder(final String treeId, final String folderId, final Date timeStamp, final User user, final Context context, final FolderServiceDecorator decorator) throws OXException {
        final DeletePerformer performer = new DeletePerformer(user, context, decorator);
        performer.doDelete(treeId, folderId, timeStamp);
        return FolderResponseImpl.newFolderResponse(null, performer.getWarnings());

    }

    @Override
    public FolderResponse<Void> deleteFolder(final String treeId, final String folderId, final Date timeStamp, final Session session, final FolderServiceDecorator decorator) throws OXException {
        final DeletePerformer performer = new DeletePerformer(ServerSessionAdapter.valueOf(session), decorator);
        performer.doDelete(treeId, folderId, timeStamp);
        return FolderResponseImpl.newFolderResponse(null, performer.getWarnings());
    }

    @Override
    public UserizedFolder getDefaultFolder(final User user, final String treeId, final ContentType contentType, final User ruser, final Context context, final FolderServiceDecorator decorator) throws OXException {
        return getDefaultFolder(user, treeId, contentType, PrivateType.getInstance(), ruser, context, decorator);
    }

    @Override
    public UserizedFolder getDefaultFolder(final User user, final String treeId, final ContentType contentType, final Type type, final User ruser, final Context context, final FolderServiceDecorator decorator) throws OXException {
        final FolderStorage folderStorage = FolderStorageRegistry.getInstance().getFolderStorageByContentType(treeId, contentType);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(treeId, contentType.toString());
        }
        final String folderId = folderStorage.getDefaultFolderID(user, treeId, contentType, type, new StorageParametersImpl(user, context));
        return new GetPerformer(user, context, decorator).doGet(treeId, folderId);
    }

    @Override
    public UserizedFolder getDefaultFolder(final User user, final String treeId, final ContentType contentType, final Session session, final FolderServiceDecorator decorator) throws OXException {
        return getDefaultFolder(user, treeId, contentType, PrivateType.getInstance(), session, decorator);
    }

    @Override
    public UserizedFolder getDefaultFolder(final User user, final String treeId, final ContentType contentType, final Type type, final Session session, final FolderServiceDecorator decorator) throws OXException {
        /*
         * prefer the default folder from config tree if possible
         */
        final ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        String folderId = Tools.getConfiguredDefaultFolder(serverSession, contentType, type);
        if (null == folderId) {
            /*
             * get default folder from storage, otherwise
             */
            final FolderStorage folderStorage = FolderStorageRegistry.getInstance().getFolderStorageByContentType(treeId, contentType);
            if (null == folderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(treeId, contentType.toString());
            }
            folderId = folderStorage.getDefaultFolderID(serverSession.getUser(), treeId, contentType, type, new StorageParametersImpl(serverSession));
        }
        return new GetPerformer(serverSession, decorator).doGet(treeId, folderId);
    }

    @Override
    public UserizedFolder getFolder(final String treeId, final String folderId, final User user, final Context context, final FolderServiceDecorator decorator) throws OXException {
        return new GetPerformer(user, context, decorator).doGet(treeId, folderId);
    }

    @Override
    public UserizedFolder getFolder(final String treeId, final String folderId, final Session session, final FolderServiceDecorator decorator) throws OXException {
        return new GetPerformer(ServerSessionAdapter.valueOf(session), decorator).doGet(treeId, folderId);
    }

    @Override
    public FolderResponse<UserizedFolder[]> getAllVisibleFolders(final String treeId, final FolderFilter filter, final User user, final Context context, final FolderServiceDecorator decorator) throws OXException {
        final AllVisibleFoldersPerformer performer = new AllVisibleFoldersPerformer(user, context, decorator);
        return FolderResponseImpl.newFolderResponse(performer.doAllVisibleFolders(treeId, filter), performer.getWarnings());
    }

    @Override
    public FolderResponse<UserizedFolder[]> getAllVisibleFolders(final String treeId, final FolderFilter filter, final Session session, final FolderServiceDecorator decorator) throws OXException {
        final AllVisibleFoldersPerformer performer = new AllVisibleFoldersPerformer(ServerSessionAdapter.valueOf(session), decorator);
        return FolderResponseImpl.newFolderResponse(performer.doAllVisibleFolders(treeId, filter), performer.getWarnings());
    }

    @Override
    public FolderResponse<UserizedFolder[]> getPath(final String treeId, final String folderId, final User user, final Context context, final FolderServiceDecorator decorator) throws OXException {
        final PathPerformer performer = new PathPerformer(user, context, decorator);
        return FolderResponseImpl.newFolderResponse(performer.doPath(treeId, folderId, true), performer.getWarnings());
    }

    @Override
    public FolderResponse<UserizedFolder[]> getPath(final String treeId, final String folderId, final Session session, final FolderServiceDecorator decorator) throws OXException {
        final PathPerformer performer = new PathPerformer(ServerSessionAdapter.valueOf(session), decorator);
        return FolderResponseImpl.newFolderResponse(performer.doPath(treeId, folderId, true), performer.getWarnings());
    }

    @Override
    public FolderResponse<UserizedFolder[]> getVisibleFolders(final String treeId, final ContentType contentType, final Type type, final boolean all, final User user, final Context context, final FolderServiceDecorator decorator) throws OXException {
        final VisibleFoldersPerformer performer = new VisibleFoldersPerformer(user, context, decorator);
        return FolderResponseImpl.newFolderResponse(performer.doVisibleFolders(treeId, contentType, type, all), performer.getWarnings());
    }

    @Override
    public FolderResponse<UserizedFolder[]> getVisibleFolders(final String treeId, final ContentType contentType, final Type type, final boolean all, final Session session, final FolderServiceDecorator decorator) throws OXException {
        final VisibleFoldersPerformer performer = new VisibleFoldersPerformer(ServerSessionAdapter.valueOf(session), decorator);
        return FolderResponseImpl.newFolderResponse(performer.doVisibleFolders(treeId, contentType, type, all), performer.getWarnings());
    }

    @Override
    public FolderResponse<UserizedFolder[]> getUserSharedFolders(final String treeId, final ContentType contentType, final Session session, final FolderServiceDecorator decorator) throws OXException {
        final UserSharedFoldersPerformer performer = new UserSharedFoldersPerformer(ServerSessionAdapter.valueOf(session), decorator);
        return FolderResponseImpl.newFolderResponse(performer.doSharedFolders(treeId, contentType), performer.getWarnings());
    }

    @Override
    public FolderResponse<UserizedFolder[]> getSubfolders(final String treeId, final String parentId, final boolean all, final User user, final Context context, final FolderServiceDecorator decorator) throws OXException {
        final ListPerformer listPerformer = new ListPerformer(user, context, decorator);
        return FolderResponseImpl.newFolderResponse(listPerformer.doList(treeId, parentId, all), listPerformer.getWarnings());
    }

    @Override
    public FolderResponse<UserizedFolder[]> getSubfolders(final String treeId, final String parentId, final boolean all, final Session session, final FolderServiceDecorator decorator) throws OXException {
        final ListPerformer listPerformer = new ListPerformer(ServerSessionAdapter.valueOf(session), decorator);
        return FolderResponseImpl.newFolderResponse(listPerformer.doList(treeId, parentId, all), listPerformer.getWarnings());
    }

    @Override
    public FolderResponse<UserizedFolder[][]> getUpdates(final String treeId, final Date timeStamp, final boolean ignoreDeleted, final ContentType[] includeContentTypes, final User user, final Context context, final FolderServiceDecorator decorator) throws OXException {
        final UpdatesPerformer performer = new UpdatesPerformer(user, context, decorator);
        return FolderResponseImpl.newFolderResponse(
            performer.doUpdates(treeId, timeStamp, ignoreDeleted, includeContentTypes),
            performer.getWarnings());
    }

    @Override
    public FolderResponse<UserizedFolder[][]> getUpdates(final String treeId, final Date timeStamp, final boolean ignoreDeleted, final ContentType[] includeContentTypes, final Session session, final FolderServiceDecorator decorator) throws OXException {
        final UpdatesPerformer performer = new UpdatesPerformer(ServerSessionAdapter.valueOf(session), decorator);
        return FolderResponseImpl.newFolderResponse(
            performer.doUpdates(treeId, timeStamp, ignoreDeleted, includeContentTypes),
            performer.getWarnings());
    }

    @Override
    public FolderResponse<Void> subscribeFolder(final String sourceTreeId, final String folderId, final String targetTreeId, final String optTargetParentId, final User user, final Context context) throws OXException {
        if (!FolderStorage.REAL_TREE_ID.equals(sourceTreeId)) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create("Subscribe only supported for real tree as source tree.");
        }
        if (KNOWN_TREES.contains(optTargetParentId)) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create("Subscribe not supported for known trees.");
        }
        final SubscribePerformer performer = new SubscribePerformer(user, context);
        performer.doSubscribe(sourceTreeId, folderId, targetTreeId, optTargetParentId);
        return FolderResponseImpl.newFolderResponse(null, performer.getWarnings());

    }

    private static final Set<String> KNOWN_TREES = ImmutableSet.of(FolderStorage.REAL_TREE_ID, OutlookFolderStorage.OUTLOOK_TREE_ID);

    @Override
    public FolderResponse<Void> subscribeFolder(final String sourceTreeId, final String folderId, final String targetTreeId, final String optTargetParentId, final Session session) throws OXException {
        if (!FolderStorage.REAL_TREE_ID.equals(sourceTreeId)) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create("Subscribe only supported for real tree as source tree.");
        }
        if (KNOWN_TREES.contains(targetTreeId)) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create("Subscribe not supported for known trees.");
        }
        final SubscribePerformer performer = new SubscribePerformer(ServerSessionAdapter.valueOf(session));
        performer.doSubscribe(sourceTreeId, folderId, targetTreeId, optTargetParentId);
        return FolderResponseImpl.newFolderResponse(null, performer.getWarnings());
    }

    @Override
    public FolderResponse<Void> unsubscribeFolder(final String treeId, final String folderId, final User user, final Context context) throws OXException {
        if (KNOWN_TREES.contains(treeId)) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create("Unsubscribe not supported for known trees.");
        }
        final UnsubscribePerformer performer = new UnsubscribePerformer(user, context);
        performer.doUnsubscribe(treeId, folderId);
        return FolderResponseImpl.newFolderResponse(null, performer.getWarnings());
    }

    @Override
    public FolderResponse<Void> unsubscribeFolder(final String treeId, final String folderId, final Session session) throws OXException {
        if (KNOWN_TREES.contains(treeId)) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create("Unsubscribe not supported for known trees.");
        }
        final UnsubscribePerformer performer = new UnsubscribePerformer(ServerSessionAdapter.valueOf(session));
        performer.doUnsubscribe(treeId, folderId);
        return FolderResponseImpl.newFolderResponse(null, performer.getWarnings());
    }

    @Override
    public FolderResponse<Void> updateFolder(final Folder folder, final Date timeStamp, final User user, final Context context, final FolderServiceDecorator decorator) throws OXException {
        final UpdatePerformer performer = new UpdatePerformer(user, context, decorator);
        performer.doUpdate(folder, timeStamp);
        return FolderResponseImpl.newFolderResponse(null, performer.getWarnings());
    }

    @Override
    public FolderResponse<Void> updateFolder(final Folder folder, final Date timeStamp, final Session session, final FolderServiceDecorator decorator) throws OXException {
        final UpdatePerformer performer = new UpdatePerformer(ServerSessionAdapter.valueOf(session), decorator);
        performer.doUpdate(folder, timeStamp);
        return FolderResponseImpl.newFolderResponse(null, performer.getWarnings());
    }

    @Override
    public Map<Integer, ContentType> getAvailableContentTypes() {
        return ContentTypeRegistry.getInstance().getAvailableContentTypes();
    }

    @Override
    public ContentType parseContentType(String value) {
        int module = com.openexchange.java.util.Tools.getUnsignedInteger(value);
        if (-1 != module) {
            return ContentTypeRegistry.getInstance().getByModule(module);
        } else {
            return ContentTypeRegistry.getInstance().getByString(value);
        }
    }

}
