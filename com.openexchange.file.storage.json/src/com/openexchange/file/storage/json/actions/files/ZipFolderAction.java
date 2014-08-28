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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.file.storage.json.actions.files;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.config.ConfigurationService;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.groupware.upload.impl.UploadUtility;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.tools.file.external.FileStorageCodes;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ZipFolderAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "zipfolder", description = "Gets a ZIP archive for a folder's infoitems", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "folder_id", description = "A folder identifier."), @Parameter(name = "recursive", description = "true or false") }, responseDescription = "The ZIP archive binary data")
@DispatcherNotes(defaultFormat = "file")
public class ZipFolderAction extends AbstractFileAction {

    private static volatile Long threshold;
    private static long threshold() {
        Long tmp = threshold;
        if (null == tmp) {
            synchronized (ZipFolderAction.class) {
                tmp = threshold;
                if (null == tmp) {
                    long defaultThreshold = 1073741824;
                    ConfigurationService service = Services.getConfigurationService();
                    if (null == service) {
                        return defaultThreshold;
                    }
                    String property = service.getProperty("com.openexchange.file.storage.zipFolderThreshold");
                    tmp = null == property ? Long.valueOf(defaultThreshold) : Long.valueOf(property.trim());
                    threshold = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    @Override
    public AJAXRequestResult handle(final InfostoreRequest request) throws OXException {
        IDBasedFileAccess fileAccess = request.getFileAccess();
        IDBasedFolderAccess folderAccess = request.getFolderAccess();

        String folderId = request.getFolderId();
        if (Strings.isEmpty(folderId)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(Param.FOLDER_ID.getName());
        }

        boolean recursive;
        {
            String tmp = request.getParameter("recursive");
            recursive = AJAXRequestDataTools.parseBoolParameter(tmp);
        }

        String folderName;
        {
            folderName = folderAccess.getFolder(folderId).getName();
            folderName = saneForFileName(folderName);
        }

        AJAXRequestData ajaxRequestData = request.getRequestData();
        if (ajaxRequestData.setResponseHeader("Content-Type", "application/zip")) {
            createZipArchive(folderId, folderName, fileAccess, recursive ? folderAccess : null, ajaxRequestData);
            return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
        }

        // No direct response possible
        // Check against threshold
        checkThreshold(folderId, fileAccess, folderAccess);

        // Create archive
        ThresholdFileHolder fileHolder = new ThresholdFileHolder();
        fileHolder.setDisposition("attachment");
        fileHolder.setName(folderName + ".zip");
        fileHolder.setContentType("application/zip");
        fileHolder.setDelivery("download");

        // Create ZIP archive
        createZipArchive(folderId, fileAccess, folderAccess, fileHolder.asOutputStream());
        ajaxRequestData.setFormat("file");

        return new AJAXRequestResult(fileHolder, "file");
    }

    private void createZipArchive(String folderId, String saneFolderName, IDBasedFileAccess fileAccess, IDBasedFolderAccess folderAccess, AJAXRequestData ajaxRequestData) throws OXException {
        // Check against threshold
        checkThreshold(folderId, fileAccess, folderAccess);

        // Set HTTP response headers
        {
            final StringBuilder sb = new StringBuilder(512);
            sb.append("attachment");
            DownloadUtility.appendFilenameParameter(saneFolderName + ".zip", "application/zip", ajaxRequestData.getUserAgent(), sb);
            ajaxRequestData.setResponseHeader("Content-Disposition", sb.toString());
        }

        // Create ZIP archive
        try {
            createZipArchive(folderId, fileAccess, folderAccess, ajaxRequestData.optOutputStream());
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    private void checkThreshold(String folderId, IDBasedFileAccess fileAccess, IDBasedFolderAccess folderAccess) throws OXException {
        long threshold = threshold();
        if (threshold > 0) {
            examineFolder4Archive(folderId, fileAccess, folderAccess, 0L, threshold);
        }
    }

    private void createZipArchive(String folderId, IDBasedFileAccess fileAccess, IDBasedFolderAccess folderAccess, OutputStream out) throws OXException {
        ZipArchiveOutputStream zipOutput = null;
        try {
            // Initialize ZIP output stream
            zipOutput = new ZipArchiveOutputStream(out);
            zipOutput.setEncoding("UTF-8");
            zipOutput.setUseLanguageEncodingFlag(true);

            // The buffer to use
            int buflen = 65536;
            byte[] buf = new byte[buflen];

            // Add to ZIP archive
            addFolder2Archive(folderId, fileAccess, folderAccess, zipOutput, "", buflen, buf);
        } finally {
            // Complete the ZIP file
            Streams.close(zipOutput);
        }
    }

    private void examineFolder4Archive(String folderId, IDBasedFileAccess fileAccess, IDBasedFolderAccess folderAccess, long totalSize, long threshold) throws OXException {
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
                        String msg = "ZIP archive exceeds max. allowed size of " + UploadUtility.getSize(threshold, 2, false, true);
                        throw AjaxExceptionCodes.HTTP_ERROR.create(HttpServletResponse.SC_FORBIDDEN, msg);
                    }
                }
            }

            SearchIterators.close(it);
            it = null;

            if (null != folderAccess) {
                for (FileStorageFolder f : folderAccess.getSubfolders(folderId, false)) {
                    examineFolder4Archive(f.getId(), fileAccess, folderAccess, total, threshold);
                }
            }
        } finally {
            SearchIterators.close(it);
        }
    }

    private void addFolder2Archive(String folderId, IDBasedFileAccess fileAccess, IDBasedFolderAccess idBasedFolderAccess, ZipArchiveOutputStream zipOutput, String pathPrefix, int buflen, byte[] buf) throws OXException {
        List<Field> columns = Arrays.<File.Field> asList(File.Field.ID, File.Field.FOLDER_ID, File.Field.FILENAME, File.Field.FILE_MIMETYPE);
        SearchIterator<File> it = fileAccess.getDocuments(folderId, columns).results();
        try {
            while (it.hasNext()) {
                File file = it.next();
                try {
                    addFile2Archive(file, fileAccess.getDocument(file.getId(), FileStorageFileAccess.CURRENT_VERSION), zipOutput, pathPrefix, buflen, buf);
                } catch (OXException e) {
                    if (!FileStorageCodes.FILE_NOT_FOUND.equals(e)) {
                        throw e;
                    }
                    // Ignore
                }
            }

            SearchIterators.close(it);
            it = null;

            if (null != idBasedFolderAccess) {
                for (FileStorageFolder f : idBasedFolderAccess.getSubfolders(folderId, false)) {
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
                    addFolder2Archive(f.getId(), fileAccess, idBasedFolderAccess, zipOutput, path, buflen, buf);
                }
            }
        } catch (IOException e) {
            throw AjaxExceptionCodes.HTTP_ERROR.create(e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
        } finally {
            SearchIterators.close(it);
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

    private static String saneForFileName(final String fileName) {
        if (Strings.isEmpty(fileName)) {
            return "archive";
        }
        final int len = fileName.length();
        final StringBuilder sb = new StringBuilder(len);
        char prev = '\0';
        for (int i = 0; i < len; i++) {
            final char c = fileName.charAt(i);
            if (Strings.isWhitespace(c)) {
                if (prev != '_') {
                    prev = '_';
                    sb.append(prev);
                }
            } else if ('/' == c) {
                if (prev != '_') {
                    prev = '_';
                    sb.append(prev);
                }
            } else if ('\\' == c) {
                if (prev != '_') {
                    prev = '_';
                    sb.append(prev);
                }
            } else if (',' == c) {
                if (prev != '_') {
                    prev = '_';
                    sb.append(prev);
                }
            } else if ('.' == c) {
                if (prev != '_') {
                    prev = '_';
                    sb.append(prev);
                }
            } else {
                prev = '\0';
                sb.append(c);
            }
        }
        String sanitized = sb.toString();
        return Strings.isEmpty(sanitized) ? "archive" : sanitized;
    }

}
