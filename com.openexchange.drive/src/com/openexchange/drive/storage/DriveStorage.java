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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.storage;

import static com.openexchange.drive.storage.DriveConstants.PATH_SEPARATOR;
import static com.openexchange.drive.storage.DriveConstants.ROOT_PATH;
import static com.openexchange.drive.storage.DriveConstants.TEMP_PATH;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.checksum.ChecksumProvider;
import com.openexchange.drive.internal.DriveServiceLookup;
import com.openexchange.drive.internal.DriveSession;
import com.openexchange.drive.storage.filter.FileFilter;
import com.openexchange.drive.storage.filter.FileNameFilter;
import com.openexchange.drive.storage.filter.Filter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.java.Strings;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link DriveStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveStorage {

    private final FolderID rootFolderID;
    private final DriveSession session;
    private final FolderCache knownFolders;

    private FileStorageFileAccess fileAccess;
    private FileStorageFolderAccess folderAccess;
    private FileStorageAccountAccess accountAccess;

    /**
     * Initializes a new {@link DriveStorage}.
     *
     * @param session The drive session
     * @param rootFolderID The ID of the root folder
     */
    public DriveStorage(DriveSession session, String rootFolderID) {
        super();
        this.session = session;
        this.rootFolderID = new FolderID(rootFolderID);
        this.knownFolders = new FolderCache();
    }

    public File copyFile(File sourceFile, String targetFileName, String targetPath) throws OXException {
        File copiedFile = new DefaultFile();
        copiedFile.setFileName(targetFileName);
        copiedFile.setTitle(targetFileName);
        copiedFile.setFolderId(getFolderID(targetPath, true));
        IDTuple sourceId = new IDTuple(sourceFile.getFolderId(), sourceFile.getId());
        IDTuple targetId = getFileAccess().copy(sourceId, copiedFile.getFolderId(), copiedFile, null, DriveConstants.FILE_FIELDS);
        copiedFile.setFolderId(targetId.getFolder());
        copiedFile.setId(targetId.getId());
        return copiedFile;
    }

    public File copyFile(File sourceFile, File targetFile) throws OXException {
        getFileAccess().saveDocument(targetFile, getDocument(sourceFile), targetFile.getSequenceNumber());
        return targetFile;
    }

    public File deleteFile(File file, boolean hard) throws OXException {
        if (hard) {
            IDTuple id = new IDTuple(file.getFolderId(), file.getId());
            List<IDTuple> notRemoved = getFileAccess().removeDocument(Arrays.asList(new IDTuple[] { id }), file.getSequenceNumber());
            if (null != notRemoved && 0 < notRemoved.size()) {
                throw DriveExceptionCodes.FILE_NOT_FOUND.create();//TODO: exception for this
            }
            return file;
        } else {
            return moveFile(file, TEMP_PATH);
        }
    }

//    public void updateFile(File file, InputStream document) throws OXException {
//        getFileAccess().saveDocument(file, document, file.getSequenceNumber());
//    }

    public InputStream getDocument(File file) throws OXException {
        return getFileAccess().getDocument(file.getFolderId(), file.getId(), file.getVersion());
    }

    public File renameFile(File file, String targetFileName) throws OXException {
        return updateFile(file, targetFileName, null);
    }

    public File moveFile(File file, String targetPath) throws OXException {
        return updateFile(file, null, targetPath);
    }

    public File updateFile(File file, String targetFileName, String targetPath) throws OXException {
        List<Field> editedFields = new ArrayList<File.Field>();
        File editedFile = new DefaultFile();
        editedFile.setId(file.getId());
        editedFile.setFolderId(file.getFolderId());
        if (null != targetFileName && false == targetFileName.equals(file.getFileName())) {
            editedFile.setFileName(targetFileName);
            editedFields.add(Field.FILENAME);
            if (null != file.getTitle() && file.getTitle().equals(file.getFileName())) {
                editedFile.setTitle(targetFileName);
                editedFields.add(Field.TITLE);
            }
        }
        if (null != targetPath) {
            FileStorageFolder targetFolder = getFolder(targetPath, true);
            if (false == file.getFolderId().equals(targetFolder.getId())) {
                editedFile.setFolderId(targetFolder.getId());
                editedFields.add(Field.FOLDER_ID);
            }
        }
        getFileAccess().saveFileMetadata(editedFile, file.getSequenceNumber(), editedFields);
        return editedFile;
    }

    private SearchIterator<File> getFilesIterator(String path) throws OXException {
        return getFileAccess().getDocuments(getFolderID(path), DriveConstants.FILE_FIELDS, Field.FILENAME, SortDirection.ASC).results();
    }

    private SearchIterator<File> getFilesIterator(String path, String pattern) throws OXException {
        return getFileAccess().search(pattern, DriveConstants.FILE_FIELDS, getFolderID(path), Field.FILENAME, SortDirection.ASC,
            FileStorageFileAccess.NOT_SET, FileStorageFileAccess.NOT_SET);
    }

    public List<File> getFiles(String path, FileFilter filter) throws OXException {
        return Filter.apply(getFilesIterator(path), filter);
    }

    public File findFile(String path, FileFilter filter) throws OXException {
        return Filter.find(getFilesIterator(path), filter);
    }

    public File findFileByName(String path, final String name) throws OXException {
        return Filter.find(getFilesIterator(path, name), new FileNameFilter() {

            @Override
            protected boolean accept(String fileName) throws OXException {
                return name.equals(fileName);
            }
        });
    }

    public File findFileByNameAndChecksum(String path, String name, final String checksum) throws OXException {
        return Filter.find(getFilesIterator(path, name), new FileFilter() {

            @Override
            public boolean accept(File file) throws OXException {
                return null != file && checksum.equals(ChecksumProvider.getMD5(session, file));
            }
        });
    }

    public File findFileByChecksum(String path, final String checksum) throws OXException {
        return Filter.find(getFilesIterator(path), new FileFilter() {

            @Override
            public boolean accept(File file) throws OXException {
                return null != file && checksum.equals(ChecksumProvider.getMD5(session, file));
            }
        });
    }

    public File createFile(String path, String fileName) throws OXException {
        File file = new DefaultFile();
        file.setFolderId(getFolderID(path, true));
        file.setFileName(fileName);
        getFileAccess().saveFileMetadata(file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);
        return file;
    }

    /**
     * Moves / renames a folder from one path to another.
     *
     * @param path The current path of the folder
     * @param newPath The new path
     * @throws OXException
     */
    public void moveFolder(String path, String newPath) throws OXException {
        if (Strings.isEmpty(newPath) || ROOT_PATH.equals(newPath)) {
            throw DriveExceptionCodes.INVALID_PATH.create(newPath);
        }
        if (Strings.isEmpty(path) || ROOT_PATH.equals(path)) {
            throw DriveExceptionCodes.INVALID_PATH.create(path);
        }
        FileStorageFolder folder = getFolder(path);
        String folderID = folder.getId();
        knownFolders.forget(path, folder, true);
        /*
         * prepare path up to new parent folder
         */
        int idx = path.lastIndexOf(PATH_SEPARATOR);
        String oldName = path.substring(idx + 1);
        String oldParentPath = 0 == idx ? ROOT_PATH : path.substring(0, idx);
        idx = newPath.lastIndexOf(PATH_SEPARATOR);
        String newName = newPath.substring(idx + 1);
        String newParentPath = 0 == idx ? ROOT_PATH : newPath.substring(0, idx);
        FileStorageFolder newParentFolder = getFolder(newParentPath, true);
        /*
         * check for move and/or rename operations
         */
        if (false == oldParentPath.equals(newParentPath)) {
            /*
             * perform move
             */
            folderID = getFolderAccess().moveFolder(folderID, newParentFolder.getId());
        }
        if (false == oldName.equals(newName)) {
            /*
             * perform rename
             */
            folderID = getFolderAccess().renameFolder(folderID, newName);
        }
    }

    public void deleteFolder(String path) throws OXException {
        if (Strings.isEmpty(path) || ROOT_PATH.equals(path)) {
            throw DriveExceptionCodes.INVALID_PATH.create(path);
        }
        FileStorageFolder folder = getFolder(path);
        getFolderAccess().deleteFolder(folder.getId());
        knownFolders.forget(path, folder, true);
    }

    public String getPath(String folderID) throws OXException {
        String path = knownFolders.getPath(folderID);
        if (null == path) {
            path = resolveToRoot(folderID);
        }
        return path;
    }

    public void createFolder(String path) throws OXException {
        getFolder(path, true);
    }

    public String getFolderID(String path) throws OXException {
        return getFolderID(path, false);
    }

    public String getFolderID(String path, boolean createIfNeeded) throws OXException {
        return getFolder(path, createIfNeeded).getId();
    }

    public FileStorageFolder getFolder(String path) throws OXException {
        return getFolder(path, false);
    }

    public FileStorageFolder getFolder(String path, boolean createIfNeeded) throws OXException {
        FileStorageFolder folder = knownFolders.getFolder(path);
        if (null == folder) {
            folder = resolveToLeaf(path, createIfNeeded);
        }
        return folder;
    }

    public Map<String, FileStorageFolder> getFolders() throws OXException {
        Map<String, FileStorageFolder> folders = new HashMap<String, FileStorageFolder>();
        FileStorageFolder rootFolder = getRootFolder();
        folders.put(ROOT_PATH, rootFolder);
        addSubfolders(folders, rootFolder, ROOT_PATH);
        return folders;
    }

    private FileStorageFolder resolveToLeaf(String path, boolean createIfNeeded) throws OXException {
        FileStorageFolder currentFolder = getRootFolder();
        String currentPath = ROOT_PATH;
        for (String name : split(path)) {
            FileStorageFolder existingFolder = knownFolders.getFolder(currentPath + name);
            if (null == existingFolder) {
                FileStorageFolder[] subfolders = getFolderAccess().getSubfolders(currentFolder.getId(), false);
                if (null != subfolders && 0 < subfolders.length) {
                    for (FileStorageFolder folder : subfolders) {
                        knownFolders.remember(currentPath + folder.getName(), folder);
                        if (name.equals(folder.getName())) {
                            existingFolder = folder;
                        }
                    }
                }
            }
            if (null == existingFolder) {
                if (false == createIfNeeded) {
                    throw DriveExceptionCodes.PATH_NOT_FOUND.create(path);
                }
                existingFolder = createFolder(currentFolder, name);
                knownFolders.remember(currentPath + name, existingFolder);
            }
            currentFolder = existingFolder;
            currentPath += name + PATH_SEPARATOR;
        }

        return currentFolder;
    }

    private String resolveToRoot(String folderID) throws OXException {
        LinkedList<FileStorageFolder> folders = new LinkedList<FileStorageFolder>();
        String currentFolderID = folderID;
        do {
            FileStorageFolder folder = getFolderAccess().getFolder(currentFolderID);
            folders.addFirst(folder);
            currentFolderID = folder.getParentId();
        } while (null != currentFolderID && false == rootFolderID.equals(currentFolderID));

        if (0 < folders.size() && rootFolderID.equals(folders.getFirst().getId())) {
            StringBuilder pathBuilder = new StringBuilder();
            for (int i = 1; i < folders.size(); i++) {
                FileStorageFolder folder = folders.get(i);
                pathBuilder.append(PATH_SEPARATOR).append(folder.getName());
                knownFolders.remember(pathBuilder.toString(), folder);
            }
            return pathBuilder.toString();
        } else {
            return null;
        }
    }

    private void addSubfolders(Map<String, FileStorageFolder> folders, FileStorageFolder parent, String path) throws OXException {
        FileStorageFolder[] subfolders = getFolderAccess().getSubfolders(parent.getId(), false);
        for (FileStorageFolder subfolder : subfolders) {
            String subPath = path + subfolder.getName();
            knownFolders.remember(subPath, subfolder);
            if (false == TEMP_PATH.equals(subPath)) {
                folders.put(subPath, subfolder);
                addSubfolders(folders, subfolder, subPath + PATH_SEPARATOR);
            }
        }
    }

    private FileStorageFolder getRootFolder() throws OXException {
        FileStorageFolder rootFolder = knownFolders.getFolder(ROOT_PATH);
        if (null == rootFolder) {
            rootFolder = getFolderAccess().getFolder(rootFolderID.getFolderId());
            knownFolders.remember(ROOT_PATH, rootFolder);
        }
        return rootFolder;
    }

    private FileStorageFolder createFolder(FileStorageFolder parent, String name) throws OXException {
        DefaultFileStorageFolder newFolder = new DefaultFileStorageFolder();
        newFolder.setName(name);
        newFolder.setParentId(parent.getId());
        newFolder.setSubscribed(parent.isSubscribed());
        for (FileStoragePermission permission : parent.getPermissions()) {
            newFolder.addPermission(permission);
        }
        String newFolderID = getFolderAccess().createFolder(newFolder);
        return getFolderAccess().getFolder(newFolderID);
    }

    private static LinkedList<String> split(String path) throws OXException {
        if (null == path || false == path.startsWith(ROOT_PATH)) {
            throw DriveExceptionCodes.INVALID_PATH.create(path);
        }
        LinkedList<String> names = new LinkedList<String>();
        for (String name : path.split(String.valueOf(PATH_SEPARATOR))) {
            if (Strings.isEmpty(name)) {
                continue;
            }
            names.addLast(name);
        }
        return names;
    }

    public FileStorageAccountAccess getAccountAccess() throws OXException {
        if (null == accountAccess) {
            accountAccess = DriveServiceLookup.getService(FileStorageServiceRegistry.class)
                .getFileStorageService(rootFolderID.getService()).getAccountAccess(rootFolderID.getAccountId(), session.getServerSession());

//            FileStorageFileAccess fileAccess1 = DriveServiceLookup.getService(FileStorageServiceRegistry.class)
//                .getFileStorageService(rootFolderID.getService()).getAccountAccess(rootFolderID.getAccountId(), session.getServerSession()).getFileAccess();
//            FileStorageFileAccess fileAccess2 = DriveServiceLookup.getService(FileStorageServiceRegistry.class)
//                .getFileStorageService(rootFolderID.getService()).getAccountManager().getAccount(rootFolderID.getAccountId(), session.getServerSession())
//                .getFileStorageService().getAccountAccess(rootFolderID.getAccountId(), session.getServerSession()).getFileAccess();
//
//            FileStorageAccountManager accountManager = DriveServiceLookup.getService(FileStorageServiceRegistry.class)
//                .getFileStorageService(rootFolderID.getService()).getAccountManager();
//            FileStorageAccount account = accountManager.getAccount(rootFolderID.getAccountId(), session.getServerSession());
//            FileStorageService service = account.getFileStorageService();
//            accountAccess = service.getAccountAccess(rootFolderID.getAccountId(), session.getServerSession());
        }
        return accountAccess;
    }

    public FileStorageFolderAccess getFolderAccess() throws OXException {
        if (null == folderAccess) {
            folderAccess = getAccountAccess().getFolderAccess();
        }
        return folderAccess;
    }

    public FileStorageFileAccess getFileAccess() throws OXException {
        if (null == fileAccess) {
            fileAccess = getAccountAccess().getFileAccess();
        }
        return fileAccess;
    }

    @Override
    public String toString() {
        return "DriveStorage[" + rootFolderID + ']';
    }
}
