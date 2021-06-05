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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.infostore.thirdparty.actions.AbstractFileRequest;

/**
 * 
 * {@link SaveAsRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class SaveAsRequest extends AbstractFileRequest<SaveAsResponse> {

    private final String folderId;
    private final int attached;
    private final int module;
    private final int attachment;
    private Map<String, String> body;

    public SaveAsRequest(final String folderId, final int attached, final int module, final int attachment, final Map<String, String> body) {
        super(false);
        this.folderId = folderId;
        this.attached = attached;
        this.module = module;
        this.attachment = attachment;
        this.body = body;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public JSONObject getBody() throws IOException, JSONException {
        return toJSONArgs(body);
    }

    @Override
    public SaveAsParser getParser() {
        return new SaveAsParser(failOnError);
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> params = new ArrayList<>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "saveAs"));
        params.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderId));
        params.add(new Parameter(AJAXServlet.PARAMETER_ATTACHEDID, this.attached));
        params.add(new Parameter(AJAXServlet.PARAMETER_MODULE, this.module));
        params.add(new Parameter(AJAXServlet.PARAMETER_ATTACHMENT, this.attachment));

        return params.toArray(new Parameter[params.size()]);
    }

    private JSONObject toJSONArgs(final Map<String, String> modified) throws JSONException {
        final JSONObject obj = new JSONObject();
        for (final String attr : modified.keySet()) {
            obj.put(attr, modified.get(attr));
        }
        return obj;
    }
}
