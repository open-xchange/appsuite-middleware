/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.json.action.account;

import static com.openexchange.folderstorage.CalendarFolderConverter.CALENDAR_CONFIG_FIELD;
import static com.openexchange.folderstorage.CalendarFolderConverter.CALENDAR_PROVIDER_FIELD;
import static com.openexchange.folderstorage.CalendarFolderConverter.EXTENDED_PROPERTIES_FIELD;
import static com.openexchange.java.Autoboxing.B;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.json.action.ChronosAction;
import com.openexchange.chronos.provider.CalendarFolderProperty;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderProperty;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ProbeAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
@RestrictedAction(type = RestrictedAction.Type.READ, module = ProbeAction.MODULE)
public class ProbeAction extends ChronosAction {

    @SuppressWarnings("hiding")
    public static final String MODULE = "calendar";

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
        CalendarSettings proposedSettings = services.getService(CalendarAccountService.class).probeAccountSettings(calendarAccess.getSession(), providerId, parsedSettings, calendarAccess);
        /*
         * return appropriate result
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
                jsonObject.put(CALENDAR_CONFIG_FIELD.getName(), CALENDAR_CONFIG_FIELD.write(property, null));
            }
            if (settings.containsExtendedProperties()) {
                ExtendedProperties clone = (ExtendedProperties) settings.getExtendedProperties().clone();
                settings.getUsedForSync().ifPresent((ufs) -> {
                    clone.add(CalendarFolderProperty.USED_FOR_SYNC(B(ufs.isUsedForSync()), ufs.isProtected()));
                });
                FolderProperty property = new FolderProperty(EXTENDED_PROPERTIES_FIELD.getName(), clone);
                jsonObject.put(EXTENDED_PROPERTIES_FIELD.getName(), EXTENDED_PROPERTIES_FIELD.write(property, null));
            }

        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
