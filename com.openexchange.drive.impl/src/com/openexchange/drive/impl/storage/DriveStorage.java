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

package com.openexchange.drive.impl.storage;

import static com.openexchange.drive.impl.DriveConstants.*;
import static com.openexchange.drive.impl.DriveUtils.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.FolderStats;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.DriveStrings;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.internal.DriveServiceLookup;
import com.openexchange.drive.impl.internal.PathNormalizer;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.management.DriveConfig;
import com.openexchange.drive.impl.metadata.DriveMetadata;
import com.openexchange.drive.impl.storage.filter.FileNameFilter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.TypeAware;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FilenameValidationUtils;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * {@link DriveStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveStorage {

    private final FolderID rootFolderID;
    private final SyncSession session;
    private final FolderCache knownFolders;

    private IDBasedFileAccess fileAccess;
    private IDBasedFolderAccess folderAccess;
    private FileStorageFolder trashFolder;
    private Boolean hasTrashFolder;

    /**
     * Initializes a new {@link DriveStorage}.
     *
     * @param session The drive session
     * @param rootFolderID The ID of the root folder
     */
    public DriveStorage(SyncSession session) {
        super();
        this.session = session;
        this.rootFolderID = new FolderID(session.getRootFolderID());
        this.knownFolders = new FolderCache();
    }

    /**
     * Invalidates any cached directories in the current session.
     */
    public void invalidateCache() {
        knownFolders.clear();
    }

    /**
     * Gets the user's permission for a specific folder.
     *
     * @param path The path to the folder to get the own permissions for
     * @return The permissions
     */
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
            getFolderAccess().startTransaction();
            T t = storageOperation.call();
            getFileAccess().commit();
            getFolderAccess().commit();
            return t;
        } catch (OXException e) {
            getFileAccess().rollback();
            getFolderAccess().rollback();
            throw e;
        } finally {
            getFileAccess().finish();
            getFolderAccess().finish();
        }
    }

    public FolderID getRootFolderID() {
        return rootFolderID;
    }

    /**
     * Gets the quota limits and current usage for this storage. This includes both restrictions on the number of allowed files and the
     * size of the files in bytes. If there's no limit, {@link Quota#UNLIMITED} is returned.
     *
     * @return An array of size 2, where the first element holds the quota of {@link Type#FILE}, and the second of {@link Type#STORAGE}
     * @throws OXException
     */
    public Quota[] getQuota() throws OXException {
        return getFolderAccess().getQuotas(rootFolderID.toUniqueID(), new Type[] { Type.STORAGE, Type.FILE });
    }

    /**
     * Copies an existing file to the supplied locations.
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
        copiedFile.setFileMIMEType(sourceFile.getFileMIMEType());
        List<Field> fileFields = Arrays.asList(new Field[] { Field.FILENAME, Field.TITLE, Field.FOLDER_ID, Field.LAST_MODIFIED, Field.VERSION });
        if (session.isTraceEnabled()) {
            session.trace(this.toString() + "cp " + combine(getPath(sourceFile.getFolderId()), sourceFile.getFileName()) + " " +
                combine(targetPath, targetFileName));
        }
        String sourceVersion = sourceFile.isCurrentVersion() ? FileStorageFileAccess.CURRENT_VERSION : sourceFile.getVersion();
        String targetId = getFileAccess().copy(sourceFile.getId(), sourceVersion, copiedFile.getFolderId(), copiedFile, null, fileFields);
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
        copiedFile.setFileMIMEType(sourceFile.getFileMIMEType());
        if (session.isTraceEnabled()) {
            session.trace(this.toString() + "cp " + combine(getPath(sourceFile.getFolderId()), sourceFile.getFileName()) + " " +
                combine(getPath(copiedFile.getFolderId()), copiedFile.getFileName()));
        }
        //TODO: use targetFile.getSequenceNumber()?
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
     * Deletes a file, preferring a "soft-delete" if available.
     *
     * @param file The file to delete
     * @return A file representing the deleted file
     * @throws OXException
     */
    public File deleteFile(File file) throws OXException {
        return deleteFile(file, false);
    }

    /**
     * Deletes a file.
     *
     * @param file The file to delete
     * @param hardDelete <code>true</code> to hard-delete the file, <code>false</code>, otherwise
     * @return A file representing the deleted file
     * @throws OXException
     */
    public File deleteFile(File file, boolean hardDelete) throws OXException {
        if (session.isTraceEnabled()) {
            session.trace(this.toString() + "rm " + (hardDelete ? "-rf " : "") + combine(getPath(file.getFolderId()), file.getFileName()));
        }
        List<String> notRemoved = getFileAccess().removeDocument(
            Arrays.asList(new String[] { file.getId() }), file.getSequenceNumber(), hardDelete);
        if (null != notRemoved && 0 < notRemoved.size()) {
            throw DriveExceptionCodes.FILE_NOT_FOUND.create(file.getFileName(), getPath(file.getFolderId()));//TODO: exception for this
        }
        return file;
    }

    /**
     * Deletes multiple files.
     *
     * @param files The files to delete
     * @param hardDelete <code>true</code> to hard-delete the files, <code>false</code>, otherwise
     * @return The files that could <b>not</b> be deleted due to an edit-delete conflict
     * @throws OXException
     */
    public List<File> deleteFiles(List<File> files, boolean hardDelete) throws OXException {
        Map<String, File> ids = new HashMap<String, File>(files.size());
        long sequenceNumber = 0;
        StringBuilder StringBuilder = session.isTraceEnabled() ? new StringBuilder() : null;
        for (File file : files) {
            ids.put(file.getId(), file);
            sequenceNumber = Math.max(sequenceNumber, file.getSequenceNumber());
            if (null != StringBuilder) {
                StringBuilder.append(' ').append(combine(getPath(file.getFolderId()), file.getFileName()));
            }
        }
        if (null != StringBuilder) {
            session.trace(this.toString() + "rm" + (hardDelete ? " -rf " : "") + StringBuilder.toString());
        }
        List<String> notRemoved = getFileAccess().removeDocument(new ArrayList<String>(ids.keySet()), sequenceNumber, hardDelete);
        if (null == notRemoved || 0 == notRemoved.size()) {
            return Collections.emptyList();
        } else {
            List<File> notRemovedFiles = new ArrayList<File>(notRemoved.size());
            for (String id : notRemoved) {
                notRemovedFiles.add(ids.get(id));
            }
            return notRemovedFiles;
        }
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
            if (session.isTraceEnabled()) {
                session.trace(this.toString() + "mv " + combine(getPath(file.getFolderId()), file.getFileName()) + " " +
                    combine(getPath(movedFile.getFolderId()), movedFile.getFileName()));
            }
            getFileAccess().saveFileMetadata(movedFile, file.getSequenceNumber(), updatedFields);
        }
        return movedFile;
    }

    /**
     * Creates an empty file.
     *
     * @param path The target folder path
     * @param fileName The target filename
     * @return A file representing the created file
     * @throws OXException
     */
    public File createFile(String path, String fileName) throws OXException {
        return createFile(path, fileName, null, null);
    }

    /**
     * Creates an empty file.
     *
     * @param path The target folder path
     * @param fileName The target filename
     * @param additionalMetadata Additional metadata when creating the file
     * @param data The binary content, or <code>null</code> to create a file without data
     * @return A file representing the created file
     * @throws OXException
     */
    public File createFile(String path, String fileName, File additionalMetadata, InputStream data) throws OXException {
        File file = null != additionalMetadata ? new DefaultFile(additionalMetadata) : new DefaultFile();
        file.setFolderId(getFolderID(path, true));
        file.setFileName(fileName);
        if (session.isTraceEnabled()) {
            session.trace(this.toString() + "touch " + combine(path, fileName));
        }
        if (null == data) {
            getFileAccess().saveFileMetadata(file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);
        } else {
            getFileAccess().saveDocument(file, data, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);
        }
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
        if (session.isTraceEnabled()) {
            session.trace(this.toString() + "mv " + path + " " + newPath);
        }
        if (false == oldParentPath.equals(newParentPath)) {
            /*
             * perform move / rename
             */
            folderID = oldName.equals(newName) ? getFolderAccess().moveFolder(folderID, newParentFolder.getId()) :
                getFolderAccess().moveFolder(folderID, newParentFolder.getId(), newName);
        } else if (false == oldName.equals(newName)) {
            /*
             * perform rename only
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
        return deleteFolder(path, false);
    }

    /**
     * Deletes a folder, preferring a "soft-delete" if available.
     *
     * @param path The path of the folder to delete
     * @param hardDelete <code>true</code> to hard-delete the folder, <code>false</code>, otherwise
     * @return The ID of the deleted folder
     * @throws OXException
     */
    public String deleteFolder(String path, boolean hardDelete) throws OXException {
        if (Strings.isEmpty(path) || ROOT_PATH.equals(path)) {
            throw DriveExceptionCodes.INVALID_PATH.create(path);
        }
        FileStorageFolder folder = getFolder(path);
        knownFolders.forget(path, folder, true);
        if (session.isTraceEnabled()) {
            session.trace(this.toString() + "rmdir " + (hardDelete ? "-rf " : "") + path);
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
        Map<String, Long> storedSequenceNumbers = getFileAccess().getSequenceNumbers(folderIDs);
        if (session.getDriveSession().useDriveMeta()) {
            for (String folderID : folderIDs) {
                Long storedSequenceNumber = storedSequenceNumbers.get(folderID);
                String path = getPath(folderID);
                FileStorageFolder folder = getFolder(path, false);
                DriveMetadata metadata = new DriveMetadata(
                    session, folder, null == storedSequenceNumber ? Long.valueOf(0) : storedSequenceNumber);
                long metadataSequenceNumber = metadata.getSequenceNumber();
                if (null == storedSequenceNumber || storedSequenceNumber.longValue() < metadataSequenceNumber) {
                    storedSequenceNumbers.put(folderID, Long.valueOf(metadataSequenceNumber));
                }
            }
        }
        return storedSequenceNumbers;
    }

    public InputStream getDocument(File file) throws OXException {
        if (session.getDriveSession().useDriveMeta() && DriveMetadata.class.isInstance(file)) {
            return ((DriveMetadata) file).getDocument();
        } else {
            return getFileAccess().getDocument(file.getId(), file.getVersion());
        }
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

    /**
     * Gets all synchronized files present in the folder identified by the supplied ID.
     *
     * @param folderID The folder ID
     * @return All synchronizable files in the folder
     * @throws OXException
     */
    public List<File> getFilesInFolder(String folderID) throws OXException {
        return getFilesInFolder(folderID, false, null, null);
    }

    /**
     * Gets all synchronized files present in the folder identified by the supplied ID.
     *
     * @param folderID The folder ID
     * @param includeDriveMeta <code>true</code> to also include the folder's <code>.drive-meta</code> file, <code>false</code>, otherwise
     * @return All synchronizable files in the folder
     */
    public List<File> getFilesInFolder(String folderID, boolean includeDriveMeta) throws OXException {
        return getFilesInFolder(folderID, false, includeDriveMeta, null, null);
    }

    /**
     * Gets a list of files in the folder identified by the supplied ID.
     *
     * @param folderID The folder ID
     * @param all <code>true</code> to include also files excluded from synchronization, <code>false</code>, otherwise
     * @param pattern The file search pattern to apply, or <code>null</code> if not used
     * @param fields The file-fields to get, or <code>null</code> to retrieve the default fields
     * @return The files
     * @throws OXException
     */
    public List<File> getFilesInFolder(String folderID, boolean all, String pattern, List<Field> fields) throws OXException {
        return getFilesInFolder(folderID, all, session.getDriveSession().useDriveMeta(), pattern, fields);
    }

    /**
     * Gets a list of files in the folder identified by the supplied ID.
     *
     * @param folderID The folder ID
     * @param all <code>true</code> to include also files excluded from synchronization, <code>false</code>, otherwise
     * @param includeDriveMeta <code>true</code> to also include the folder's <code>.drive-meta</code> file, <code>false</code>, otherwise
     * @param pattern The file search pattern to apply, or <code>null</code> if not used
     * @param fields The file-fields to get, or <code>null</code> to retrieve the default fields
     * @return The files
     */
    public List<File> getFilesInFolder(String folderID, boolean all, boolean includeDriveMeta, String pattern, List<Field> fields) throws OXException {
        FileStorageFolder folder = getFolderAccess().getFolder(folderID);
        if (null == folder) {
            throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(folderID, rootFolderID.getAccountId(), rootFolderID.getService(),
                session.getServerSession().getUserId(), session.getServerSession().getContextId());
        }
        List<File> files = new ArrayList<File>();
        /*
         * prepare appropriate file filter
         */
        FileNameFilter filter;
        if (all) {
            filter = FileNameFilter.ACCEPT_ALL;
        } else {
            final String path = getPath(folderID);
            final Set<String> existingNames = DriveUtils.getNormalizedFolderNames(session.getStorage().getSubfolders(path).values());
            filter = new FileNameFilter() {

                @Override
                protected boolean accept(String fileName) throws OXException {
                    return false == FilenameValidationUtils.isInvalidFileName(fileName) &&
                        false == DriveUtils.isIgnoredFileName(session.getDriveSession(), path, fileName) &&
                        false == existingNames.contains(PathNormalizer.normalize(fileName));
                }
            };
        }
        /*
         * include .drive-meta as needed
         */
        if (includeDriveMeta) {
            File driveMetaFile = new DriveMetadata(session, folder);
            if (filter.accept(driveMetaFile)) {
                files.add(driveMetaFile);
            }
        }
        /*
         * add (possibly filtered) directory contents
         */
        if (null == folder.getOwnPermission() || FileStoragePermission.READ_OWN_OBJECTS > folder.getOwnPermission().getReadPermission()) {
            return files;
        }
        files.addAll(filter.findAll(searchDocuments(folderID, pattern, null != fields ? fields : DriveConstants.FILE_FIELDS)));
        return files;
    }

    /**
     * Gets a file with a specific name in a path.
     *
     * @param path The path of the directory to look for the file
     * @param name The name of the file
     * @return The file, or <code>null</code> if not found.
     * @throws OXException
     */
    public File getFileByName(String path, String name) throws OXException {
        return getFileByName(path, name, false);
    }

    /**
     * Gets a file with a specific name in a path.
     *
     * @param path The path of the directory to look for the file
     * @param name The name of the file
     * @param normalizeFileNames <code>true</code> to also consider not-normalized filenames, <code>false</code>, otherwise
     * @return The file, or <code>null</code> if not found.
     * @throws OXException
     */
    public File getFileByName(String path, final String name, boolean normalizeFileNames) throws OXException {
        return getFileByName(path, name, DriveConstants.FILE_FIELDS, normalizeFileNames);
    }

    /**
     * Gets a file with a specific name in a path.
     *
     * @param path The path of the directory to look for the file
     * @param name The name of the file
     * @param fields The metadata to load
     * @param normalizeFileNames <code>true</code> to also consider not-normalized filenames, <code>false</code>, otherwise
     * @return The file, or <code>null</code> if not found.
     * @throws OXException
     */
    public File getFileByName(String path, final String name, List<Field> fields, final boolean normalizeFileNames) throws OXException {
        if (session.getDriveSession().useDriveMeta() && DriveConstants.METADATA_FILENAME.equals(name)) {
            return new DriveMetadata(session, getFolder(path, false));
        }
        String folderID = getFolderID(path);
        List<File> files = FileNameFilter.byName(name, normalizeFileNames).findAll(getDocuments(folderID, name, fields));
        if (null == files || 0 == files.size() && false == supports(new FolderID(folderID), FileStorageCapability.SEARCH_BY_TERM)) {
            /*
             * also search file by listing the directory's contents as fallback for limited storages
             */
            files = FileNameFilter.byName(name, normalizeFileNames).findAll(getFilesInFolder(session.getStorage().getFolderID(path)));
        }
        return selectFile(files, name);
    }

    /**
     * Gets all documents that are considered as "shared" by the user, i.e. those documents of the user that have been shared to at least
     * one other entity, and are located below the root folder.
     *
     * @param fields The metadata to fetch
     * @return The shared files, or an empty list if there are none
     */
    public List<File> getSharedFiles(List<Field> fields) throws OXException {
        if (false == supports(rootFolderID, FileStorageCapability.OBJECT_PERMISSIONS)) {
            return Collections.emptyList();
        }
        List<File> files = new ArrayList<File>();
        SearchIterator<File> searchIterator = null;
        try {
            searchIterator = getFileAccess().getUserSharedDocuments(fields, Field.FILENAME, SortDirection.DEFAULT);
            while (searchIterator.hasNext()) {
                File file = searchIterator.next();
                /*
                 * only include synchronized files, i.e. files below the root path
                 */
                if (null != getPath(file.getFolderId())) {
                    files.add(file);
                }
            }
        } finally {
            SearchIterators.close(searchIterator);
        }
        return files;
    }

    /**
     * Gets all folders that are considered as "shared" by the user, i.e. those folders of the user that have been shared to at least
     * one other entity, and are located below the root folder.
     *
     * @return The shared folders, or an empty list if there are none
     */
    public List<FileStorageFolder> getSharedFolders() throws OXException {
        Set<String> capabilities = getRootFolder().getCapabilities();
        if (null == capabilities || false == capabilities.contains(FileStorageFolder.CAPABILITY_PERMISSIONS)) {
            return Collections.emptyList();
        }
        List<FileStorageFolder> folders = new ArrayList<FileStorageFolder>();
        FileStorageFolder[] sharedFolders = getFolderAccess().getUserSharedFolders();
        if (null != sharedFolders && 0 < sharedFolders.length) {
            for (FileStorageFolder folder : sharedFolders) {
                /*
                 * only include synchronized folders, i.e. folders below the root path
                 */
                if (null != getPath(folder.getId())) {
                    folders.add(folder);
                }
            }
        }
        return folders;
    }

    /**
     * Selects the "best matching" file from the supplied list.
     *
     * @param files The possible files
     * @param name The name to match
     * @return The best matching file, or <code>null</code> if list was empty
     */
    private static File selectFile(List<File> files, String name) {
        if (null == files || 0 == files.size()) {
            return null;
        } else if (1 == files.size()) {
            return files.get(0);
        } else {
            File normalizedFile = null;
            for (File file : files) {
                if (name.equals(file.getFileName())) {
                    return file;
                }
                if (PathNormalizer.isNormalized(file.getFileName())) {
                    normalizedFile = file;
                }
            }
            return null != normalizedFile ? normalizedFile : files.get(0);
        }
    }

    /**
     * Gets the directory path for the supplied folder identifier, relative to the root synchronization folder.
     *
     * @param folderID The folder identifier to get the path for
     * @return The path
     */
    public String getPath(String folderID) throws OXException {
        if (rootFolderID.toUniqueID().equals(folderID)) {
            return ROOT_PATH;
        }
        String path = knownFolders.getPath(folderID);
        if (null == path) {
            path = resolveToRoot(folderID);
        }
        return path;
    }

    /**
     * Gets the identifier of the folder behind the supplied path.
     *
     * @param path The path to get the folder identifier for
     * @return The folder identifier
     */
    public String getFolderID(String path) throws OXException {
        return getFolderID(path, false);
    }

    /**
     * Gets the identifier of the folder behind the supplied path.
     *
     * @param path The path to get the folder identifier for
     * @param createIfNeeded <code>true</code> to create new folders in case the path not exists, <code>false</code>, otherwise
     * @return The folder identifier
     */
    public String getFolderID(String path, boolean createIfNeeded) throws OXException {
        return getFolder(path, createIfNeeded).getId();
    }

    /**
     * Gets the folder behind the supplied path.
     *
     * @param path The path to get the folder for
     * @return The folder
     */
    public FileStorageFolder getFolder(String path) throws OXException {
        return getFolder(path, false);
    }

    /**
     * Gets the folder behind the supplied path.
     *
     * @param path The path to get the folder for
     * @param createIfNeeded <code>true</code> to create new folders in case the path not exists, <code>false</code>, otherwise
     * @return The folder
     */
    public FileStorageFolder getFolder(String path, boolean createIfNeeded) throws OXException {
        FileStorageFolder folder = knownFolders.getFolder(path);
        if (null == folder) {
            folder = resolveToLeaf(path, createIfNeeded, true);
        }
        return folder;
    }

    /**
     * Optionally gets the folder behind the supplied path, not throwing exceptions in case it not yet exists.
     *
     * @param path The path to get the folder for
     * @return The folder, or <code>null</code> if not found
     */
    public FileStorageFolder optFolder(String path) throws OXException {
        FileStorageFolder folder = knownFolders.getFolder(path);
        if (null == folder) {
            folder = resolveToLeaf(path, false, false);
        }
        return folder;
    }

    /**
     * Gets a value indicating whether a trash folder is available for the synchronized account or not.
     *
     * @return <code>true</code> if the folder is available, <code>false</code>, otherwise
     * @throws OXException
     */
    public boolean hasTrashFolder() throws OXException {
        if (null == hasTrashFolder) {
            try {
                hasTrashFolder = Boolean.valueOf(null != getTrashFolder());
            } catch (OXException e) {
                if (FileStorageExceptionCodes.NO_SUCH_FOLDER.equals(e)) {
                    hasTrashFolder = Boolean.FALSE;
                } else {
                    throw e;
                }
            }
        }
        return hasTrashFolder.booleanValue();
    }

    /**
     * Gets the trash folder of the synchronized account
     *
     * @return The trash folder
     * @throws OXException If no trash folder is available
     */
    public FileStorageFolder getTrashFolder() throws OXException {
        if (null == trashFolder) {
            trashFolder = getFolderAccess().getTrashFolder(rootFolderID.toUniqueID());
        }
        return trashFolder;
    }

    /**
     * Gets all folders in the storage recursively. The "temp" folder, as well as the trash folder including all subfolders are ignored
     * implicitly.
     *
     * @return The folders, each one mapped to its corresponding relative path
     * @throws OXException
     */
    public Map<String, FileStorageFolder> getFolders() throws OXException {
        return getFolders(-1);
    }

    /**
     * Gets all folders in the storage recursively. The "temp" folder, as well as the trash folder including all subfolders are ignored
     * implicitly.
     *
     * @param limit The maximum number of folders to add, or <code>-1</code> for no limitations
     * @return The folders, each one mapped to its corresponding relative path
     */
    public Map<String, FileStorageFolder> getFolders(int limit) throws OXException {
        return getFolders(limit, -1);
    }

    /**
     * Gets all folders in the storage recursively. The "temp" folder, as well as the trash folder including all subfolders are ignored
     * implicitly.
     *
     * @param limit The maximum number of folders to add, or <code>-1</code> for no limitations
     * @param maxDepth The maximum subtree depth folders to add, or <code>-1</code> for no limitations
     * @return The folders, each one mapped to its corresponding relative path
     */
    public Map<String, FileStorageFolder> getFolders(int limit, int maxDepth) throws OXException {
        Map<String, FileStorageFolder> folders = new HashMap<String, FileStorageFolder>();
        FileStorageFolder rootFolder = getRootFolder();
        folders.put(ROOT_PATH, rootFolder);
        if (0 != maxDepth) {
            addSubfolders(folders, rootFolder, ROOT_PATH, true, limit, maxDepth);
        }
        return folders;
    }

    /**
     * Gets all (direct) subfolders in the supplied path. The "temp" folder, as well as the trash folder are ignored implicitly.
     *
     * @param path The path to get the direct subfolders for
     * @return The subfolders, each one mapped to its corresponding relative path, or an empty map if there are none
     */
    public Map<String, FileStorageFolder> getSubfolders(String path) throws OXException {
        Map<String, FileStorageFolder> folders = new HashMap<String, FileStorageFolder>();
        FileStorageFolder folder = getFolder(path);
        addSubfolders(folders, folder, path, false, -1);
        return folders;
    }

    public String getVersionComment() {
        String device = Strings.isEmpty(session.getDeviceName()) ? session.getServerSession().getClient() : session.getDeviceName();
        String product = DriveConfig.getInstance().getShortProductName();
        String format = StringHelper.valueOf(session.getDriveSession().getLocale()).getString(DriveStrings.VERSION_COMMENT);
        return String.format(format, product, device);
    }

    private SearchIterator<File> searchDocuments(String folderID, String pattern, List<Field> fields) throws OXException {
        if (null == pattern) {
            return getDocuments(folderID, fields);
        }
        // search
        return getFileAccess().search(pattern, null != fields ? fields : DriveConstants.FILE_FIELDS, folderID, null,
            SortDirection.DEFAULT, FileStorageFileAccess.NOT_SET, FileStorageFileAccess.NOT_SET);
    }

    private SearchIterator<File> getDocuments(String folderID, String filename, List<Field> fields) throws OXException {
        if (null == filename) {
            return getDocuments(folderID, fields);
        } else if (supports(new FolderID(folderID), FileStorageCapability.SEARCH_BY_TERM)) {
            return getFileAccess().search(Collections.singletonList(folderID), new FileNameTerm(filename, false, false),
                null != fields ? fields : DriveConstants.FILE_FIELDS, null, SortDirection.DEFAULT,
                FileStorageFileAccess.NOT_SET, FileStorageFileAccess.NOT_SET);
        } else {
            return getFileAccess().search(filename, fields, folderID, null, SortDirection.DEFAULT,
                FileStorageFileAccess.NOT_SET, FileStorageFileAccess.NOT_SET);
        }
    }

    private SearchIterator<File> getDocuments(String folderID, List<Field> fields) throws OXException {
        return getFileAccess().getDocuments(
            folderID, null != fields ? fields : DriveConstants.FILE_FIELDS, null, SortDirection.DEFAULT).results();
    }

    private FileStorageFolder resolveToLeaf(String path, boolean createIfNeeded, boolean throwOnAbsence) throws OXException {
        FileStorageFolder currentFolder = getRootFolder();
        String currentPath = ROOT_PATH;
        for (String name : split(path)) {
            String normalizedName = PathNormalizer.normalize(name);
            FileStorageFolder existingFolder = knownFolders.getFolder(currentPath + normalizedName);
            if (null == existingFolder) {
                FileStorageFolder[] subfolders = getFolderAccess().getSubfolders(currentFolder.getId(), false);
                if (null != subfolders && 0 < subfolders.length) {
                    for (FileStorageFolder folder : subfolders) {
                        String normalizedFolderName = PathNormalizer.normalize(folder.getName());
                        knownFolders.remember(currentPath + normalizedFolderName, folder);
                        if (normalizedName.equalsIgnoreCase(normalizedFolderName)) {
                            existingFolder = folder;
                        }
                    }
                }
            }
            if (null == existingFolder) {
                if (false == createIfNeeded) {
                    if (throwOnAbsence) {
                        throw DriveExceptionCodes.PATH_NOT_FOUND.create(path);
                    } else {
                        return null;
                    }
                }
                try {
                    existingFolder = createFolder(currentFolder, name);
                } catch (OXException e) {
                    if ("FLD-0012".equals(e.getErrorCode())) {
                        session.trace("Name conflict during folder creation (" + e.getMessage() + "), trying again...");
                        existingFolder = resolveToLeaf(path, false, false);
                        if (null == existingFolder) {
                            throw e;
                        }
                    } else {
                        throw e;
                    }
                }
                knownFolders.remember(currentPath + normalizedName, existingFolder);
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
        } while (null != currentFolderID && false == "0".equals(currentFolderID) && false == rootFolderID.toUniqueID().equals(currentFolderID));
        if (0 < folders.size() && rootFolderID.toUniqueID().equals(folders.getFirst().getParentId())) {
            StringBuilder pathBuilder = new StringBuilder();
            for (int i = 0; i < folders.size(); i++) {
                FileStorageFolder folder = folders.get(i);
                pathBuilder.append(PATH_SEPARATOR).append(PathNormalizer.normalize(folder.getName()));
                knownFolders.remember(pathBuilder.toString(), folder);
            }
            return pathBuilder.toString();
        } else {
            return null;
        }
    }

    /**
     * Adds all found subfolders of the supplied parent folder. The "temp" folder, as well as the trash folder(s) including all subfolders
     * are ignored implicitly.
     *
     * @param folders The map to add the subfolders
     * @param parent The parent folder
     * @param path The path of the parent folder
     * @param recursive <code>true</code> to add the subfolders recursively, <code>false</code> to only add the direct subfolders
     * @param limit The maximum number of folders to add, or <code>-1</code> for no limitations
     */
    private void addSubfolders(Map<String, FileStorageFolder> folders, FileStorageFolder parent, String path, boolean recursive, int limit) throws OXException {
        addSubfolders(folders, parent, path, recursive, limit, -1);
    }

    /**
     * Adds all found subfolders of the supplied parent folder. The "temp" folder, as well as the trash folder(s) including all subfolders
     * are ignored implicitly.
     *
     * @param folders The map to add the subfolders
     * @param parent The parent folder
     * @param path The path of the parent folder
     * @param recursive <code>true</code> to add the subfolders recursively, <code>false</code> to only add the direct subfolders
     * @param limit The maximum number of folders to add, or <code>-1</code> for no limitations
     * @param maxDepth The maximum subtree depth folders to add, or <code>-1</code> for no limitations
     */
    private void addSubfolders(Map<String, FileStorageFolder> folders, FileStorageFolder parent, String path, boolean recursive, int limit, int maxDepth) throws OXException {
        FileStorageFolder[] subfolders = getFolderAccess().getSubfolders(parent.getId(), false);
        for (FileStorageFolder subfolder : subfolders) {
            String name = PathNormalizer.normalize(subfolder.getName());
            String subPath = DriveConstants.ROOT_PATH.equals(path) ? path + name : path + DriveConstants.PATH_SEPARATOR + name;
            knownFolders.remember(subPath, subfolder);
            if (false == isExcludedSubfolder(subfolder, subPath) && (-1 == limit || limit > folders.size())) {
                folders.put(subPath, subfolder);
                if (recursive && (-1 == maxDepth || maxDepth > DriveUtils.split(subPath).size())) {
                    addSubfolders(folders, subfolder, subPath, true, limit, maxDepth);
                }
            }
        }
    }

    /**
     * Gets a value indicating whether the supplied folder is excluded or not, i.e. it is the temp- or a trash-folder.
     *
     * @param folder The folder to check
     * @param path The folder path to check
     * @return <code>true</code> if the folder is excluded, <code>false</code>, otherwise
     * @throws OXException
     */
    private boolean isExcludedSubfolder(FileStorageFolder folder, String path) throws OXException {
        if (DriveUtils.isInvalidPath(path) || FilenameValidationUtils.isInvalidFolderName(folder.getName())) {
            return true;
        }
        if (session.getTemp().supported() && path.equals(session.getTemp().getPath(false))) {
            return true;
        }
        if (TypeAware.class.isInstance(folder) && FileStorageFolderType.TRASH_FOLDER.equals(((TypeAware)folder).getType())) {
            return true;
        }
        if (TypeAware.class.isInstance(folder)) {
            return FileStorageFolderType.TRASH_FOLDER.equals(((TypeAware)folder).getType());
        }
        String trashFolderID = hasTrashFolder() && null != getTrashFolder() ? getTrashFolder().getId() : null;
        if (null != trashFolderID && trashFolderID.equals(folder.getId())) {
            return true;
        }
        return false;
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
        if (session.isTraceEnabled()) {
            session.trace(this.toString() + "mkdir " + combine(getPath(parent.getId()), name));
        }
        String newFolderID = getFolderAccess().createFolder(newFolder);
        return getFolderAccess().getFolder(newFolderID);
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
     * Gets a value indicating if the underlying storage system supports one or more capabilities or not.
     *
     * @param fileID The file identifier to check the capabilities for
     * @return <code>true</code> if all capabilities are supported, <code>false</code>, otherwise
     */
    public boolean supports(FileID fileID, FileStorageCapability...capabilities) throws OXException {
        return supports(fileID.getService(), fileID.getAccountId(), capabilities);
    }

    /**
     * Gets a value indicating if the underlying storage system supports one or more capabilities or not.
     *
     * @param folderID The folder identifier to check the capabilities for
     * @return <code>true</code> if all capabilities are supported, <code>false</code>, otherwise
     */
    public boolean supports(FolderID folderID, FileStorageCapability...capabilities) throws OXException {
        return supports(folderID.getService(), folderID.getAccountId(), capabilities);
    }

    /**
     * Gets statistics for a specific folder.
     *
     * @param folderID The identifier of the folder to get the statistics for
     * @param recursive <code>true</code> to include subfolders, <code>false</code>, otherwise
     * @return The folder statistics
     */
    public FolderStats getFolderStats(String folderID, boolean recursive) throws OXException {
        IDBasedFolderAccess folderAccess = getFolderAccess();
        FolderStats stats = new FolderStats();
        stats.incrementNumFiles(folderAccess.getNumFiles(folderID));
        stats.incrementTotalSize(folderAccess.getTotalSize(folderID));
        FileStorageFolder[] subfolders = folderAccess.getSubfolders(folderID, true);
        if (null != subfolders && 0 < subfolders.length) {
            stats.incrementNumFolders(subfolders.length);
            if (recursive) {
                for (FileStorageFolder subfolder : subfolders) {
                    stats.add(getFolderStats(subfolder.getId(), recursive));
                }
            }
        }
        return stats;
    }

    /**
     * Gets a value indicating if the underlying storage system supports one or more capabilities or not.
     *
     * @param serviceID The service identifier
     * @param accountID The account identifier
     * @return <code>true</code> if all capabilities are supported, <code>false</code>, otherwise
     */
    private boolean supports(String serviceID, String accountID, FileStorageCapability...capabilities) throws OXException {
        return getFileAccess().supports(serviceID, accountID, capabilities);
    }

    @Override
    public String toString() {
        return session.getServerSession().getLogin() + ':' + rootFolderID.toUniqueID() + "# ";
    }

}
