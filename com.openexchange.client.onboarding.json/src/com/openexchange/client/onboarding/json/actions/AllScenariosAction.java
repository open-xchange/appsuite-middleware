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

import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.client.onboarding.ClientDevice;
import com.openexchange.client.onboarding.ClientDevices;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.DeviceAwareScenario;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AllScenariosAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class AllScenariosAction extends AbstractOnboardingAction {

    /**
     * Initializes a new {@link AllScenariosAction}.
     *
     * @param services
     */
    public AllScenariosAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        OnboardingService onboardingService = getOnboardingService();

        String deviceId = requestData.checkParameter("device");
        Device device = Device.deviceFor(deviceId);
        if (null == device) {
            throw OnboardingExceptionCodes.INVALID_DEVICE_ID.create(deviceId);
        }

        ClientDevice clientDevice;
        {
            String clientDeviceId = requestData.getParameter("client");
            clientDevice = ClientDevices.getClientDeviceFor(clientDeviceId);
        }

        List<DeviceAwareScenario> scenarios = onboardingService.getScenariosFor(clientDevice, device, session);
        return new AJAXRequestResult(scenarios, "onboardingScenario");
    }

}
