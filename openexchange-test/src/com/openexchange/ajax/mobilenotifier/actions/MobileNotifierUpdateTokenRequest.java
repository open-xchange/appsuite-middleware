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

package com.openexchange.ajax.mobilenotifier.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.AbstractAJAXResponse;

/**
 * {@link MobileNotifierUpdateTokenRequest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class MobileNotifierUpdateTokenRequest extends AbstractMobileNotifierRequest<AbstractAJAXResponse> {

    private boolean failOnError;
    private String serviceId;
    private String token;
    private String newToken;

    /**
     * Initializes a new {@link MobileNotifierUpdateTokenRequest}.
     */
    public MobileNotifierUpdateTokenRequest(String serviceId, String token, String newToken, boolean failOnError) {
        super();
        this.serviceId = serviceId;
        this.token = token;
        this.newToken = newToken;
        this.failOnError = failOnError;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        final List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "updateToken"));
        params.add(new Parameter("serviceId", serviceId));
        params.add(new Parameter("token", token));
        params.add(new Parameter("newToken", newToken));
        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends AbstractAJAXResponse> getParser() {
        return new MobileNotifierUpdateTokenParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }
}
