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

package com.openexchange.webdav.client.jackrabbit;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.tools.stream.CountingOnlyInputStream;

/**
 * {@link HttpResponseStream}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class HttpResponseStream extends CountingOnlyInputStream {

    /**
     * Initializes a new {@link HttpResponseStream} for given HTTP response.
     *
     * @param response The HTTP response to create the stream for
     * @return The newly created response stream
     * @throws IOException If initialization fails
     */
    public static HttpResponseStream createHttpResponseStreamFor(HttpResponse response) throws IOException {
        if (response == null) {
            return null;
        }

        HttpEntity entity = response.getEntity();
        if (null == entity) {
            throw new IOException("got no response entity");
        }

        long contentLength = entity.getContentLength();
        return new HttpResponseStream(entity.getContent(), contentLength, response);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final HttpResponse response;
    private final long contentLength;

    /**
     * Initializes a new {@link HttpResponseStream}.
     *
     * @param entityStream The response entity's input stream
     * @param contentLength The length of the content, which is the number of bytes of the content, or a negative number if unknown
     * @param response The HTTP response whose entity stream shall be read from
     */
    private HttpResponseStream(InputStream entityStream, long contentLength, HttpResponse response) {
        super(entityStream);
        this.response = response;
        this.contentLength = contentLength;
    }

    @Override
    public void close() throws IOException {
        /*
         * close underlying HTTP response if entity not entirely consumed
         */
        try {
            if (0 < contentLength && contentLength > getCount()) {
                // Invoke with consumeEntity=false since stream is closed in finally block
                HttpClients.close(response, false);
            } else {
                HttpClients.close(response, true);
            }
        } finally {
            super.close();
        }
    }

}
