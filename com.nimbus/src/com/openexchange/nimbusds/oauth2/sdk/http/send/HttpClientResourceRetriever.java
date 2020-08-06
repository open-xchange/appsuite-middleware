/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
