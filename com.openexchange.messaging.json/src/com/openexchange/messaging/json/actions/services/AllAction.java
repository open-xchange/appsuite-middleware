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

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * Lists all known messaging services. No parameters are needed. Returns a JSONArray consisting of the JSON representations of all known
 * MessagingServices.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AllAction extends AbstractMessagingServiceAction {

    /**
     * Initializes a new {@link AllAction}.
     *
     * @param registry The registry for {@link MessagingService messaging services}
     */
    public AllAction(final MessagingServiceRegistry registry) {
        super(registry);
    }

    @Override
    public AJAXRequestResult doIt(final AJAXRequestData request, final ServerSession session) throws JSONException, OXException {
        final List<MessagingService> services = registry.getAllServices(session.getUserId(), session.getContextId());
        if (services.isEmpty()) {
            return new AJAXRequestResult(new JSONArray());
        }
        final JSONArray result = new JSONArray();
        for (final MessagingService service : services) {
            result.put(getWriter(session).write(service));
        }
        return new AJAXRequestResult(result);
    }

}
