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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.DriveQuota;
import com.openexchange.drive.DriveSettings;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Quota;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link SettingsAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SettingsAction extends AbstractDriveAction {

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        /*
         * get request data
         */
        String rootFolderID = requestData.getParameter("root");
        if (Strings.isEmpty(rootFolderID)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("root");
        }
        /*
         * get settings
         */
        DriveSettings settings = getDriveService().getSettings(session);
        /*
         * return json result
         */
        JSONObject jsonObject = new JSONObject();
        if (null != settings) {
            try {
                jsonObject.put("helpLink", settings.getHelpLink());
                DriveQuota driveQuota = settings.getQuota();
                if (null != driveQuota) {
                    jsonObject.put("quotaManageLink", driveQuota.getManageLink());
                    if (null != driveQuota.getQuota()) {
                        JSONArray jsonArray = new JSONArray(2);
                        for (Quota q : driveQuota.getQuota()) {
                            if (Quota.UNLIMITED != q.getLimit()) {
                                JSONObject jsonQuota = new JSONObject();
                                jsonQuota.put("limit", q.getLimit());
                                jsonQuota.put("use", q.getUsage());
                                jsonQuota.put("type", String.valueOf(q.getType()).toLowerCase());
                                jsonArray.put(jsonQuota);
                            }
                        }
                        jsonObject.put("quota", jsonArray);
                    }
                }
                jsonObject.put("serverVersion", settings.getServerVersion());
                jsonObject.put("supportedApiVersion", settings.getSupportedApiVersion());
                jsonObject.put("minApiVersion", settings.getMinApiVersion());
                jsonObject.putOpt("minUploadChunk", settings.getMinUploadChunk());
                jsonObject.put("localizedFolderNames", settings.getLocalizedFolders());
                jsonObject.put("capabilities", settings.getCapabilities());
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        }
        return new AJAXRequestResult(jsonObject, "json");
    }

}
