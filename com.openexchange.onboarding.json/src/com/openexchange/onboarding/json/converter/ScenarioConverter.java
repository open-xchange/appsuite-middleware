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

import java.util.Collection;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.onboarding.DefaultOnboardingRequest;
import com.openexchange.onboarding.DeviceAwareScenario;
import com.openexchange.onboarding.DisplayResult;
import com.openexchange.onboarding.Icon;
import com.openexchange.onboarding.OnboardingAction;
import com.openexchange.onboarding.OnboardingSelection;
import com.openexchange.onboarding.Result;
import com.openexchange.onboarding.Scenario;
import com.openexchange.onboarding.StringResult;
import com.openexchange.onboarding.service.OnboardingService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ScenarioConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ScenarioConverter implements ResultConverter {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ScenarioConverter}.
     */
    public ScenarioConverter(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getInputFormat() {
        return "onboardingScenario";
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
        try {
            Object resultObject = result.getResultObject();
            if (resultObject instanceof DeviceAwareScenario) {
                DeviceAwareScenario scenario = (DeviceAwareScenario) resultObject;
                result.setResultObject(toJson(scenario, requestData, session), "json");
            } else {
                if ((resultObject instanceof Collection)) {
                    throw AjaxExceptionCodes.UNEXPECTED_RESULT.create(OnboardingSelection.class.getSimpleName(), null == resultObject ? "null" : resultObject.getClass().getSimpleName());
                }

                Collection<DeviceAwareScenario> scenarios = (Collection<DeviceAwareScenario>) resultObject;
                JSONArray jScenarios = new JSONArray(scenarios.size());
                for (DeviceAwareScenario scenario : scenarios) {
                    jScenarios.put(toJson(scenario, requestData, session));
                }
                result.setResultObject(jScenarios, "json");
            }
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject toJson(DeviceAwareScenario scenario, AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        OnboardingService onboardingService = services.getOptionalService(OnboardingService.class);
        if (null == onboardingService) {
            throw ServiceExceptionCode.absentService(OnboardingService.class);
        }

        JSONObject jScenario = new JSONObject(8);
        jScenario.put("id", scenario.getId());
        jScenario.put("enabled", scenario.isEnabled(session));
        put2Json("displayName", scenario.getDisplayName(session), jScenario);
        put2Json("description", scenario.getDescription(session), jScenario);
        put2Json("icon", scenario.getIcon(session), jScenario);
        {
            List<OnboardingAction> actions = scenario.getActions();
            JSONArray jActions = new JSONArray(actions.size());
            for (OnboardingAction action : actions) {
                switch (action) {
                    case DISPLAY:
                        {
                            Result result = onboardingService.execute(new DefaultOnboardingRequest(scenario, action, scenario.getDevice(), requestData.getHostData(), null), session);
                            JSONObject jAction = new JSONObject(2);
                            jAction.put("id", action.getId());
                            jAction.put("data", new JSONObject(((DisplayResult) result).getConfiguration()));
                            jActions.put(jAction);
                        }
                        break;
                    case LINK:
                        {
                            Result result = onboardingService.execute(new DefaultOnboardingRequest(scenario, action, scenario.getDevice(), requestData.getHostData(), null), session);
                            JSONObject jAction = new JSONObject(2);
                            jAction.put("id", action.getId());
                            jAction.put("link", ((StringResult) result).getResult());
                            jActions.put(jAction);
                        }
                        break;
                    case DOWNLOAD:
                        jActions.put(new JSONObject(1).put("id", action.getId()));
                        break;
                    case EMAIL:
                        jActions.put(new JSONObject(1).put("id", action.getId()));
                        break;
                    case SMS:
                        jActions.put(new JSONObject(1).put("id", action.getId()));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknwon action: " + action.getId());
                }
            }
            jScenario.put("actions", jActions);
        }
        {
            List<Scenario> alternatives = scenario.getAlternatives(session);
            JSONArray jAlternatives = new JSONArray(alternatives.size());
            for (Scenario alternative : alternatives) {
                JSONObject jAlternative = new JSONObject(6);
                jScenario.put("id", alternative.getId());
                jScenario.put("enabled", alternative.isEnabled(session));
                put2Json("displayName", alternative.getDisplayName(session), jScenario);
                put2Json("description", alternative.getDescription(session), jScenario);
                put2Json("icon", alternative.getIcon(session), jScenario);
                jAlternatives.put(jAlternative);
            }
            jScenario.put("alternatives", jAlternatives);
        }

        return jScenario;
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
