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
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;

/**
 * {@link GetDocumentRequest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GetDocumentRequest extends AbstractInfostoreRequest<GetDocumentResponse> {

    private final String id;
    private final String folder;
    private final String version;
    private Parameter[] additionalParameters;
    private String mimeType;

    public GetDocumentRequest(String folder, String id, String version) {
        this(folder, id, version, null);
    }
        
    public GetDocumentRequest(String folder, String id, String version, String mimeType) {
        super();
        this.folder = folder;
        this.id = id;
        this.version = version;
        this.mimeType = mimeType;
    }

    public GetDocumentRequest(String folder, String id) {
        this(folder, id, null);
    }

    public void setAdditionalParameters(Parameter... additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    @Override
    public Object getBody() throws JSONException {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() {
        Params params = new Params(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DOCUMENT, AJAXServlet.PARAMETER_ID, id, AJAXServlet.PARAMETER_FOLDERID, folder);
        if (null != version && !version.equalsIgnoreCase("-1")) {
            params.add(AJAXServlet.PARAMETER_VERSION, version);
        }
        if (null != additionalParameters) {
            params.add(additionalParameters);
        }
        if (mimeType != null) {
            params.add(AJAXServlet.PARAMETER_CONTENT_TYPE, this.mimeType);
        }
        return params.toArray();
    }

    @Override
    public AbstractAJAXParser<? extends GetDocumentResponse> getParser() {
        return new GetDocumentParser(getFailOnError());
    }

    private static class GetDocumentParser extends AbstractAJAXParser<GetDocumentResponse> {

        private HttpResponse httpResponse;
        private int statusCode;
        private String reasonPhrase;
        private final boolean failOnError;

        protected GetDocumentParser(boolean failOnError) {
            super(failOnError);
            this.failOnError = failOnError;
        }

        @Override
        protected GetDocumentResponse createResponse(Response response) throws JSONException {
            return new GetDocumentResponse(httpResponse);
        }

        @Override
        protected Response getResponse(String body) throws JSONException {
            throw new JSONException("Method not supported when parsing redirect responses.");
        }

        @Override
        public String checkResponse(HttpResponse resp, HttpRequest request) throws ParseException, IOException {
            statusCode = resp.getStatusLine().getStatusCode();
            reasonPhrase = resp.getStatusLine().getReasonPhrase();
            httpResponse = resp;
            if (failOnError) {
                assertEquals("Response code is not okay.", HttpServletResponse.SC_OK, statusCode);
            } else {
                if (statusCode >= HttpServletResponse.SC_BAD_REQUEST) {
                    return Integer.toString(statusCode) + (null == reasonPhrase ? "" : reasonPhrase);
                }
            }
            return null;
        }

        @Override
        public GetDocumentResponse parse(String body) throws JSONException {
            return new GetDocumentResponse(httpResponse);
        }

    }

}
