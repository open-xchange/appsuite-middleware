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

import static com.openexchange.ajax.framework.AJAXRequest.Method.PUT;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.attach.AttachmentTools;
import com.openexchange.groupware.container.CommonObject;

/**
 * {@link ListRequest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class ListRequest extends AbstractAttachmentRequest<ListResponse> {

    private int[] attachmentIds;
    private int[] columns;
    private TimeZone timezone;
    private int objectId;
    private int folderId;
    private int module;

    public ListRequest(int folderId, int objectId, int moduleId, int[] attachmentIds, int[] columns) {
        super();
        this.objectId = objectId;
        this.folderId = folderId;
        this.module = moduleId;
        this.attachmentIds = attachmentIds;
        this.columns = columns;
    }
    
    public ListRequest(CommonObject object, int[] attachmentIds, int[] columns, TimeZone timezone) {
        super();
        this.objectId = object.getObjectID();
        this.folderId = object.getParentFolderID();
        this.module = AttachmentTools.determineModule(object);
        this.attachmentIds = attachmentIds;
        this.columns = columns;
        this.timezone = timezone;
    }

    @Override
    public Method getMethod() {
        return PUT;
    }

    @Override
    public Parameter[] getParameters() {
        List<Parameter> params = new ArrayList<Parameter>();
        params.add(new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST));
        params.add(new URLParameter(AJAXServlet.PARAMETER_ATTACHEDID, objectId));
        params.add(new URLParameter(AJAXServlet.PARAMETER_FOLDERID, folderId));
        params.add(new URLParameter(AJAXServlet.PARAMETER_MODULE, module));
        params.add(new URLParameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        if (null != timezone) {
            params.add(new Parameter(AJAXServlet.PARAMETER_TIMEZONE, timezone.getID()));
        }
        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public ListParser getParser() {
        return new ListParser(columns);
    }

    @Override
    public Object getBody() {
        JSONArray array = new JSONArray();
        for (int attachmentId : attachmentIds) {
            array.put(attachmentId);
        }
        return array.toString();
    }
}
