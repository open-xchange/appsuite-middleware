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

package com.openexchange.ajax.attach.actions;

import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link GetZippedDocumentsParser}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GetZippedDocumentsParser extends AbstractAJAXParser<GetZippedDocumentsResponse> {

    private int contentLength;
    private HttpResponse httpResponse;
    private String respString;

    /**
     * Initializes a new {@link GetZippedDocumentsParser}.
     */
    public GetZippedDocumentsParser(boolean failOnError) {
        super(failOnError);
    }

    @Override
    public GetZippedDocumentsResponse parse(final String body) throws JSONException {
        final boolean isJSON = body.startsWith("{");
        if (isJSON) {
            return super.parse(body);
        }
        JSONObject json = new JSONObject();
        json.put("document", body);
        return super.parse(json.toString());
    }

    @Override
    public String checkResponse(HttpResponse response, HttpRequest request) throws ParseException, IOException {
        httpResponse = response;
        Header[] headers = response.getAllHeaders();
        for (Header h : headers) {
            if (h.getName().equals("Content-Length")) {
                contentLength = Integer.parseInt(h.getValue());
                break;
            }
        }
        respString = EntityUtils.toString(response.getEntity());
        return respString;
    }

    @Override
    protected GetZippedDocumentsResponse createResponse(Response response) throws JSONException {
        return new GetZippedDocumentsResponse(httpResponse, response, contentLength, respString);
    }

}
