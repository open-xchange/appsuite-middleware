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

package com.openexchange.ajax.attach.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.attach.AttachmentTools;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.search.Order;

/**
 * {@link AllRequest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AllRequest extends AbstractAttachmentRequest<AllResponse> {

    private final int folderId;
    private final int attachedId;
    private final int moduleId;
    private final int[] columns;
    private final int sort;
    private final Order order;
    private final boolean failOnError;

    public AllRequest(final CommonObject obj, final int[] columns, final int sort, final Order order) {
        this(obj, columns, sort, order, true);
    }

    public AllRequest(final CommonObject obj, final int[] columns, final int sort, final Order order, boolean failOnError) {
        this(obj.getParentFolderID(), obj.getObjectID(), AttachmentTools.determineModule(obj), columns, sort, order, failOnError);
    }

    public AllRequest(final CommonObject obj, final int[] columns) {
        this(obj, columns, true);
    }

    public AllRequest(final CommonObject obj, final int[] columns, boolean failOnError) {
        this(obj, columns, -1, null, failOnError);
    }

    public AllRequest(final int folderId, final int attachedId, final int moduleId, final int[] columns, final int sort, final Order order) {
        this(folderId, attachedId, moduleId, columns, sort, order, false);
    }
    public AllRequest(final int folderId, final int attachedId, final int moduleId, final int[] columns, final int sort, final Order order, boolean failOnError) {
        this.folderId = folderId;
        this.attachedId = attachedId;
        this.moduleId = moduleId;
        this.columns = columns;
        this.sort = sort;
        this.order = order;
        this.failOnError = failOnError;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL));
        parameters.add(new Parameter(AJAXServlet.PARAMETER_ATTACHEDID, attachedId));
        parameters.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderId));
        parameters.add(new Parameter(AJAXServlet.PARAMETER_MODULE, moduleId));
        parameters.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));

        if (sort > 0) {
            parameters.add(new Parameter(AJAXServlet.PARAMETER_SORT, sort));
            parameters.add(new Parameter(AJAXServlet.PARAMETER_ORDER, OrderFields.write(order)));
        }

        return parameters.toArray(new Parameter[parameters.size()]);
    }

    @Override
    public AllParser getParser() {
        return new AllParser(failOnError, columns);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

}
