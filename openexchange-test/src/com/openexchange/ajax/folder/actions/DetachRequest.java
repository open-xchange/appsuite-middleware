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
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.infostore.thirdparty.actions.AbstractFileRequest;

/**
 * 
 * {@link DetachRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class DetachRequest extends AbstractFileRequest<DetachResponse> {

    private final String id;
    private final Date timestamp;
    private final int[] versions;

    public DetachRequest(String id, Date timestamp) {
        this(id, timestamp, null);
    }

    public DetachRequest(String id, Date timestamp, final int[] versions) {
        super(false);
        this.id = id;
        this.timestamp = timestamp;
        this.versions = versions;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public JSONArray getBody() throws IOException, JSONException {
        if (this.versions != null) {
            final StringBuffer data = new StringBuffer("[");

            if (versions.length > 0) {
                for (final int id : versions) {
                    data.append(id);
                    data.append(',');
                }
                data.deleteCharAt(data.length() - 1);
            }

            data.append(']');
            return new JSONArray(data.toString());
        }
        return null;
    }

    @Override
    public DetachParser getParser() {
        return new DetachParser(false);
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> params = new ArrayList<>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "detach"));
        if (this.id != null) {
            params.add(new Parameter("id", id));
        }
        if (this.timestamp != null) {
            params.add(new Parameter("timestamp", timestamp.getTime()));
        }
        return params.toArray(new Parameter[params.size()]);
    }
}
