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

package com.openexchange.ajax.subscribe.actions;

import java.util.List;
import java.util.Map;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class AllSubscriptionsRequest extends AbstractBulkSubscriptionRequest<AllSubscriptionsResponse> {

    private String folderID;

    public void setFolderID(String folderID) {
        this.folderID = folderID;
    }

    public String getFolderID() {
        return folderID;
    }

    public AllSubscriptionsRequest() {}

    public AllSubscriptionsRequest(String folder, List<String> columns) {
        this();
        setFolderID(folder);
        setColumns(columns);
    }

    public AllSubscriptionsRequest(String folder, List<String> columns, Map<String, List<String>> dynamicColumns) {
        this(folder, columns);
        setDynamicColumns(dynamicColumns);
    }

    public AllSubscriptionsRequest(List<String> columns) {
        this(null, columns);
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
        Params params = new Params(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL);
        if (getFolderID() != null) {
            params.add("folder", getFolderID());
        }

        if (getColumns() != null) {
            params.add(getColumnsAsParameter());
        }

        if (getDynamicColumns() != null) {
            params.add(getDynamicColumnsAsParameter());
        }

        return params.toArray();
    }

    @Override
    public AbstractAJAXParser<AllSubscriptionsResponse> getParser() {
        return new AbstractAJAXParser<AllSubscriptionsResponse>(getFailOnError()) {

            @Override
            protected AllSubscriptionsResponse createResponse(final Response response) throws JSONException {
                return new AllSubscriptionsResponse(response);
            }
        };
    }

}
