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

package com.openexchange.ajax.folder.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.Header;

/**
 * {@link GetRequestNew}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class GetRequestNew implements AJAXRequest<GetResponseNew> {

    private final boolean failOnError;

    private final API api;

    private final String folderId;

    private final int[] columns;

    public GetRequestNew(API api, String folderId, int[] columns) {
        this(api, folderId, columns, true);
    }

    public GetRequestNew(API api, String folderId, int[] columns, boolean failOnError) {
        super();
        this.api = api;
        this.folderId = folderId;
        this.columns = columns;
        this.failOnError = failOnError;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public String getServletPath() {
        return api.getUrl();
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET));
        params.add(new Parameter(AJAXServlet.PARAMETER_ID, folderId));
        params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        if (api.getTreeId() != -1) {
            params.add(new Parameter("tree", api.getTreeId()));
        }

        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public GetParserNew getParser() {
        return new GetParserNew(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

}
