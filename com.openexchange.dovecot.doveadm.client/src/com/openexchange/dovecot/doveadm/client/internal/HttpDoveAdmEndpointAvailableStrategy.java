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

package com.openexchange.dovecot.doveadm.client.internal;

import static com.openexchange.dovecot.doveadm.client.internal.HttpDoveAdmClient.close;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import com.google.common.io.BaseEncoding;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.rest.client.endpointpool.Endpoint;
import com.openexchange.rest.client.endpointpool.EndpointAvailableStrategy;


/**
 * {@link HttpDoveAdmEndpointAvailableStrategy}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class HttpDoveAdmEndpointAvailableStrategy implements EndpointAvailableStrategy {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HttpDoveAdmEndpointAvailableStrategy.class);

    private final String authorizationHeaderValue;

    /**
     * Initializes a new {@link HttpDoveAdmEndpointAvailableStrategy}.
     */
    public HttpDoveAdmEndpointAvailableStrategy(String apiKey) {
        super();
        String encodedApiKey = BaseEncoding.base64().encode(apiKey.getBytes(Charsets.UTF_8));
        authorizationHeaderValue = "X-Dovecot-API " + encodedApiKey;
    }

    private void setCommonHeaders(HttpRequestBase request) {
        request.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);
    }

    @Override
    public AvailableResult isEndpointAvailable(Endpoint endpoint, HttpClient httpClient) throws OXException {
        URI uri;
        try {
            uri = HttpDoveAdmClient.buildUri(new URI(endpoint.getBaseUri()), null, null);
        } catch (URISyntaxException e) {
            // ignore
            LOG.warn("The URI to check for re-availability is wrong", e);
            return AvailableResult.NONE;
        }

        HttpGet get = null;
        HttpResponse response = null;
        try {
            get = new HttpGet(uri);
            setCommonHeaders(get);
            response = httpClient.execute(get);
            int status = response.getStatusLine().getStatusCode();
            if (200 == status) {
                LOG.info("DoveAdm end-point {} is re-available and is therefore removed from black-list", uri);
                return AvailableResult.AVAILABLE;
            }
            if (401 == status) {
                return AvailableResult.NONE;
            }
        } catch (IOException e) {
            // ignore
        } finally {
            close(get, response);
        }

        LOG.info("DoveAdm end-point {} is (still) not available and is therefore removed from black-list", uri);
        return AvailableResult.UNAVAILABLE;
    }

}
