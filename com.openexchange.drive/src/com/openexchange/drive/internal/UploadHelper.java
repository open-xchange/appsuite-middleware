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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.comparison.ServerFileVersion;
import com.openexchange.drive.storage.DriveConstants;
import com.openexchange.drive.storage.filter.FileFilter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageIgnorableVersionFileAccess;
import com.openexchange.file.storage.FileStorageRandomFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Streams;
import com.openexchange.tools.iterator.SearchIterator;

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
        File uploadFile = upload(newVersion, uploadStream, offset);
        /*
         * check if upload is completed
         */
        if (-1 == totalLength || uploadFile.getFileSize() >= totalLength) {
            if (null != originalVersion) {
                /*
                 * overwrite original file if still valid and unchanged
                 */
                File currentFile = session.getStorage().findFileByName(path, originalVersion.getName());
                if (null != currentFile && originalVersion.getChecksum().equals(session.getMD5(currentFile))) {
                    //TODO: overwrite directly during update
//                    session.getStorage().updateFile(uploadFile, newVersion.getName(), path);
                    //workaround:
                    {
                        session.getStorage().getFileAccess().saveDocument(
                            currentFile, session.getStorage().getDocument(uploadFile), uploadFile.getSequenceNumber());
                        session.getStorage().deleteFile(uploadFile, true);
                    }
                    return new ServerFileVersion(currentFile, newVersion.getChecksum());//TODO: validate checksum
                }
            }
            /*
             * file transfer finished, save document & rename file to target file name
             */
            File updatedFile = session.getStorage().updateFile(uploadFile, newVersion.getName(), path);
            return new ServerFileVersion(updatedFile, newVersion.getChecksum());//TODO: validate checksum
        } else {
            /*
             * no new version yet
             */
            return null;
        }
    }

    private File upload(FileVersion newVersion, InputStream uploadStream, long offset) throws OXException {
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
        FileStorageFileAccess fileAccess = session.getStorage().getFileAccess();
        List<Field> modifiedFields = Arrays.asList(File.Field.FILE_SIZE);
        if (0 == offset) {
            /*
             * write initial file data, preferably not incrementing the version number
             */
            if (FileStorageIgnorableVersionFileAccess.class.isInstance(fileAccess)) {
                ((FileStorageIgnorableVersionFileAccess)fileAccess).saveDocument(
                    uploadFile, uploadStream, uploadFile.getSequenceNumber(), modifiedFields, false);
            } else {
                fileAccess.saveDocument(uploadFile, uploadStream, uploadFile.getSequenceNumber());
            }
        } else if (FileStorageRandomFileAccess.class.isInstance(fileAccess)) {
            /*
             * append file data via random file access
             */
            ((FileStorageRandomFileAccess)fileAccess).saveDocument(
                uploadFile, uploadStream, uploadFile.getSequenceNumber(), modifiedFields, false, offset);
        } else {
            /*
             * work around filestore limitation and append file data via temporary managed file
             */
            appendViaTemporaryFile(uploadFile, uploadStream);
        }
        return uploadFile;
    }

    private void appendViaTemporaryFile(File uploadFile, InputStream uploadStream) throws OXException {
        ManagedFile managedFile = null;
        try {
            InputStream inputStream = null;
            try {
                inputStream = session.getStorage().getDocument(uploadFile);
                managedFile = concatenateToFile(inputStream, uploadStream);
            } finally {
                Streams.close(inputStream);
            }
            uploadFile.setFileSize(managedFile.getSize());
            session.getStorage().getFileAccess().saveDocument(uploadFile, managedFile.getInputStream(), uploadFile.getSequenceNumber());
        } finally {
            if (null != managedFile) {
                DriveServiceLookup.getService(ManagedFileManagement.class, true).removeByID(managedFile.getID());
            }
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
        final String uploadFileName = getUploadFilename(checksum);
        session.getStorage().getFolder(TEMP_PATH, true); // to ensure the temp folder exists
        File uploadFile = session.getStorage().findFile(TEMP_PATH, new FileFilter() {

            @Override
            public boolean accept(File file) throws OXException {
                return null != file && (uploadFileName.equals(file.getFileName()) ||
                    null == file.getFileName() && uploadFileName.equals(file.getTitle()));
            }
        });
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


    public static FileVersion perform(DriveSession session, InputStream uploadStream, String folderID, FileVersion originalVersion, FileVersion newVersion,
        long offset, long totalLength) throws OXException {

        IDBasedFileAccess fileAccess = session.getFileAccess();
        String uploadFileName = getUploadFileName(newVersion);
        File uploadFile = session.getFileByName(folderID, uploadFileName);

        //TODO: write through directly to filestore
        ManagedFileManagement fileManagement = DriveServiceLookup.getService(ManagedFileManagement.class, true);
        ManagedFile managedFile;
        if (null == uploadFile) {
            if (0 < offset) {
                throw new OXException();
            }
            /*
             * prepare new file if needed
             */
            uploadFile = new DefaultFile();
            uploadFile.setFolderId(folderID);
            uploadFile.setFileName(uploadFileName);
            fileAccess.saveFileMetadata(uploadFile, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);
            managedFile = fileManagement.createManagedFile(fileManagement.newTempFile());
        } else {
            /*
             * prepare temp file with current state
             */
            managedFile = fileManagement.createManagedFile(fileAccess.getDocument(uploadFile.getId(), uploadFile.getVersion()));
        }
        long currentLength = managedFile.getFile().length();
        if (offset != currentLength) {
            throw new OXException();
        }
        /*
         * append data to managed file
         */
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(managedFile.getFile(), true);
            byte[] buffer = new byte[4096];
            for (int read; (read = uploadStream.read(buffer)) > 0;) {
                outputStream.write(buffer, 0, read);
                currentLength += read;
            }
            outputStream.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            Streams.close(uploadStream, outputStream);
        }



        List<Field> editedFields = new ArrayList<File.Field>();
        File editedFile = new DefaultFile();
        editedFile.setId(uploadFile.getId());
        editedFile.setFolderId(uploadFile.getFolderId());

        if (-1 == totalLength || currentLength >= totalLength) {

            if (null != originalVersion) {
                /*
                 * overwrite original file and remove upload file if original file still valid and unchanged
                 */
                File originalFile = filter(fileAccess.getDocuments(folderID), originalVersion.getName());
                // TODO
                if (null != originalFile && session.getMD5(originalFile).equals(originalVersion.getChecksum())) {
                    File overwrittenFile = new DefaultFile();
                    overwrittenFile.setId(originalFile.getId());
                    overwrittenFile.setFolderId(originalFile.getFolderId());
                    fileAccess.saveDocument(overwrittenFile, managedFile.getInputStream(), originalFile.getSequenceNumber());
                    fileAccess.removeDocument(Arrays.asList(new String[] { uploadFile.getId() }), uploadFile.getSequenceNumber());
                    return new ServerFileVersion(overwrittenFile, newVersion.getChecksum());//TODO: validate checksum
                }
            }
            /*
             * file transfer finished, save document & rename file to target file name
             */
            editedFile.setFileName(newVersion.getName());
            editedFields.add(Field.FILENAME);
            editedFile.setTitle(newVersion.getName());
            editedFields.add(Field.TITLE);
            fileAccess.saveDocument(editedFile, managedFile.getInputStream(), editedFile.getSequenceNumber(), editedFields);
            return new ServerFileVersion(editedFile, newVersion.getChecksum());//TODO: validate checksum
        } else {
            /*
             * save so far uploaded document
             */
            editedFile.setFileName(uploadFile.getFileName());
            editedFields.add(Field.FILENAME);
            editedFile.setTitle(uploadFile.getFileName());
            editedFields.add(Field.TITLE);
            fileAccess.saveDocument(editedFile, managedFile.getInputStream(), uploadFile.getSequenceNumber());
            return null;
        }
    }

    public static String getUploadFileName(FileVersion fileVersion) {
        return '.' + fileVersion.getChecksum() + ".tmp";
    }

    public static boolean isUploadFileName(String fileName) {
        return null != fileName && fileName.matches("(?i)\\A\\.[A-F0-9]{32}\\.tmp\\z");
    }

    private static File filter(TimedResult<File> files, String name) throws OXException {
        SearchIterator<File> searchIterator = null;
        try {
            searchIterator = files.results();
            while (searchIterator.hasNext()) {
                File file = searchIterator.next();
                if (name.equals(file.getFileName())) {
                    return file;
                }
            }
        } finally {
            if (null != searchIterator) {
                searchIterator.close();
            }
        }
        return null;
    }

}
