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

package com.openexchange.file.storage.json.ziputil;

import static com.openexchange.file.storage.json.actions.files.AbstractFileAction.getZipDocumentsCompressionLevel;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.json.actions.files.IdVersionPair;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.groupware.upload.impl.UploadUtility;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link ZipMaker} - A utility class to create a ZIP archive.<br>
 * An instance of this class is not <i>thread-safe</i>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
@NotThreadSafe
public class ZipMaker {

    private final IDBasedFileAccess fileAccess;
    private final IDBasedFolderAccess folderAccess;
    private final List<IdVersionPair> idVersionPairs;
    private final boolean recursive;

    /**
     * Initializes a new {@link ZipMaker}.
     *
     * @param idVersionPairs The list of resources to archive
     * @param fileAccess The file access instance
     * @param folderAccess The folder access instance
     */
    public ZipMaker(List<IdVersionPair> idVersionPairs, boolean recursive, IDBasedFileAccess fileAccess, IDBasedFolderAccess folderAccess) {
        super();
        this.idVersionPairs = idVersionPairs;
        this.recursive = recursive;
        this.fileAccess = fileAccess;
        this.folderAccess = folderAccess;
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates the ZIP archives and writes its content to specified output stream
     *
     * @param out The output stream to write to
     * @throws OXException If create operation fails
     * @return long with the number of bytes that have been written to the {@link OutputStream}
     */
    public long writeZipArchive(OutputStream out) throws OXException {
        ZipArchiveOutputStream zipOutput = null;
        try {
            // Initialize ZIP output stream
            zipOutput = new ZipArchiveOutputStream(out);
            zipOutput.setEncoding("UTF-8");
            zipOutput.setUseLanguageEncodingFlag(true);
            zipOutput.setLevel(getZipDocumentsCompressionLevel());

            // The buffer to use
            int buflen = 65536;
            byte[] buf = new byte[buflen];

            // Add to ZIP archive
            for (IdVersionPair idVersionPair : idVersionPairs) {
                if (null == idVersionPair.getIdentifier()) {
                    // Resource denotes a folder
                    addFolder2Archive(idVersionPair.getFolderId(), zipOutput, "", buflen, buf);
                } else {
                    // Resource denotes a file
                    File file = fileAccess.getFileMetadata(idVersionPair.getIdentifier(), idVersionPair.getVersion());
                    addFile2Archive(file, fileAccess.getDocument(idVersionPair.getIdentifier(), idVersionPair.getVersion()), zipOutput, "", buflen, buf);
                }
            }
            return zipOutput.getBytesWritten();
        } finally {
            // Complete the ZIP file
            Streams.close(zipOutput);
        }
    }

    private void addFolder2Archive(String folderId, ZipArchiveOutputStream zipOutput, String pathPrefix, int buflen, byte[] buf) throws OXException {
        try {
            List<File> files = getFilesInFolder(folderId, Field.ID, Field.FOLDER_ID, Field.FILENAME, Field.FILE_MIMETYPE);
            for (File file : files) {
                try {
                    addFile2Archive(file, fileAccess.getDocument(file.getId(), FileStorageFileAccess.CURRENT_VERSION), zipOutput, pathPrefix, buflen, buf);
                } catch (OXException e) {
                    if (!FileStorageCodes.FILE_NOT_FOUND.equals(e)) {
                        throw e;
                    }
                    // Ignore
                }
            }

            if (recursive) {
                for (FileStorageFolder f : folderAccess.getSubfolders(folderId, false)) {
                    String name = f.getName();
                    int num = 1;
                    ZipArchiveEntry entry;
                    String path;
                    while (true) {
                        try {
                            final String entryName;
                            {
                                final int pos = name.indexOf('.');
                                if (pos < 0) {
                                    entryName = name + (num > 1 ? "_(" + num + ")" : "");
                                } else {
                                    entryName = name.substring(0, pos) + (num > 1 ? "_(" + num + ")" : "") + name.substring(pos);
                                }
                            }

                            // Assumes the entry represents a directory if and only if the name ends with a forward slash "/".
                            path = pathPrefix + entryName + "/";
                            entry = new ZipArchiveEntry(path);
                            zipOutput.putArchiveEntry(entry);
                            break;
                        } catch (final java.util.zip.ZipException e) {
                            final String message = e.getMessage();
                            if (message == null || !message.startsWith("duplicate entry")) {
                                throw e;
                            }
                            num++;
                        }
                    }
                    zipOutput.closeArchiveEntry();
                    // Add its files
                    addFolder2Archive(f.getId(), zipOutput, path, buflen, buf);
                }
            }
        } catch (IOException e) {
            throw AjaxExceptionCodes.HTTP_ERROR.create(e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
    }

    private void addFile2Archive(File file, InputStream in, ZipArchiveOutputStream zipOutput, String pathPrefix, int buflen, byte[] buf) throws OXException {
        try {
            // Add ZIP entry to output stream
            String name = file.getFileName();
            if (null == name) {
                final List<String> extensions = MimeType2ExtMap.getFileExtensions(file.getFileMIMEType());
                name = extensions == null || extensions.isEmpty() ? "part.dat" : "part." + extensions.get(0);
            }
            int num = 1;
            ZipArchiveEntry entry;
            while (true) {
                try {
                    final String entryName;
                    {
                        final int pos = name.indexOf('.');
                        if (pos < 0) {
                            entryName = name + (num > 1 ? "_(" + num + ")" : "");
                        } else {
                            entryName = name.substring(0, pos) + (num > 1 ? "_(" + num + ")" : "") + name.substring(pos);
                        }
                    }
                    entry = new ZipArchiveEntry(pathPrefix + entryName);
                    zipOutput.putArchiveEntry(entry);
                    break;
                } catch (final java.util.zip.ZipException e) {
                    final String message = e.getMessage();
                    if (message == null || !message.startsWith("duplicate entry")) {
                        throw e;
                    }
                    num++;
                }
            }

            // Transfer bytes from the file to the ZIP file
            long size = 0;
            for (int read; (read = in.read(buf, 0, buflen)) > 0;) {
                zipOutput.write(buf, 0, read);
                size += read;
            }
            entry.setSize(size);

            // Complete the entry
            zipOutput.closeArchiveEntry();
        } catch (IOException e) {
            throw AjaxExceptionCodes.HTTP_ERROR.create(e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
        } finally {
            Streams.close(in);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Checks the expected ZIP archive against specified size threshold.
     *
     * @param threshold The size threshold in bytes
     * @throws OXException If size threshold gets exceeded
     */
    public void checkThreshold(long threshold) throws OXException {
        if (threshold > 0) {
            long total = 0L;
            for (IdVersionPair idVersionPair : idVersionPairs) {
                total = examineResources4Archive(idVersionPair, total, threshold);
            }
        }
    }

    private long examineResources4Archive(IdVersionPair idVersionPair, long totalSize, long threshold) throws OXException {
        if (null == idVersionPair.getIdentifier()) {
            // Resource denotes a folder
            String folderId = idVersionPair.getFolderId();
            List<Field> columns = Arrays.<File.Field> asList(File.Field.ID, File.Field.FILE_SIZE);
            SearchIterator<File> it = fileAccess.getDocuments(folderId, columns).results();
            try {
                long total = totalSize;
                while (it.hasNext()) {
                    File file = it.next();
                    long fileSize = file.getFileSize();
                    if (fileSize > 0) {
                        total += fileSize;
                        if (total > threshold) {
                            throw FileStorageExceptionCodes.ARCHIVE_MAX_SIZE_EXCEEDED.create(UploadUtility.getSize(threshold, 2, false, true));
                        }
                    }
                }

                SearchIterators.close(it);
                it = null;

                if (recursive) {
                    for (FileStorageFolder f : folderAccess.getSubfolders(folderId, false)) {
                        total = examineResources4Archive(new IdVersionPair(null, null, f.getId()), total, threshold);
                    }
                }

                return total;
            } finally {
                SearchIterators.close(it);
            }
        }

        // Resource denotes a file
        File file = fileAccess.getFileMetadata(idVersionPair.getIdentifier(), idVersionPair.getVersion());
        long fileSize = file.getFileSize();
        if (fileSize > 0) {
            long total = totalSize;
            total += fileSize;
            if (total > threshold) {
                throw FileStorageExceptionCodes.ARCHIVE_MAX_SIZE_EXCEEDED.create(UploadUtility.getSize(threshold, 2, false, true));
            }
            return total;
        }
        return totalSize;
    }

    /**
     * Gets the document metadata for all files in a folder.
     *
     * @param folderId The identifier of the parent folder to get the file metadata for
     * @param columns The metadata to fetch
     * @return Metadata for each file in a list, or an empty list if no files were found
     */
    private List<File> getFilesInFolder(String folderId, File.Field...columns) throws OXException {
        SearchIterator<File> iterator = null;
        try {
            iterator = fileAccess.getDocuments(folderId, Arrays.asList(columns)).results();
            return SearchIterators.asList(iterator);
        } finally {
            SearchIterators.close(iterator);
        }
    }

}
