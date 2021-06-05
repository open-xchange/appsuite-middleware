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

package com.openexchange.modules.json.actions;

import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.modules.json.ModelParser;
import com.openexchange.modules.model.Attribute;
import com.openexchange.modules.model.Model;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link RequestPrototype}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RequestPrototype<T extends Model<T>> {

    protected ModelParser<T> parser;

    protected AJAXRequestData req;

    protected List<Attribute<T>> jsonFields;

    private List<Attribute<T>> fields;

    private T body;

    protected ServerSession session;

    public RequestPrototype(AJAXRequestData req, ModelParser<T> parser, ServerSession session) {
        this.req = req;
        this.parser = parser;
        this.session = session;
    }

    public boolean require(String... params) throws OXException {
        List<String> missingParameters = req.getMissingParameters(params);
        if (missingParameters.isEmpty()) {
            return true;
        }

        throw AjaxExceptionCodes.MISSING_PARAMETER.create( missingParameters.toString());
    }

    public T getBody() throws JSONException {
        if (body != null) {
            return body;
        }
        JSONObject data = (JSONObject) req.getData();
        if (data == null) {
            return null;
        }

        if (jsonFields == null) {
            this.fields = parser.getFields(data);
            return body = parser.parse(data);
        }

        this.fields = parser.getFields(data, jsonFields);
        return body = parser.parse(data, jsonFields);
    }

    protected void setJsonFields(List<Attribute<T>> jsonFields) {
        this.jsonFields = jsonFields;
    }

    public List<Attribute<T>> getFields() {
        return fields;
    }

    public AJAXRequestData getRequestData() {
        return req;
    }

    public String getParameter(String name) {
        return req.getParameter(name);
    }

    public Iterator<String> getParameterNames() {
        return req.getParameterNames();
    }

    public ServerSession getSession() {
        return session;
    }
}
