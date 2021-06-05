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

package com.openexchange.rest.client.httpclient.internal;

import static com.openexchange.rest.client.httpclient.internal.HttpClientMetrics.getRequestTimer;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;

/**
 * A {@link HttpRequestExecutor} that monitors executed requests in terms of method, response time and response status.
 *
 * This was inspired by io.micrometer.core.instrument.binder.httpcomponents.MicrometerHttpRequestExecutor.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class MeteredHttpRequestExecutor extends HttpRequestExecutor {

    private final String clientName;

    /**
     * Initializes a new {@link MeteredHttpRequestExecutor}.
     * 
     * @param clientName The identifier of the HTTP client
     */
    public MeteredHttpRequestExecutor(String clientName) {
        super();
        this.clientName = clientName;
    }

    @Override
    public HttpResponse execute(HttpRequest request, HttpClientConnection conn, HttpContext context) throws IOException, HttpException {
        long start = System.nanoTime();
        String status = "UNKNOWN";
        try {
            HttpResponse response = super.execute(request, conn, context);
            status = response != null ? Integer.toString(response.getStatusLine().getStatusCode()) : "CLIENT_ERROR";
            return response;
        } catch (IOException | HttpException | RuntimeException e) {
            status = "IO_ERROR";
            throw e;
        } finally {
            getRequestTimer(clientName, request.getRequestLine().getMethod(), status).record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }
}
