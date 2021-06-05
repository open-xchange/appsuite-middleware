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

package com.openexchange.ajax.userfeedback.actions;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.framework.Params;

/**
 * 
 * {@link StoreRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class StoreRequest implements AJAXRequest<StoreResponse> {

    private static final String STORE_PATH = "/ajax/userfeedback";

    private boolean failOnError = false;

    private String type;

    private String feedback;

    public StoreRequest() {
        this("default");
    }

    public StoreRequest(String type) {
        this(type, null);
    }

    public StoreRequest(String type, String feedback) {
        super();
        this.type = type;
        this.feedback = feedback;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        Params params = new Params(AJAXServlet.PARAMETER_ACTION, "store");
        if (type != null) {
            params.add(new Parameter("type", type));
        }
        return params.toArray();
    }

    @Override
    public AbstractAJAXParser<? extends StoreResponse> getParser() {
        return new StoreParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        if (feedback == null) {
            return null;
        }
        return new JSONObject(feedback);
    }

    @Override
    public String getServletPath() {
        return STORE_PATH;
    }
}
