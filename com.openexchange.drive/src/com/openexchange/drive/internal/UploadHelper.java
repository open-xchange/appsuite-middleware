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

package com.openexchange.drive.internal;

import static com.openexchange.drive.storage.DriveConstants.TEMP_PATH;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.comparison.ServerFileVersion;
import com.openexchange.drive.storage.DriveConstants;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageIgnorableVersionFileAccess;
import com.openexchange.file.storage.FileStorageRandomFileAccess;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Streams;

/**
 * {@link UploadHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UploadHelper {

    private final DriveSession session;

    /**
     * Initializes a new {@link UploadHelper}.
     *
     * @param session The drive session
     */
    public UploadHelper(DriveSession session) {
        super();
        this.session = session;
    }

    public FileVersion perform(String path, FileVersion originalVersion, FileVersion newVersion, InputStream uploadStream, long offset, long totalLength) throws OXException {
        /*
         * save data
         */
        Entry<File, String> uploadEntry = upload(newVersion, uploadStream, offset);
        String checksum = uploadEntry.getValue();
        File uploadFile = uploadEntry.getKey();
        /*
         * check if upload is completed
         */
        if (-1 == totalLength || uploadFile.getFileSize() >= totalLength) {
            /*
             * validate checksum if available
             */
            if (null != checksum && false == checksum.equals(newVersion.getChecksum())) {
                throw DriveExceptionCodes.UPLOADED_FILE_CHECKSUM_ERROR.create(checksum, newVersion.getName(), newVersion.getChecksum());
            }
            /*
             * save document at target path/name
             */
            if (null != originalVersion) {
                File originalFile = session.getStorage().findFileByNameAndChecksum(path, originalVersion.getName(), originalVersion.getChecksum());
                if (null != originalFile) {
                    File copiedFile = session.getStorage().copyFile(uploadFile, originalFile);
                    session.getStorage().deleteFile(uploadFile);
                    return new ServerFileVersion(copiedFile, newVersion.getChecksum());
                }
            }
            File movedFile = session.getStorage().moveFile(uploadFile, newVersion.getName(), path);
            return new ServerFileVersion(movedFile, newVersion.getChecksum());
        } else {
            /*
             * no new version yet
             */
            return null;
        }
    }

    private Entry<File, String> upload(FileVersion newVersion, InputStream uploadStream, long offset) throws OXException {
        /*
         * get/create upload file
         */
        File uploadFile = getUploadFile(newVersion.getChecksum(), true);
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
        FileStorageFileAccess fileAccess = session.getStorage().getFileAccess();
        List<Field> modifiedFields = Arrays.asList(File.Field.FILE_SIZE);
        if (0 == offset) {
            /*
             * write initial file data, setting the first version number
             */
            checksum = saveDocumentAndChecksum(uploadFile, uploadStream, uploadFile.getSequenceNumber(), modifiedFields, false);
        } else if (FileStorageRandomFileAccess.class.isInstance(fileAccess)) {
            /*
             * append file data via random file access, preferably not incrementing the version number
             */
            ((FileStorageRandomFileAccess)fileAccess).saveDocument(
                uploadFile, uploadStream, uploadFile.getSequenceNumber(), modifiedFields, false, offset);
        } else {
            /*
             * work around filestore limitation and append file data via temporary managed file
             */
            checksum = appendViaTemporaryFile(uploadFile, uploadStream);
        }
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
            FileStorageFileAccess fileAccess = session.getStorage().getFileAccess();
            if (ignoreVersion && FileStorageIgnorableVersionFileAccess.class.isInstance(fileAccess)) {
                ((FileStorageIgnorableVersionFileAccess)fileAccess).saveDocument(file, digestStream, sequenceNumber, modifiedFields, true);
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

    public long getUploadOffset(FileVersion file) throws OXException {
        File uploadFile = getUploadFile(file.getChecksum(), false);
        return null == uploadFile ? 0 : uploadFile.getFileSize();
    }

    private File getUploadFile(String checksum, boolean createIfAbsent) throws OXException {
        /*
         * check for existing partial upload
         */
        String uploadFileName = getUploadFilename(checksum);
        session.getStorage().getFolder(TEMP_PATH, true); // to ensure the temp folder exists
        File uploadFile = session.getStorage().findFileByName(TEMP_PATH, uploadFileName);
        if (null == uploadFile && createIfAbsent) {
            /*
             * create new upload file
             */
            uploadFile = session.getStorage().createFile(TEMP_PATH, uploadFileName);
        }
        return uploadFile;
    }

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
