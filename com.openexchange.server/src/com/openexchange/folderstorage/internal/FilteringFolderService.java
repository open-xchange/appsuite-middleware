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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.config.admin.HideAdminService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderFilter;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.RestoringFolderService;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link FilteringFolderService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class FilteringFolderService implements FolderService, RestoringFolderService {

    private final FolderService delegate;

    public FilteringFolderService(FolderService lDelegate) {
        this.delegate = lDelegate;
    }

    private FolderResponse<UserizedFolder[]> removeAdminFromFolderPermissions(int contextId, FolderResponse<UserizedFolder[]> folders) throws OXException {
        HideAdminService hideAdminService = ServerServiceRegistry.getInstance().getService(HideAdminService.class, false);
        if (hideAdminService == null) {
            return folders;
        }
        UserizedFolder[] updated = hideAdminService.removeAdminFromFolderPermissions(contextId, folders.getResponse());
        return FolderResponseImpl.newFolderResponse(updated, folders.getWarnings());
    }

    private UserizedFolder removeAdminFromFolderPermission(int contextId, UserizedFolder folder) throws OXException {
        HideAdminService hideAdminService = ServerServiceRegistry.getInstance().getService(HideAdminService.class, false);
        if (hideAdminService == null) {
            return folder;
        }
        return hideAdminService.removeAdminFromFolderPermissions(contextId, new UserizedFolder[] { folder })[0];
    }

    private FolderResponse<UserizedFolder[][]> removeAdmin(int contextId, FolderResponse<UserizedFolder[][]> folders) throws OXException {
        HideAdminService hideAdminService = ServerServiceRegistry.getInstance().getService(HideAdminService.class, false);
        if (hideAdminService == null) {
            return folders;
        }
        UserizedFolder[][] userizedFolders = folders.getResponse();
        UserizedFolder[] modified = hideAdminService.removeAdminFromFolderPermissions(contextId, userizedFolders[0]);
        UserizedFolder[] deleted = hideAdminService.removeAdminFromFolderPermissions(contextId, userizedFolders[1]);
        return FolderResponseImpl.newFolderResponse(new UserizedFolder[][] { modified, deleted }, folders.getWarnings());
    }

    private UserizedFolder getOriginalFolder(final String treeId, final String folderId, final User user, final Context context, final FolderServiceDecorator decorator) throws OXException {
        return delegate.getFolder(treeId, folderId, user, context, decorator);
    }

    private UserizedFolder getOriginalFolder(final String treeId, final String folderId, final Session session, final FolderServiceDecorator decorator) throws OXException {
        return delegate.getFolder(treeId, folderId, session, decorator);
    }

    @Override
    public Map<Integer, List<ContentType>> getAvailableContentTypes() {
        return delegate.getAvailableContentTypes();
    }

    @Override
    public ContentType parseContentType(String value) {
        return delegate.parseContentType(value);
    }

    @Override
    public void reinitialize(String treeId, Session session) throws OXException {
        delegate.reinitialize(treeId, session);
    }

    @Override
    public FolderResponse<Void> checkConsistency(String treeId, User user, Context context) throws OXException {
        return delegate.checkConsistency(treeId, user, context);
    }

    @Override
    public FolderResponse<Void> checkConsistency(String treeId, Session session) throws OXException {
        return delegate.checkConsistency(treeId, session);
    }

    @Override
    public UserizedFolder getFolder(String treeId, String folderId, User user, Context context, FolderServiceDecorator decorator) throws OXException {
        UserizedFolder folder = delegate.getFolder(treeId, folderId, user, context, decorator);
        return removeAdminFromFolderPermission(context.getContextId(), folder);
    }

    @Override
    public UserizedFolder getFolder(String treeId, String folderId, Session session, FolderServiceDecorator decorator) throws OXException {
        UserizedFolder folder = delegate.getFolder(treeId, folderId, session, decorator);
        return removeAdminFromFolderPermission(session.getContextId(), folder);
    }

    @Override
    public FolderResponse<UserizedFolder[]> getAllVisibleFolders(String treeId, FolderFilter filter, User user, Context context, FolderServiceDecorator decorator) throws OXException {
        FolderResponse<UserizedFolder[]> folders = delegate.getAllVisibleFolders(treeId, filter, user, context, decorator);
        return removeAdminFromFolderPermissions(context.getContextId(), folders);
    }

    @Override
    public FolderResponse<UserizedFolder[]> getAllVisibleFolders(String treeId, FolderFilter filter, Session session, FolderServiceDecorator decorator) throws OXException {
        FolderResponse<UserizedFolder[]> folders = delegate.getAllVisibleFolders(treeId, filter, session, decorator);
        return removeAdminFromFolderPermissions(session.getContextId(), folders);
    }

    @Override
    public UserizedFolder getDefaultFolder(User user, String treeId, ContentType contentType, User ruser, Context context, FolderServiceDecorator decorator) throws OXException {
        UserizedFolder folder = delegate.getDefaultFolder(user, treeId, contentType, ruser, context, decorator);
        return removeAdminFromFolderPermission(context.getContextId(), folder);
    }

    @Override
    public UserizedFolder getDefaultFolder(User user, String treeId, ContentType contentType, Type type, User ruser, Context context, FolderServiceDecorator decorator) throws OXException {
        UserizedFolder folder = delegate.getDefaultFolder(user, treeId, contentType, type, ruser, context, decorator);
        return removeAdminFromFolderPermission(context.getContextId(), folder);
    }

    @Override
    public UserizedFolder getDefaultFolder(User user, String treeId, ContentType contentType, Session session, FolderServiceDecorator decorator) throws OXException {
        UserizedFolder folder = delegate.getDefaultFolder(user, treeId, contentType, session, decorator);
        return removeAdminFromFolderPermission(session.getContextId(), folder);
    }

    @Override
    public UserizedFolder getDefaultFolder(User user, String treeId, ContentType contentType, Type type, Session session, FolderServiceDecorator decorator) throws OXException {
        UserizedFolder folder = delegate.getDefaultFolder(user, treeId, contentType, type, session, decorator);
        return removeAdminFromFolderPermission(session.getContextId(), folder);
    }

    @Override
    public FolderResponse<Void> subscribeFolder(String sourceTreeId, String folderId, String targetTreeId, String optTargetParentId, User user, Context context) throws OXException {
        return delegate.subscribeFolder(sourceTreeId, folderId, targetTreeId, optTargetParentId, user, context);
    }

    @Override
    public FolderResponse<Void> subscribeFolder(String sourceTreeId, String folderId, String targetTreeId, String optTargetParentId, Session session) throws OXException {
        return delegate.subscribeFolder(sourceTreeId, folderId, targetTreeId, optTargetParentId, session);
    }

    @Override
    public FolderResponse<Void> unsubscribeFolder(String treeId, String folderId, User user, Context context) throws OXException {
        return delegate.unsubscribeFolder(treeId, folderId, user, context);
    }

    @Override
    public FolderResponse<Void> unsubscribeFolder(String treeId, String folderId, Session session) throws OXException {
        return delegate.unsubscribeFolder(treeId, folderId, session);
    }

    @Override
    public FolderResponse<UserizedFolder[]> getVisibleFolders(String treeId, ContentType contentType, Type type, boolean all, User user, Context context, FolderServiceDecorator decorator) throws OXException {
        FolderResponse<UserizedFolder[]> folders = delegate.getVisibleFolders(treeId, contentType, type, all, user, context, decorator);
        return removeAdminFromFolderPermissions(context.getContextId(), folders);
    }

    @Override
    public FolderResponse<UserizedFolder[]> getVisibleFolders(String treeId, ContentType contentType, Type type, boolean all, Session session, FolderServiceDecorator decorator) throws OXException {
        FolderResponse<UserizedFolder[]> folders = delegate.getVisibleFolders(treeId, contentType, type, all, session, decorator);
        return removeAdminFromFolderPermissions(session.getContextId(), folders);
    }

    @Override
    public FolderResponse<UserizedFolder[]> getVisibleFolders(String rootFolderId, String treeId, ContentType contentType, Type type, boolean all, Session session, FolderServiceDecorator decorator) throws OXException {
        FolderResponse<UserizedFolder[]> folders = delegate.getVisibleFolders(rootFolderId, treeId, contentType, type, all, session, decorator);
        return removeAdminFromFolderPermissions(session.getContextId(), folders);
    }

    @Override
    public FolderResponse<UserizedFolder[]> getUserSharedFolders(String treeId, ContentType contentType, Session session, FolderServiceDecorator decorator) throws OXException {
        FolderResponse<UserizedFolder[]> folders = delegate.getUserSharedFolders(treeId, contentType, session, decorator);
        return removeAdminFromFolderPermissions(session.getContextId(), folders);
    }

    @Override
    public FolderResponse<UserizedFolder[]> getSubfolders(String treeId, String parentId, boolean all, User user, Context context, FolderServiceDecorator decorator) throws OXException {
        FolderResponse<UserizedFolder[]> folders = delegate.getSubfolders(treeId, parentId, all, user, context, decorator);
        return removeAdminFromFolderPermissions(context.getContextId(), folders);
    }

    @Override
    public FolderResponse<UserizedFolder[]> getSubfolders(String treeId, String parentId, boolean all, Session session, FolderServiceDecorator decorator) throws OXException {
        FolderResponse<UserizedFolder[]> folders = delegate.getSubfolders(treeId, parentId, all, session, decorator);
        return removeAdminFromFolderPermissions(session.getContextId(), folders);
    }

    @Override
    public FolderResponse<UserizedFolder[]> getPath(String treeId, String folderId, User user, Context context, FolderServiceDecorator decorator) throws OXException {
        FolderResponse<UserizedFolder[]> folders = delegate.getPath(treeId, folderId, user, context, decorator);
        return removeAdminFromFolderPermissions(context.getContextId(), folders);
    }

    @Override
    public FolderResponse<UserizedFolder[]> getPath(String treeId, String folderId, Session session, FolderServiceDecorator decorator) throws OXException {
        FolderResponse<UserizedFolder[]> folders = delegate.getPath(treeId, folderId, session, decorator);
        return removeAdminFromFolderPermissions(session.getContextId(), folders);
    }

    @Override
    public FolderResponse<UserizedFolder[][]> getUpdates(String treeId, Date timeStamp, boolean ignoreDeleted, ContentType[] includeContentTypes, User user, Context context, FolderServiceDecorator decorator) throws OXException {
        FolderResponse<UserizedFolder[][]> folders = delegate.getUpdates(treeId, timeStamp, ignoreDeleted, includeContentTypes, user, context, decorator);
        HideAdminService hideAdminService = ServerServiceRegistry.getInstance().getService(HideAdminService.class, false);
        if (hideAdminService == null) {
            return folders;
        }
        return removeAdmin(context.getContextId(), folders);
    }

    @Override
    public FolderResponse<UserizedFolder[][]> getUpdates(String treeId, Date timeStamp, boolean ignoreDeleted, ContentType[] includeContentTypes, Session session, FolderServiceDecorator decorator) throws OXException {
        FolderResponse<UserizedFolder[][]> folders = delegate.getUpdates(treeId, timeStamp, ignoreDeleted, includeContentTypes, session, decorator);
        return removeAdmin(session.getContextId(), folders);
    }

    @Override
    public FolderResponse<Void> deleteFolder(String treeId, String folderId, Date timeStamp, User user, Context context, FolderServiceDecorator decorator) throws OXException {
        return delegate.deleteFolder(treeId, folderId, timeStamp, user, context, decorator);
    }

    @Override
    public FolderResponse<Void> deleteFolder(String treeId, String folderId, Date timeStamp, Session session, FolderServiceDecorator decorator) throws OXException {
        return delegate.deleteFolder(treeId, folderId, timeStamp, session, decorator);
    }

    @Override
    public FolderResponse<Void> clearFolder(String treeId, String folderId, User user, Context context) throws OXException {
        return delegate.clearFolder(treeId, folderId, user, context);
    }

    @Override
    public FolderResponse<Void> clearFolder(String treeId, String folderId, Session session) throws OXException {
        return delegate.clearFolder(treeId, folderId, session);
    }

    @Override
    public FolderResponse<Void> updateFolder(Folder folder, Date timeStamp, User user, Context context, FolderServiceDecorator decorator) throws OXException {
        if (null == folder.getPermissions()) {
            return delegate.updateFolder(folder, timeStamp, user, context, decorator); // not needed without 'set' permissions
        }
        HideAdminService hideAdminService = ServerServiceRegistry.getInstance().getService(HideAdminService.class, false);
        if (null == hideAdminService || false == hideAdminService.showAdmin(context.getContextId())) {
            return delegate.updateFolder(folder, timeStamp, user, context, decorator); // currently not available or not applicable in this context
        }
        UserizedFolder originalFolder = getOriginalFolder(folder.getTreeID(), folder.getID(), user, context, decorator);
        Permission[] enhancedFolderPermissions = hideAdminService.addAdminToFolderPermissions(context.getContextId(), originalFolder.getPermissions(), folder.getPermissions());
        folder.setPermissions(enhancedFolderPermissions);
        return delegate.updateFolder(folder, timeStamp, user, context, decorator);
    }

    @Override
    public FolderResponse<Void> updateFolder(Folder folder, Date timeStamp, Session session, FolderServiceDecorator decorator) throws OXException {
        if (null == folder.getPermissions()) {
            return delegate.updateFolder(folder, timeStamp, session, decorator); // not needed without 'set' permissions
        }
        HideAdminService hideAdminService = ServerServiceRegistry.getInstance().getService(HideAdminService.class, false);
        if (null == hideAdminService || false == hideAdminService.showAdmin(session.getContextId())) {
            return delegate.updateFolder(folder, timeStamp, session, decorator); // currently not available or not applicable in this context
        }
        UserizedFolder originalFolder = getOriginalFolder(folder.getTreeID(), folder.getID(), session, decorator);
        Permission[] enhancedFolderPermissions = hideAdminService.addAdminToFolderPermissions(session.getContextId(), originalFolder.getPermissions(), folder.getPermissions());
        folder.setPermissions(enhancedFolderPermissions);
        return delegate.updateFolder(folder, timeStamp, session, decorator);
    }

    @Override
    public FolderResponse<String> createFolder(Folder folder, User user, Context context, FolderServiceDecorator decorator) throws OXException {
        return delegate.createFolder(folder, user, context, decorator);
    }

    @Override
    public FolderResponse<String> createFolder(Folder folder, Session session, FolderServiceDecorator decorator) throws OXException {
        return delegate.createFolder(folder, session, decorator);
    }

    @Override
    public FolderResponse<Map<String, List<UserizedFolder>>> restoreFolderFromTrash(String tree, List<String> folderIds, UserizedFolder defaultDestFolder, Session session, FolderServiceDecorator decorator) throws OXException {
        if (false == (delegate instanceof RestoringFolderService)) {
            throw FileStorageExceptionCodes.NO_RESTORE_SUPPORT.create();
        }
        FolderResponse<Map<String, List<UserizedFolder>>> restoreFolderFromTrash = ((RestoringFolderService) delegate).restoreFolderFromTrash(tree, folderIds, defaultDestFolder, session, decorator);

        HideAdminService hideAdminService = ServerServiceRegistry.getInstance().getService(HideAdminService.class, false);
        if (hideAdminService == null) {
            return restoreFolderFromTrash;
        }
        Map<String, List<UserizedFolder>> response = restoreFolderFromTrash.getResponse();
        for (Entry<String, List<UserizedFolder>> entry : response.entrySet()) {
            UserizedFolder[] removeAdminFromFolderPermissions = hideAdminService.removeAdminFromFolderPermissions(session.getContextId(), entry.getValue().stream().toArray(UserizedFolder[]::new));
            response.put(entry.getKey(), Arrays.asList(removeAdminFromFolderPermissions));
        }
        return FolderResponseImpl.newFolderResponse(response, restoreFolderFromTrash.getWarnings());
    }
}
