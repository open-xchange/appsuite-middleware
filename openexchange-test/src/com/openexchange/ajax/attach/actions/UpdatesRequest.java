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

import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Attachment;

/**
 * 
 * {@link UpdatesRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class UpdatesRequest extends AbstractAttachmentRequest<UpdatesResponse> {

    private int folderId;
    private int objectId;
    private int moduleId;
    private long timestamp;
    private int[] columns;

    public UpdatesRequest(int folderId, int objectId, int moduleId, int[] columns, long timestamp) {
        super();
        this.folderId = folderId;
        this.objectId = objectId;
        this.moduleId = moduleId;
        this.columns = columns;
        this.timestamp = timestamp;
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() throws JSONException {
        return new Parameter[] { 
            new URLParameter(AJAXServlet.PARAMETER_ACTION, Attachment.ACTION_UPDATES),
            new URLParameter(AJAXServlet.PARAMETER_MODULE, moduleId),
            new URLParameter(AJAXServlet.PARAMETER_FOLDERID, folderId),
            new URLParameter(AJAXServlet.PARAMETER_ATTACHEDID, objectId),
            new URLParameter(AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(timestamp)),
            new URLParameter(AJAXServlet.PARAMETER_COLUMNS, columns),
        };
    }

    @Override
    public UpdatesParser getParser() {
        return new UpdatesParser(false);
    }
}
