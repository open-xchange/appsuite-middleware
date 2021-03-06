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
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.infostore.thirdparty.actions.AbstractFileRequest;

/**
 *
 * {@link LockRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class LockRequest extends AbstractFileRequest<LockResponse> {

    private final String id;
    private final Long timeDiff;

    public LockRequest(final String id) {
        this(id, null);
    }

    public LockRequest(final String id, final Long timeDiff) {
        super(false);
        this.id = id;
        this.timeDiff = timeDiff;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public JSONObject getBody() throws IOException, JSONException {
        return null;
    }

    @Override
    public LockParser getParser() {
        return new LockParser(failOnError);
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> params = new ArrayList<>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "lock"));
        params.add(new Parameter("id", id));
        if (timeDiff != null) {
            params.add(new Parameter("diff", timeDiff.longValue()));
        }

        return params.toArray(new Parameter[params.size()]);
    }
}
