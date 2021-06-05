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

package com.openexchange.authentication.application.impl.api;

import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.authentication.application.ApplicationPassword;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AddAppPassword}
 * Action to add an application specific password to the users account
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class AddAppPassword extends AbstractAppPasswordAction {

    private final static String PARAMETER_APPLICATION_TYPE = "appScope";   // Scope name for the addition
    private final static String PARAMETER_APPLICATION_NAME = "appName";   // User defined name

    /**
     * Initializes a new {@link AddAppPassword}.
     *
     * @param lookup The service lookup
     */
    public AddAppPassword(ServiceLookup lookup) {
        super(lookup);
    }

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException {
        String appType = requestData.requireParameter(PARAMETER_APPLICATION_TYPE);
        String appName = requestData.requireParameter(PARAMETER_APPLICATION_NAME);
        ApplicationPassword lpass = getService().addPassword(session, appName, appType);
        boolean sameLoginName = Objects.equals(session.getLogin(), lpass.getLogin());
        try {
            return new AJAXRequestResult(new JSONObject().put("password", lpass.getAppPassword()).put("login", lpass.getLogin()).put("newLogin", false == sameLoginName));
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
    }
}
