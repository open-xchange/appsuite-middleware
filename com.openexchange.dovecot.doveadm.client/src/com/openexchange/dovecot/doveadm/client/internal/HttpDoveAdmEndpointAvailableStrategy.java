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
 *    trademarks of the OX Software GmbH. group of companies.
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
                LOG.info("DoveAdm end-point {} is re-available and will therefore removed from black-list", uri);
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

        LOG.info("DoveAdm end-point {} is (still) not available and will therefore removed from black-list", uri);
        return AvailableResult.UNAVAILABLE;
    }

}
