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

package com.openexchange.drive.json.action;

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.json.internal.Services;
import com.openexchange.drive.json.json.JsonDirectoryAction;
import com.openexchange.drive.json.json.JsonDirectoryVersion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SyncFoldersAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SyncFoldersAction implements AJAXActionService {

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        /*
         * get request data
         */
        String rootFolderID = requestData.getParameter("root");
        if (Strings.isEmpty(rootFolderID)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("root");
        }
        Object data = requestData.getData();
        if (null == data || false == JSONObject.class.isInstance(data)) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        JSONObject dataObject = (JSONObject)data;
        /*
         * get original and current client folder versions
         */
        List<DirectoryVersion> originalVersions = null;
        List<DirectoryVersion> clientVersions = null;
        try {
            originalVersions = JsonDirectoryVersion.deserialize(dataObject.optJSONArray("originalVersions"));
            clientVersions = JsonDirectoryVersion.deserialize(dataObject.optJSONArray("clientVersions"));
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
        /*
         * determine sync actions
         */
        DriveService driveService = Services.getService(DriveService.class, true);
        List<DriveAction<DirectoryVersion>> actions = driveService.syncFolders(session, rootFolderID, originalVersions, clientVersions);
        /*
         * return json result
         */
        try {
            return new AJAXRequestResult(JsonDirectoryAction.serialize(actions), "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
