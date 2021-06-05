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

package com.openexchange.multifactor.json.actions;

import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.MultifactorRequest;
import com.openexchange.multifactor.json.converter.mapper.MultifactorDeviceMapper;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AJAXMultifactorRequest} represents the data required for performing a multifactor request
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class AJAXMultifactorRequest {

    /**
     * The name of the multifactor provider to perform an action with
     */
    private static final String PARAM_MULTIFACTOR_PROVIDER_NAME   = "providerName";
    /**
     * The device id parameter
     */
    private static final String PARAM_MULTIFACTOR_DEVICE_ID = "deviceId";

    /**
     * A name based filter list for providers
     */
    private static final String PARAM_MULTIFACTOR_PROVIDER_LIST   = "providers";

    private final ServerSession       session;
    private final Locale              locale;
    private final AJAXRequestData     requestData;
    private MultifactorRequest        multifactorRequest;

    /**
     * Initializes a new {@link AJAXMultifactorRequest}.
     *
     * @param requestData The request data
     * @param session The session
     * @param locale the user's locale, or null if this action is not executed in a user's context
     * @param parameters The parameters
     */
    public AJAXMultifactorRequest(AJAXRequestData requestData, ServerSession session, Locale locale) {
        this.requestData = requestData;
        this.session = session;
        this.locale = locale;
    }

    /**
     * Extracts the JSONObject payload from the request
     *
     * @return The extracted JSON data
     * @throws OXException if the json payload is missing, or an parsing error occurs
     */
    public JSONObject requireJsonBody() throws OXException {
        JSONObject jsonBody = getJsonBody();
        if (jsonBody == null) {
            throw AjaxExceptionCodes.ILLEGAL_REQUEST_BODY.create();
        }
        return jsonBody;
    }

    /**
     * Extracts the JSONObject payload from the request
     *
     * @return The extracted JSON data, or null if not present
     */
    private JSONObject getJsonBody() {
        return requestData.getData() instanceof JSONObject ? (JSONObject) requestData.getData() : null;
    }

    /**
     * Gets the related {@link ServerSession}
     *
     * @return The {@link ServerSession}
     */
    protected ServerSession getServerSession() {
        return session;
    }

    /**
     * Gets the related {@link MultifactorRequest}
     *
     * @return The {@link MultifactorRequest}
     * @throws OXException
     */
    protected MultifactorRequest getMultifactorRequest() {
        if (multifactorRequest == null) {
            multifactorRequest = new MultifactorRequest(session, locale);
        }
        return multifactorRequest;
    }

    /**
     * Parses a {@link MultifactorDevice} from the given request data
     *
     * @return the parsed {@link MultifactorDevice}
     * @throws OXException
     */
    protected MultifactorDevice parseDevice() throws OXException {
        try {
            return MultifactorDeviceMapper.getInstance().deserialize(requireJsonBody(), MultifactorDeviceMapper.getInstance().getMappedFields());
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets a list of provider names provided within this request
     *
     * @return A list of provider names provided in this request, or null if no provider names were specified
     */
    protected String[] getProviderList() {
        final String parameter = requestData.getParameter(PARAM_MULTIFACTOR_PROVIDER_LIST);
        return Strings.isEmpty(parameter) ? null : Strings.splitByCommaNotInQuotes(parameter);
    }

    /**
     * Gets the name of the provider to perform an action with; delete, rename devices etc..
     *
     * @return The name of the provider to perform an action against
     * @throws OXException if the parameter is missing
     */
    protected String getProviderName() throws OXException {
        return requestData.requireParameter(PARAM_MULTIFACTOR_PROVIDER_NAME);
    }

    /**
     * Gets the id of the device
     *
     * @return The device id
     * @throws OXException if the parameter is missing
     */
    protected String getDeviceID() throws OXException {
        return requestData.requireParameter(PARAM_MULTIFACTOR_DEVICE_ID);
    }
}
