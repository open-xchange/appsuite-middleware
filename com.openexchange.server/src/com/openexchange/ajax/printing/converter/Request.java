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

package com.openexchange.ajax.printing.converter;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXState;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.Dispatchers;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.templating.TemplateErrorMessage;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * {@link Request}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Request {
    private final Dispatcher dispatcher;
    private final ServerSession session;

    private final boolean trusted;

    private String action;
    private String module;

    private final Map<String, String> parameters = new HashMap<String, String>();

    private Object body;

    public Request(Dispatcher dispatcher, ServerSession session, boolean trusted) {
        this.dispatcher = dispatcher;
        this.session = session;
        this.trusted = trusted;
    }

    public Request action(String action) {
        this.action = action;
        return this;
    }

    public Request module(String module) {
        this.module = module;
        return this;
    }

    public Request param(String name, Object value) {
        this.parameters.put(name, value.toString());
        return this;
    }

    public Request body(Object body) {
        this.body = body;
        return this;
    }

    public Object perform() throws OXException {
        if (!trusted) {
            throw TemplateErrorMessage.AccessDenied.create();
        }

        AJAXRequestData req = new AJAXRequestData();

        req.setHostname("localhost");
        req.setAction(action);
        req.setServletRequestURI("");

        for(Map.Entry<String, String> param : parameters.entrySet()) {
            req.putParameter(param.getKey(), param.getValue());
        }
        req.setModule(module);

        try {
            req.setData(JSONCoercion.coerceToJSON(body));
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.toString());
        }

        AJAXState state = null;
        AJAXRequestResult result = null;
        Exception exc = null;
        try {
            state = dispatcher.begin();
            req.setFormat("native");
            result = dispatcher.perform(req, state, ServerSessionAdapter.valueOf(session));
            return result.getResultObject();
        } catch (OXException e) {
            exc = e;
            throw e;
        } catch (RuntimeException e) {
            exc = e;
            throw OXException.general(e.getMessage(), e);
        } finally {
            Dispatchers.signalDone(result, exc);
            dispatcher.end(state);
        }
    }
}
