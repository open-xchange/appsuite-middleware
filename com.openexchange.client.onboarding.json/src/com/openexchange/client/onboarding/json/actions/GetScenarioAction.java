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

import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.client.onboarding.ClientDevice;
import com.openexchange.client.onboarding.ClientDevices;
import com.openexchange.client.onboarding.CompositeId;
import com.openexchange.client.onboarding.DeviceAwareScenario;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.client.onboarding.OnboardingUtility;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetScenarioAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class GetScenarioAction extends AbstractOnboardingAction {

    /**
     * Initializes a new {@link GetScenarioAction}.
     *
     * @param services
     */
    public GetScenarioAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        OnboardingService onboardingService = getOnboardingService();

        String sCompositeId = requestData.checkParameter("id");
        CompositeId compositeId = OnboardingUtility.parseCompositeId(sCompositeId);

        ClientDevice clientDevice;
        {
            String clientDeviceId = requestData.getParameter("client");
            clientDevice = ClientDevices.getClientDeviceFor(clientDeviceId);
        }

        if (false == clientDevice.implies(compositeId.getDevice())) {
            throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create(compositeId.getScenarioId());
        }

        DeviceAwareScenario scenario = onboardingService.getScenario(compositeId.getScenarioId(), clientDevice, compositeId.getDevice(), session);
        return new AJAXRequestResult(scenario, "onboardingScenario");
    }

}
