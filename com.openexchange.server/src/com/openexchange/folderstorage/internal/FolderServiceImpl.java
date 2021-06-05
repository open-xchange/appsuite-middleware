/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.folderstorage.internal;

import java.util.Date;
import java.util.List;
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
import com.openexchange.folderstorage.RestoringFolderService;
import com.openexchange.folderstorage.TrashAwareFolderService;
import com.openexchange.folderstorage.TrashResult;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.internal.performers.AllVisibleFoldersPerformer;
import com.openexchange.folderstorage.internal.performers.ClearPerformer;
import com.openexchange.folderstorage.internal.performers.ConsistencyPerformer;
import com.openexchange.folderstorage.internal.performers.CreatePerformer;
import com.openexchange.folderstorage.internal.performers.DefaultFolderPerformer;
import com.openexchange.folderstorage.internal.performers.DeletePerformer;
import com.openexchange.folderstorage.internal.performers.GetPerformer;
import com.openexchange.folderstorage.internal.performers.ListPerformer;
import com.openexchange.folderstorage.internal.performers.PathPerformer;
import com.openexchange.folderstorage.internal.performers.ReinitializePerformer;
import com.openexchange.folderstorage.internal.performers.RestorePerformer;
import com.openexchange.folderstorage.internal.performers.FileStorageSearchPerformer;
import com.openexchange.folderstorage.internal.performers.SubscribePerformer;
import com.openexchange.folderstorage.internal.performers.UnsubscribePerformer;
import com.openexchange.folderstorage.internal.performers.UpdatePerformer;
import com.openexchange.folderstorage.internal.performers.UpdatesPerformer;
import com.openexchange.folderstorage.internal.performers.UserSharedFoldersPerformer;
import com.openexchange.folderstorage.internal.performers.VisibleFoldersPerformer;
import com.openexchange.folderstorage.outlook.OutlookFolderStorage;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;

/**
 * {@link FolderServiceImpl} - The {@link FolderService} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderServiceImpl implements FolderService, TrashAwareFolderService, RestoringFolderService {

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
        DefaultFolderPerformer performer = new DefaultFolderPerformer(ruser, context, decorator);
        return performer.doGet(user, treeId, contentType, type);
    }

    @Override
    public UserizedFolder getDefaultFolder(final User user, final String treeId, final ContentType contentType, final Session session, final FolderServiceDecorator decorator) throws OXException {
        return getDefaultFolder(user, treeId, contentType, PrivateType.getInstance(), session, decorator);
    }

    @Override
    public UserizedFolder getDefaultFolder(final User user, final String treeId, final ContentType contentType, final Type type, final Session session, final FolderServiceDecorator decorator) throws OXException {
        DefaultFolderPerformer performer = new DefaultFolderPerformer(ServerSessionAdapter.valueOf(session), decorator);
        return performer.doGet(user, treeId, contentType, type);
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
    public FolderResponse<UserizedFolder[]> getVisibleFolders(String rootFolderId, String treeId, ContentType contentType, Type type, boolean all, Session session, FolderServiceDecorator decorator) throws OXException {
        VisibleFoldersPerformer performer = new VisibleFoldersPerformer(ServerSessionAdapter.valueOf(session), decorator);
        return FolderResponseImpl.newFolderResponse(performer.doVisibleFolders(rootFolderId, treeId, contentType, type, all), performer.getWarnings());
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
    public Map<Integer, List<ContentType>> getAvailableContentTypes() {
        return ContentTypeRegistry.getInstance().getAvailableContentTypes();
    }

    @Override
    public ContentType parseContentType(String value) {
        int module = com.openexchange.java.util.Tools.getUnsignedInteger(value);
        return module >= 0 ? ContentTypeRegistry.getInstance().getByModule(module) : ContentTypeRegistry.getInstance().getByString(value);
    }

    @Override
    public FolderResponse<TrashResult> trashFolder(String treeId, String folderId, Date timeStamp, Session session, FolderServiceDecorator decorator) throws OXException {
        final DeletePerformer performer = new DeletePerformer(ServerSessionAdapter.valueOf(session), decorator);
        TrashResult result = performer.doTrash(treeId, folderId, timeStamp);
        return FolderResponseImpl.newFolderResponse(result, performer.getWarnings());
    }

    @Override
    public FolderResponse<Map<String, List<UserizedFolder>>> restoreFolderFromTrash(String tree, List<String> folderIds, UserizedFolder defaultDestFolder, Session session, FolderServiceDecorator decorator) throws OXException {
        RestorePerformer performer = new RestorePerformer(ServerSessionAdapter.valueOf(session), decorator);
        Map<String, List<UserizedFolder>> result = performer.doRestore(tree, folderIds, defaultDestFolder);
        return FolderResponseImpl.newFolderResponse(result, performer.getWarnings());
    }

    @Override
    public FolderResponse<List<UserizedFolder>> searchFolderByName(String treeId, String folderId, ContentType contentType, String query, long date, boolean includeSubfolders, boolean all, int start, int end, Session session, FolderServiceDecorator decorator) throws OXException {
        if (false == InfostoreContentType.getInstance().toString().equals(contentType.toString())) {
            // Folder search by name for non-infostore module
            throw FolderExceptionErrorMessage.NO_SEARCH_SUPPORT.create();
        }

        FileStorageSearchPerformer performer = new FileStorageSearchPerformer(ServerSessionAdapter.valueOf(session), decorator);
        List<UserizedFolder> folders = performer.doSearch(treeId, folderId, query, date, includeSubfolders, all, start, end);
        return FolderResponseImpl.newFolderResponse(folders, performer.getWarnings());
    }

}
