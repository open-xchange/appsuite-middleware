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

import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.util.Map;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import com.openexchange.ajax.oauth.provider.EndpointTest;
import com.openexchange.java.Charsets;

public final class GETResponse extends AbstractResponse {

    private final GETRequest request;

    GETResponse(GETRequest request, HttpResponse loginPageResponse) throws IOException {
        super(loginPageResponse);
        this.request = request;
    }

    public POSTRequest preparePOSTRequest() {
        assertOK();
        assertNotNull(body);
        Map<String, String> hiddenFormFields = HttpTools.getHiddenFormFields(new String(body, Charsets.UTF_8));
        POSTRequest postRequest = new POSTRequest().setScheme(request.scheme).setHostname(request.hostname).setPort(request.port);
        for (String param : hiddenFormFields.keySet()) {
            String value = hiddenFormFields.get(param);
            if (param != null && value != null) {
                postRequest.setParameter(param, value);
            }
        }

        postRequest.setHeader(HttpHeaders.REFERER, request.scheme + "://" + request.hostname + EndpointTest.AUTHORIZATION_ENDPOINT);
        return postRequest;
    }

}
