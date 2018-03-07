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

package com.openexchange.chronos.json.action.account;

import static com.openexchange.folderstorage.CalendarFolderConverter.CALENDAR_CONFIG_FIELD;
import static com.openexchange.folderstorage.CalendarFolderConverter.CALENDAR_PROVIDER_FIELD;
import static com.openexchange.folderstorage.CalendarFolderConverter.EXTENDED_PROPERTIES_FIELD;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.json.action.ChronosAction;
import com.openexchange.chronos.json.oauth.ChronosOAuthScope;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderProperty;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ProbeAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
@OAuthAction(ChronosOAuthScope.OAUTH_READ_SCOPE)
public class ProbeAction extends ChronosAction {

    /**
     * Initializes a new {@link ProbeAction}.
     *
     * @param services A service lookup reference
     */
    protected ProbeAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {
        /*
         * extract targeted calendar provider
         */
        JSONObject jsonObject = extractJsonBody(requestData);
        String providerId = jsonObject.optString(CALENDAR_PROVIDER_FIELD.getName());
        if (Strings.isEmpty(providerId)) {
            throw AjaxExceptionCodes.MISSING_FIELD.create(CALENDAR_PROVIDER_FIELD.getName());
        }
        /*
         * parse & probe client-supplied calendar settings
         */
        CalendarSettings parsedSettings = parseSettings(jsonObject);
        CalendarSettings proposedSettings = services.getService(CalendarAccountService.class).probeAccountSettings(
            calendarAccess.getSession(), providerId, parsedSettings, calendarAccess);
        /*
         *  return appropriate result
         */
        JSONObject resultObject = new JSONObject();
        writeSettings(proposedSettings, resultObject);
        resultObject.putSafe(CALENDAR_PROVIDER_FIELD.getName(), providerId);
        return new AJAXRequestResult(resultObject, "json");
    }

    private static CalendarSettings parseSettings(JSONObject jsonObject) throws OXException {
        CalendarSettings settings = new CalendarSettings();
        if (jsonObject.has("title")) {
            settings.setName(jsonObject.optString("title", null));
        }
        if (jsonObject.has(CALENDAR_CONFIG_FIELD.getName())) {
            try {
                FolderProperty property = CALENDAR_CONFIG_FIELD.parse(jsonObject.get(CALENDAR_CONFIG_FIELD.getName()));
                settings.setConfig(null == property ? null : (JSONObject) property.getValue());
            } catch (ClassCastException | JSONException e) {
                throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create(e);
            }
        }
        if (jsonObject.has(EXTENDED_PROPERTIES_FIELD.getName())) {
            try {
                FolderProperty property = EXTENDED_PROPERTIES_FIELD.parse(jsonObject.get(EXTENDED_PROPERTIES_FIELD.getName()));
                settings.setExtendedProperties(null == property ? null : (ExtendedProperties) property.getValue());
            } catch (ClassCastException | JSONException e) {
                throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create(e);
            }
        }
        return settings;
    }

    private static void writeSettings(CalendarSettings settings, JSONObject jsonObject) throws OXException {
        try {
            if (settings.containsName()) {
                jsonObject.put("title", settings.getName());
            }
            if (settings.containsConfig()) {
                FolderProperty property = new FolderProperty(CALENDAR_CONFIG_FIELD.getName(), settings.getConfig());
                jsonObject.put(CALENDAR_CONFIG_FIELD.getName(), CALENDAR_CONFIG_FIELD.write(property));
            }
            if (settings.containsExtendedProperties()) {
                FolderProperty property = new FolderProperty(EXTENDED_PROPERTIES_FIELD.getName(), settings.getExtendedProperties());
                jsonObject.put(EXTENDED_PROPERTIES_FIELD.getName(), EXTENDED_PROPERTIES_FIELD.write(property));
            }
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
