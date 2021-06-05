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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public final class POSTResponse extends AbstractResponse {

    private final URI redirectLocation;

    POSTResponse(HttpResponse response) throws IOException {
        super(response);
        String location = getHeader(HttpHeaders.LOCATION);
        if (location == null) {
            redirectLocation = null;
        } else {
            redirectLocation = URI.create(location);
        }
    }

    public URI getRedirectLocation() {
        return redirectLocation;
    }

    public void assertRedirect() {
        assertStatus(HttpServletResponse.SC_FOUND);
        assertNotNull("Location header was missing in response", redirectLocation);
    }

    public GETResponse followRedirect(HttpClient client) throws IOException {
        assertRedirect();
        try {
            URI location = getRedirectLocation();
            Map<String, String> params = HttpTools.extractQueryParams(location);
            GETRequest getRequest = new GETRequest().setScheme(location.getScheme()).setHostname(location.getHost()).setPort(location.getPort());
            for (String param : params.keySet()) {
                getRequest.setParameter(param, params.get(param));
            }

            return getRequest.execute(client);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
