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

package com.openexchange.ajax.group.actions;

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.group.Group;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class DeleteRequest extends AbstractGroupRequest<DeleteResponse> {

    private final int groupId;

    private final Date lastModified;

    private final boolean failOnError;

    public DeleteRequest(final int groupId, final Date lastModified, final boolean failOnError) {
        super();
        this.groupId = groupId;
        this.lastModified = lastModified;
        this.failOnError = failOnError;
    }

    public DeleteRequest(final int groupId, final Date lastModified) {
        this(groupId, lastModified, true);
    }

    public DeleteRequest(Group group, boolean failOnError) {
        this(group.getIdentifier(), group.getLastModified(), failOnError);
    }

    public DeleteRequest(Group group) {
        this(group, true);
    }

    @Override
    public Object getBody() throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(DataFields.ID, groupId);
        return json;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE), new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, lastModified)
        };
    }

    @Override
    public DeleteParser getParser() {
        return new DeleteParser(failOnError);
    }
}
