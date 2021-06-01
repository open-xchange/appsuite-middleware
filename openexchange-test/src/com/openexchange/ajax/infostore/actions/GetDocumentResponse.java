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

package com.openexchange.ajax.infostore.actions;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import com.openexchange.ajax.framework.AbstractAJAXResponse;

/**
 * {@link GetDocumentResponse}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GetDocumentResponse extends AbstractAJAXResponse {

    private final HttpResponse response;
    private final HttpEntity entity;

    public GetDocumentResponse(HttpResponse response) {
        super(null);
        this.response = response;
        this.entity = response.getEntity();
    }

    public int getStatusCode() {
        return response.getStatusLine().getStatusCode();
    }

    public String getContentType() {
        return entity.getContentType().getValue();
    }

    public InputStream getContent() throws IllegalStateException, IOException {
        return null != entity ? entity.getContent() : null;
    }

    public byte[] getContentAsByteArray() throws IOException {
        return null != entity ? EntityUtils.toByteArray(entity) : null;
    }

    public long getContentLength() {
        return null != entity ? entity.getContentLength() : 0L;
    }

    @Override
    public Object getData() {
        try {
            return getContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasError() {
        return HttpServletResponse.SC_OK != getStatusCode();
    }

    public HttpResponse getHttpResponse() {
        return response;
    }

}
