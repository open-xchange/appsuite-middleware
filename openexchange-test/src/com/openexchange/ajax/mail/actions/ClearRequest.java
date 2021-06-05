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

package com.openexchange.ajax.mail.actions;

import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link ClearRequest}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 *
 */
public class ClearRequest extends AbstractMailRequest<ClearResponse> {

    private final String[] folderIds;
    private boolean hardDelete = false;
    private boolean failOnError = true;

    public ClearRequest(final String folderId) {
        this.folderIds = new String[] { folderId };
    }

    public ClearRequest(final String[] folderIds) {
        this.folderIds = folderIds;
    }

    public ClearRequest(final String[] folderIds, final boolean failOnError) {
        this.folderIds = folderIds;
        this.failOnError = failOnError;
    }

    @Override
    public Object getBody() throws JSONException {
        final JSONArray array = new JSONArray();
        for (final String folderId : folderIds) {
            array.put(folderId);
        }
        return array;
    }

    /**
     * Sets the hard delete flag
     *
     * @param hardDelete The hard delete flag to set
     */
    public ClearRequest setHardDelete(final boolean hardDelete) {
        this.hardDelete = hardDelete;
        return this;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_CLEAR), new Parameter(AJAXServlet.PARAMETER_HARDDELETE, hardDelete) };
    }

    @Override
    public AbstractAJAXParser<ClearResponse> getParser() {
        return new AbstractAJAXParser<ClearResponse>(failOnError) {

            @Override
            protected ClearResponse createResponse(final Response response) throws JSONException {
                return new ClearResponse(response);
            }
        };
    }

}
