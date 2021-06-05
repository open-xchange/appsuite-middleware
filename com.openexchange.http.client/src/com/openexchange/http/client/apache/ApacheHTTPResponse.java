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

package com.openexchange.http.client.apache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPResponse;

/**
 *
 * {@link ApacheHTTPResponse}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ApacheHTTPResponse implements HTTPResponse {

    private final HttpResponse resp;
    private final ApacheClientRequestBuilder coreBuilder;
    private final CookieStore cookieStore;

    /**
     * Initializes a new {@link ApacheHTTPResponse}.
     *
     * @param resp The {@link HttpResponse}
     * @param coreBuilder The {@link ApacheClientRequestBuilder}
     * @param cookieStore The {@link CookieStore} of the client used
     */
    public ApacheHTTPResponse(HttpResponse resp, ApacheClientRequestBuilder coreBuilder, CookieStore cookieStore) {
        this.resp = resp;
        this.coreBuilder = coreBuilder;
        this.cookieStore = cookieStore;
    }

    @Override
    public int getStatus() {
        return resp.getStatusLine().getStatusCode();
    }

    @Override
    public <R> R getPayload(Class<R> type) throws OXException {
        return coreBuilder.extractPayload(resp, type);
    }

    @Override
    public Map<String, String> getHeaders() {
        Header[] responseHeaders = resp.getAllHeaders();
        Map<String, String> headers = new HashMap<String, String>();
        for (Header header : responseHeaders) {
            headers.put(header.getName(), header.getValue());
        }
        return headers;
    }

    @Override
    public Map<String, String> getCookies() {
        List<Cookie> cookies = cookieStore.getCookies();
        Map<String, String> r = new HashMap<String, String>();
        for (Cookie cookie : cookies) {
            r.put(cookie.getName(), cookie.getValue());
        }
        return r;
    }

}
