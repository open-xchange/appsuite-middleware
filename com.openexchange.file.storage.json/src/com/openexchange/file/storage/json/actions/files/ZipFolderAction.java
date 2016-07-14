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

package com.openexchange.file.storage.json.actions.files;

import java.io.IOException;
import java.util.Collections;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.json.ziputil.ZipMaker;
import com.openexchange.java.Strings;
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

        // Initialize ZIP maker for folder resource
        ZipMaker zipMaker = new ZipMaker(Collections.singletonList(new IdVersionPair(null, null, folderId)), recursive, fileAccess, folderAccess);

        // Check against size threshold
        zipMaker.checkThreshold(threshold());

        AJAXRequestData ajaxRequestData = request.getRequestData();
        if (ajaxRequestData.setResponseHeader("Content-Type", "application/zip")) {
            // Set HTTP response headers
            {
                final StringBuilder sb = new StringBuilder(512);
                sb.append("attachment");
                DownloadUtility.appendFilenameParameter(folderName + ".zip", "application/zip", ajaxRequestData.getUserAgent(), sb);
                ajaxRequestData.setResponseHeader("Content-Disposition", sb.toString());
            }

            // Write ZIP archive
            long bytesWritten = 0;
            try {
                bytesWritten = zipMaker.writeZipArchive(ajaxRequestData.optOutputStream());
            } catch (IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }

            // Signal direct response
            AJAXRequestResult result = new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
            if(bytesWritten != 0) {
                result.setResponseProperty("X-Content-Size", bytesWritten);
            }
            return result;
        }

        // No direct response possible

        // Create archive
        ThresholdFileHolder fileHolder = new ThresholdFileHolder();
        fileHolder.setDisposition("attachment");
        fileHolder.setName(folderName + ".zip");
        fileHolder.setContentType("application/zip");
        fileHolder.setDelivery("download");

        // Create ZIP archive
        zipMaker.writeZipArchive(fileHolder.asOutputStream());

        ajaxRequestData.setFormat("file");
        return new AJAXRequestResult(fileHolder, "file");
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
