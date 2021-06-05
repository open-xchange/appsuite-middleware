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

package com.openexchange.jump.json.actions;

import java.util.Set;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.jump.Endpoint;
import com.openexchange.jump.EndpointHandler;
import com.openexchange.jump.JumpExceptionCodes;
import com.openexchange.jump.json.JumpRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link IdentityTokenAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IdentityTokenAction extends AbstractJumpAction {

    /**
     * Initializes a new {@link IdentityTokenAction}.
     *
     * @param services The service look-up
     */
    public IdentityTokenAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final JumpRequest request) throws OXException, JSONException {
        // Require system
        final String systemName = request.requireParameter("system");

        // Look-up endpoint
        final Endpoint endpoint = getJumpSerivce().requireEndpoint(systemName);

        // Generate token
        final UUID token = UUID.randomUUID();

        final ServerSession session = request.getSession();
        boolean handled = false;
        for (final EndpointHandler endpointHandler : getEndpointHandlerRegistry().getHandlers()) {
            final Set<String> namesOfInterest = endpointHandler.systemNamesOfInterest();
            if (null != namesOfInterest && namesOfInterest.contains(endpoint.getSystemName())) {
                handled |= endpointHandler.handleEndpoint(token, endpoint, session);
            }
        }
        if (!handled) {
            throw JumpExceptionCodes.NO_SUCH_ENDPOINT_HANDLER.create(systemName);
        }

        return new AJAXRequestResult(new JSONObject(2).put("token", UUIDs.getUnformattedString(token)), "json");
    }

}
