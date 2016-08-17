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

package com.openexchange.file.storage.boxcom;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import com.box.boxjavalibv2.BoxClient;
import com.box.boxjavalibv2.dao.BoxCollection;
import com.box.boxjavalibv2.dao.BoxFile;
import com.box.boxjavalibv2.dao.BoxFolder;
import com.box.boxjavalibv2.dao.BoxTypedObject;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.box.boxjavalibv2.exceptions.BoxServerException;
import com.box.boxjavalibv2.requests.requestobjects.BoxFolderDeleteRequestObject;
import com.box.boxjavalibv2.requests.requestobjects.BoxFolderRequestObject;
import com.box.boxjavalibv2.requests.requestobjects.BoxPagingRequestObject;
import com.box.boxjavalibv2.resourcemanagers.IBoxFoldersManager;
import com.box.restclientv2.exceptions.BoxRestException;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.boxcom.access.BoxOAuthAccess;
import com.openexchange.session.Session;

/**
 * {@link BoxFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BoxFolderAccess extends AbstractBoxResourceAccess implements FileStorageFolderAccess {

    //private final BoxAccountAccess accountAccess;
    private final int userId;
    private final String accountDisplayName;

    /**
     * Initializes a new {@link BoxFolderAccess}.
     */
    public BoxFolderAccess(final BoxOAuthAccess boxAccess, final FileStorageAccount account, final Session session, final BoxAccountAccess accountAccess) throws OXException {
        super(boxAccess, account, session);
        //this.accountAccess = accountAccess;
        userId = session.getUserId();
        accountDisplayName = account.getDisplayName();
    }

    protected void checkFolderValidity(BoxTypedObject typedObject) throws OXException {
        if (isFolder(typedObject) || isTrashed(typedObject)) {
            throw FileStorageExceptionCodes.NOT_A_FILE.create(BoxConstants.ID, typedObject.getId());
        }
    }

    protected void checkFolderValidity(BoxFolder folder) throws OXException {
        if (isTrashed(folder)) {
            throw FileStorageExceptionCodes.NOT_A_FILE.create(BoxConstants.ID, toFileStorageFolderId(folder.getId()));
        }
    }

    protected com.openexchange.file.storage.boxcom.BoxFolder parseBoxFolder(BoxFolder dir) throws OXException {
        return new com.openexchange.file.storage.boxcom.BoxFolder(userId).parseDirEntry(dir, rootFolderId, accountDisplayName);
    }

    @Override
    public boolean exists(final String folderId) throws OXException {
        return perform(new BoxClosure<Boolean>() {

            @Override
            protected Boolean doPerform(BoxOAuthAccess boxAccess) throws BoxRestException, BoxServerException, AuthFatalFailureException, OXException {
                try {
                    BoxClient boxClient = (BoxClient) boxAccess.getClient().client;
                    BoxFolder folder = boxClient.getFoldersManager().getFolder(toBoxFolderId(folderId), null);
                    checkFolderValidity(folder);
                    return Boolean.TRUE;
                } catch (final BoxRestException e) {
                    if (404 == e.getStatusCode()) {
                        return Boolean.FALSE;
                    }
                    throw e;
                } catch (final BoxServerException e) {
                    if (404 == e.getStatusCode()) {
                        return Boolean.FALSE;
                    }
                    throw e;
                }
            }
        }).booleanValue();
    }

    @Override
    public FileStorageFolder getFolder(final String folderId) throws OXException {
        return perform(new BoxClosure<FileStorageFolder>() {

            @Override
            protected FileStorageFolder doPerform(BoxOAuthAccess boxAccess) throws BoxRestException, BoxServerException, AuthFatalFailureException, OXException {
                try {
                    BoxClient boxClient = (BoxClient) boxAccess.getClient().client;
                    BoxFolder folder = boxClient.getFoldersManager().getFolder(toBoxFolderId(folderId), null);
                    checkFolderValidity(folder);

                    return parseBoxFolder(folder);
                } catch (final BoxServerException e) {
                    throw handleHttpResponseError(folderId, account.getId(), e);
                }
            }
        });
    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder getTrashFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        return new FileStorageFolder[0];
    }

    @Override
    public FileStorageFolder[] getUserSharedFolders() throws OXException {
        return new FileStorageFolder[0];
    }

    @Override
    public FileStorageFolder[] getSubfolders(final String parentIdentifier, final boolean all) throws OXException {
        return perform(new BoxClosure<FileStorageFolder[]>() {

            @Override
            protected FileStorageFolder[] doPerform(BoxOAuthAccess boxAccess) throws BoxRestException, BoxServerException, AuthFatalFailureException, OXException {
                try {
                    BoxClient boxClient = (BoxClient) boxAccess.getClient().client;
                    IBoxFoldersManager foldersManager = boxClient.getFoldersManager();
                    BoxFolder boxfolder = foldersManager.getFolder(toBoxFolderId(parentIdentifier), null);

                    List<FileStorageFolder> folders = new LinkedList<FileStorageFolder>();

                    BoxCollection itemCollection = boxfolder.getItemCollection();
                    if (itemCollection.getTotalCount().intValue() <= itemCollection.getEntries().size()) {
                        for (BoxTypedObject child : itemCollection.getEntries()) {
                            if (isFolder(child)) {
                                folders.add(parseBoxFolder(foldersManager.getFolder(child.getId(), null)));
                            }
                        }
                    } else {
                        int offset = 0;
                        final int limit = 100;

                        int resultsFound;
                        do {
                            BoxPagingRequestObject reqObj = BoxPagingRequestObject.pagingRequestObject(limit, offset);
                            BoxCollection collection = foldersManager.getFolderItems(toBoxFolderId(parentIdentifier), reqObj);

                            List<BoxTypedObject> entries = collection.getEntries();
                            resultsFound = entries.size();
                            for (BoxTypedObject typedObject : entries) {
                                if (isFolder(typedObject)) {
                                    folders.add(parseBoxFolder(foldersManager.getFolder(typedObject.getId(), null)));
                                }
                            }

                            offset += limit;
                        } while (resultsFound == limit);
                    }

                    return folders.toArray(new FileStorageFolder[folders.size()]);
                } catch (final BoxServerException e) {
                    throw handleHttpResponseError(parentIdentifier, account.getId(), e);
                }
            }
        });
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        com.openexchange.file.storage.boxcom.BoxFolder rootFolder = new com.openexchange.file.storage.boxcom.BoxFolder(userId);
        rootFolder.setRootFolder(true);
        rootFolder.setId(FileStorageFolder.ROOT_FULLNAME);
        rootFolder.setParentId(null);
        rootFolder.setName(accountDisplayName);
        rootFolder.setSubfolders(true);
        rootFolder.setSubscribedSubfolders(true);
        return rootFolder;
    }

    @Override
    public String createFolder(final FileStorageFolder toCreate) throws OXException {
        return perform(new BoxClosure<String>() {

            @Override
            protected String doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                BoxClient boxClient = (BoxClient) boxAccess.getClient().client;

                BoxFolderRequestObject reqObj = BoxFolderRequestObject.createFolderRequestObject(toCreate.getName(), toBoxFolderId(toCreate.getParentId()));
                BoxFolder createdFolder = boxClient.getFoldersManager().createFolder(reqObj);
                return toFileStorageFolderId(createdFolder.getId());
            }
        });
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        // Neither support for subscription nor permissions
        return identifier;
    }

    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        return moveFolder(folderId, newParentId, null);
    }

    @Override
    public String moveFolder(final String folderId, final String newParentId, final String newName) throws OXException {
        return perform(new BoxClosure<String>() {

            @Override
            protected String doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                BoxClient boxClient = (BoxClient) boxAccess.getClient().client;

                BoxFolderRequestObject reqObj = BoxFolderRequestObject.updateFolderRequestObject();
                if (null != newName) {
                    reqObj.setName(newName);
                }
                if (null != newParentId) {
                    reqObj.setParent(toBoxFolderId(newParentId));
                }
                BoxFolder updatedFolder = boxClient.getFoldersManager().updateFolderInfo(toBoxFolderId(folderId), reqObj);

                return toFileStorageFolderId(updatedFolder.getId());
            }
        });
    }

    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        return perform(new BoxClosure<String>() {

            @Override
            protected String doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                BoxClient boxClient = (BoxClient) boxAccess.getClient().client;

                BoxFolderRequestObject reqObj = BoxFolderRequestObject.updateFolderRequestObject();
                if (null != newName) {
                    reqObj.setName(newName);
                }
                BoxFolder updatedFolder = boxClient.getFoldersManager().updateFolderInfo(toBoxFolderId(folderId), reqObj);

                return toFileStorageFolderId(updatedFolder.getId());
            }
        });
    }

    @Override
    public String deleteFolder(String folderId) throws OXException {
        return deleteFolder(folderId, false);
    }

    @Override
    public String deleteFolder(final String folderId, boolean hardDelete) throws OXException {
        return perform(new BoxClosure<String>() {

            @Override
            protected String doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                BoxClient boxClient = (BoxClient) boxAccess.getClient().client;

                BoxFolderDeleteRequestObject reqObj = BoxFolderDeleteRequestObject.deleteFolderRequestObject(true);
                boxClient.getFoldersManager().deleteFolder(toBoxFolderId(folderId), reqObj);

                return folderId;
            }
        });
    }

    @Override
    public void clearFolder(String folderId) throws OXException {
        clearFolder(folderId, false);
    }

    @Override
    public void clearFolder(final String folderId, boolean hardDelete) throws OXException {
        perform(new BoxClosure<Void>() {

            @Override
            protected Void doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                BoxClient boxClient = (BoxClient) boxAccess.getClient().client;
                BoxFolder boxfolder = boxClient.getFoldersManager().getFolder(toBoxFolderId(folderId), null);

                List<BoxFile> files = new LinkedList<BoxFile>();
                List<BoxFolder> folders = new LinkedList<BoxFolder>();

                BoxCollection itemCollection = boxfolder.getItemCollection();
                if (itemCollection.getTotalCount().intValue() <= itemCollection.getEntries().size()) {
                    for (BoxTypedObject child : itemCollection.getEntries()) {
                        if (isFolder(child)) {
                            folders.add((BoxFolder) child);
                        } else {
                            files.add((BoxFile) child);
                        }
                    }
                } else {
                    int offset = 0;
                    final int limit = 100;

                    int resultsFound;
                    do {
                        BoxPagingRequestObject reqObj = BoxPagingRequestObject.pagingRequestObject(limit, offset);
                        BoxCollection collection = boxClient.getFoldersManager().getFolderItems(toBoxFolderId(folderId), reqObj);

                        List<BoxTypedObject> entries = collection.getEntries();
                        resultsFound = entries.size();
                        for (BoxTypedObject typedObject : entries) {
                            if (isFolder(typedObject)) {
                                folders.add((BoxFolder) typedObject);
                            } else {
                                files.add((BoxFile) typedObject);
                            }
                        }

                        offset += limit;
                    } while (resultsFound == limit);
                }

                for (BoxFolder trashMe : folders) {
                    BoxFolderDeleteRequestObject reqOb = BoxFolderDeleteRequestObject.deleteFolderRequestObject(true);
                    boxClient.getFoldersManager().deleteFolder(trashMe.getId(), reqOb);
                }

                for (BoxFile trashMe : files) {
                    boxClient.getFilesManager().deleteFile(trashMe.getId(), null);
                }

                return null;
            }
        });
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(final String folderId) throws OXException {
        return perform(new BoxClosure<FileStorageFolder[]>() {

            @Override
            protected FileStorageFolder[] doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                BoxClient boxClient = (BoxClient) boxAccess.getClient().client;

                List<FileStorageFolder> list = new LinkedList<FileStorageFolder>();

                String fid = toBoxFolderId(folderId);
                BoxFolder dir = boxClient.getFoldersManager().getFolder(fid, null);
                FileStorageFolder f = parseBoxFolder(dir);
                list.add(f);

                while (!rootFolderId.equals(fid)) {
                    fid = dir.getParent().getId();
                    dir = boxClient.getFoldersManager().getFolder(fid, null);
                    f = parseBoxFolder(dir);
                    list.add(f);
                }

                return list.toArray(new FileStorageFolder[list.size()]);
            }
        });
    }

    @Override
    public Quota getStorageQuota(final String folderId) throws OXException {
        return Type.STORAGE.getUnlimited();
    }

    @Override
    public Quota getFileQuota(final String folderId) throws OXException {
        return Type.FILE.getUnlimited();
    }

    @Override
    public Quota[] getQuotas(final String folder, final Type[] types) throws OXException {
        final Quota[] ret = new Quota[types.length];
        for (int i = 0; i < types.length; i++) {
            ret[i] = types[i].getUnlimited();
        }
        return ret;
    }

}
