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

package com.openexchange.drive.impl.internal;

import static com.openexchange.drive.impl.DriveConstants.ROOT_PATH;
import java.sql.Connection;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.DriveShareLink;
import com.openexchange.drive.DriveShareTarget;
import com.openexchange.drive.DriveUtility;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.FolderStats;
import com.openexchange.drive.NotificationParameters;
import com.openexchange.drive.RestoreContent;
import com.openexchange.drive.TrashContent;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.checksum.ChecksumProvider;
import com.openexchange.drive.impl.checksum.DirectoryChecksum;
import com.openexchange.drive.impl.comparison.ServerDirectoryVersion;
import com.openexchange.drive.impl.metadata.DirectoryMetadataParser;
import com.openexchange.drive.impl.metadata.FileMetadataParser;
import com.openexchange.drive.impl.metadata.JsonDirectoryMetadata;
import com.openexchange.drive.impl.metadata.JsonFileMetadata;
import com.openexchange.drive.impl.storage.DriveStorage;
import com.openexchange.drive.impl.storage.StorageOperation;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FolderPath;
import com.openexchange.file.storage.OriginAwareFileStorageFolder;
import com.openexchange.file.storage.composition.FilenameValidationUtils;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Collators;
import com.openexchange.java.Reference;
import com.openexchange.session.Session;
import com.openexchange.share.LinkUpdate;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.notification.Entities;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link DriveUtilityImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveUtilityImpl implements DriveUtility {

    static final Logger LOG = LoggerFactory.getLogger(DriveUtilityImpl.class);

    private static final String MODIFIED_BY = "modified_by";
    private static final String CREATED_BY = "created_by";
    private static final String MODIFIED = "modified";
    private static final String CREATED = "created";
    private static final String NAME = "name";
    private static final String CHECKSUM = "checksum";
    private static final String PATH = "path";
    private static final String ORIGIN = "origin";
    private static final String DIRECTORIES = "directories";
    private static final String FILES = "files";

    private static final DriveUtility instance = new DriveUtilityImpl();

    /**
     * Gets the drive utility instance.
     *
     * @return The drive utility instance
     */
    public static DriveUtility getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link DriveUtilityImpl}.
     */
    private DriveUtilityImpl() {
        super();
    }

    @Override
    public boolean isInvalidPath(String path) throws OXException {
        return DriveUtils.isInvalidPath(path);
    }

    @Override
    public boolean isInvalidFileName(String fileName) {
        return FilenameValidationUtils.isInvalidFileName(fileName);
    }

    @Override
    public boolean isIgnoredFileName(String fileName) {
        return DriveUtils.isIgnoredFileName(fileName);
    }

    @Override
    public boolean isIgnoredFileName(DriveSession session, String path, String fileName) throws OXException {
        return DriveUtils.isIgnoredFileName(session, path, fileName);
    }

    @Override
    public boolean isDriveSession(Session session) {
        return DriveUtils.isDriveSession(session);
    }

    @Override
    public List<JSONObject> getSubfolderMetadata(DriveSession session) throws OXException {
        SyncSession syncSession = new SyncSession(session);
        Map<String, FileStorageFolder> subfolders = syncSession.getStorage().getSubfolders(DriveConstants.ROOT_PATH);
        if (null == subfolders || 0 == subfolders.size()) {
            return Collections.emptyList();
        }
        List<JSONObject> metadata = new ArrayList<>();
        List<FileStorageFolder> folders = new ArrayList<>(subfolders.values());
        if (1 < folders.size()) {
            Collections.sort(folders, new FolderComparator(session.getLocale()));
        }
        try {
            for (FileStorageFolder subfolder : folders) {
                JSONObject jsonObject = new JsonDirectoryMetadata(syncSession, subfolder).build(false);
                jsonObject.put(PATH, syncSession.getStorage().getPath(subfolder.getId()));
                jsonObject.put(NAME, subfolder.getName());
                metadata.add(jsonObject);
            }
        } catch (JSONException e) {
            throw DriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
        return metadata;
    }

    private static final class FolderComparator implements Comparator<FileStorageFolder> {

        private final Collator collator;

        /**
         * Initializes a new {@link FolderComparator}.
         *
         * @param locale The locale to use, or <code>null</code> to fall back to the default locale
         */
        public FolderComparator(Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(null == locale ? Locale.US : locale);
        }

        @Override
        public int compare(FileStorageFolder folder1, FileStorageFolder folder2) {
            return collator.compare(folder1.getName(), folder2.getName());
        }
    }

    @Override
    public JSONObject getSharesMetadata(DriveSession session) throws OXException {
        final SyncSession syncSession = new SyncSession(session);
        return syncSession.getStorage().wrapInTransaction(new StorageOperation<JSONObject>() {

            @Override
            public JSONObject call() throws OXException {
                JSONObject jsonObject = new JSONObject(2);
                try {
                    jsonObject.put(DIRECTORIES, getDirectorySharesMetadata(syncSession));
                    jsonObject.put(FILES, getFileSharesMetadata(syncSession));
                } catch (JSONException e) {
                    throw DriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
                }
                return jsonObject;
            }
        });
    }

    @Override
    public JSONObject getFileMetadata(DriveSession session, final String path, final FileVersion fileVersion) throws OXException {
        final SyncSession syncSession = new SyncSession(session);
        return syncSession.getStorage().wrapInTransaction(new StorageOperation<JSONObject>() {

            @Override
            public JSONObject call() throws OXException {
                try {
                    return getFileMetadata(syncSession, path, fileVersion);
                } catch (JSONException e) {
                    throw DriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
                }
            }
        });
    }

    @Override
    public JSONObject getDirectoryMetadata(DriveSession session, final DirectoryVersion directoryVersion) throws OXException {
        final SyncSession syncSession = new SyncSession(session);
        return syncSession.getStorage().wrapInTransaction(new StorageOperation<JSONObject>() {

            @Override
            public JSONObject call() throws OXException {
                try {
                    return getDirectoryMetadata(syncSession, directoryVersion);
                } catch (JSONException e) {
                    throw DriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
                }
            }
        });
    }

    @Override
    public void updateFile(DriveSession session, final String path, final FileVersion fileVersion, JSONObject jsonObject, NotificationParameters parameters) throws OXException {
        List<Field> fields = new ArrayList<>();
        final File metadata = FileMetadataParser.parse(jsonObject, fields);
        if (fields.isEmpty()) {
            return;
        }
        final SyncSession syncSession = new SyncSession(session);
        syncSession.trace("About to update metadata for file [" + fileVersion + "]: " + jsonObject);
        final boolean notify = null != parameters.getNotificationTransport();
        final Reference<ShareTarget> targetReference = new Reference<ShareTarget>();
        Entities entities = syncSession.getStorage().wrapInTransaction(new StorageOperation<Entities>() {

            @Override
            public Entities call() throws OXException {
                /*
                 * get the original file
                 */
                List<Field> fields = new ArrayList<>();
                fields.addAll(DriveConstants.FILE_FIELDS);
                fields.add(Field.OBJECT_PERMISSIONS);
                File file = syncSession.getStorage().getFileByName(path, fileVersion.getName(), fields, true);
                if (null == file || false == ChecksumProvider.matches(syncSession, file, fileVersion.getChecksum())) {
                    throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(fileVersion.getName(), fileVersion.getChecksum(), path);
                }
                /*
                 * apply new metadata (permissions only at the moment) & save
                 */
                List<Field> updatedFields = Collections.singletonList(Field.OBJECT_PERMISSIONS);
                DefaultFile updatedFile = new DefaultFile(file);
                updatedFile.setObjectPermissions(metadata.getObjectPermissions());
                String fileID = syncSession.getStorage().getFileAccess().saveFileMetadata(updatedFile, file.getSequenceNumber(), updatedFields);
                /*
                 * re-get updated file to determine added permissions as needed
                 */
                if (notify) {
                    File reloadedFile = syncSession.getStorage().getFileAccess().getFileMetadata(fileID, FileStorageFileAccess.CURRENT_VERSION);
                    targetReference.setValue(new ShareTarget(DriveConstants.FILES_MODULE, reloadedFile.getFolderId(), reloadedFile.getId()));
                    return ShareHelper.getAddedPermissions(file, reloadedFile);
                }
                return null;
            }
        });
        syncSession.trace("Metadata for file [" + fileVersion + "] updated successfully.");
        /*
         * send notifications if needed
         */
        if (notify && null != entities && 0 < entities.size()) {
            ShareHelper shareHelper = new ShareHelper(syncSession);
            parameters.addWarnings(shareHelper.sendNotifications(
                targetReference.getValue(), parameters.getNotificationTransport(), parameters.getNotificationMessage(), entities));
        }
    }

    @Override
    public void updateDirectory(DriveSession session, final DirectoryVersion directoryVersion, JSONObject jsonObject, final boolean cascadePermissions, NotificationParameters parameters) throws OXException {
        final FileStorageFolder folder = DirectoryMetadataParser.parse(jsonObject);
        final SyncSession syncSession = new SyncSession(session);
        syncSession.trace("About to update metadata for directory [" + directoryVersion + "]: " + jsonObject);
        final boolean notify = null != parameters.getNotificationTransport();
        final Reference<ShareTarget> targetReference = new Reference<>();
        Entities entities = syncSession.getStorage().wrapInTransaction(new StorageOperation<Entities>() {

            @Override
            public Entities call() throws OXException {
                /*
                 * get the original directory version
                 */
                FileStorageFolder originalFolder = syncSession.getStorage().getFolder(directoryVersion.getPath());
                ServerDirectoryVersion serverVersion = ServerDirectoryVersion.valueOf(directoryVersion, syncSession);
                String folderID = serverVersion.getDirectoryChecksum().getFolderID().toString();
                /*
                 * apply new metadata (permissions only at the moment) & save
                 */
                DefaultFileStorageFolder updatedFolder = new DefaultFileStorageFolder();
                updatedFolder.setPermissions(folder.getPermissions());
                folderID = syncSession.getStorage().getFolderAccess().updateFolder(folderID, updatedFolder, cascadePermissions);
                /*
                 * re-get updated folder to determine added permissions as needed
                 */
                if (notify) {
                    FileStorageFolder reloadedFolder = syncSession.getStorage().getFolderAccess().getFolder(folderID);
                    targetReference.setValue(new ShareTarget(DriveConstants.FILES_MODULE, reloadedFolder.getId()));
                    return ShareHelper.getAddedPermissions(originalFolder, reloadedFolder);
                }
                return null;
            }
        });
        syncSession.trace("Metadata for directory [" + directoryVersion + "] updated successfully.");
        /*
         * send notifications if needed
         */
        if (notify && null != entities && 0 < entities.size()) {
            ShareHelper shareHelper = new ShareHelper(syncSession);
            parameters.addWarnings(shareHelper.sendNotifications(
                targetReference.getValue(), parameters.getNotificationTransport(), parameters.getNotificationMessage(), entities));
        }
    }

    @Override
    public void moveFile(final DriveSession session, final String path, final FileVersion fileVersion, String newPath, String newName) throws OXException {
        if (null == newPath && null == newName) {
            return;
        }
        final String targetPath = null != newPath ? newPath : path;
        final String targetName = null != newName ? newName : fileVersion.getName();
        final SyncSession syncSession = new SyncSession(session);
        syncSession.trace("About to move file [" + fileVersion + "], targetPath: " + targetPath + ", targetName: " + targetName);
        syncSession.getStorage().wrapInTransaction(new StorageOperation<Void>() {

            @Override
            public Void call() throws OXException {
                /*
                 * get the original file
                 */
                File file = syncSession.getStorage().getFileByName(path, fileVersion.getName(), true);
                if (null == file || false == ChecksumProvider.matches(syncSession, file, fileVersion.getChecksum())) {
                    throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(fileVersion.getName(), fileVersion.getChecksum(), path);
                }
                /*
                 * verify the target name & path
                 */
                if (null == syncSession.getStorage().getFolder(targetPath)) {
                    throw DriveExceptionCodes.PATH_NOT_FOUND.create(targetPath);
                }
                if (session.useDriveMeta() && DriveConstants.METADATA_FILENAME.equals(fileVersion.getName())) {
                    throw DriveExceptionCodes.NO_MODIFY_FILE_PERMISSION.create(fileVersion.getName(), targetPath);
                }
                if (session.useDriveMeta() && DriveConstants.METADATA_FILENAME.equals(targetName)) {
                    throw DriveExceptionCodes.NO_CREATE_FILE_PERMISSION.create(targetName);
                }
                if (FilenameValidationUtils.isInvalidFileName(targetName)) {
                    throw DriveExceptionCodes.INVALID_FILENAME.create(targetName);
                }
                if (DriveUtils.isIgnoredFileName(session, path, targetName)) {
                    throw DriveExceptionCodes.IGNORED_FILENAME.create(targetName);
                }
                if (null != syncSession.getStorage().getFileByName(targetPath, targetName, true)) {
                    throw DriveExceptionCodes.FILE_ALREADY_EXISTS.create(targetName, targetPath);
                }
                if (null != syncSession.getStorage().optFolder(DriveUtils.combine(targetPath, targetName))) {
                    throw DriveExceptionCodes.LEVEL_CONFLICTING_FILENAME.create(targetName, targetPath);
                }
                /*
                 * move the file; further permission checks are performed internally
                 */
                syncSession.getStorage().moveFile(file, targetName, targetPath);
                return null;
            }
        });
        syncSession.trace("File [" + fileVersion + "] moved successfully.");
    }

    @Override
    public void moveDirectory(final DriveSession session, final DirectoryVersion directoryVersion, final String newPath) throws OXException {
        if (null == newPath) {
            return;
        }
        final SyncSession syncSession = new SyncSession(session);
        syncSession.trace("About to move directory [" + directoryVersion + "], newPath: " + newPath);
        syncSession.getStorage().wrapInTransaction(new StorageOperation<Void>() {

            @Override
            public Void call() throws OXException {
                /*
                 * get the server directory checksum (verifies the checksum implicitly)
                 */
                ServerDirectoryVersion.valueOf(directoryVersion, syncSession);
                /*
                 * verify the target name & path
                 */
                if (DriveUtils.isInvalidPath(newPath)) {
                    throw DriveExceptionCodes.INVALID_PATH.create(newPath);
                }
                if (DriveUtils.isIgnoredPath(syncSession, newPath)) {
                    throw DriveExceptionCodes.IGNORED_PATH.create(newPath);
                }
                String firstNewName = null;
                StringBuilder strBuilder = new StringBuilder(ROOT_PATH);
                for (String name : DriveUtils.split(newPath)) {
                    firstNewName = name;
                    String normalizedName = PathNormalizer.normalize(name);
                    FileStorageFolder existingFolder = syncSession.getStorage().optFolder(strBuilder.toString() + normalizedName);
                    if (null == existingFolder) {
                        break;
                    }
                    strBuilder.append(name).append(DriveConstants.PATH_SEPARATOR);
                }
                String lastExistingPath = strBuilder.toString();
                if (null != syncSession.getStorage().getFileByName(lastExistingPath, firstNewName, true)) {
                    throw DriveExceptionCodes.LEVEL_CONFLICTING_PATH.create(newPath, lastExistingPath);
                }
                /*
                 * move the folder; further permission checks are performed internally
                 */
                syncSession.getStorage().moveFolder(directoryVersion.getPath(), newPath);
                return null;
            }
        });
        syncSession.trace("Folder [" + directoryVersion + "] moved successfully.");
    }

    JSONArray getFileSharesMetadata(SyncSession session) throws OXException, JSONException {
        List<FileStorageCapability> specialCapabilites = new ArrayList<>();
        List<Field> fields = new ArrayList<File.Field>();
        fields.addAll(DriveConstants.FILE_FIELDS);
        fields.add(Field.CREATED_BY);
        fields.add(Field.MODIFIED_BY);
        if (session.getStorage().supports(session.getStorage().getRootFolderID(), FileStorageCapability.OBJECT_PERMISSIONS)) {
            specialCapabilites.add(FileStorageCapability.OBJECT_PERMISSIONS);
            fields.add(Field.OBJECT_PERMISSIONS);
            fields.add(Field.SHAREABLE);
        }
        FileStorageCapability[] fileStorageCapabilities = specialCapabilites.toArray(new FileStorageCapability[specialCapabilites.size()]);
        List<File> files = session.getStorage().getSharedFiles(fields);
        JSONArray jsonArray = new JSONArray(files.size());
        for (File file : files) {
            JSONObject jsonObject = new JsonFileMetadata(session, file).build(fileStorageCapabilities);
            jsonObject.put(PATH, session.getStorage().getPath(file.getFolderId()));
            jsonObject.put(CHECKSUM, ChecksumProvider.getChecksum(session, file).getChecksum());
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    JSONArray getDirectorySharesMetadata(SyncSession session) throws OXException, JSONException {
        List<FileStorageFolder> folders = session.getStorage().getSharedFolders();
        if (null == folders || folders.isEmpty()) {
            return new JSONArray(0);
        }
        List<String> folderIDs = new ArrayList<>(folders.size());
        for (FileStorageFolder folder : folders) {
            folderIDs.add(folder.getId());
        }
        List<DirectoryChecksum> checksums = ChecksumProvider.getChecksums(session, folderIDs);
        JSONArray jsonArray = new JSONArray(folders.size());
        for (int i = 0; i < folderIDs.size(); i++) {
            FileStorageFolder folder = folders.get(i);
            JSONObject jsonObject = new JsonDirectoryMetadata(session, folder).build(false);
            jsonObject.put(CHECKSUM, checksums.get(i).getChecksum());
            jsonObject.put(PATH, session.getStorage().getPath(folder.getId()));
            jsonObject.put(NAME, folder.getName());
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    JSONObject getFileMetadata(SyncSession session, String path, FileVersion fileVersion) throws OXException, JSONException {
        File file = session.getStorage().getFileByName(path, fileVersion.getName(), true);
        if (null == file || false == ChecksumProvider.matches(session, file, fileVersion.getChecksum())) {
            throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(fileVersion.getName(), fileVersion.getChecksum(), path);
        }
        JSONObject jsonObject = new JsonFileMetadata(session, session.getStorage().getFile(file.getId())).build();
        jsonObject.put(PATH, session.getStorage().getPath(file.getFolderId()));
        jsonObject.put(CHECKSUM, ChecksumProvider.getChecksum(session, file).getChecksum());
        return jsonObject;
    }

    JSONObject getDirectoryMetadata(SyncSession session, DirectoryVersion directoryVersion) throws OXException, JSONException {
        ServerDirectoryVersion serverVersion = ServerDirectoryVersion.valueOf(directoryVersion, session);
        FileStorageFolder folder = session.getStorage().getFolder(serverVersion.getPath());
        JSONObject jsonObject = new JsonDirectoryMetadata(session, folder).build(false);
        jsonObject.put(CHECKSUM, serverVersion.getChecksum());
        jsonObject.put(PATH, serverVersion.getPath());
        jsonObject.put(NAME, folder.getName());
        if (null != folder.getCreationDate()) {
            jsonObject.put(CREATED, folder.getCreationDate().getTime());
        }
        if (null != folder.getLastModifiedDate()) {
            jsonObject.put(MODIFIED, folder.getLastModifiedDate().getTime());
        }
        jsonObject.put(CREATED_BY, folder.getCreatedBy());
        jsonObject.put(MODIFIED_BY, folder.getModifiedBy());
        return jsonObject;
    }

    @Override
    public DriveShareLink getLink(DriveSession session, DriveShareTarget target) throws OXException {
        return new ShareHelper(new SyncSession(session)).getLink(target);
    }

    @Override
    public DriveShareLink optLink(DriveSession session, DriveShareTarget target) throws OXException {
        return new ShareHelper(new SyncSession(session)).optLink(target);
    }

    @Override
    public DriveShareLink updateLink(DriveSession session, DriveShareTarget target, LinkUpdate linkUpdate) throws OXException {
        return new ShareHelper(new SyncSession(session)).updateLink(target, linkUpdate);
    }

    @Override
    public void deleteLink(DriveSession session, DriveShareTarget target) throws OXException {
        new ShareHelper(new SyncSession(session)).deleteLink(target);
    }

    @Override
    public void notify(DriveSession session, DriveShareTarget target, int[] entityIDs, NotificationParameters parameters) throws OXException {
        ShareHelper shareHelper = new ShareHelper(new SyncSession(session));
        parameters.addWarnings(shareHelper.notifyEntities(target, parameters.getNotificationTransport(), parameters.getNotificationMessage(), entityIDs));
    }

    @Override
    public JSONArray autocomplete(final DriveSession session, final String query, Map<String, Object> parameters) throws OXException {
        return AutocompleteHelper.autocomplete(session, query, parameters);
    }

    @Override
    public FolderStats getTrashFolderStats(DriveSession session) throws OXException {
        DriveStorage storage = new SyncSession(session).getStorage();
        if (false == storage.hasTrashFolder()) {
            return null;
        }
        FileStorageFolder trashFolder = storage.getTrashFolder();
        return storage.getFolderStats(trashFolder.getId(), true);
    }

    @Override
    public JSONObject getTrashContent(DriveSession session) throws OXException {
        SyncSession syncSession = new SyncSession(session);
        DriveStorage storage = syncSession.getStorage();
        if (false == storage.hasTrashFolder()) {
            return null;
        }
        TrashContent trashContent = storage.getTrashContent();
        JSONObject result = new JSONObject(2);

        try {
            FileStorageFolder[] subfolders = trashContent.getSubfolders();
            if (subfolders != null && subfolders.length > 0) {
                JSONArray folderArray = loadFolders(syncSession, subfolders);
                result.put(DIRECTORIES, filterUnwantedFields(folderArray));
            }

            SearchIterator<File> files = trashContent.getFiles();
            if (files != null) {
                JSONArray fileArray = loadFiles(syncSession, files);
                if(fileArray.length() > 0) {
                    result.put(FILES, filterUnwantedFields(fileArray));
                }
            }
        } catch (JSONException ex) {
            throw DriveExceptionCodes.IO_ERROR.create(ex, ex.getMessage());
        }

        return result;
    }

    final static String[] UNWANTED_FIELDS = new String[] { "own_rights", "permissions", "extended_permissions", "jump", "shareable", "preview", "thumbnail" };

    private JSONArray filterUnwantedFields(JSONArray array) {
        array.forEach(new Consumer<Object>() {

            @Override
            public void accept(Object t) {
                if (t instanceof JSONObject) {
                    JSONObject json = (JSONObject) t;
                    for (String field : UNWANTED_FIELDS) {
                        json.remove(field);
                    }
                }
            }
        });
        return array;
    }

    private JSONArray loadFiles(SyncSession syncSession, SearchIterator<File> files) throws OXException, JSONException {
        JSONArray fileArray = new JSONArray();
        while (files.hasNext()) {
            File file = files.next();
            JSONObject jsonObject = new JsonFileMetadata(syncSession, file).build();
            if (file.getOrigin() != null) {
                jsonObject.put(ORIGIN, OriginPathFolderFieldWriter.getInstance().getOrigin(file.getOrigin(), syncSession));
            }
            fileArray.put(jsonObject);
        }
        return fileArray;
    }

    private JSONArray loadFolders(SyncSession syncSession, FileStorageFolder[] subfolders) throws OXException, JSONException {
        JSONArray folderArray = new JSONArray(subfolders.length);
        for (FileStorageFolder folder : subfolders) {
            JSONObject jsonObject = new JsonDirectoryMetadata(syncSession, folder).build(false);
            jsonObject.put(NAME, folder.getName());
            if (null != folder.getCreationDate()) {
                jsonObject.put(CREATED, folder.getCreationDate().getTime());
            }
            if (null != folder.getLastModifiedDate()) {
                jsonObject.put(MODIFIED, folder.getLastModifiedDate().getTime());
            }
            jsonObject.put(CREATED_BY, folder.getCreatedBy());
            jsonObject.put(MODIFIED_BY, folder.getModifiedBy());
            FolderPath origin = ((OriginAwareFileStorageFolder) folder).getOrigin();
            jsonObject.put(ORIGIN, OriginPathFolderFieldWriter.getInstance().getOrigin(origin, syncSession));
            folderArray.put(jsonObject);
        }
        return folderArray;
    }


    @Override
    public FolderStats emptyTrash(DriveSession session) throws OXException {
        DriveStorage storage = new SyncSession(session).getStorage();
        if (false == storage.hasTrashFolder()) {
            return null;
        }
        FileStorageFolder trashFolder = storage.getTrashFolder();
        storage.getFolderAccess().clearFolder(trashFolder.getId(), true);
        return storage.getFolderStats(trashFolder.getId(), true);
    }

    @Override
    public void removeFromTrash(DriveSession session, List<String> files, List<String> folders) throws OXException {
        new SyncSession(session).getStorage().deleteFromTrash(files, folders);
    }

    @Override
    public RestoreContent restoreFromTrash(DriveSession session, List<String> files, List<String> folders) throws OXException {
        return new SyncSession(session).getStorage().restoreFromTrash(files, folders);
    }


    private static final class OriginPathFolderFieldWriter {

        private static final String USER_INFOSTORE_FOLDER_ID   = Integer.toString(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
        private static final String PUBLIC_INFOSTORE_FOLDER_ID = Integer.toString(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);
        private static final String TREE_ID = "1";

        private static OriginPathFolderFieldWriter INSTANCE = new OriginPathFolderFieldWriter();

        public static OriginPathFolderFieldWriter getInstance() {
            return INSTANCE;
        }

        /**
         * Initializes a new {@link OriginPathFolderFieldWriter}.
         */
        private OriginPathFolderFieldWriter() {
            super();
        }

        public String getOrigin(FolderPath originPath, SyncSession session) {
            if (null == originPath) {
                return null;
            }

            try {
                String folderId;
                switch (originPath.getType()) {
                    case PRIVATE:
                        folderId = getInfoStoreDefaultFolder(session).getID();
                        break;
                    case PUBLIC:
                        folderId = PUBLIC_INFOSTORE_FOLDER_ID;
                        break;
                    case SHARED:
                        folderId = USER_INFOSTORE_FOLDER_ID;
                        break;
                    case UNDEFINED: /* fall-through */
                    default:
                        folderId = getInfoStoreDefaultFolder(session).getID();
                        originPath = FolderPath.EMPTY_PATH;
                }

                UserizedFolder folder = getFolderFor(folderId, session);
                Locale locale = session.getDriveSession().getLocale();
                StringBuilder sb = new StringBuilder();
                sb.append(folder.getLocalizedName(locale));

                if (!originPath.isEmpty()) {
                    boolean searchInSubfolders = true;
                    for (String folderName : originPath.getPathForRestore()) {
                        boolean found = false;
                        if (searchInSubfolders) {
                            UserizedFolder[] subfolders = getSuboldersFor(folderId, session);
                            for (int i = 0; !found && i < subfolders.length; i++) {
                                UserizedFolder subfolder = subfolders[i];
                                if (folderName.equals(subfolder.getName())) {
                                    found = true;
                                    sb.append("/").append(subfolder.getLocalizedName(locale));
                                }
                            }
                        }

                        if (false == found) {
                            sb.append("/").append(folderName);
                            searchInSubfolders = false;
                        }
                    }
                }

                return sb.toString();
            } catch (OXException e) {
                LOG.debug("Failed to determine original path", e);
                return null;
            }
        }

        private UserizedFolder getInfoStoreDefaultFolder(SyncSession session) throws OXException {
            FolderServiceDecorator decorator = initDecorator(session);
            FolderService folderService = DriveServiceLookup.getService(FolderService.class, true);
            return folderService.getDefaultFolder(session.getServerSession().getUser(), TREE_ID, InfostoreContentType.getInstance(), session.getServerSession(), decorator);
        }

        private UserizedFolder getFolderFor(String folderId, SyncSession session) throws OXException {
            FolderServiceDecorator decorator = initDecorator(session);
            FolderService folderService = DriveServiceLookup.getService(FolderService.class, true);
            return folderService.getFolder(TREE_ID, folderId, session.getServerSession(), decorator);
        }

        private UserizedFolder[] getSuboldersFor(String folderId, SyncSession session) throws OXException {
            FolderServiceDecorator decorator = initDecorator(session);
            FolderService folderService = DriveServiceLookup.getService(FolderService.class, true);
            FolderResponse<UserizedFolder[]> subfolderResponse = folderService.getSubfolders(TREE_ID, folderId, true, session.getServerSession(), decorator);
            return subfolderResponse.getResponse();
        }

        /**
         * Creates and initializes a folder service decorator ready to use with calls to the underlying folder service.
         *
         * @param session The sync session
         * @return A new folder service decorator
         */
        private FolderServiceDecorator initDecorator(SyncSession session) {
            FolderServiceDecorator decorator = new FolderServiceDecorator();
            Object connection = session.getServerSession().getParameter(Connection.class.getName());
            if (null != connection) {
                decorator.put(Connection.class.getName(), connection);
            }
            decorator.put("altNames", Boolean.TRUE.toString());
            decorator.setLocale(session.getDriveSession().getLocale());
            return decorator;
        }
    }

}
