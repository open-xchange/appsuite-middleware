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

package com.openexchange.share.impl.groupware;

import static com.openexchange.osgi.Tools.requireService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.Range;
import com.openexchange.file.storage.UserizedFile;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedAdministrativeFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tx.ConnectionHolder;


/**
 * {@link FileStorageHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class FileStorageHandler implements ModuleHandler {

    private static final String SHARED_FILES_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link FileStorageHandler}.
     *
     * @param services A service lookup reference
     */
    public FileStorageHandler(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public List<TargetProxy> loadTargets(List<ShareTarget> targets, HandlerParameters parameters) throws OXException {
        List<TargetProxy> files = new ArrayList<TargetProxy>(targets.size());
        ConnectionHolder.CONNECTION.set(parameters.getWriteCon());
        try {
            if (parameters.isAdministrative()) {
                IDBasedAdministrativeFileAccess fileAccess = getAdministrativeFileAccess(parameters.getContext());
                Iterator<ShareTarget> targetIt = targets.iterator();
                while (targetIt.hasNext()) {
                    ShareTarget target = targetIt.next();
                    FileID fileID = new FileID(target.getItem());
                    if (fileID.getFolderId() == null) {
                        fileID.setFolderId(new FolderID(target.getFolder()).getFolderId());
                    }
                    File file = fileAccess.getFileMetadata(fileID.toUniqueID(), FileStorageFileAccess.CURRENT_VERSION);
                    files.add(new FileTargetProxy(file));
                }
            } else {
                Iterator<ShareTarget> targetIt = targets.iterator();
                IDBasedFileAccess fileAccess = getFileAccess(parameters.getSession());
                try {
                    fileAccess.startTransaction();
                    while (targetIt.hasNext()) {
                        ShareTarget target = targetIt.next();
                        FileID fileID = new FileID(target.getItem());
                        if (fileID.getFolderId() == null) {
                            fileID.setFolderId(new FolderID(target.getFolder()).getFolderId());
                        }
                        File file = fileAccess.getFileMetadata(fileID.toUniqueID(), FileStorageFileAccess.CURRENT_VERSION);
                        files.add(new FileTargetProxy(file));
                    }
                    fileAccess.commit();
                } catch (OXException e) {
                    fileAccess.rollback();
                    throw e;
                } finally {
                    fileAccess.finish();
                }
            }
        } finally {
            ConnectionHolder.CONNECTION.set(null);
        }

        return files;
    }

    @Override
    public TargetProxy loadTarget(ShareTarget target, Session session) throws OXException {
        FileID fileID = new FileID(target.getItem());
        if (fileID.getFolderId() == null) {
            fileID.setFolderId(new FolderID(target.getFolderToLoad()).getFolderId());
        }

        IDBasedFileAccess fileAccess = getFileAccess(session);
        return new FileTargetProxy(fileAccess.getFileMetadata(fileID.toUniqueID(), FileStorageFileAccess.CURRENT_VERSION));
    }

    @Override
    public boolean isVisible(String folder, String item, int contextID, int userID) throws OXException {
        FileID fileID = new FileID(item);
        if (null == fileID.getFolderId()) {
            fileID.setFolderId(new FolderID(folder).getFolderId());
        }
        Context context = requireService(ContextService.class, services).getContext(contextID);
        return getAdministrativeFileAccess(context).canRead(fileID.toUniqueID(), userID);
    }

    @Override
    public boolean mayAdjust(ShareTarget target, Session session) throws OXException {
        FileID fileID = new FileID(target.getItem());
        if (null == fileID.getFolderId()) {
            fileID.setFolderId(new FolderID(target.getFolder()).getFolderId());
        }
        Context context = requireService(ContextService.class, services).getContext(session.getContextId());
        IDBasedAdministrativeFileAccess administrativeFileAccess = getAdministrativeFileAccess(context);
        if (administrativeFileAccess.canRead(fileID.toUniqueID(), session.getUserId())) {
            File file = administrativeFileAccess.getFileMetadata(fileID.toUniqueID(), FileStorageFileAccess.CURRENT_VERSION);
            return file.isShareable();
        }

        return false;
    }

    @Override
    public boolean exists(String folder, String item, int contextID, int guestID) throws OXException {
        FileID fileID = new FileID(item);
        if (null == fileID.getFolderId()) {
            fileID.setFolderId(new FolderID(folder).getFolderId());
        }
        Context context = requireService(ContextService.class, services).getContext(contextID);
        return getAdministrativeFileAccess(context).exists(fileID.toUniqueID(), FileStorageFileAccess.CURRENT_VERSION);
    }

    @Override
    public TargetProxy loadTarget(String folderId, String item, Context context, int guestID) throws OXException {
        FileID fileID = new FileID(item);
        if (fileID.getFolderId() == null) {
            fileID.setFolderId(new FolderID(folderId).getFolderId());
        }

        IDBasedAdministrativeFileAccess fileAccess = getAdministrativeFileAccess(context);
        File file = fileAccess.getFileMetadata(fileID.toUniqueID(), FileStorageFileAccess.CURRENT_VERSION);
        ShareTarget shareTarget = new ShareTarget(FolderObject.INFOSTORE, file.getFolderId(), file.getId());
        if (guestID > 0) {
            shareTarget = adjustTarget(shareTarget, context.getContextId(), context.getMailadmin(), guestID);
        }
        return new FileTargetProxy(file, shareTarget);
    }

    @Override
    public boolean canShare(TargetProxy proxy, HandlerParameters parameters) {
        return ((FileTargetProxy) proxy).getFile().isShareable();
    }

    @Override
    public void updateObjects(List<TargetProxy> modified, HandlerParameters parameters) throws OXException {
        ConnectionHolder.CONNECTION.set(parameters.getWriteCon());
        try {
            if (parameters.isAdministrative()) {
                IDBasedAdministrativeFileAccess fileAccess = getAdministrativeFileAccess(parameters.getContext());
                for (TargetProxy proxy : modified) {
                    File file = ((FileTargetProxy) proxy).getFile();
                    fileAccess.saveFileMetadata(file, file.getLastModified().getTime(), Collections.singletonList(Field.OBJECT_PERMISSIONS));
                }
            } else {
                IDBasedFileAccess fileAccess = getFileAccess(parameters.getSession());
                try {
                    fileAccess.startTransaction();
                    for (TargetProxy proxy : modified) {
                        File file = ((FileTargetProxy) proxy).getFile();
                        FileID fileID = new FileID(file.getId());
                        if (false == fileAccess.supports(fileID.getService(), fileID.getAccountId(), FileStorageCapability.OBJECT_PERMISSIONS)) {
                            throw FileStorageExceptionCodes.NO_PERMISSION_SUPPORT.create(fileID.getService(), file.getFolderId(), parameters.getContext().getContextId());
                        }
                        fileAccess.saveFileMetadata(file, file.getLastModified().getTime(), Collections.singletonList(Field.OBJECT_PERMISSIONS));
                    }
                    fileAccess.commit();
                } catch (OXException e) {
                    fileAccess.rollback();
                    throw e;
                } finally {
                    fileAccess.finish();
                }
            }
        } finally {
            ConnectionHolder.CONNECTION.set(null);
        }
    }

    @Override
    public void touchObjects(List<TargetProxy> touched, HandlerParameters parameters) throws OXException {
        ConnectionHolder.CONNECTION.set(parameters.getWriteCon());
        try {
            if (parameters.isAdministrative()) {
                IDBasedAdministrativeFileAccess fileAccess = getAdministrativeFileAccess(parameters.getContext());
                for (TargetProxy proxy : touched) {
                    File file = ((FileTargetProxy) proxy).getFile();
                    fileAccess.saveFileMetadata(file, file.getLastModified().getTime(), Collections.singletonList(Field.OBJECT_PERMISSIONS));
                }
            } else {
                IDBasedFileAccess fileAccess = getFileAccess(parameters.getSession());
                try {
                    fileAccess.startTransaction();
                    for (TargetProxy proxy : touched) {
                        File file = ((FileTargetProxy) proxy).getFile();
                        fileAccess.touch(file.getId());
                    }
                    fileAccess.commit();
                } catch (OXException e) {
                    fileAccess.rollback();
                    throw e;
                } finally {
                    fileAccess.finish();
                }
            }
        } finally {
            ConnectionHolder.CONNECTION.set(null);
        }
    }

    private IDBasedAdministrativeFileAccess getAdministrativeFileAccess(Context context) throws OXException {
        IDBasedFileAccessFactory factory = requireService(IDBasedFileAccessFactory.class, services);
        return factory.createAccess(context.getContextId());
    }

    private IDBasedFileAccess getFileAccess(Session session) throws OXException {
        IDBasedFileAccessFactory factory = requireService(IDBasedFileAccessFactory.class, services);
        return factory.createAccess(session);
    }

    @Override
    public List<TargetProxy> listTargets(int contextID, int guestID) throws OXException {
        List<TargetProxy> targets = new ArrayList<>();
        Context context = requireService(ContextService.class, services).getContext(contextID);
        IDBasedAdministrativeFileAccess administrativeFileAccess = getAdministrativeFileAccess(context);
        List<Field> fields = Arrays.asList(Field.ID, Field.FOLDER_ID, Field.OBJECT_PERMISSIONS);
        TimedResult<File> timedResult = administrativeFileAccess.getDocuments(SHARED_FILES_FOLDER_ID, guestID, fields, Field.ID, SortDirection.ASC, null);
        SearchIterator<File> searchIterator = null;
        try {
            searchIterator = timedResult.results();
            while (searchIterator.hasNext()) {
                File file = searchIterator.next();
                targets.add(new FileTargetProxy(file));
            }
        } finally {
            SearchIterators.close(searchIterator);
        }
        return targets;
    }

    @Override
    public boolean hasTargets(int contextID, int guestID) throws OXException {
        Context context = requireService(ContextService.class, services).getContext(contextID);
        IDBasedAdministrativeFileAccess administrativeFileAccess = getAdministrativeFileAccess(context);
        List<Field> fields = Arrays.asList(Field.ID, Field.FOLDER_ID);
        TimedResult<File> timedResult = administrativeFileAccess.getDocuments(SHARED_FILES_FOLDER_ID, guestID, fields, Field.ID, SortDirection.ASC, Range.valueOf(0, 1));
        SearchIterator<File> searchIterator = null;
        try {
            searchIterator = timedResult.results();
            return searchIterator.hasNext();
        } finally {
            SearchIterators.close(searchIterator);
        }
    }

    @Override
    public ShareTargetPath getPath(ShareTarget target, Session session) throws OXException {
        if (SHARED_FILES_FOLDER_ID.equals(target.getFolder())) {
            File file = getFileAccess(session).getFileMetadata(target.getItem(), FileStorageFileAccess.CURRENT_VERSION);
            String folderId;
            String fileId;
            if (file instanceof UserizedFile) {
                UserizedFile uFile = (UserizedFile) file;
                folderId = uFile.getOriginalFolderId();
                fileId = uFile.getOriginalId();
            } else {
                folderId = file.getFolderId();
                fileId = file.getId();
            }

            return new ShareTargetPath(target.getModule(), folderId, fileId);
        }

        return new ShareTargetPath(target.getModule(), target.getFolder(), target.getItem());
    }

    @Override
    public ShareTargetPath getPath(ShareTarget target, int contextID, int guestID) throws OXException {
        if (SHARED_FILES_FOLDER_ID.equals(target.getFolder())) {
            Context context = requireService(ContextService.class, services).getContext(contextID);
            File file = getAdministrativeFileAccess(context).getFileMetadata(target.getItem(), FileStorageFileAccess.CURRENT_VERSION);
            String folderId;
            String fileId;
            if (file instanceof UserizedFile) {
                UserizedFile uFile = (UserizedFile) file;
                folderId = uFile.getOriginalFolderId();
                fileId = uFile.getOriginalId();
            } else {
                folderId = file.getFolderId();
                fileId = file.getId();
            }

            return new ShareTargetPath(target.getModule(), folderId, fileId);
        }

        return new ShareTargetPath(target.getModule(), target.getFolder(), target.getItem());
    }

    @Override
    public ShareTarget adjustTarget(ShareTarget target, Session session, int targetUserId) throws OXException {
        return adjustTarget(target, session.getContextId(), session.getUserId(), targetUserId);
    }

    @Override
    public ShareTarget adjustTarget(ShareTarget target, int contextId, int requestUserId, int targetUserId) throws OXException {
        FolderID folderID = new FolderID(SHARED_FILES_FOLDER_ID);
        FileID fileID = new FileID(target.getItem());
        fileID.setFolderId(folderID.getFolderId());
        return new ShareTarget(target.getModule(), folderID.toUniqueID(), fileID.toUniqueID());
    }

}
