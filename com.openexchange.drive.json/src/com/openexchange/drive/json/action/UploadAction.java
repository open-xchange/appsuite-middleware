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

package com.openexchange.drive.json.action;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.SyncResult;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.internal.Services;
import com.openexchange.drive.json.json.JsonDriveAction;
import com.openexchange.drive.json.json.JsonFileVersion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link UploadAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UploadAction extends AbstractDriveAction {

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        /*
         * no limits for upload
         */
        enableUnlimitedBodySize(requestData);
        /*
         * get parameters
         */
        String path = requestData.getParameter("path");
        if (Strings.isEmpty(path)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("path");
        }
        String newName = requestData.getParameter("newName");
        if (Strings.isEmpty(newName)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("newName");
        }
        String newChecksum = requestData.getParameter("newChecksum");
        if (Strings.isEmpty(newChecksum)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("newChecksum");
        }
        String checksum = null;
        if (requestData.containsParameter("checksum")) {
            checksum = requestData.getParameter("checksum");
        }
        String name = null;
        if (requestData.containsParameter("name")) {
            name = requestData.getParameter("name");
        }
        long totalLength = -1L;
        if (requestData.containsParameter("totalLength")) {
            totalLength = requestData.getParameter("totalLength", Long.class).longValue();
        }
        long offset = 0L;
        if (requestData.containsParameter("offset")) {
            offset = requestData.getParameter("offset", Long.class).longValue();
        }
        String contentType = null;
        if (requestData.containsParameter("contentType")) {
            contentType = requestData.getParameter("contentType");
        }
        if (Strings.isEmpty(contentType)) {
            contentType = MimeType2ExtMap.getContentType(newName); // as fallback
        }
        Date created = null;
        if (requestData.containsParameter("created")) {
            Long value = requestData.getParameter("created", Long.class);
            if (null != value) {
                created = new Date(value.longValue());
            }
        }
        Date modified = null;
        if (requestData.containsParameter("modified")) {
            Long value = requestData.getParameter("modified", Long.class);
            if (null != value) {
                modified = new Date(value.longValue());
            }
        }
        /*
         * construct referenced file versions from parameters
         */
        FileVersion newFile = new JsonFileVersion(newChecksum, newName);
        FileVersion originalFile = null == checksum ? null : new JsonFileVersion(checksum, null == name ? newName : name);
        /*
         * hand over upload stream
         */
        DriveService driveService = Services.getService(DriveService.class, true);
        SyncResult<FileVersion> syncResult = null;
        InputStream uploadStream = null;
        try {
            uploadStream = requestData.getUploadStream();
            if (null == uploadStream) {
                throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
            }
            syncResult = driveService.upload(session, path, uploadStream, originalFile, newFile, contentType, offset, totalLength, created, modified);
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (OXException e) {
            if ("DRV-0038".equals(e.getErrorCode())) {
                // The connected client closed the connection unexpectedly
                org.slf4j.LoggerFactory.getLogger(UploadAction.class).debug("", e);
                return AJAXRequestResult.EMPTY_REQUEST_RESULT;
            }
            throw e;
        } finally {
            Streams.close(uploadStream);
        }
        /*
         * return json result
         */
        try {
            if (null != session.isDiagnostics()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("diagnostics", syncResult.getDiagnostics());
                jsonObject.put("actions", JsonDriveAction.serializeActions(syncResult.getActionsForClient(), session.getLocale()));
                return new AJAXRequestResult(jsonObject, "json");
            }
            return new AJAXRequestResult(JsonDriveAction.serializeActions(syncResult.getActionsForClient(), session.getLocale()), "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
