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
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link FileParser}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class FileParser extends AbstractAJAXParser<FileResponse> {

    private byte[] fileBytes;

    /**
     * Initializes a new {@link FileParser}.
     *
     * @param failOnError
     */
    public FileParser(boolean failOnError) {
        super(failOnError);
    }

    /**
     * @see com.openexchange.ajax.framework.AbstractAJAXParser#checkResponse(org.apache.http.HttpResponse)
     */
    @Override
    public String checkResponse(HttpResponse resp, HttpRequest request) throws ParseException, IOException {
        assertEquals("Response code is not okay.", HttpStatus.SC_OK, resp.getStatusLine().getStatusCode());
        HttpEntity entity = resp.getEntity();
        assertEquals("Response contained wrong mime type.", "application/octet-stream", ContentType.getOrDefault(entity).toString());
        fileBytes = EntityUtils.toByteArray(entity);

        return null;
    }

    /**
     * @see com.openexchange.ajax.framework.AbstractAJAXParser#createResponse(com.openexchange.ajax.container.Response)
     */
    @Override
    protected FileResponse createResponse(Response response) throws JSONException {
        return null;
    }

    /**
     * @see com.openexchange.ajax.framework.AbstractAJAXParser#parse(java.lang.String)
     */
    @Override
    public FileResponse parse(String body) throws JSONException {
        return new FileResponse(fileBytes);
    }

}
