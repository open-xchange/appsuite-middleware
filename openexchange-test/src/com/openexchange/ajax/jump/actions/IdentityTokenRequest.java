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

package com.openexchange.ajax.jump.actions;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link IdentityTokenRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class IdentityTokenRequest extends AbstractJumpRequest<IdentityTokenResponse> {

    private final String systemName;
    private final boolean failOnError;

    /**
     * Initializes a new {@link IdentityTokenRequest}.
     */
    public IdentityTokenRequest(final String systemName) {
        this(true, systemName);
    }

    /**
     * Initializes a new {@link IdentityTokenRequest}.
     */
    public IdentityTokenRequest(final boolean failOnError, final String systemName) {
        super();
        this.failOnError = failOnError;
        this.systemName = systemName;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return com.openexchange.ajax.framework.AJAXRequest.Method.GET;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        final List<Parameter> list = new LinkedList<Parameter>();
        list.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "identityToken"));
        list.add(new Parameter("system", systemName));
        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends IdentityTokenResponse> getParser() {
        return new IdentityTokenParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    private static class IdentityTokenParser extends AbstractAJAXParser<IdentityTokenResponse> {

        /**
         * Initializes a new {@link IdentityTokenParser}.
         */
        protected IdentityTokenParser(boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected IdentityTokenResponse createResponse(Response response) throws JSONException {
            final JSONObject jObject = (JSONObject) response.getData();
            return new IdentityTokenResponse(response, jObject.getString("token"));
        }
    }

}
