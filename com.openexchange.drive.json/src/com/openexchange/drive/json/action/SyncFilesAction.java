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

import java.util.List;
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
import com.openexchange.drive.json.pattern.JsonDirectoryPattern;
import com.openexchange.drive.json.pattern.JsonFilePattern;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link SyncFilesAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SyncFilesAction extends AbstractDriveAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SyncFilesAction.class);

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        /*
         * get request data
         */
        String path = requestData.getParameter("path");
        if (Strings.isEmpty(path)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("path");
        }
        Object data = requestData.getData();
        if (null == data || false == JSONObject.class.isInstance(data)) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        JSONObject dataObject = (JSONObject)data;
        /*
         * get original and current client file versions
         */
        List<FileVersion> originalFiles = null;
        List<FileVersion> clientFiles = null;
        try {
            originalFiles = JsonFileVersion.deserialize(dataObject.optJSONArray("originalVersions"));
            clientFiles = JsonFileVersion.deserialize(dataObject.optJSONArray("clientVersions"));
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
        /*
         * extract file- and directory exclusions if present
         */
        try {
            session.setDirectoryExclusions(JsonDirectoryPattern.deserialize(dataObject.optJSONArray("directoryExclusions")));
            session.setFileExclusions(JsonFilePattern.deserialize(dataObject.optJSONArray("fileExclusions")));
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
        /*
         * determine sync actions & return json result
         */
        try {
            DriveService driveService = Services.getService(DriveService.class, true);
            SyncResult<FileVersion> syncResult = driveService.syncFiles(session, path, originalFiles, clientFiles);
            if (null != session.isDiagnostics()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("diagnostics", syncResult.getDiagnostics());
                jsonObject.put("actions", JsonDriveAction.serializeActions(syncResult.getActionsForClient(), session.getLocale()));
                return new AJAXRequestResult(jsonObject, "json");
            }
            return new AJAXRequestResult(JsonDriveAction.serializeActions(syncResult.getActionsForClient(), session.getLocale()), "json");
        } catch (OXException e) {
            if ("DRV".equals(e.getPrefix())) {
                LOG.debug("Error performing syncFiles request", e);
            } else {
                LOG.warn("Error performing syncFiles request", e);
            }
            throw e;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
