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

package com.openexchange.ajax.drive.updater;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link UpdateXMLParser}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UpdateXMLParser extends AbstractAJAXParser<UpdateXMLResponse> {

    private String xml;

    /**
     * Initializes a new {@link UpdateXMLParser}.
     * 
     * @param failOnError
     */
    protected UpdateXMLParser(boolean failOnError) {
        super(failOnError);
    }

    @Override
    public String checkResponse(HttpResponse resp, HttpRequest request) throws ParseException, IOException {
        assertEquals("Response code is not okay.", HttpStatus.SC_OK, resp.getStatusLine().getStatusCode());
        HttpEntity entity = resp.getEntity();
        xml = EntityUtils.toString(entity);

        return null;
    }

    /**
     * @see com.openexchange.ajax.framework.AbstractAJAXParser#createResponse(com.openexchange.ajax.container.Response)
     */
    @Override
    protected UpdateXMLResponse createResponse(Response response) throws JSONException {
        return null;
    }

    /**
     * @see com.openexchange.ajax.framework.AbstractAJAXParser#parse(java.lang.String)
     */
    @Override
    public UpdateXMLResponse parse(String body) throws JSONException {
        return new UpdateXMLResponse(xml);
    }

}
