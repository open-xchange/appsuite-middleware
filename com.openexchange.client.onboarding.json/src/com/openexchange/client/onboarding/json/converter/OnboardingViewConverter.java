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

package com.openexchange.client.onboarding.json.converter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.client.onboarding.CompositeId;
import com.openexchange.client.onboarding.DefaultOnboardingRequest;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.DeviceAwareScenario;
import com.openexchange.client.onboarding.FontAwesomeIcon;
import com.openexchange.client.onboarding.Icon;
import com.openexchange.client.onboarding.IconType;
import com.openexchange.client.onboarding.OnboardingAction;
import com.openexchange.client.onboarding.OnboardingSMSConstants;
import com.openexchange.client.onboarding.Platform;
import com.openexchange.client.onboarding.ResultObject;
import com.openexchange.client.onboarding.Scenario;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.client.onboarding.service.OnboardingView;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link OnboardingViewConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingViewConverter implements ResultConverter {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OnboardingViewConverter.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link OnboardingViewConverter}.
     */
    public OnboardingViewConverter(ServiceLookup services) {
        super();
        this.services = services;
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
            throw AjaxExceptionCodes.UNEXPECTED_RESULT.create(OnboardingView.class.getSimpleName(), null == resultObject ? "null" : resultObject.getClass().getSimpleName());
        }

        try {
            OnboardingView view = (OnboardingView) resultObject;
            result.setResultObject(toJson(view, requestData, session), "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject toJson(OnboardingView view, AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        OnboardingService onboardingService = services.getOptionalService(OnboardingService.class);
        if (null == onboardingService) {
            throw ServiceExceptionCode.absentService(OnboardingService.class);
        }

        JSONObject jView = new JSONObject(6);

        // Platforms
        {
            JSONArray jPlatforms = new JSONArray(4);
            for (Platform platform : view.getPlatforms()) {
                JSONObject jPlatform = new JSONObject(4);
                jPlatform.put("id", platform.getId());
                jPlatform.put("enabled", platform.isEnabled(session));
                put2Json("name", platform.getDisplayName(session), jPlatform);
                put2Json("description", platform.getDescription(session), jPlatform);
                put2Json("icon", platform.getIcon(session), jPlatform);
                jPlatforms.put(jPlatform);
            }
            jView.put("platforms", jPlatforms);
        }

        // Devices, scenario-action-association, and actions
        {
            Map<Device, List<CompositeId>> devices = view.getDevices();

            ActionCollector actionCollector = new ActionCollector();
            Set<String> scenarioIds = new LinkedHashSet<String>(16, 0.9F);
            JSONArray jScenario2Action = new JSONArray(16);
            JSONArray jScenarios = new JSONArray(16);
            JSONArray jDevices = new JSONArray(devices.size());

            // Iterate available (device -> scenarios) mapping and fill collections accordingly
            for (Entry<Device, List<CompositeId>> deviceEntry : devices.entrySet()) {
                Device device = deviceEntry.getKey();
                List<CompositeId> compositeIds = deviceEntry.getValue();

                JSONObject jDevice = new JSONObject(8);
                jDevice.put("id", device.getId());
                jDevice.put("enabled", device.isEnabled(session));
                jDevice.put("platform", device.getPlatform().getId());
                put2Json("name", device.getDisplayName(session), jDevice);
                put2Json("description", device.getDescription(session), jDevice);
                put2Json("icon", device.getIcon(session), jDevice);

                if (null == compositeIds || compositeIds.isEmpty()) {
                    jDevice.put("scenarios", new JSONArray(0));
                } else {
                    JSONArray jCompositeIds = new JSONArray(compositeIds.size());
                    for (CompositeId compositeId : compositeIds) {
                        // Check for an appropriate scenario-action-association entry
                        Set<String> missingCapabilities = new LinkedHashSet<String>(4);
                        JSONObject jScenario2ActionEntry = createScenario2ActionEntry(compositeId, actionCollector, requestData, onboardingService, missingCapabilities, session);
                        if (null != jScenario2ActionEntry) {
                            // Add to device's list of supported scenarios
                            jCompositeIds.put(compositeId.toString());

                            // Remember associated scenario
                            if (scenarioIds.add(compositeId.getScenarioId())) {
                                Scenario scenario = onboardingService.getScenario(compositeId.getScenarioId(), session);
                                boolean available = onboardingService.isAvailableFor(compositeId.getScenarioId(), session);
                                JSONObject jScenario = toJson(scenario, available, session);

                                // Add missing capabilities for currently processed scenario (if any)
                                if (false == missingCapabilities.isEmpty()) {
                                    JSONArray jMissingCaps = new JSONArray(missingCapabilities.size());
                                    for (String missingCapability : missingCapabilities) {
                                        jMissingCaps.put(missingCapability);
                                    }
                                    jScenario.put("missing_capabilities", jMissingCaps);
                                }

                                // Add to jScenarios array
                                jScenarios.put(jScenario);
                            }

                            // Add to scenario-to-action mapping
                            jScenario2Action.put(jScenario2ActionEntry);
                        }
                    }
                    jDevice.put("scenarios", jCompositeIds);
                }

                jDevices.put(jDevice);
            }

            jView.put("devices", jDevices);
            jView.put("scenarios", jScenarios);
            jView.put("actions", actionCollector.jActions);
            jView.put("matching", jScenario2Action);
        }

        return jView;
    }

    private JSONObject createScenario2ActionEntry(CompositeId compositeId, ActionCollector actionCollector, AJAXRequestData requestData, OnboardingService onboardingService, Collection<String> missingCapabilities, Session session) throws OXException, JSONException {
        DeviceAwareScenario scenario = onboardingService.getScenario(compositeId.getScenarioId(), compositeId.getDevice(), session);

        JSONArray jActionIds = new JSONArray(10);
        collectActions(scenario, compositeId.getScenarioId(), jActionIds, actionCollector, requestData, onboardingService, session);
        if (jActionIds.length() > 0) {
            JSONObject jScenario2ActionEntry = new JSONObject(6);
            jScenario2ActionEntry.put("id", compositeId.toString());

            boolean enabled = scenario.isEnabled(session);
            jScenario2ActionEntry.put("enabled", enabled);
            if (!enabled) {
                Collection<String> missingCapsFromScenario = scenario.getMissingCapabilities(session);
                if (null != missingCapsFromScenario && !missingCapsFromScenario.isEmpty()) {
                    missingCapabilities.addAll(missingCapsFromScenario);
                    // TODO: Remove adding "missing_capabilities" to jScenario2ActionEntry instance once work-around for bug #46968 becomes obsolete
                    // ---
                    JSONArray jMissingCaps = new JSONArray(missingCapsFromScenario.size());
                    for (String missingCapability : missingCapsFromScenario) {
                        jMissingCaps.put(missingCapability);
                    }
                    jScenario2ActionEntry.put("missing_capabilities", jMissingCaps);
                    // ---
                }
            }

            jScenario2ActionEntry.put("actions", jActionIds);
            return jScenario2ActionEntry;
        }

        return null;
    }

    private void collectActions(DeviceAwareScenario scenario, String scenarioAppendix, JSONArray jActionIds, ActionCollector actionCollector, AJAXRequestData requestData, OnboardingService onboardingService, Session session) throws OXException, JSONException {
        List<OnboardingAction> actions = scenario.getActions();
        for (OnboardingAction action : actions) {
            String actionId = null; // <--- Set to a valid, non-null identifier in case action has been successfully added
            switch (action) {
                case SMS: {
                    if (!actionCollector.usedIds.containsKey(action.getId())) {
                        JSONObject smsAction = new JSONObject(2);
                        smsAction.put("id", action.getId());
                        ConfigViewFactory configViewFactory = services.getService(ConfigViewFactory.class);
                        ConfigView view = configViewFactory.getView(session.getUserId(), session.getContextId());
                        smsAction.put("default", view.opt(OnboardingSMSConstants.SMS_DEFAULT_COUNTRY, String.class, "DE").toUpperCase());
                        actionCollector.addAction(action.getId(), smsAction);
                    }
                    actionId = action.getId();
                }
                    break;
                case DOWNLOAD:
                    // Generic action
                    // fall-through
                case EMAIL:
                    // Generic action
                    {
                        if (!actionCollector.usedIds.containsKey(action.getId())) {
                            actionCollector.addAction(action.getId(), new JSONObject(2).put("id", action.getId()));
                        }
                        actionId = action.getId();
                    }
                    break;
                case DISPLAY:
                    {
                        String compositeActionId = new StringBuilder(action.getId()).append('/').append(scenario.getCompositeId().getScenarioId()).toString();
                        if (!actionCollector.usedIds.containsKey(compositeActionId)) {
                            try {
                                ResultObject result = onboardingService.execute(createOnboardingRequest(scenario, requestData, action), session);
                                JSONObject jAction = new JSONObject(2);
                                jAction.put("id", compositeActionId);
                                jAction.put("data", result.getObject());
                                actionCollector.addAction(compositeActionId, jAction);
                                actionId = null == scenarioAppendix ? action.getId() : new StringBuilder(action.getId()).append('/').append(scenarioAppendix).toString();
                            } catch (OXException e) {
                                LOGGER.warn("Failed to retrieve data for action '{}' and will therefore be ignored.", compositeActionId, e);
                                actionId = null;
                            }
                        } else {
                            actionId = null == scenarioAppendix ? action.getId() : new StringBuilder(action.getId()).append('/').append(scenarioAppendix).toString();
                        }
                    }
                    break;
                case LINK:
                    {
                        String compositeActionId = new StringBuilder(action.getId()).append('/').append(scenario.getCompositeId().getScenarioId()).toString();
                        try {
                            JSONObject jAction = actionCollector.usedIds.get(compositeActionId);
                            if (null == jAction) {
                                ResultObject result = onboardingService.execute(createOnboardingRequest(scenario, requestData, action), session);
                                jAction = new JSONObject(2);
                                jAction.put("id", compositeActionId);
                                jAction.put(scenario.getDevice().getId(), result.getObject());
                                actionCollector.addAction(compositeActionId, jAction);
                            } else {
                                ResultObject result = onboardingService.execute(createOnboardingRequest(scenario, requestData, action), session);
                                jAction.put(scenario.getDevice().getId(), result.getObject());
                            }
                            actionId = null == scenarioAppendix ? action.getId() : new StringBuilder(action.getId()).append('/').append(scenarioAppendix).toString();
                        } catch (OXException e) {
                            LOGGER.warn("Failed to retrieve link for action '{}' and will therefore be ignored.", compositeActionId, e);
                            actionId = null;
                        }
                    }
                    break;
                default:
                    break;

            }

            if (null != actionId) {
                jActionIds.put(actionId);
            }
        }

        for (Scenario alternative : scenario.getAlternatives(session)) {
            DeviceAwareScenario alternativeDeviceAwareScenario = onboardingService.getScenario(alternative.getId(), scenario.getDevice(), session);
            collectActions(alternativeDeviceAwareScenario, alternative.getId(), jActionIds, actionCollector, requestData, onboardingService, session);
        }
    }

    private JSONObject toJson(Scenario scenario, boolean enabled, ServerSession session) throws OXException, JSONException {
        JSONObject jScenario = new JSONObject(8);
        jScenario.put("id", scenario.getId());
        jScenario.put("enabled", enabled);
        put2Json("name", scenario.getDisplayName(session), jScenario);
        put2Json("description", scenario.getDescription(session), jScenario);
        put2Json("icon", scenario.getIcon(session), jScenario);
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

    private DefaultOnboardingRequest createOnboardingRequest(DeviceAwareScenario scenario, AJAXRequestData requestData, OnboardingAction action) {
        return new DefaultOnboardingRequest(scenario, action, scenario.getDevice(), requestData.getHostData(), null);
    }

    // -------------------------------------------------------------------------------------------

    private static class ActionCollector {

        final Map<String, JSONObject> usedIds;
        final JSONArray jActions;

        ActionCollector() {
            super();
            usedIds = new HashMap<String, JSONObject>(10, 0.9F);
            jActions = new JSONArray(10);
        }

        void addAction(String id, JSONObject jAction) {
            usedIds.put(id, jAction);
            jActions.put(jAction);
        }
    }

}
