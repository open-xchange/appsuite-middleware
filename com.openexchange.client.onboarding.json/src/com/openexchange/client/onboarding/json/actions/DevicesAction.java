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
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.client.onboarding.ClientDevice;
import com.openexchange.client.onboarding.CompositeId;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.client.onboarding.service.OnboardingView;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DevicesAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class DevicesAction extends AbstractOnboardingAction {

    /**
     * Initializes a new {@link DevicesAction}.
     *
     * @param services
     */
    public DevicesAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        OnboardingService onboardingService = getOnboardingService();

        OnboardingView view = onboardingService.getViewFor(ClientDevice.IMPLIES_ALL, session);
        Map<Device, List<CompositeId>> availableDevices = view.getDevices();

        Device[] devices = Device.values();
        JSONObject jResult = new JSONObject(devices.length);
        for (Device device : devices) {
            jResult.put(device.getId(), availableDevices.containsKey(device));
        }
        return new AJAXRequestResult(jResult, "json");
    }

}
