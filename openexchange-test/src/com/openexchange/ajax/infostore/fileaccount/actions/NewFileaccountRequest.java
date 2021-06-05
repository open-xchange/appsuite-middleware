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

package com.openexchange.ajax.infostore.fileaccount.actions;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class NewFileaccountRequest extends AbstractFileaccountRequest<NewFileaccountResponse> {

    private final JSONObject body;

    /**
     * Initializes a new {@link NewFileaccountRequest}.
     */
    public NewFileaccountRequest(String displayName, String filestorageService, JSONObject configuration) {
        body = new JSONObject();
        try {
            body.put("filestorageService", filestorageService);
            body.put("displayName", displayName);
            body.put("configuration", configuration);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getBody() throws JSONException {
        return body;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() throws JSONException {
        List<Parameter> tmp = new ArrayList<Parameter>(3);
        tmp.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW));
        return tmp.toArray(new Parameter[tmp.size()]);
    }

    @Override
    public AbstractAJAXParser<NewFileaccountResponse> getParser() {
        return new NewFileaccountParser(getFailOnError());
    }

}
