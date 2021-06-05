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

package com.openexchange.proxy.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.apache.http.HttpResponse;
import org.apache.http.impl.io.ChunkedInputStream;
import org.apache.http.util.EntityUtils;
import com.openexchange.java.Streams;
import com.openexchange.proxy.Header;
import com.openexchange.proxy.Response;

/**
 * {@link ResponseImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResponseImpl implements Response {

    private final HttpResponse response;

    /**
     * Initializes a new {@link ResponseImpl}.
     *
     * @param response The delegatee
     */
    public ResponseImpl(final HttpResponse response) {
        super();
        this.response = response;
    }

    @Override
    public int getStatusCode() {
        return response.getStatusLine().getStatusCode();
    }

    @Override
    public String getStatusText() {
        return response.getStatusLine().getReasonPhrase();
    }

    @Override
    public Header[] getResponseHeaders() {
        org.apache.http.Header[] headers = response.getAllHeaders();
        if (null == headers) {
            return null;
        }
        final int length = headers.length;
        final Header[] ret = new Header[length];
        for (int i = 0; i < length; i++) {
            ret[i] = new HeaderImpl(headers[i]);
        }
        return ret;
    }

    @Override
    public Header getResponseHeader(final String headerName) {
        return new HeaderImpl(response.getFirstHeader(headerName));
    }

    @Override
    public Header[] getResponseHeaders(final String headerName) {
        final org.apache.http.Header[] headers = response.getHeaders(headerName);
        if (null == headers) {
            return null;
        }
        final int length = headers.length;
        final Header[] ret = new Header[length];
        for (int i = 0; i < length; i++) {
            ret[i] = new HeaderImpl(headers[i]);
        }
        return ret;
    }

    @Override
    public Header[] getResponseFooters() {
        InputStream inputStream = null;
        try {
            inputStream = response.getEntity().getContent();

            if (inputStream instanceof ChunkedInputStream) {
                org.apache.http.Header[] footers = ((ChunkedInputStream) inputStream).getFooters();

                final Header[] ret = new Header[footers.length];
                int i = 0;
                for (org.apache.http.Header footer : footers) {
                    ret[i++] = new HeaderImpl(footer);
                }
                return ret;
            }
        } catch (UnsupportedOperationException e) {
           // nothing to do
        } catch (IOException e) {
            // nothing to do
        } finally {
            Streams.close(inputStream);
        }
        return new Header[0];
    }

    @Override
    public Header getResponseFooter(final String footerName) {
        return Arrays.asList(getResponseFooters()).stream().filter((header) -> header.getName().equals(footerName)).findFirst().orElse(null);
    }

    @Override
    public byte[] getResponseBody() throws IOException {
        return EntityUtils.toByteArray(response.getEntity());
    }

    @Override
    public String getResponseBodyAsString() throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    @Override
    public InputStream getResponseBodyAsStream() throws IOException {
        return response.getEntity().getContent();
    }

}
