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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;

/**
 * 
 * {@link DetachRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class DetachRequest extends AbstractAttachmentRequest<DetachResponse> {

    private final int moduleId;

    private final int folderId;

    private final int attachedId;

    private final int[] versions;

    public DetachRequest(final int folder, final int attached, final int module, final int[] versions) {
        super();
        this.moduleId = module;
        this.versions = versions;
        this.folderId = folder;
        this.attachedId = attached;
    }

    @Override
    public JSONArray getBody() throws JSONException {
        if (versions != null) {
            final StringBuffer data = new StringBuffer("[");
            for (final int id : versions) {
                data.append(id);
                data.append(',');
            }
            data.setLength(data.length() - 1);
            data.append(']');
            return new JSONArray(data.toString());
        }
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() throws JSONException {
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DETACH));
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_MODULE, moduleId));
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_FOLDERID, folderId));
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_ATTACHEDID, attachedId));

        return parameters.toArray(new Parameter[parameters.size()]);
    }

    @Override
    public DetachParser getParser() {
        return new DetachParser(true);
    }
}
