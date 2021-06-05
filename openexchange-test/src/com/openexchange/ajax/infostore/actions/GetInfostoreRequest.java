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

package com.openexchange.ajax.infostore.actions;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class GetInfostoreRequest extends AbstractInfostoreRequest<GetInfostoreResponse> {

    private String id;
    private int[] columns;
    private int version;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public GetInfostoreRequest() {
        super();
    }

    public GetInfostoreRequest(String id) {
        setId(id);
    }

    public GetInfostoreRequest(String id, int... columns) {
        this(id, -1, columns);
    }

    public GetInfostoreRequest(String id, int version, int... columns) {
        setId(id);
        this.columns = columns;
        this.version = version;
    }

    @Override
    public Object getBody() throws JSONException {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() {
        if (null == columns || columns.length == 0) {
            return new Params(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET, AJAXServlet.PARAMETER_ID, String.valueOf(getId())).toArray();
        }
        final List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_ID, String.valueOf(getId())));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        if (version != -1) {
            parameterList.add(new Parameter(AJAXServlet.PARAMETER_VERSION, version));
        }
        return parameterList.toArray(new Parameter[parameterList.size()]);
    }

    @Override
    public AbstractAJAXParser<GetInfostoreResponse> getParser() {
        return new AbstractAJAXParser<GetInfostoreResponse>(getFailOnError()) {

            @Override
            protected GetInfostoreResponse createResponse(final Response response) throws JSONException {
                return new GetInfostoreResponse(response);
            }
        };
    }

}
