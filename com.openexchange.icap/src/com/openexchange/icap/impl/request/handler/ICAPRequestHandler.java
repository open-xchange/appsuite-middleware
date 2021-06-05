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

package com.openexchange.icap.impl.request.handler;

import java.io.IOException;
import java.net.Socket;
import com.openexchange.icap.ICAPRequest;
import com.openexchange.icap.ICAPResponse;

/**
 * {@link ICAPRequestHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public interface ICAPRequestHandler {

    /**
     * Handles/Executes the specified {@link ICAPRequest}, i.e. sending it over the wire and performing
     * all necessary tasks to yield an {@link ICAPResponse}.
     * 
     * @param request The {@link ICAPRequest} to handle/execute
     * @param socket The socket with the connection to the ICAP server.
     * @return The {@link ICAPResponse} of the ICAP server
     * @throws IOException if an I/O error is occurred
     */
    ICAPResponse handle(ICAPRequest request, Socket socket) throws IOException;
}
