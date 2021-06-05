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

package com.openexchange.ajax.oauth.actions;

import java.util.LinkedList;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.CommonDeleteResponse;

/**
 * @author <a href="mailto:markus.wagner@open-xchange.com">Markus Wagner</a>
 */
public class DeleteOAuthAccountRequest extends AbstractOAuthAccountRequest<CommonDeleteResponse> {

    private final int id;

    private boolean failOnError = false;

    public DeleteOAuthAccountRequest(final int id) {
        this.id = id;
    }

    public DeleteOAuthAccountRequest(final int id, final boolean failOnError) {
        this.id = id;
        this.failOnError = failOnError;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        LinkedList<Parameter> params = new LinkedList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE));
        params.add(new Parameter(AJAXServlet.PARAMETER_ID, id));
        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public DeleteOAuthAccountParser getParser() {
        return new DeleteOAuthAccountParser(failOnError);
    }

    @Override
    public Object getBody() {
        return new JSONObject();
    }

}
