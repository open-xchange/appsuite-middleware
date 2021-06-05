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

package com.openexchange.client.onboarding.json.actions;

import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.client.onboarding.ClientDevice;
import com.openexchange.client.onboarding.ClientDevices;
import com.openexchange.client.onboarding.CompositeId;
import com.openexchange.client.onboarding.DefaultOnboardingRequest;
import com.openexchange.client.onboarding.OnboardingAction;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.client.onboarding.OnboardingUtility;
import com.openexchange.client.onboarding.ResultObject;
import com.openexchange.client.onboarding.Scenario;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ExecuteAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class ExecuteAction extends AbstractOnboardingAction {

    /**
     * Initializes a new {@link ExecuteAction}.
     * @param services
     */
    public ExecuteAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        OnboardingService onboardingService = getOnboardingService();

        // Check for composite identifier
        String sCompositeId = requestData.getParameter("id");
        if (Strings.isEmpty(sCompositeId)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
        }

        // Parse composite identifier
        CompositeId compositeId = OnboardingUtility.parseCompositeId(sCompositeId);

        // Check for action
        String sAction = requestData.getParameter("action_id");
        if (Strings.isEmpty(sAction)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("action_id");
        }

        // Target client
        OnboardingAction action = OnboardingAction.actionFor(sAction);
        if (null == action) {
            throw AjaxExceptionCodes.INVALID_PARAMETER.create("action_id");
        }

        ClientDevice clientDevice;
        {
            String clientDeviceId = requestData.getParameter("client");
            clientDevice = ClientDevices.getClientDeviceFor(clientDeviceId);
        }

        if (false == clientDevice.implies(compositeId.getDevice())) {
            throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create(compositeId.getScenarioId());
        }

        // Parse optional form content
        Map<String, Object> input = null;
        {
            Object data = requestData.getData();
            if (data instanceof JSONObject) {
                JSONObject jFormContent = (JSONObject) data;
                @SuppressWarnings("unchecked") Map<String, Object> map = (Map<String, Object>) JSONCoercion.coerceToNative(jFormContent);
                input = map;
            }
        }

        Scenario scenario = onboardingService.getScenario(compositeId.getScenarioId(), session);

        // Create on-boarding request & execute it
        DefaultOnboardingRequest request = new DefaultOnboardingRequest(scenario, action, clientDevice, compositeId.getDevice(), requestData.getHostData(), input);
        ResultObject resultObject = onboardingService.execute(request, session);

        // Return result
        String format = resultObject.getFormat();
        if (null == format) {
            if (resultObject.hasWarnings()) {
                AJAXRequestResult result = new AJAXRequestResult(resultObject.getObject());
                result.addWarnings(resultObject.getWarnings());
            } else {
                return new AJAXRequestResult(resultObject.getObject());
            }
        }
        if ("file".equals(format)) {
            requestData.setFormat(format);
        }
        AJAXRequestResult result = new AJAXRequestResult(resultObject.getObject(), format);
        if (resultObject.hasWarnings()) {
            result.addWarnings(resultObject.getWarnings());
        }
        return result;
    }

}
