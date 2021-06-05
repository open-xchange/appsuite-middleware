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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import com.openexchange.icap.ICAPRequest;
import com.openexchange.icap.ICAPResponse;
import com.openexchange.tools.io.IOUtils;

/**
 * {@link OptionsICAPRequestHandler} - Handles the request/response cycle for the OPTIONS method.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.10">RFC-3507, Section 4.10</a>
 */
public class OptionsICAPRequestHandler extends AbstractICAPRequestHandler {

    /**
     * Initialises a new {@link OptionsICAPRequestHandler}.
     */
    public OptionsICAPRequestHandler() {
        super();
    }

    @Override
    ICAPResponse handle(ICAPRequest request, Socket socket, InputStream inputStream, OutputStream outputStream) throws IOException {
        StringBuilder requestBuilder = new StringBuilder(128);

        // Prepare
        prepareRequestMethod(request, requestBuilder);
        prepareRequestHeaders(request, requestBuilder);
        markEndOfHeaders(requestBuilder);

        // Write to wire
        sendData(outputStream, requestBuilder.toString());

        // Read data
        try {
            return readResponse(inputStream, socket);
        } finally {
            IOUtils.closeQuietly(socket);
        }
    }
}
