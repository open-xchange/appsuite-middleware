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
        if(multifactorRequest == null) {
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
