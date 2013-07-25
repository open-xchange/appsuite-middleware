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

package com.openexchange.appstore.json.converter;

import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.appstore.AppException;
import com.openexchange.appstore.Application;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ApplicationResultConverter}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ApplicationResultConverter implements ResultConverter {

    @Override
    public String getInputFormat() {
        return "application";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        String action = requestData.getAction();
        if (action.equals("get") || action.equals("crawl")) {
            convertGet(result);
        } else if (action.equals("installed")) {
            convertInstalled(result);
        } else if (action.equals("install")) {
            convertInstall(result);
        }
    }

    private void convertInstall(AJAXRequestResult result) {
        final Object resultObject = result.getResultObject();
        if (resultObject instanceof Boolean) {
            final JSONArray json = new JSONArray(1).put(resultObject);
            result.setResultObject(json, getOutputFormat());
        } else if (resultObject instanceof Collection) {
            Collection<Boolean> values = (Collection<Boolean>) resultObject;
            final JSONArray json = new JSONArray(values.size());
            for (Boolean value : values) {
                json.put(value);
            }
            result.setResultObject(json, getOutputFormat());
        }
    }

    private void convertInstalled(AJAXRequestResult result) {
        final Object resultObject = result.getResultObject();
        if (resultObject instanceof Application) {
            Application app = (Application) resultObject;
            final JSONArray appJson = new JSONArray(1);
            appJson.put(app.getName());
            result.setResultObject(appJson, getOutputFormat());
        } else if (resultObject instanceof Collection) {
            Collection<Application> apps = (Collection<Application>) resultObject;
            final JSONArray appJson = new JSONArray(apps.size());
            for (Application app : apps) {
                appJson.put(app.getName());
            }
            result.setResultObject(appJson, getOutputFormat());
        }
    }

    private void convertGet(AJAXRequestResult result) throws OXException {
        final Object resultObject = result.getResultObject();
        if (resultObject instanceof Application) {
            try {
                final Application app = (Application) resultObject;
                JSONObject jsonCont = new JSONObject(app.getDescription());
                jsonCont.put("path", app.getRelativePath());
                final JSONArray appJson = new JSONArray(1);
                appJson.put(jsonCont);
                result.setResultObject(appJson, getOutputFormat());
            } catch (JSONException e) {
                throw AppException.jsonError();
            }
        } else if (resultObject instanceof Collection) {
            final Collection<Application> apps = (Collection<Application>) resultObject;
            final JSONArray appJson = new JSONArray(apps.size());
            for (final Application app : apps) {
                try {
                    JSONObject jsonCont = new JSONObject(app.getDescription());
                    jsonCont.put("path", app.getRelativePath());
                    if (app.getStatus() != null) {
                        jsonCont.put("state", app.getStatus().getKeyword());
                    }
                    appJson.put(jsonCont);
                } catch (JSONException e) {
                    throw AppException.jsonError();
                }
            }
            result.setResultObject(appJson, getOutputFormat());
        }
    }

}
