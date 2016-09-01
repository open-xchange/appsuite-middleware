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
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFolder.Info;
import com.box.sdk.BoxItem;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.boxcom.access.BoxOAuthAccess;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link BoxFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
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

    protected void checkFolderValidity(Info folderInfo) throws OXException {
        if (isFolderTrashed(folderInfo)) {
            throw FileStorageExceptionCodes.NOT_A_FOLDER.create(BoxConstants.ID, toFileStorageFolderId(folderInfo.getID()));
        }
    }

    protected com.openexchange.file.storage.boxcom.BoxFolder parseBoxFolder(Info dir) throws OXException {
        return new com.openexchange.file.storage.boxcom.BoxFolder(userId).parseDirEntry(dir, rootFolderId, accountDisplayName);
    }

    @Override
    public boolean exists(final String folderId) throws OXException {
        return perform(new BoxClosure<Boolean>() {

            @Override
            protected Boolean doPerform(BoxOAuthAccess boxAccess) throws BoxAPIException, OXException {
                try {
                    BoxAPIConnection apiConnection = getAPIConnection();
                    com.box.sdk.BoxFolder boxFolder = new com.box.sdk.BoxFolder(apiConnection, toBoxFolderId(folderId));
                    Info folderInfo = boxFolder.getInfo("trashed_at", "id");
                    checkFolderValidity(folderInfo);
                    return Boolean.TRUE;
                } catch (final BoxAPIException e) {
                    if (404 == e.getResponseCode()) {
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
            protected FileStorageFolder doPerform(BoxOAuthAccess boxAccess) throws BoxAPIException, OXException {
                try {
                    BoxAPIConnection apiConnection = getAPIConnection();
                    com.box.sdk.BoxFolder boxFolder = new com.box.sdk.BoxFolder(apiConnection, toBoxFolderId(folderId));
                    Info folderInfo = boxFolder.getInfo();
                    checkFolderValidity(folderInfo);

                    return parseBoxFolder(folderInfo);
                } catch (final BoxAPIException e) {
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
            protected FileStorageFolder[] doPerform(BoxOAuthAccess boxAccess) throws BoxAPIException, OXException {
                try {
                    BoxAPIConnection apiConnection = getAPIConnection();
                    com.box.sdk.BoxFolder parentBoxFolder = new com.box.sdk.BoxFolder(apiConnection, toBoxFolderId(parentIdentifier));
                    List<FileStorageFolder> folders = new LinkedList<>();
                    for (BoxItem.Info itemInfo : parentBoxFolder) {
                        if (itemInfo instanceof com.box.sdk.BoxFolder.Info) {
                            com.box.sdk.BoxFolder.Info i = (com.box.sdk.BoxFolder.Info) itemInfo;
                            folders.add(parseBoxFolder(i));
                        }
                    }
                    return folders.toArray(new FileStorageFolder[folders.size()]);
                } catch (final BoxAPIException e) {
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
            protected String doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                BoxAPIConnection apiConnection = getAPIConnection();
                com.box.sdk.BoxFolder parentBoxFolder = new com.box.sdk.BoxFolder(apiConnection, toBoxFolderId(toCreate.getParentId()));

                Info createdChildFolder = parentBoxFolder.createFolder(toCreate.getName());
                return toFileStorageFolderId(createdChildFolder.getID());
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
            protected String doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                BoxAPIConnection apiConnection = getAPIConnection();
                com.box.sdk.BoxFolder boxFolder = new com.box.sdk.BoxFolder(apiConnection, toBoxFolderId(folderId));

                // Rename & Move has to be done with two subsequent requests
                // Rename
                if (!Strings.isEmpty(newName)) {
                    boxFolder.rename(newName);
                }
                // Move
                if (!Strings.isEmpty(newParentId)) {
                    com.box.sdk.BoxFolder destinationBoxFolder = new com.box.sdk.BoxFolder(apiConnection, toBoxFolderId(newParentId));
                    boxFolder.move(destinationBoxFolder);
                }

                return toFileStorageFolderId(boxFolder.getID());
            }
        });
    }

    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        return perform(new BoxClosure<String>() {

            @Override
            protected String doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                BoxAPIConnection apiConnection = getAPIConnection();
                com.box.sdk.BoxFolder boxFolder = new com.box.sdk.BoxFolder(apiConnection, toBoxFolderId(folderId));
                if (!Strings.isEmpty(newName)) {
                    boxFolder.rename(newName);
                }

                return toFileStorageFolderId(boxFolder.getID());
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
            protected String doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                BoxAPIConnection apiConnection = getAPIConnection();
                com.box.sdk.BoxFolder boxFolder = new com.box.sdk.BoxFolder(apiConnection, toBoxFolderId(folderId));
                boxFolder.delete(true);

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
            protected Void doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                BoxAPIConnection apiConnection = getAPIConnection();
                com.box.sdk.BoxFolder boxFolder = new com.box.sdk.BoxFolder(apiConnection, toBoxFolderId(folderId));

                List<String> files = new LinkedList<>();
                List<String> folders = new LinkedList<>();

                for (BoxItem.Info itemInfo : boxFolder) {
                    if (itemInfo instanceof com.box.sdk.BoxFile.Info) {
                        com.box.sdk.BoxFile.Info i = (com.box.sdk.BoxFile.Info) itemInfo;
                        files.add(i.getID());
                    } else if (itemInfo instanceof com.box.sdk.BoxFolder.Info) {
                        com.box.sdk.BoxFolder.Info i = (com.box.sdk.BoxFolder.Info) itemInfo;
                        folders.add(i.getID());
                    }
                }

                for (String fileId : files) {
                    com.box.sdk.BoxFile trashMe = new com.box.sdk.BoxFile(apiConnection, fileId);
                    trashMe.delete();
                }
                for (String folderId : folders) {
                    com.box.sdk.BoxFolder trashMe = new com.box.sdk.BoxFolder(apiConnection, folderId);
                    trashMe.delete(true);
                }
                return null;
            }
        });
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(final String folderId) throws OXException {
        return perform(new BoxClosure<FileStorageFolder[]>() {

            @Override
            protected FileStorageFolder[] doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                BoxAPIConnection apiConnection = getAPIConnection();
                String fid = toBoxFolderId(folderId);
                com.box.sdk.BoxFolder boxFolder = new com.box.sdk.BoxFolder(apiConnection, fid);

                Info info = boxFolder.getInfo();
                List<FileStorageFolder> list = new LinkedList<FileStorageFolder>();

                FileStorageFolder f = parseBoxFolder(info);
                list.add(f);
                while (!rootFolderId.equals(fid)) {
                    // Check if we reached a root folder
                    if (info.getParent() == null) {
                        fid = "0";
                        continue;
                    }
                    fid = info.getParent().getID();
                    boxFolder = new com.box.sdk.BoxFolder(apiConnection, fid);
                    info = boxFolder.getInfo();
                    f = parseBoxFolder(info);
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
