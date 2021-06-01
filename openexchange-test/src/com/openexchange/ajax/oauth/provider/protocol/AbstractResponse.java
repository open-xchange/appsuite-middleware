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

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import com.openexchange.java.Charsets;

abstract class AbstractResponse {

    protected final Map<String, String> headers = new HashMap<>();
    protected final int statusCode;
    protected final byte[] body;

    protected AbstractResponse(HttpResponse response) throws IOException {
        super();
        statusCode = response.getStatusLine().getStatusCode();
        HeaderIterator hit = response.headerIterator();
        while (hit.hasNext()) {
            // The last value of a header always wins
            Header header = hit.nextHeader();
            headers.put(header.getName().toLowerCase(), header.getValue());
        }

        HttpEntity entity = response.getEntity();
        if (entity == null) {
            body = null;
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            entity.writeTo(baos);
            body = baos.toByteArray();
        }
    }

    public void assertStatus(int statusCode) {
        assertEquals("Unexpected status code", statusCode, this.statusCode);
    }

    public void assertOK() {
        assertStatus(HttpServletResponse.SC_OK);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    public boolean containsHeader(String name) {
        return headers.containsKey(name.toLowerCase());
    }

    public byte[] getBody() {
        return body;
    }

    public String getBodyAsString() {
        return new String(body, Charsets.UTF_8);
    }

}
