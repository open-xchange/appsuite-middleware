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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.internal.Services;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link UnsubscribeAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UnsubscribeAction extends AbstractDriveAction {

    @Override
    protected boolean requiresRootFolderID() {
        return false;
    }

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        /*
         * get parameters
         */
        String token = requestData.getParameter("token");
        if (Strings.isEmpty(token)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("token");
        }
        String serviceID = requestData.getParameter("service");
        if (Strings.isEmpty(serviceID)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("service");
        }
        /*
         * get optional root folder identifiers
         */
        List<String> rootFolderIDs = null;
        Object data = requestData.getData();
        if (null != data) {
            if (false == JSONObject.class.isInstance(data)) {
                throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
            }
            JSONObject dataObject = (JSONObject) data;
            JSONArray rootArray = dataObject.optJSONArray("root");
            if (null != rootArray && 0 < rootArray.length()) {
                rootFolderIDs = new ArrayList<String>(rootArray.length());
                try {
                    for (int i = 0; i < rootArray.length(); i++) {
                        rootFolderIDs.add(rootArray.getString(i));
                    }
                } catch (JSONException e) {
                    throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        }
        /*
         * remove subscription(s)
         */
        DriveSubscriptionStore subscriptionStore = Services.getService(DriveSubscriptionStore.class, true);
        subscriptionStore.unsubscribe(session.getServerSession(), serviceID, token, rootFolderIDs);
        /*
         * return empty json object to indicate success
         */
        return new AJAXRequestResult(new JSONObject(0), "json");
    }

}
