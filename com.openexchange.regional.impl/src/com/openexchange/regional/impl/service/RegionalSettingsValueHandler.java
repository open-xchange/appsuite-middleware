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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.regional.impl.service;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.regional.RegionalSettingField;
import com.openexchange.regional.RegionalSettings;
import com.openexchange.regional.RegionalSettingsService;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * {@link RegionalSettingsValueHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class RegionalSettingsValueHandler implements IValueHandler {

    private final RegionalSettingsService settingsService;

    /**
     * Initializes a new {@link RegionalSettingsValueHandler}.
     * 
     * @param settingsService The underlying regional settings service
     */
    public RegionalSettingsValueHandler(RegionalSettingsService settingsService) {
        super();
        this.settingsService = settingsService;
    }

    @Override
    public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
        RegionalSettings regionalSettings = settingsService.get(session.getContextId(), session.getUserId());
        setting.setSingleValue(serialize(regionalSettings, RegionalSettingField.values()));
    }

    @Override
    public void writeValue(Session session, Context ctx, User user, Setting setting) throws OXException {
        JSONObject settings = extractBody(setting.getSingleValue());
        if (settings.isEmpty()) {
            settingsService.delete(ctx.getContextId(), user.getId());
        } else {
            settingsService.save(ctx.getContextId(), user.getId(), deserialize(settings, RegionalSettingField.values()), user.getLocale());
        }
    }

    @Override
    public boolean isAvailable(UserConfiguration userConfig) {
        return true;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public int getId() {
        return -1;
    }

    /**
     * Extracts the body from the specified single value object
     * 
     * @param singleValue The single value object
     * @return The JSONObject with the extracted values or an empty JSONObject
     *         if the specified value does not denote a valid value or denotes
     *         an empty JSONObject.
     */
    private static JSONObject extractBody(Object singleValue) {
        try {
            if (false == (singleValue instanceof JSONObject)) {
                return new JSONObject();
            }
            JSONObject settings = (JSONObject) singleValue;
            if (false == settings.hasAndNotNull("data")) {
                return settings;
            }
            Object bodyCandidate = settings.get("data");
            if (bodyCandidate instanceof String) {
                return new JSONObject((String) bodyCandidate);
            }
            return new JSONObject();
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    /**
     * De-serializes the specified {@link JSONObject} to a {@link RegionalSettings} object
     *
     * @param settings The {@link JSONObject} with the settings
     * @param values The set fields
     * @return The {@link RegionalSettings} object
     */
    private static RegionalSettings deserialize(JSONObject settings, RegionalSettingField[] values) {
        RegionalSettingsImpl.Builder builder = RegionalSettingsImpl.newBuilder();
        for (RegionalSettingField field : values) {
            SerialiserUtil.setField(builder, field, settings.opt(field.getName()));
        }
        return builder.build();
    }

    /**
     * Serialises the specified {@link RegionalSettings} object to a {@link JSONObject}
     *
     * @param regionalSettings The settings to serialise
     * @param values The set fields
     * @return The serialised {@link JSONObject}
     */
    private static JSONObject serialize(RegionalSettings regionalSettings, RegionalSettingField[] values) throws OXException {
        if (regionalSettings == null) {
            return null;
        }
        try {
            JSONObject j = new JSONObject();
            for (RegionalSettingField field : values) {
                Object value = SerialiserUtil.getField(regionalSettings, field);
                if (null != value) {
                    j.put(field.getName(), value);
                }
            }
            return j.isEmpty() ? null : j;
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
    }

}
