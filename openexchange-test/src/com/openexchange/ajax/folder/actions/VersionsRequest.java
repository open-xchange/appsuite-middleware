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
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.infostore.thirdparty.actions.AbstractFileRequest;
import com.openexchange.groupware.search.Order;

/**
 * 
 * {@link VersionsRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class VersionsRequest extends AbstractFileRequest<VersionsResponse> {

    private final String folderId;
    private final int[] columns;
    private final int sort;
    private final Order order;

    public VersionsRequest(String folderId, int[] fields) {
        this(folderId, fields, -1, null);
    }

    public VersionsRequest(String folderId, int[] fields, int sort, Order order) {
        super(true);
        this.sort = sort;
        this.order = order;
        this.folderId = folderId;
        this.columns = fields;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    @Override
    public VersionsParser getParser() {
        return new VersionsParser(true, this.columns);
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> params = new ArrayList<>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "versions"));
        if (this.folderId != null) {
            params.add(new Parameter("id", folderId));
        }
        if ((this.columns != null) && (this.columns.length > 0)) {
            String colsArray2String = com.openexchange.test.common.tools.URLParameter.colsArray2String(this.columns);
            params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, colsArray2String));
        }
        if (this.sort != -1) {
            params.add(new Parameter("sort", this.sort));
        }
        if (this.order != null) {
            params.add(new Parameter("order", OrderFields.write(this.order)));
        }
        return params.toArray(new Parameter[params.size()]);
    }
}
