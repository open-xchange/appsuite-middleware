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

package com.openexchange.multifactor.provider.u2f.impl;

import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.DefaultRegistrationChallenge;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;
import com.yubico.u2f.data.messages.RegisterRequestData;

/**
 * {@link U2FRegistrationChallenge}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class U2FRegistrationChallenge extends DefaultRegistrationChallenge {

    private static final String REQUEST_ID_PARAMETER = "requestId";
    private static final String REGISTERED_KEYS_PARAMETER = "registeredKeys";
    private static final String REGISTER_REQUESTS_PARAMETER = "registerRequests";

    /**
     * Initializes a new {@link U2FRegistrationChallenge}.
     *
     * @param deviceId The device id
     * @param data The {@link RegisterRequestData} for the challenge
     * @throws OXException
     */
    public U2FRegistrationChallenge(String deviceId, RegisterRequestData data) throws OXException {
        super(deviceId, new HashMap<>(3));
        JSONObject json;
        try {
            json = new JSONObject(data.toJson());
            challenge.put(REQUEST_ID_PARAMETER, data.getRequestId());
            challenge.put(REGISTERED_KEYS_PARAMETER, json.getJSONArray(REGISTERED_KEYS_PARAMETER));
            challenge.put(REGISTER_REQUESTS_PARAMETER, json.getJSONArray(REGISTER_REQUESTS_PARAMETER));
        } catch (JSONException e) {
            throw MultifactorExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

}
