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

package com.openexchange.mail.filter.json.v2.actions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.EnqueuableAJAXActionService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mailfilter.Credentials;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractMailFilterAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public abstract class AbstractMailFilterAction implements EnqueuableAJAXActionService {

    protected final ServiceLookup services;
    private static final String UserNameParameter = "username";

    /**
     * Initializes a new {@link AbstractMailFilterAction}.
     */
    protected AbstractMailFilterAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    protected JSONObject getJSONBody(Object data) throws OXException {

        if (!(data instanceof JSONObject)){
            throw AjaxExceptionCodes.INVALID_REQUEST_BODY.create(JSONObject.class.getSimpleName(), data.getClass().getSimpleName());
        }

        return (JSONObject) data;
    }

    protected JSONArray getJSONArrayBody(Object data) throws OXException {

        if (!(data instanceof JSONArray)){
            throw AjaxExceptionCodes.INVALID_REQUEST_BODY.create(JSONObject.class.getSimpleName(), data.getClass().getSimpleName());
        }

        return (JSONArray) data;
    }

    protected Integer getUniqueId(final JSONObject json) throws OXException {
        if (json.has("id") && !json.isNull("id")) {
            try {
                return Integer.valueOf(json.getInt("id"));
            } catch (JSONException e) {
                throw MailFilterExceptionCode.ID_MISSING.create();
            }
        }
        throw MailFilterExceptionCode.MISSING_PARAMETER.create("id");
    }

    protected Credentials getCredentials(Session session, AJAXRequestData request) {
        Credentials credentials = new Credentials(session);
        String userName = getUserName(request);
        if (Strings.isNotEmpty(userName)) {
            credentials.setUsername(userName);
        }
        return credentials;
    }

    private String getUserName(AJAXRequestData request) {
        return request.getParameter(UserNameParameter);
    }

    @Override
    public Result isEnqueueable(AJAXRequestData request, ServerSession session) throws OXException {
        return EnqueuableAJAXActionService.resultFor(false);
    }

}
