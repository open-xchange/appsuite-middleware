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

package com.openexchange.ajax.onboarding.actions;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.framework.Params;

/**
 * {@link ExecuteRequest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class ExecuteRequest implements AJAXRequest<OnboardingTestResponse> {

    private final String id;
    private final String actionId;
    private final JSONObject body;
    private final boolean failOnError;

    public ExecuteRequest(String id, String actionId) {
        this(id, actionId, null, true);
    }

    public ExecuteRequest(String id, String actionId, JSONObject body) {
        this(id, actionId, body, true);
    }

    public ExecuteRequest(String id, String actionId, JSONObject body, boolean failOnError) {
        this.id = id;
        this.actionId = actionId;
        this.body = body;
        this.failOnError = failOnError;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.PUT;
    }

    @Override
    public String getServletPath() {
        return "/ajax/onboarding";
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        return new Params(AJAXServlet.PARAMETER_ACTION, "execute", AJAXServlet.PARAMETER_ID, id, "action_id", actionId).toArray();
    }

    @Override
    public AbstractAJAXParser<? extends OnboardingTestResponse> getParser() {
        return new Parser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return body;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    private static final class Parser extends AbstractAJAXParser<OnboardingTestResponse> {

        public Parser(boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected OnboardingTestResponse createResponse(Response response) throws JSONException {
            return new OnboardingTestResponse(response);
        }

    }

}
