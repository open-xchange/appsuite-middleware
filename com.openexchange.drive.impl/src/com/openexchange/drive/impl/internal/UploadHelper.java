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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.checksum.ChecksumProvider;
import com.openexchange.drive.impl.storage.StorageOperation;
import com.openexchange.drive.impl.sync.RenameTools;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.file.storage.search.OrTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Streams;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * {@link UploadHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UploadHelper {

    private final SyncSession session;

    /**
     * Initializes a new {@link UploadHelper}.
     *
     * @param session The sync session
     */
    public UploadHelper(SyncSession session) {
        super();
        this.session = session;
    }

    public File perform(final String path, final FileVersion originalVersion, final FileVersion newVersion, final InputStream uploadStream,
        final String contentType, final long offset, final long totalLength, final Date created, final Date modified) throws OXException {
        /*
         * Try to save directly if applicable (no upload resume, no replace, total length is known and smaller than threshold)
         */
        if (null == originalVersion && 0 >= offset && 0 < totalLength && session.getOptimisticSaveThreshold() >= totalLength) {

            Entry<File, String> uploadEntry = session.getStorage().wrapInTransaction(new StorageOperation<Entry<File, String>>() {

                @Override
                public Entry<File, String> call() throws OXException {
                    return saveOptimistically(path, newVersion, uploadStream, contentType, created, modified);
                }
            });
            /*
             * validate checksum
             */
            String checksum = uploadEntry.getValue();
            final File uploadFile = uploadEntry.getKey();
            if (null == checksum) {
                checksum = ChecksumProvider.getChecksum(session, uploadFile).getChecksum();
            }
            if (false == checksum.equals(newVersion.getChecksum())) {
                /*
                 * checksum mismatch, clean up & throw error
                 */
                session.getStorage().deleteFile(uploadFile, true);
                throw DriveExceptionCodes.UPLOADED_FILE_CHECKSUM_ERROR.create(checksum, newVersion.getName(), newVersion.getChecksum());
            }
            return uploadFile;
        }
        /*
         * save data
         */
        Entry<File, String> uploadEntry = session.getStorage().wrapInTransaction(new StorageOperation<Entry<File, String>>() {

            @Override
            public Entry<File, String> call() throws OXException {
                return upload(path, newVersion, uploadStream, contentType, offset, totalLength);
            }
        });
        String checksum = uploadEntry.getValue();
        final File uploadFile = uploadEntry.getKey();
        /*
         * check if upload is completed
         */
        if (-1 == totalLength || uploadFile.getFileSize() >= totalLength) {
            /*
             * validate checksum
             */
            if (null == checksum) {
                checksum = ChecksumProvider.getChecksum(session, uploadFile).getChecksum();
            }
            if (false == checksum.equals(newVersion.getChecksum())) {
                /*
                 * checksum mismatch, clean up & throw error
                 */
                session.getStorage().deleteFile(uploadFile);
                throw DriveExceptionCodes.UPLOADED_FILE_CHECKSUM_ERROR.create(checksum, newVersion.getName(), newVersion.getChecksum());
            }
            /*
             * save document at target path/name
             */
            uploadFile.setFileMD5Sum(checksum);
            final String md5 = checksum;
            return session.getStorage().wrapInTransaction(new StorageOperation<File>() {

                @Override
                public File call() throws OXException {
                    /*
                     * prepare metadata
                     */
                    long sequenceNumber = uploadFile.getSequenceNumber();
                    File file = new DefaultFile();
                    List<Field> fields = new ArrayList<File.Field>();
                    file.setFileName(newVersion.getName());
                    fields.add(Field.FILENAME);
                    file.setVersionComment(session.getStorage().getVersionComment());
                    fields.add(Field.VERSION_COMMENT);
                    file.setFileSize(uploadFile.getFileSize());
                    fields.add(Field.FILE_SIZE);
                    file.setFileMD5Sum(md5);
                    fields.add(Field.FILE_MD5SUM);
                    if (null != contentType) {
                        file.setFileMIMEType(contentType);
                        fields.add(Field.FILE_MIMETYPE);
                    }
                    if (null != created) {
                        file.setCreated(created);
                        fields.add(Field.CREATED);
                    }
                    Date now = new Date();
                    file.setLastModified(null != modified && modified.before(now) ? modified : now);
                    fields.add(Field.LAST_MODIFIED);

                    if (null != originalVersion) {
                        File originalFile = session.getStorage().getFileByName(path, originalVersion.getName(), true);
                        if (null != originalFile && ChecksumProvider.matches(session, originalFile, originalVersion.getChecksum())) {
                            /*
                             * move upload file as new version for existing item
                             */
                            file.setId(originalFile.getId());
                            file.setFolderId(originalFile.getFolderId());
                            if (null != originalFile.getTitle() && originalFile.getTitle().equals(originalFile.getFileName())) {
                                file.setTitle(newVersion.getName());
                                fields.add(Field.TITLE);
                            }
                            InputStream data = null;
                            try {
                                data = session.getStorage().getFileAccess().getDocument(uploadFile.getId(), uploadFile.getVersion());
                                session.getStorage().getFileAccess().saveDocument(file, data, sequenceNumber, fields);
                            } finally {
                                Streams.close(data);
                            }
                            /*
                             * delete upload file
                             */
                            session.getStorage().deleteFile(uploadFile, true);
                            return file;
                        } else {
                            /*
                             * update conflict, move file into target folder using an alternative name
                             */
                            file.setId(uploadFile.getId());
                            file.setFolderId(session.getStorage().getFolderID(path, true));
                            fields.add(Field.FOLDER_ID);
                            String alternativeName = RenameTools.findRandomAlternativeName(newVersion.getName(), session.getDeviceName());
                            file.setFileName(alternativeName);
                            file.setTitle(alternativeName);
                            fields.add(Field.TITLE);
                            session.getStorage().getFileAccess().saveFileMetadata(file, sequenceNumber, fields);
                            return file;
                        }
                    }
                    /*
                     * move upload file into target folder
                     */
                    file.setId(uploadFile.getId());
                    file.setFolderId(session.getStorage().getFolderID(path, true));
                    fields.add(Field.FOLDER_ID);
                    file.setTitle(newVersion.getName());
                    fields.add(Field.TITLE);
                    session.getStorage().getFileAccess().saveFileMetadata(file, sequenceNumber, fields);
                    return file;
                }
            });
        } else {
            /*
             * no new version yet
             */
            return null;
        }
    }

    private Entry<File, String> saveOptimistically(String path, FileVersion newVersion, InputStream uploadStream, String contentType, Date created, Date modified) throws OXException {
        File file = new DefaultFile();
        List<Field> fields = new ArrayList<File.Field>();
        file.setFolderId(session.getStorage().getFolderID(path, true));
        fields.add(Field.FOLDER_ID);
        file.setFileName(newVersion.getName());
        fields.add(Field.FILENAME);
        file.setVersionComment(session.getStorage().getVersionComment());
        fields.add(Field.VERSION_COMMENT);
        if (null != contentType) {
            file.setFileMIMEType(contentType);
            fields.add(Field.FILE_MIMETYPE);
        }
        if (null != created) {
            file.setCreated(created);
            fields.add(Field.CREATED);
        }
        Date now = new Date();
        file.setLastModified(null != modified && modified.before(now) ? modified : now);
        fields.add(Field.LAST_MODIFIED);
        if (session.isTraceEnabled()) {
            String fullPath;
            if (session.getRootFolderID().equals(file.getFolderId())) {
                fullPath = DriveConstants.ROOT_PATH + file.getFileName();
            } else {
                fullPath = session.getStorage().getPath(file.getFolderId()) + '/' + file.getFileName();
            }
            session.trace(session.getStorage().toString() + ">> " + fullPath);
        }
        String checksum = saveDocumentAndChecksum(file, uploadStream, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, fields, false);
        return new AbstractMap.SimpleEntry<File, String>(file, checksum);
    }

    /**
     * Perfom the upload
     */
    Entry<File, String> upload(String path, FileVersion newVersion, InputStream uploadStream, String contentType, long offset, long totalLength) throws OXException {
        /*
         * get/create upload file
         */
        File uploadFile = getUploadFile(path, newVersion.getChecksum());
        /*
         * check current offset
         */
        if (offset != uploadFile.getFileSize()) {
            throw DriveExceptionCodes.INVALID_FILE_OFFSET.create(offset);
        }
        /*
         * process upload
         */
        String checksum = null;
        IDBasedFileAccess fileAccess = session.getStorage().getFileAccess();
        List<Field> modifiedFields = Arrays.asList(File.Field.FILE_SIZE, File.Field.FILE_MIMETYPE, File.Field.VERSION_COMMENT);
        uploadFile.setVersionComment(session.getStorage().getVersionComment());
        uploadFile.setFileMIMEType(contentType);
        uploadFile.setFileSize(totalLength - offset);
        if (session.isTraceEnabled()) {
            String fullPath;
            if (session.getRootFolderID().equals(uploadFile.getFolderId())) {
                fullPath = DriveConstants.ROOT_PATH + uploadFile.getFileName();
            } else {
                fullPath = session.getStorage().getPath(uploadFile.getFolderId()) + '/' + uploadFile.getFileName();
            }
            session.trace(session.getStorage().toString() + ">> " + fullPath);
        }
        if (0 == offset) {
            /*
             * write initial file data, setting the first version number
             */
            checksum = saveDocumentAndChecksum(uploadFile, uploadStream, uploadFile.getSequenceNumber(), modifiedFields, false);
        } else if (session.getStorage().supports(new FolderID(uploadFile.getFolderId()), FileStorageCapability.RANDOM_FILE_ACCESS)) {
            /*
             * append file data via random file access (not incrementing the version number)
             */
            fileAccess.saveDocument(uploadFile, uploadStream, uploadFile.getSequenceNumber(), modifiedFields, offset);
        } else {
            /*
             * work around filestore limitation and append file data via temporary managed file
             */
            checksum = appendViaTemporaryFile(uploadFile, uploadStream);
        }
        uploadFile = fileAccess.getFileMetadata(uploadFile.getId(), uploadFile.getVersion()); //TODO: always necessary?
        return new AbstractMap.SimpleEntry<File, String>(uploadFile, checksum);
    }

    private String appendViaTemporaryFile(File uploadFile, InputStream uploadStream) throws OXException {
        ManagedFile managedFile = null;
        try {
            InputStream inputStream = null;
            try {
                inputStream = session.getStorage().getDocument(uploadFile);
                managedFile = concatenateToFile(inputStream, uploadStream);
            } finally {
                Streams.close(inputStream);
            }
            List<Field> modifiedFields = Arrays.asList(File.Field.FILE_SIZE);
            uploadFile.setFileSize(managedFile.getFile().length());
            return saveDocumentAndChecksum(uploadFile, managedFile.getInputStream(), uploadFile.getSequenceNumber(), modifiedFields, true);
        } finally {
            if (null != managedFile) {
                DriveServiceLookup.getService(ManagedFileManagement.class, true).removeByID(managedFile.getID());
            }
        }
    }

    private String saveDocumentAndChecksum(File file, InputStream inputStream, long sequenceNumber, List<Field> modifiedFields, boolean ignoreVersion) throws OXException {
        DigestInputStream digestStream = null;
        try {
            digestStream = new DigestInputStream(inputStream, MessageDigest.getInstance("MD5"));
            IDBasedFileAccess fileAccess = session.getStorage().getFileAccess();
            if (ignoreVersion && session.getStorage().supports(new FolderID(file.getFolderId()), FileStorageCapability.IGNORABLE_VERSION)) {
                fileAccess.saveDocument(file, digestStream, sequenceNumber, modifiedFields, true);
            } else {
                fileAccess.saveDocument(file, digestStream, sequenceNumber, modifiedFields);
            }
            byte[] digest = digestStream.getMessageDigest().digest();
            return jonelo.jacksum.util.Service.format(digest);
        } catch (NoSuchAlgorithmException e) {
            throw DriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(digestStream);
        }
    }

    private File getUploadFile(String path, String checksum) throws OXException {
        /*
         * check for existing partial upload
         */
        String uploadFileName = getUploadFilename(checksum);
        String uploadPath;
        File uploadFile;
        if (session.getTemp().supported()) {
            boolean existedBefore = session.getTemp().exists();
            uploadPath = session.getTemp().getPath(true);
            if (null == uploadPath) {
                session.trace("Unable to get path to temp folder, falling back to direct upload.");
                uploadPath = path;
            }
            uploadFile = existedBefore ? session.getStorage().getFileByName(uploadPath, uploadFileName) : null;
        } else {
            uploadPath = path;
            uploadFile = session.getStorage().getFileByName(path, uploadFileName);
        }
        if (null == uploadFile) {
            /*
             * create new upload file
             */
            if (session.isTraceEnabled()) {
                session.trace("Creating new upload file at: " + DriveUtils.combine(uploadPath, uploadFileName));
            }
            uploadFile = session.getStorage().createFile(uploadPath, uploadFileName);
            if (null != uploadFile && session.isTraceEnabled()) {
                session.trace("Upload file created: [" + uploadFile.getId() + ']');
            }
        } else if (session.isTraceEnabled()) {
            session.trace("Using existing upload file at " + DriveUtils.combine(uploadPath, uploadFileName) +
                " [" + uploadFile.getId() + "], current size: " + uploadFile.getFileSize() +
                ", last modified: " + (null != uploadFile.getLastModified() ?
                    DriveConstants.LOG_DATE_FORMAT.get().format(uploadFile.getLastModified()) : "(unknown)"));
        }
        return new DefaultFile(uploadFile);
    }

    /**
     * Gets the expected upload offset for the supplied file version based on its checksum. If no upload file yet exists, an offset
     * of <code>0</code> is assumed implicitly.
     *
     * @param path The path where the file version should be uploaded to
     * @param fileVersion The file version
     * @return The offset in bytes of an existing partial upload file, or <code>0</code> if no such file exists
     * @throws OXException
     */
    public long getUploadOffset(String path, FileVersion file) throws OXException {
        List<Long> offsets = getUploadOffsets(path, Collections.singletonList(file));
        return null != offsets && 0 < offsets.size() ? offsets.get(0).longValue() : 0L;
    }

    /**
     * Gets the expected upload offset for the supplied file versions based on their checksums. If no upload file yet exists, an offset
     * of <code>0</code> is assumed implicitly.
     *
     * @param path The path where the file versions should be uploaded to
     * @param fileVersions The file versions
     * @return A list holding the upload offsets for each file version, in the same order as the passed list of file versions
     * @throws OXException
     */
    public List<Long> getUploadOffsets(String path, List<FileVersion> fileVersions) throws OXException {
        if (null == fileVersions || 0 == fileVersions.size()) {
            return Collections.emptyList();
        }
        String uploadPath;
        if (session.getTemp().supported()) {
            /*
             * partial uploads would be stored in temp folder, if it exists
             */
            if (false == session.getTemp().exists()) {
                return Collections.emptyList();
            }
            uploadPath = session.getTemp().getPath(true);
            if (null == uploadPath) {
                session.trace("Unable to get path to temp folder, falling back to direct upload.");
                uploadPath = path;
            }
        } else {
            /*
             * partial uploads are placed inside the target path itself
             */
            uploadPath = path;
        }
        List<Long> uploadOffsets = new ArrayList<Long>(fileVersions.size());
        String folderID = session.getStorage().getFolderID(uploadPath, false);
        List<File> files = findUploadFiles(folderID, fileVersions);
        for (FileVersion fileVersion : fileVersions) {
            String fileName = getUploadFilename(fileVersion.getChecksum());
            long offset = 0;
            for (File file : files) {
                if (fileName.equals(file.getFileName())) {
                    offset = file.getFileSize();
                    break;
                }
            }
            uploadOffsets.add(Long.valueOf(offset));
        }
        return uploadOffsets;
    }

    /**
     * Finds all files representing temporary uploads in a folder for any of the supplied file versions.
     *
     * @param folderID The folder to search in
     * @param fileVersions The file versions to match
     * @return The found files
     * @throws OXException
     */
    private List<File> findUploadFiles(final String folderID, final List<FileVersion> fileVersions) throws OXException {
        List<File> files = new ArrayList<File>();
        final List<Field> fields = Arrays.asList(Field.FILENAME, Field.FILE_SIZE);
        SearchIterator<File> searchIterator = null;
        try {
            searchIterator = session.getStorage().wrapInTransaction(new StorageOperation<SearchIterator<File>>() {

                @Override
                public SearchIterator<File> call() throws OXException {
                    if (session.getStorage().supports(new FolderID(folderID), FileStorageCapability.SEARCH_BY_TERM)) {
                        return session.getStorage().getFileAccess().search(Collections.singletonList(folderID),
                            getSearchTermForUploadFiles(fileVersions), fields, null,
                            SortDirection.DEFAULT, FileStorageFileAccess.NOT_SET, FileStorageFileAccess.NOT_SET);
                    } else {
                        String pattern = 1 == fileVersions.size() ?
                            getUploadFilename(fileVersions.get(0).getChecksum()) : "*" + DriveConstants.FILEPART_EXTENSION;
                        return session.getStorage().getFileAccess().search(pattern, fields, folderID, null,
                            SortDirection.DEFAULT, FileStorageFileAccess.NOT_SET, FileStorageFileAccess.NOT_SET);
                    }
                }
            });
            while (searchIterator.hasNext()) {
                File file = searchIterator.next();
                if (null != file && null != file.getFileName()) {
                    for (FileVersion fileVersion : fileVersions) {
                        String uploadFilename = getUploadFilename(fileVersion.getChecksum());
                        if (uploadFilename.equals(file.getFileName())) {
                            files.add(file);
                            break;
                        }
                    }
                }
            }
        } finally {
            SearchIterators.close(searchIterator);
        }
        return files;
    }

    /**
     * Constructs a search term to match any partial upload files for the supplied file versions.
     *
     * @param filesToUpload The files to construct the search term for
     * @return The search term, or <code>null</code> if supplied files were empty
     */
    private static SearchTerm<?> getSearchTermForUploadFiles(List<FileVersion> filesToUpload) {
        if (null == filesToUpload || 0 == filesToUpload.size()) {
            return null;
        } else if (1 == filesToUpload.size()) {
            String fileName = getUploadFilename(filesToUpload.get(0).getChecksum());
            return new FileNameTerm(fileName, false, false);
        } else {
            List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>();
            for (FileVersion fileVersion : filesToUpload) {
                String fileName = getUploadFilename(fileVersion.getChecksum());
                terms.add(new FileNameTerm(fileName, false, false));
            }
            return new OrTerm(terms);
        }
    }

    /**
     * Constructs the filename as used for partial uploads based on the supplied file checksum string.
     *
     * @param checksum The file's checksum
     * @return The name of the corresponding upload file
     */
    private static String getUploadFilename(String checksum) {
        return '.' + checksum + DriveConstants.FILEPART_EXTENSION;
    }

    private static ManagedFile concatenateToFile(InputStream firstStream, InputStream secondStream) throws OXException {
        /*
         * create target file
         */
        ManagedFileManagement fileManagement = DriveServiceLookup.getService(ManagedFileManagement.class, true);
        ManagedFile managedFile = fileManagement.createManagedFile(fileManagement.newTempFile());
        /*
         * append both streams to managed file
         */
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(managedFile.getFile(), true);
            byte[] buffer = new byte[4096];
            for (int read; (read = firstStream.read(buffer)) > 0;) {
                outputStream.write(buffer, 0, read);
            }
            for (int read; (read = secondStream.read(buffer)) > 0;) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        } catch (IOException e) {
            throw DriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(outputStream);
        }
        return managedFile;
    }

}
