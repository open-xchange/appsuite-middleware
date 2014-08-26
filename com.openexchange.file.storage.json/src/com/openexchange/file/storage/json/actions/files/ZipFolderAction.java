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
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link ZipFolderAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "zipfolder", description = "Gets a ZIP archive for a folder's infoitems", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "folder_id", description = "A folder identifier.")
},
responseDescription = "The ZIP archive binary data")
public class ZipFolderAction extends AbstractFileAction {

    @Override
    public AJAXRequestResult handle(final InfostoreRequest request) throws OXException {

        IDBasedFileAccess fileAccess = request.getFileAccess();

        String folderId = request.getFolderId();

        TimedResult<File> documents = fileAccess.getDocuments(folderId, Arrays.<File.Field> asList(File.Field.ID, File.Field.FILENAME, File.Field.FILE_MIMETYPE ));

        AJAXRequestData ajaxRequestData = request.getRequestData();
        if (ajaxRequestData.setResponseHeader("Content-Type", "application/zip")) {
            try {
                final StringBuilder sb = new StringBuilder(512);
                sb.append("attachment");
                DownloadUtility.appendFilenameParameter("archive.zip", "application/zip", ajaxRequestData.getUserAgent(), sb);
                ajaxRequestData.setResponseHeader("Content-Disposition", sb.toString());
                createZipArchive(documents.results(), fileAccess, ajaxRequestData.optOutputStream());
                // Streamed
                return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
            } catch (final IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }

        throw AjaxExceptionCodes.BAD_REQUEST.create();
    }

    private void createZipArchive(SearchIterator<File> it, IDBasedFileAccess fileAccess, OutputStream out) throws OXException {
        final ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(out);
        zipOutput.setEncoding("UTF-8");
        zipOutput.setUseLanguageEncodingFlag(true);
        try {
            int buflen = 8192;
            byte[] buf = new byte[buflen];

            while (it.hasNext()) {
                File file = it.next();
                addPart2Archive(file, fileAccess.getDocument(file.getId(), FileStorageFileAccess.CURRENT_VERSION), zipOutput, buflen, buf);
            }

        } finally {
            // Complete the ZIP file
            Streams.close(zipOutput);
            try {
                it.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private void addPart2Archive(File file, InputStream in, final ZipArchiveOutputStream zipOutput, final int buflen, final byte[] buf) throws OXException {
        try {
            /*
             * Add ZIP entry to output stream
             */
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
                    entry = new ZipArchiveEntry(entryName);
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
            /*
             * Transfer bytes from the file to the ZIP file
             */
            long size = 0;
            for (int read; (read = in.read(buf, 0, buflen)) > 0;) {
                zipOutput.write(buf, 0, read);
                size += read;
            }
            entry.setSize(size);
            /*
             * Complete the entry
             */
            zipOutput.closeArchiveEntry();
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(in);
        }
    }

}
