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

package com.openexchange.messaging.json.actions.services;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * Loads a certain messaging service. Parameters are:
 * <dl>
 *  <dt>id</dt><dd>The messaging service id.</dd>
 * </dl>
 * Returns the JSON representation of the loaded messaging service id.
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GetAction extends AbstractMessagingServiceAction {

    public GetAction(final MessagingServiceRegistry registry) {
        super(registry);
    }

    @Override
    protected AJAXRequestResult doIt(final AJAXRequestData request, final ServerSession session) throws JSONException, OXException {
        final String id = request.getParameter("id");
        if (id == null) {
            throw MessagingExceptionCodes.MISSING_PARAMETER.create("id");
        }
        final MessagingService service = registry.getMessagingService(id, session.getUserId(), session.getContextId());
        if (null == service) {
            throw MessagingExceptionCodes.UNKNOWN_MESSAGING_SERVICE.create(id);
        }
        final JSONObject responseObject = getWriter(session).write(service);
        return new AJAXRequestResult(responseObject);
    }
}
