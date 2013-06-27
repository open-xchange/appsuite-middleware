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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.List;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.MimeType2ExtMap;

/**
 * {@link ZipDocumentsAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@DispatcherNotes(defaultFormat = "file", allowPublicSession = true)
public class ZipDocumentsAction extends AbstractFileAction {

    /**
     * Initializes a new {@link ZipDocumentsAction}.
     */
    public ZipDocumentsAction() {
        super();
    }

    @Override
    public AJAXRequestResult handle(final InfostoreRequest request) throws OXException {
        // Get IDs
        final List<IdVersionPair> idVersionPairs = request.getIdVersionPairs();
        // Get file access
        final IDBasedFileAccess fileAccess = request.getFileAccess();
        // Initialize ZIP'ing
        final ThresholdFileHolder thresholdFileHolder = new ThresholdFileHolder();
        final ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(thresholdFileHolder.asOutputStream());
        zipOutput.setEncoding("UTF-8");
        zipOutput.setUseLanguageEncodingFlag(true);
        try {
            final int buflen = 8192;
            final byte[] buf = new byte[buflen];
            for (final IdVersionPair idVersionPair : idVersionPairs) {
                final String id = idVersionPair.getIdentifier();
                String version = idVersionPair.getVersion();
                if (null == version) {
                    version = FileStorageFileAccess.CURRENT_VERSION;
                }
                final File fileMetadata = fileAccess.getFileMetadata(id, version);
                final InputStream in = fileAccess.getDocument(id, version);
                try {
                    /*
                     * Add ZIP entry to output stream
                     */
                    String name = fileMetadata.getFileName();
                    if (null == name) {
                        final List<String> extensions = MimeType2ExtMap.getFileExtensions(fileMetadata.getFileMIMEType());
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
        } finally {
            // Complete the ZIP file
            Streams.close(zipOutput);
        }
        // Set meta information
        final AJAXRequestData requestData = request.getRequestData();
        if (null != requestData) {
            requestData.setFormat("file");
        }
        thresholdFileHolder.setContentType("application/zip");
        thresholdFileHolder.setName("documents.zip");
        // Return AJAX result
        return new AJAXRequestResult(thresholdFileHolder, "file");
    }

}
