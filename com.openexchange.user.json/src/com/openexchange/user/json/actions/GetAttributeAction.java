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

package com.openexchange.user.json.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserExceptionCode;
import com.openexchange.user.UserService;

/**
 * {@link GetAttributeAction} - Maps the action to an <tt>update</tt> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GetAttributeAction extends AbstractUserAction {

    /**
     * The <tt>getAttribute</tt> action string.
     */
    public static final String ACTION = "getAttribute";

    /**
     * Initializes a new {@link GetAttributeAction}.
     */
    public GetAttributeAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        try {
            /*
             * Parse parameters
             */
            final int id = checkIntParameter(AJAXServlet.PARAMETER_ID, request);
            /*
             * Only allow for session-associated user
             */
            if (id != session.getUserId() && id != session.getContext().getMailadmin()) {
                throw UserExceptionCode.PERMISSION_ACCESS.create(Integer.valueOf(session.getContextId()));
            }
            final String name = checkStringParameter("name", request);
            /*
             * Get user service
             */
            final UserService userService = services.getService(UserService.class);
            /*
             * Perform update
             */
            final String value = userService.getUserAttribute(name, id, session.getContext());
            /*
             * Return
             */
            final JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("value", value);
            return new AJAXRequestResult(json);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
        }
    }

}
