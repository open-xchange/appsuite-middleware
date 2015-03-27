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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.drive.json.action.share;

import java.util.Date;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.internal.Services;
import com.openexchange.exception.OXException;
import com.openexchange.share.json.actions.ShareJSONParser;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link UpdateLinkAction}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class UpdateLinkAction extends AbstractDriveShareAction {

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        try {
            Date clientTimestamp = new Date(requestData.getParameter("timestamp", Long.class).longValue());

            JSONObject json = (JSONObject) requestData.requireData();

            String token = json.getString("token");

            Date expiry = null;
            if (json.has("expiry_date")) {
                String exp = json.getString("expiry_date");
                try {
                    expiry = exp == null ? null : new Date(ShareJSONParser.removeTimeZoneOffset(Long.valueOf(exp), getTimeZone(requestData, session.getServerSession())));
                } catch (NumberFormatException e) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("expiry_date", expiry, e);
                }
            }

            Map<String, Object> meta = null;
            if (json.has("meta")) {
                meta = (Map<String, Object>) JSONCoercion.coerceToNative(json.getJSONObject("meta"));
            }

            String password = json.optString("password", null);
            int bits = json.optInt("bits", -1);

            DriveService driveService = Services.getService(DriveService.class, true);
            driveService.updateShare(session, clientTimestamp, token, expiry, meta, password, bits);

            /*
             * return empty result in case of success
             */
            AJAXRequestResult result = new AJAXRequestResult(new JSONObject(), "json");
            result.setTimestamp(new Date());
            return result;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

}
