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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.config.ConfigurationService;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.json.LongPollingListener;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.internal.ListenerRegistrar;
import com.openexchange.drive.json.internal.Services;
import com.openexchange.drive.json.json.JsonDriveAction;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ListenAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ListenAction extends AbstractDriveAction {

    @Override
    protected boolean requiresRootFolderID() {
        return false;
    }

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        /*
         * get request data
         */
        long timeout;
        String timeoutValue = requestData.getParameter("timeout");
        if (false == Strings.isEmpty(timeoutValue)) {
            try {
                timeout = Long.valueOf(timeoutValue).longValue();
            } catch (NumberFormatException e) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, "timeout", timeoutValue);
            }
        } else {
            timeout = getDefaultTimeout();
        }
        /*
         * get optional root folder identifiers, falling back to the session's default root folder id
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
        if (null == rootFolderIDs) {
            if (Strings.isEmpty(session.getRootFolderID())) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("root");
            }
            rootFolderIDs = Collections.singletonList(session.getRootFolderID());
        }
        /*
         * get or create a polling listener for this session and await result
         */
        AJAXRequestResult result = null;
        try {
            LongPollingListener listener = ListenerRegistrar.getInstance().getOrCreate(session, rootFolderIDs);
            result = listener.await(timeout);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (null != cause && OXException.class.isInstance(e.getCause())) {
                throw (OXException)cause;
            }
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        /*
         * create and return result if available
         */
        if (null != result) {
            return result;
        } else {
            /*
             * use empty actions as fallback
             */
            List<DriveAction<? extends DriveVersion>> actions = new ArrayList<DriveAction<? extends DriveVersion>>(0);
            try {
                return new AJAXRequestResult(JsonDriveAction.serialize(actions, session.getLocale()), "json");
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        }
    }

    private static long getDefaultTimeout() {
        ConfigurationService configurationService = Services.getService(ConfigurationService.class);
        if (null != configurationService) {
            String value = configurationService.getProperty("com.openexchange.drive.listenTimeout");
            if (false == Strings.isEmpty(value)) {
                try {
                    return Long.valueOf(value).longValue();
                } catch (NumberFormatException e) {
                    org.slf4j.LoggerFactory.getLogger(ListenAction.class).error(
                        "Invalid configuration value for \"com.openexchange.drive.listenTimeout\"", e);
                }
            }
        }
        return 90 * 1000;
    }

}
