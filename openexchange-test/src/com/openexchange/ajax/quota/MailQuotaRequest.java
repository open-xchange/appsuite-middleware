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
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;

/**
 * {@link MailQuotaRequest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class MailQuotaRequest implements AJAXRequest<MailQuotaResponse> {

    private boolean failOnError;

    /**
     * Initializes a new {@link MailQuotaRequest}.
     */
    public MailQuotaRequest() {
        super();
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
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, "mail") };
    }

    @Override
    public AbstractAJAXParser<? extends MailQuotaResponse> getParser() {
        return new AbstractAJAXParser<MailQuotaResponse>(failOnError) {

            @Override
            protected MailQuotaResponse createResponse(Response response) throws JSONException {
                return new MailQuotaResponse(response);
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
