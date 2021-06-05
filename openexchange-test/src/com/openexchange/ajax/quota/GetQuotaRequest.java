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

package com.openexchange.ajax.quota;

import java.io.IOException;
import java.util.LinkedList;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;

/**
 * {@link GetQuotaRequest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GetQuotaRequest implements AJAXRequest<GetQuotaResponse> {

    private final String module;
    private final String account;
    private boolean failOnError;

    /**
     * Initializes a new {@link GetQuotaRequest}.
     *
     * @param module The module identifier, or <code>null</code> if not defined
     * @param account The account identifier, or <code>null</code> if not defined
     */
    public GetQuotaRequest(String module, String account) {
        super();
        this.module = module;
        this.account = account;
        this.failOnError = true;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public String getServletPath() {
        return "/ajax/quota";
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        LinkedList<Parameter> parameters = new LinkedList<Parameter>();
        parameters.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET));
        if (null != module) {
            parameters.add(new Parameter("module", module));
        }
        if (null != account) {
            parameters.add(new Parameter("account", account));
        }
        return parameters.toArray(new Parameter[parameters.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends GetQuotaResponse> getParser() {
        return new AbstractAJAXParser<GetQuotaResponse>(failOnError) {

            @Override
            protected GetQuotaResponse createResponse(Response response) throws JSONException {
                return new GetQuotaResponse(response);
            }
        };
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    /**
     * Gets the failOnError
     *
     * @return The failOnError
     */
    public boolean isFailOnError() {
        return failOnError;
    }

    /**
     * Sets the failOnError
     *
     * @param failOnError The failOnError to set
     */
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

}
