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

package com.openexchange.multifactor.json.converter;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.Challenge;
import com.openexchange.multifactor.RegistrationChallenge;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MultifactorChallengeResultConverter}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorChallengeResultConverter implements ResultConverter {

    private static final String DEVICE_ID = "deviceId";
    private static final String CHALLENGE = "challenge";

    public static String INPUT_FORMAT = "multifactor_challenge_result";

    @Override
    public String getInputFormat() {
        return INPUT_FORMAT;
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
        try {
            if (resultObject instanceof RegistrationChallenge) {
                RegistrationChallenge challenge = (RegistrationChallenge) resultObject;
                JSONObject json = new JSONObject();
                json.put(DEVICE_ID, challenge.getDeviceId());
                json.put(CHALLENGE, challenge.getChallenge());
                result.setResultObject(json);
                return;
            } else if (resultObject instanceof Challenge) {
                Challenge challenge = (Challenge) resultObject;
                JSONObject json = new JSONObject();
                json.put(CHALLENGE, challenge.getChallenge());
                result.setResultObject(json);
                return;
            } else {
                throw new UnsupportedOperationException();
            }
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
    }
}
