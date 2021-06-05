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

package com.openexchange.nimbusds.oauth2.sdk.http.send;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import com.nimbusds.jose.util.Resource;
import com.nimbusds.jose.util.ResourceRetriever;

/**
 * {@link HttpClientResourceRetriever} - A retriever of resources specified by URL using a provider's {@link HttpClient}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class HttpClientResourceRetriever implements ResourceRetriever {

    private final HttpClientProvider httpClientProvider;

    /**
     * Initializes a new {@link HttpClientResourceRetriever}.
     *
     * @param httpClientProvider The provider for the <code>HttpClient</code> instance to use
     */
    public HttpClientResourceRetriever(HttpClientProvider httpClientProvider) {
        super();
        this.httpClientProvider = httpClientProvider;
    }

    @Override
    public Resource retrieveResource(URL url) throws IOException {
        HttpClient httpClient = httpClientProvider.getHttpClient();

        HttpGet get = null;
        HttpResponse response = null;
        try {
            get = new HttpGet(url.toURI());
            response = httpClient.execute(get);

            // Check HTTP code + message
            int statusCode = response.getStatusLine().getStatusCode();
            String statusMessage = response.getStatusLine().getReasonPhrase();

            // Ensure 2xx status code
            if (statusCode > 299 || statusCode < 200) {
                throw new IOException("HTTP " + statusCode + ": " + statusMessage);
            }

            HttpEntity entity = response.getEntity();
            try {
                String content = EntityUtils.toString(entity);
                return new Resource(content, entity.getContentType().getValue());
            } finally {
                EntityUtils.consumeQuietly(entity);
            }
        } catch (URISyntaxException e) {
            throw new IOException("Failed to obtain URI equivalent for URL: " + url.toString(), e);
        } finally {
            Utils.close(get, response);
        }
    }

}
