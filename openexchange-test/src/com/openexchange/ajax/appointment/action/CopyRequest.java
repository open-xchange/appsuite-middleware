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

package com.openexchange.ajax.appointment.action;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * 
 * {@link CopyRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class CopyRequest extends AbstractAppointmentRequest<CopyResponse> {

    private final int folderId;
    private final int objectId;
    private final boolean ignoreConlicts;
    private JSONObject body;

    public CopyRequest(final int folderId, final int objectId, JSONObject body, boolean ignoreConlicts) {
        this.folderId = folderId;
        this.objectId = objectId;
        this.ignoreConlicts = ignoreConlicts;
        this.body = body;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject getBody() throws JSONException {
        return body;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Parameter[] getParameters() {
         Parameter[] params = new Parameter[] { 
            new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_COPY), 
            new URLParameter(AJAXServlet.PARAMETER_FOLDERID, String.valueOf(folderId)),
            new URLParameter(AJAXServlet.PARAMETER_ID, objectId),
            new URLParameter(AppointmentFields.IGNORE_CONFLICTS, ignoreConlicts)
        };
        return params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractAJAXParser<CopyResponse> getParser() {
        return new CopyParser(false);
    }
}
