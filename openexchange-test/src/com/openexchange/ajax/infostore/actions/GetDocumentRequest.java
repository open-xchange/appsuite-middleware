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
 *    trademarks of the OX Software GmbH group of companies.
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

    public GetDocumentRequest(String folder, String id, String version) {
        super();
        this.folder = folder;
        this.id = id;
        this.version = version;
    }

    public GetDocumentRequest(String folder, String id) {
        this(folder, id, null);
    }

    public void setAdditionalParameters(Parameter...additionalParameters) {
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
        Params params = new Params(
            AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DOCUMENT,
            AJAXServlet.PARAMETER_ID, id,
            AJAXServlet.PARAMETER_FOLDERID, folder
        );
        if (null != version) {
            params.add(AJAXServlet.PARAMETER_VERSION, version);
        }
        if (null != additionalParameters) {
            params.add(additionalParameters);
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
