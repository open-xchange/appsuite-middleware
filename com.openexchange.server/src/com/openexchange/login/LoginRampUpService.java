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

package com.openexchange.login;

import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;


/**
 * A {@link LoginRampUpService} contributes data based on the client id sent in the login request.
 * This allows optimization of the number of calls needed for an initial login.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface LoginRampUpService {

    /**
     * Return whether this ramp up service wants to contribute to a given client ID
     * Note that only one RampUpService will ever be used for a given client.
     * @param client
     * @return true, if this ramp up service deals with the given client
     */
    boolean contributesTo(String client);

    /**
     * Generate the contribution for the given session
     * @param session The authenticated session
     * @param request
     * @return The contribution
     * @throws OXException
     */
    JSONObject getContribution(ServerSession session, AJAXRequestData request) throws OXException;

}
