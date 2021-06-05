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

package com.openexchange.ajax.passwordchange.actions;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.writer.ResponseWriter;

/**
 * {@link PasswordChangeScriptResultRequest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.4
 */
public class PasswordChangeScriptResultRequest implements AJAXRequest<PasswordChangeScriptResultResponse> {

    private final boolean failOnError;

    public PasswordChangeScriptResultRequest() {
        this(true);
    }

    public PasswordChangeScriptResultRequest(boolean failOnError) {
        super();
        this.failOnError = failOnError;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public String getServletPath() {
        return "/ajax/logintest";
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, "pwdchangeresult") };
    }

    @Override
    public PasswordParser getParser() {
        return new PasswordParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    private static final class PasswordParser extends AbstractAJAXParser<PasswordChangeScriptResultResponse> {

        PasswordParser(final boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected PasswordChangeScriptResultResponse createResponse(final Response response) throws JSONException {
            PasswordChangeScriptResultResponse pwdResultResponse = new PasswordChangeScriptResultResponse(response);
            JSONObject json = ResponseWriter.getJSON(response);
            if (json.has(ResponseFields.DATA)) {
                JSONObject pwData = (JSONObject) json.get(ResponseFields.DATA);
                pwdResultResponse.setPassword(pwData.optString("password"));
            }
            return pwdResultResponse;
        }
    }

}
