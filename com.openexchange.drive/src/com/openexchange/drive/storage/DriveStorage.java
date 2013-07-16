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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.internal.DriveServiceLookup;
import com.openexchange.drive.internal.DriveSession;
import com.openexchange.drive.storage.filter.FileNameFilter;
import com.openexchange.drive.storage.filter.Filter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.file.storage.composition.IDBasedIgnorableVersionFileAccess;
import com.openexchange.file.storage.composition.IDBasedRandomFileAccess;
import com.openexchange.file.storage.composition.IDBasedSequenceNumberProvider;
import com.openexchange.java.Strings;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link DriveStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveStorage {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(DriveStorage.class);

    private final FolderID rootFolderID;
    private final DriveSession session;
    private final FolderCache knownFolders;

    private IDBasedFileAccess fileAccess;
    private IDBasedFolderAccess folderAccess;

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

    public FileStoragePermission getOwnPermission(String path) throws OXException {
        return getFolder(path).getOwnPermission();
    }

    /**
     * Performs the passed storage operation inside a transaction.
     *
     * @param storageOperation The storage operation to execute
     * @return The result of the operation
     * @throws OXException
     */
    public <T> T wrapInTransaction(StorageOperation<T> storageOperation) throws OXException {
        try {
            getFileAccess().startTransaction();
            T t = storageOperation.call();
            getFileAccess().commit();
            return t;
        } catch (OXException e) {
            getFileAccess().rollback();
            throw e;
        } finally {
            getFileAccess().finish();
        }
    }

    /**
     * Gets the quota limits and current usage for this storage. This includes both restrictions on the number of allowed files and the
     * size of the files in bytes. If there's no limit, {@link Quota#UNLIMITED} is returned.
     *
     * @return An array of size 2, where the first element holds the quota of {@link Type#FILE}, and the second of {@link Type#STORAGE}
     * @throws OXException
     */
    public Quota[] getQuota() throws OXException {
        return getFolderAccess().getQuotas(rootFolderID.getFolderId(), new Type[] { Type.STORAGE, Type.FILE });
    }

    /**
     * Copies an existing file the supplied locations.
     *
     * @param sourceFile The source file to copy
     * @param targetFileName The target filename
     * @param targetPath The target path
     * @return A file representing the copied file
     * @throws OXException
     */
    public File copyFile(File sourceFile, String targetFileName, String targetPath) throws OXException {
        File copiedFile = new DefaultFile();
        copiedFile.setFileName(targetFileName);
        copiedFile.setTitle(targetFileName);
        copiedFile.setFolderId(getFolderID(targetPath, true));
        copiedFile.setLastModified(new Date());
        copiedFile.setVersion("1");
        List<Field> fileFields = Arrays.asList(new Field[] { Field.FILENAME, Field.TITLE, Field.FOLDER_ID });
        if (LOG.isDebugEnabled()) {
            LOG.debug(this.toString() + "cp " + combine(getPath(sourceFile.getFolderId()), sourceFile.getFileName()) + " " +
                combine(targetPath, targetFileName));
        }
        String targetId = getFileAccess().copy(sourceFile.getId(), copiedFile.getFolderId(), copiedFile, null, fileFields);
        copiedFile.setId(targetId);
        return copiedFile;
    }

    /**
     * Overwrites an existing file with the contents of another one.
     *
     * @param sourceFile The source file to copy
     * @param targetFile The target file to overwrite
     * @return A file representing the copied file
     * @throws OXException
     */
    public File copyFile(File sourceFile, File targetFile) throws OXException {
        File copiedFile = new DefaultFile(targetFile);
        copiedFile.setLastModified(new Date());
        copiedFile.setFileSize(sourceFile.getFileSize());
        copiedFile.setFileMD5Sum(sourceFile.getFileMD5Sum());
        if (LOG.isDebugEnabled()) {
            LOG.debug(this.toString() + "cp " + combine(getPath(sourceFile.getFolderId()), sourceFile.getFileName()) + " " +
                combine(getPath(copiedFile.getFolderId()), copiedFile.getFileName()));
        }
        getFileAccess().saveDocument(copiedFile, getDocument(sourceFile), copiedFile.getSequenceNumber());
        return copiedFile;
    }

    /**
     * Moves an existing file to another one.
     *
     * @param sourceFile The source file to move
     * @param targetFile The target file to overwrite
     * @return A file representing the moved file
     * @throws OXException
     */
    public File moveFile(File sourceFile, File targetFile) throws OXException {
        File copiedFile = this.copyFile(sourceFile, targetFile);
        this.deleteFile(sourceFile);
        return copiedFile;
    }

    /**
     * Deletes a file.
     *
     * @param file The file to delete
     * @return A file representing the deleted file
     * @throws OXException
     */
    public File deleteFile(File file) throws OXException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(this.toString() + "rm " + combine(getPath(file.getFolderId()), file.getFileName()));
        }
        List<String> notRemoved = getFileAccess().removeDocument(Arrays.asList(new String[] { file.getId() }), file.getSequenceNumber());
        if (null != notRemoved && 0 < notRemoved.size()) {
            throw DriveExceptionCodes.FILE_NOT_FOUND.create();//TODO: exception for this
        }
        return file;
    }

    /**
     * Renames a file.
     *
     * @param file The file to rename
     * @param targetFileName The target filename
     * @return A file representing the renamed file
     * @throws OXException
     */
    public File renameFile(File file, String targetFileName) throws OXException {
        return moveFile(file, targetFileName, null);
    }

    /**
     * Moves a file to another folder
     *
     * @param file The file to move
     * @param targetPath The target folder
     * @return A file representing the renamed file
     * @throws OXException
     */
    public File moveFile(File file, String targetPath) throws OXException {
        return moveFile(file, null, targetPath);
    }

    /**
     * Moves a file to another folder and/or filename.
     *
     * @param file The file to move
     * @param targetFileName The target filename
     * @param targetPath The target path
     * @return A file representing the moved file
     * @throws OXException
     */
    public File moveFile(File file, String targetFileName, String targetPath) throws OXException {
        List<Field> updatedFields = new ArrayList<File.Field>();
        File movedFile = new DefaultFile(file);
        if (null != targetFileName && false == targetFileName.equals(file.getFileName())) {
            movedFile.setFileName(targetFileName);
            updatedFields.add(Field.FILENAME);
            if (null != file.getTitle() && file.getTitle().equals(file.getFileName())) {
                movedFile.setTitle(targetFileName);
                updatedFields.add(Field.TITLE);
            }
        }
        if (null != targetPath) {
            String targetFolderID = getFolderID(targetPath, true);
            if (false == file.getFolderId().equals(targetFolderID)) {
                movedFile.setFolderId(targetFolderID);
                updatedFields.add(Field.FOLDER_ID);
            }

            FileStorageFolder targetFolder = getFolder(targetPath, true);
            if (false == file.getFolderId().equals(targetFolder.getId())) {
                movedFile.setFolderId(targetFolder.getId());
                updatedFields.add(Field.FOLDER_ID);
            }
        }
        if (0 < updatedFields.size()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(this.toString() + "mv " + combine(getPath(file.getFolderId()), file.getFileName()) + " " +
                    combine(getPath(movedFile.getFolderId()), movedFile.getFileName()));
            }
            getFileAccess().saveFileMetadata(movedFile, file.getSequenceNumber(), updatedFields);
        }
        return movedFile;
    }

    /**
     * Creates a file.
     *
     * @param path The target folder path
     * @param fileName The target filename
     * @return A file representing the created file
     * @throws OXException
     */
    public File createFile(String path, String fileName) throws OXException {
        File file = new DefaultFile();
        file.setFolderId(getFolderID(path, true));
        file.setFileName(fileName);
        if (LOG.isDebugEnabled()) {
            LOG.debug(this.toString() + "touch " + combine(path, fileName));
        }
        getFileAccess().saveFileMetadata(file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);
        return file;
    }

    /**
     * Moves / renames a folder from one path to another.
     *
     * @param path The current path of the folder
     * @param newPath The new path
     * @return The new ID of the moved folder
     * @throws OXException
     */
    public String moveFolder(String path, String newPath) throws OXException {
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
        if (LOG.isDebugEnabled()) {
            LOG.debug(this.toString() + "mv " + path + " " + newPath);
        }
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
        return folderID;
    }

    /**
     * Creates a new folder, including all denoted subfolders along the path if needed.
     *
     * @param path The path to the folder to create
     * @return The ID of the created folder
     * @throws OXException
     */
    public String createFolder(String path) throws OXException {
        return getFolderID(path, true);
    }

    /**
     * Deletes a folder.
     *
     * @param path The path of the folder to delete
     * @return The ID of the deleted folder
     * @throws OXException
     */
    public String deleteFolder(String path) throws OXException {
        if (Strings.isEmpty(path) || ROOT_PATH.equals(path)) {
            throw DriveExceptionCodes.INVALID_PATH.create(path);
        }
        FileStorageFolder folder = getFolder(path);
        knownFolders.forget(path, folder, true);
        if (LOG.isDebugEnabled()) {
            LOG.debug(this.toString() + "rmdir " + path);
        }
        getFolderAccess().deleteFolder(folder.getId());
        return folder.getId();
    }

    /**
     * Gets the sequence numbers for the contents of the supplied folders to quickly determine which folders contain changes. An updated
     * sequence number in a folder indicates a change, for example a new, modified or deleted file.
     *
     * @param folderIds A list of folder IDs to get the sequence numbers for
     * @return A map holding the resulting sequence numbers to each requested folder ID
     * @throws OXException
     */
    public Map<String, Long> getSequenceNumbers(List<String> folderIDs) throws OXException {
        if (null == folderIDs || 0 == folderIDs.size()) {
            return Collections.emptyMap();
        }
        IDBasedFileAccess fileStorageFileAccess = getFileAccess();
        if (false == IDBasedSequenceNumberProvider.class.isInstance(fileStorageFileAccess)) {
            throw new UnsupportedOperationException("IDBasedSequenceNumberProvider is needed");
        }
        return ((IDBasedSequenceNumberProvider)fileStorageFileAccess).getSequenceNumbers(folderIDs);
    }

    public InputStream getDocument(File file) throws OXException {
        return getFileAccess().getDocument(file.getId(), file.getVersion());
    }

    /**
     * Gets file metadata for the supplied file ID.
     *
     * @param id The (unique) identifier of the file
     * @return The file metadata
     * @throws OXException
     */
    public File getFile(String id) throws OXException {
        return getFile(id, FileStorageFileAccess.CURRENT_VERSION);
    }

    /**
     * Gets file metadata for the supplied file ID with the specified version.
     *
     * @param id The (unique) identifier of the file
     * @param version The file version to get
     * @return The file metadata
     * @throws OXException
     */
    public File getFile(String id, String version) throws OXException {
        return getFileAccess().getFileMetadata(id, version);
    }

    public List<File> getFiles(String path) throws OXException {
        return getFilesInFolder(getFolderID(path));
    }

    public List<File> getFilesInFolder(String folderID) throws OXException {
        return Filter.apply(getFilesIterator(folderID, null), new FileNameFilter() {

            @Override
            protected boolean accept(String fileName) throws OXException {
                return null != fileName && false == Strings.isEmpty(fileName);
            }
        });
    }

    public File findFileByName(String path, final String name) throws OXException {
        return findFileByName(path, name, DriveConstants.FILE_FIELDS);
    }

    public File findFileByName(String path, final String name, List<Field> fields) throws OXException {
        return Filter.find(getFilesIterator(getFolderID(path), name, fields), new FileNameFilter() {

            @Override
            protected boolean accept(String fileName) throws OXException {
                return name.equals(fileName);
            }
        });
    }

    public String getPath(String folderID) throws OXException {
        String path = knownFolders.getPath(folderID);
        if (null == path) {
            path = resolveToRoot(folderID);
        }
        return path;
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

    private SearchIterator<File> getFilesIterator(String folderID, String pattern) throws OXException {
        return getFilesIterator(folderID, pattern, DriveConstants.FILE_FIELDS);
    }

    private SearchIterator<File> getFilesIterator(String folderID, String pattern, List<Field> fields) throws OXException {
        if (null != pattern) {
            return getFileAccess().search(pattern, fields, folderID, null, SortDirection.DEFAULT,
                FileStorageFileAccess.NOT_SET, FileStorageFileAccess.NOT_SET);
        } else {
            return getFileAccess().getDocuments(folderID, fields, null, SortDirection.DEFAULT).results();
        }
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
        } while (null != currentFolderID && false == "0".equals(currentFolderID) && false == rootFolderID.getFolderId().equals(currentFolderID));
        if (0 < folders.size() && rootFolderID.getFolderId().equals(folders.getFirst().getParentId())) {
            StringBuilder pathBuilder = new StringBuilder();
            for (int i = 0; i < folders.size(); i++) {
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
            rootFolder = getFolderAccess().getFolder(rootFolderID.toUniqueID());
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
        if (LOG.isDebugEnabled()) {
            LOG.debug(this.toString() + "mkdir " + combine(getPath(parent.getId()), name));
        }
        String newFolderID = getFolderAccess().createFolder(newFolder);
        return getFolderAccess().getFolder(newFolderID);
    }

    public static LinkedList<String> split(String path) throws OXException {
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

    public static String combine(String path1, String path2) {
        if (Strings.isEmpty(path1)) {
            return path2;
        } else if (Strings.isEmpty(path2)) {
            return path1;
        } else if (path1.endsWith("/")) {
            return path2.startsWith("/") ? path1 + path2.substring(1) : path1 + path2;
        } else {
            return path2.startsWith("/") ? path1 + path2 : path1 + '/' + path2;
        }
    }

    public IDBasedFolderAccess getFolderAccess() throws OXException {
        if (null == folderAccess) {
            IDBasedFolderAccessFactory factory = DriveServiceLookup.getService(IDBasedFolderAccessFactory.class, true);
            folderAccess = factory.createAccess(session.getServerSession());
        }
        return folderAccess;
    }

    public IDBasedFileAccess getFileAccess() throws OXException {
        if (null == fileAccess) {
            IDBasedFileAccessFactory factory = DriveServiceLookup.getService(IDBasedFileAccessFactory.class, true);
            fileAccess = factory.createAccess(session.getServerSession());
        }
        return fileAccess;
    }

    /**
     * Gets a value indicating if the underlying storage allows to perform random access file operations or not.
     *
     * @return <code>true</code> if random file access is supported, <code>false</code>, otherwise
     * @throws OXException
     */
    public boolean isRandomFileAccess() throws OXException {
        return IDBasedFileAccess.class.isInstance(getFileAccess()) &&
            ((IDBasedRandomFileAccess)getFileAccess()).supportsRandomFileAccess(rootFolderID.getService(), rootFolderID.getAccountId());
    }

    /**
     * Gets a value indicating if the underlying storage allows to ignore versions when updating or not.
     *
     * @return <code>true</code> if ignorable versions are supported, <code>false</code>, otherwise
     * @throws OXException
     */
    public boolean isIgnorableVersionFileAccess() throws OXException {
        return IDBasedIgnorableVersionFileAccess.class.isInstance(getFileAccess()) &&
            ((IDBasedIgnorableVersionFileAccess)getFileAccess())
            .supportsIgnorableVersion(rootFolderID.getService(), rootFolderID.getAccountId());
    }

    /**
     * Gets a value indicating whether folder sequence numbers are supported by the underlying storage or not.
     *
     * @return <code>true</code> if sequence numbers are supported, <code>false</code>, otherwise
     * @throws OXException
     */
    public boolean supportsFolderSequenceNumbers() throws OXException {
        return IDBasedSequenceNumberProvider.class.isInstance(getFileAccess()) &&
            ((IDBasedSequenceNumberProvider)getFileAccess()).supportsSequenceNumbers(rootFolderID.toUniqueID());
    }

    @Override
    public String toString() {
        return session.getServerSession().getLogin() + ':' + rootFolderID.toUniqueID() + "# ";
    }

}
