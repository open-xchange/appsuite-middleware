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

package com.openexchange.ajax.oauth.provider.protocol;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import com.openexchange.ajax.oauth.provider.EndpointTest;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class POSTRequest extends AbstractRequest<POSTRequest> {

    @SuppressWarnings("hiding")
    String sessionId;
    String login;
    String password;
    boolean accessDenied;

    public POSTRequest() {
        super();
    }

    public static GETRequest newGETRequest() {
        return new GETRequest();
    }

    public POSTRequest setLogin(String login) {
        this.login = login;
        return this;
    }

    public POSTRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public POSTRequest setAccessDenied() {
        accessDenied = true;
        return this;
    }

    public POSTResponse submit(HttpClient client) throws IOException {
        Map<String, String> requestSpecificParams = new HashMap<>(5);
        requestSpecificParams.put("access_denied", Boolean.toString(accessDenied));
        if (login != null) {
            requestSpecificParams.put("login", login);
        }
        if (password != null) {
            requestSpecificParams.put("password", password);
        }

        List<NameValuePair> params = prepareParams(requestSpecificParams);
        try {
            HttpPost request = new HttpPost(
                new URIBuilder()
                    .setScheme(scheme)
                    .setHost(hostname)
                    .setPort(port)
                    .setPath(EndpointTest.AUTHORIZATION_ENDPOINT)
                    .build());

            for (String header : headers.keySet()) {
                String value = headers.get(header);
                if (value == null) {
                    request.removeHeaders(header);
                } else {
                    request.setHeader(header, value);
                }
            }

            request.setEntity(new UrlEncodedFormEntity(params));
            return new POSTResponse(client.execute(request));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
