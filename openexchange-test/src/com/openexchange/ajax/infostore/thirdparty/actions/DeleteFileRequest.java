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

package com.openexchange.ajax.infostore.thirdparty.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link DeleteFileRequest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class DeleteFileRequest extends AbstractFileRequest<DeleteFileResponse> {

    private final String fileId;
    private final String folderId;

    long futureTimestamp;

    /**
     *
     * Initializes a new {@link DeleteFileRequest}.
     * 
     * @param fileId
     * @param folderId
     */
    public DeleteFileRequest(String fileId, final String folderId) {
        super(true);
        this.fileId = fileId;
        this.folderId = folderId;
        Date date = new Date();
        this.futureTimestamp = date.getTime() + 10000L;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> list = new ArrayList<Parameter>();
        list.add(new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE));
        list.add(new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, futureTimestamp));
        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends DeleteFileResponse> getParser() {
        return new DeleteFileParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        JSONArray jsonArr = new JSONArray();
        JSONObject body = new JSONObject();
        body.put("id", fileId);
        body.put("folder", folderId);
        jsonArr.put(body);
        return jsonArr;
    }
}
