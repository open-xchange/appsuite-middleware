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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.onboarding.json.converter;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.onboarding.Device;
import com.openexchange.onboarding.Icon;
import com.openexchange.onboarding.Module;
import com.openexchange.onboarding.OnboardingConfiguration;
import com.openexchange.onboarding.OnboardingSelection;
import com.openexchange.onboarding.Platform;
import com.openexchange.onboarding.service.OnboardingView;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ConfigurationViewConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ConfigurationViewConverter implements ResultConverter {

    /**
     * Initializes a new {@link ConfigurationViewConverter}.
     */
    public ConfigurationViewConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "onboardingView";
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
        Object resultObject = result.getResultObject();
        if (!(resultObject instanceof OnboardingView)) {
            throw AjaxExceptionCodes.UNEXPECTED_RESULT.create(OnboardingSelection.class.getSimpleName(), null == resultObject ? "null" : resultObject.getClass().getSimpleName());
        }

        try {
            OnboardingView view = (OnboardingView) resultObject;
            result.setResultObject(toJson(view, session), "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject toJson(OnboardingView view, ServerSession session) throws OXException, JSONException {
        JSONObject jView = new JSONObject(6);

        // Platforms
        {
            JSONObject jPlatforms = new JSONObject(4);
            for (Platform platform : view.getPlatforms()) {
                JSONObject jPlatform = new JSONObject(4);
                jPlatform.put("id", platform.getId());
                jPlatform.put("enabled", platform.isEnabled(session));
                put2Json("displayName", platform.getDisplayName(session), jPlatform);
                put2Json("description", platform.getDescription(session), jPlatform);
                put2Json("icon", platform.getIcon(session), jPlatform);
                jPlatforms.put(platform.getId(), jPlatform);
            }
            jView.put("platforms", jPlatforms);
        }

        // Devices
        {
            JSONObject jDevices = new JSONObject(8);
            for (Device device : view.getDevices()) {
                JSONObject jDevice = new JSONObject(4);
                jDevice.put("id", device.getId());
                jDevice.put("enabled", device.isEnabled(session));
                put2Json("displayName", device.getDisplayName(session), jDevice);
                put2Json("description", device.getDescription(session), jDevice);
                put2Json("icon", device.getIcon(session), jDevice);
                jDevices.put(device.getId(), jDevice);
            }
            jView.put("devices", jDevices);
        }

        // Modules
        {
            JSONObject jModules = new JSONObject(8);
            for (Module module : view.getModules()) {
                JSONObject jModule = new JSONObject(4);
                jModule.put("id", module.getId());
                jModule.put("enabled", module.isEnabled(session));
                put2Json("displayName", module.getDisplayName(session), jModule);
                put2Json("description", module.getDescription(session), jModule);
                put2Json("icon", module.getIcon(session), jModule);
                jModules.put(module.getId(), jModule);
            }
            jView.put("modules", jModules);
        }

        // Services
        {
            JSONObject jServices = new JSONObject(32);
            for (OnboardingSelection selection : view.getSelections()) {
                JSONObject jSelections;
                {
                    String serviceId = selection.getEntityPath().getService().getId();
                    JSONObject jService = jServices.optJSONObject(serviceId);
                    if (null == jService) {
                        jService = new JSONObject(4);
                        OnboardingConfiguration service = selection.getEntityPath().getService();
                        jService.put("id", serviceId);
                        jService.put("enabled", service.isEnabled(session));
                        put2Json("displayName", service.getDisplayName(session), jService);
                        put2Json("description", service.getDescription(session), jService);
                        put2Json("icon", service.getIcon(session), jService);
                        jServices.put(serviceId, jService);
                    }

                    jSelections = jService.optJSONObject("selections");
                    if (null == jSelections) {
                        jSelections = new JSONObject(8);
                        jService.put("selections", jSelections);
                    }
                }

                JSONObject jSelection = new JSONObject(6);
                jSelection.put("id", selection.getCompositeId());
                jSelection.put("enabled", selection.isEnabled(session));
                put2Json("displayName", selection.getDisplayName(session), jSelection);
                put2Json("description", selection.getDescription(session), jSelection);
                put2Json("icon", selection.getIcon(session), jSelection);
                put2Json("type", selection.getType().getId(), jSelection);
                jSelections.put(selection.getCompositeId(), jSelection);
            }
            jView.put("services", jServices);
        }

        return jView;
    }

    private void put2Json(String key, Object value, JSONObject jObject) throws JSONException {
        if (null == value) {
            jObject.put(key, JSONObject.NULL);
        } else {
            if (value instanceof Icon) {
                jObject.put(key, Charsets.toAsciiString(Base64.encodeBase64(((Icon) value).getData(), false)));
            } else {
                jObject.put(key, value);
            }
        }
    }

}
