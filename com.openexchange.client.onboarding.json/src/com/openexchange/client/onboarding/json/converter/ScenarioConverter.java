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

package com.openexchange.client.onboarding.json.converter;

import java.util.Arrays;
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
import com.openexchange.client.onboarding.ClientDevice;
import com.openexchange.client.onboarding.DefaultOnboardingRequest;
import com.openexchange.client.onboarding.DeviceAwareScenario;
import com.openexchange.client.onboarding.FontAwesomeIcon;
import com.openexchange.client.onboarding.Icon;
import com.openexchange.client.onboarding.IconType;
import com.openexchange.client.onboarding.OnboardingAction;
import com.openexchange.client.onboarding.ResultObject;
import com.openexchange.client.onboarding.Scenario;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
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
                OnboardingService onboardingService = services.getOptionalService(OnboardingService.class);
                if (null == onboardingService) {
                    throw ServiceExceptionCode.absentService(OnboardingService.class);
                }
                result.setResultObject(toJson(scenario, requestData, session, onboardingService), "json");
            } else {
                if (!(resultObject instanceof Collection)) {
                    throw AjaxExceptionCodes.UNEXPECTED_RESULT.create(DeviceAwareScenario.class.getSimpleName(), null == resultObject ? "null" : resultObject.getClass().getSimpleName());
                }

                OnboardingService onboardingService = services.getOptionalService(OnboardingService.class);
                if (null == onboardingService) {
                    throw ServiceExceptionCode.absentService(OnboardingService.class);
                }

                @SuppressWarnings("unchecked") Collection<DeviceAwareScenario> scenarios = (Collection<DeviceAwareScenario>) resultObject;
                JSONArray jScenarios = new JSONArray(scenarios.size());
                for (DeviceAwareScenario scenario : scenarios) {
                    jScenarios.put(toJson(scenario, requestData, session, onboardingService));
                }
                result.setResultObject(jScenarios, "json");
            }
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private DefaultOnboardingRequest createOnboardingRequest(DeviceAwareScenario scenario, AJAXRequestData requestData, OnboardingAction action) {
        return new DefaultOnboardingRequest(scenario, action, ClientDevice.IMPLIES_ALL, scenario.getDevice(), requestData.getHostData(), null);
    }

    private JSONObject toJson(DeviceAwareScenario scenario, AJAXRequestData requestData, ServerSession session, OnboardingService onboardingService) throws OXException, JSONException {
        JSONObject jScenario = new JSONObject(8);
        jScenario.put("id", scenario.getId());
        jScenario.put("enabled", scenario.isEnabled(session));
        put2Json("name", scenario.getDisplayName(session), jScenario);
        put2Json("description", scenario.getDescription(session), jScenario);
        put2Json("icon", scenario.getIcon(session), jScenario);
        {
            List<OnboardingAction> actions = scenario.getActions();
            JSONArray jActions = new JSONArray(actions.size());
            for (OnboardingAction action : actions) {
                switch (action) {
                    case DISPLAY:
                        {
                            ResultObject result = onboardingService.execute(createOnboardingRequest(scenario, requestData, action), session);
                            JSONObject jAction = new JSONObject(2);
                            jAction.put("id", action.getId());
                            jAction.put("data", result.getObject());
                            jActions.put(jAction);
                        }
                        break;
                    case LINK:
                        {
                            ResultObject result = onboardingService.execute(createOnboardingRequest(scenario, requestData, action), session);
                            JSONObject jAction = new JSONObject(2);
                            jAction.put("id", action.getId());
                            jAction.put("link", result.getObject());
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
            if (null != alternatives) {
                JSONArray jAlternatives = new JSONArray(alternatives.size());
                for (Scenario alternative : alternatives) {
                    DeviceAwareScenario deviceAwareScenario = onboardingService.getScenario(alternative.getId(), ClientDevice.IMPLIES_ALL, scenario.getDevice(), session);
                    JSONObject jAlternative = toJson(deviceAwareScenario, requestData, session, onboardingService);
                    jAlternatives.put(jAlternative);
                }
                jScenario.put("alternatives", jAlternatives);
            }
        }

        return jScenario;
    }

    private void put2Json(String key, Object value, JSONObject jObject) throws JSONException {
        if (null == value) {
            jObject.put(key, JSONObject.NULL);
        } else {
            if (value instanceof Icon) {
                Icon icon = (Icon) value;
                if (IconType.FONT_AWESOME.equals(icon.getType())) {
                    FontAwesomeIcon fontAwesomeIcon = (FontAwesomeIcon) icon;
                    jObject.put(key, new JSONArray(Arrays.asList(fontAwesomeIcon.getNames())));
                } else {
                    byte[] binaryData = icon.getData();
                    if (binaryData == null || binaryData.length == 0) {
                        jObject.put(key, JSONObject.NULL);
                    } else {
                        jObject.put(key, Charsets.toAsciiString(Base64.encodeBase64(binaryData, false)));
                    }
                }
            } else {
                jObject.put(key, value);
            }
        }
    }

}
