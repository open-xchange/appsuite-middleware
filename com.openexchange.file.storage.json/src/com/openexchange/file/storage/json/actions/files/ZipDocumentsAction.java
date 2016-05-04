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
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.json.ziputil.ZipMaker;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

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
    public AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        // Get IDs
        List<IdVersionPair> idVersionPairs;
        {
            String value = request.getParameter("body");
            if (Strings.isEmpty(value)) {
                idVersionPairs = request.getIdVersionPairs();
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(value);
                    int len = jsonArray.length();
                    idVersionPairs = new ArrayList<IdVersionPair>(len);
                    for (int i = 0; i < len; i++) {
                        JSONObject tuple = jsonArray.getJSONObject(i);

                        // Identifier
                        String id = tuple.optString(Param.ID.getName(), null);

                        // Folder
                        String folderId = tuple.optString(Param.FOLDER_ID.getName(), null);

                        // Version
                        String version = tuple.optString(Param.VERSION.getName(), FileStorageFileAccess.CURRENT_VERSION);

                        // Check validity
                        if (null == id && null == folderId) {
                            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("body", "Invalid resource identifier: " + tuple);
                        }

                        idVersionPairs.add(new IdVersionPair(id, version, folderId));
                    }
                } catch (JSONException e) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, "body", e.getMessage());
                }
            }
        }

        boolean recursive;
        {
            String tmp = request.getParameter("recursive");
            recursive = AJAXRequestDataTools.parseBoolParameter(tmp);
        }

        // Get file/folder access
        IDBasedFileAccess fileAccess = request.getFileAccess();
        IDBasedFolderAccess folderAccess = request.getFolderAccess();

        // Initialize ZIP maker for folder resource
        ZipMaker zipMaker = new ZipMaker(idVersionPairs, recursive, fileAccess, folderAccess);

        // Check against size threshold
        zipMaker.checkThreshold(threshold());

        AJAXRequestData ajaxRequestData = request.getRequestData();
        if (ajaxRequestData.setResponseHeader("Content-Type", "application/zip")) {
            // Set HTTP response headers
            {
                final StringBuilder sb = new StringBuilder(512);
                sb.append("attachment");
                DownloadUtility.appendFilenameParameter("documents.zip", "application/zip", ajaxRequestData.getUserAgent(), sb);
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
            if (bytesWritten != 0) {
                result.setResponseProperty("X-Content-Size", bytesWritten);
            }
            return result;
        }

        // No direct response possible

        // Create archive
        ThresholdFileHolder fileHolder = new ThresholdFileHolder();
        fileHolder.setDisposition("attachment");
        fileHolder.setName("documents.zip");
        fileHolder.setContentType("application/zip");
        fileHolder.setDelivery("download");

        // Create ZIP archive
        zipMaker.writeZipArchive(fileHolder.asOutputStream());

        ajaxRequestData.setFormat("file");
        return new AJAXRequestResult(fileHolder, "file");
    }

}
