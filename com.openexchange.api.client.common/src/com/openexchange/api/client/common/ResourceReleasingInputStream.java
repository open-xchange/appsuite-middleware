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

package com.openexchange.api.client.common;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.tools.stream.CountingOnlyInputStream;

/**
 * {@link ResourceReleasingInputStream} - Wraps the content stream of a {@link HttpResponse}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class ResourceReleasingInputStream extends CountingOnlyInputStream {

    private final static Logger LOG = LoggerFactory.getLogger(ResourceReleasingInputStream.class);

    private final HttpRequestBase request;
    private final HttpResponse response;
    private final long contentLength;

    /**
     * Initializes a new {@link ResourceReleasingInputStream}.
     * <br>
     * Releases the given request and response resources if the response's InputStream gets closed.
     *
     * @param request The request to close along the response
     * @param response The response to wrap
     * @throws IOException If the stream could not be created
     */
    @SuppressWarnings("resource")
    public ResourceReleasingInputStream(HttpRequestBase request, HttpResponse response) throws IOException {
        this(request, response, response.getEntity().getContent());
    }

    /**
     * Initializes a new {@link ResourceReleasingInputStream}.
     * Releases the given request and response resources if the given InputStream gets closed.
     *
     * @param request The request to close along the response
     * @param response The response to wrap
     * @param inputStream The {@link InpuStream}
     */
    public ResourceReleasingInputStream(HttpRequestBase request, HttpResponse response, InputStream inputStream) {
        super(inputStream);
        this.request = request;
        this.response = response;
        this.contentLength = response.getEntity().getContentLength();
    }

    @Override
    public void close() throws IOException {
        if (0 < contentLength && contentLength > getCount()) {
            LOG.warn("Closing not entirely consumed response {}", response);
        }
        HttpClients.close(request, response);
    }
}
