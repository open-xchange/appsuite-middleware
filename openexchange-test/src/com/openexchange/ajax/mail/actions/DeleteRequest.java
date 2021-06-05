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
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.mail.TestMail;

/**
 * {@link DeleteRequest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DeleteRequest extends AbstractMailRequest<DeleteResponse> {

    private final String[][] folderAndMailIds;

    private final boolean hardDelete;

    private boolean failOnError = true;

    public DeleteRequest(String folder, String id, boolean hardDelete) {
        this(new String[][] { { folder, id } }, hardDelete);
    }

    public DeleteRequest(String[] folderAndMailId, boolean hardDelete) {
        this(new String[][] { folderAndMailId }, hardDelete);
    }

    public DeleteRequest(final String[][] folderAndMailIds) {
        this(folderAndMailIds, false);
    }

    public DeleteRequest(final String[][] folderAndMailIds, final boolean hardDelete) {
        this.folderAndMailIds = folderAndMailIds;
        this.hardDelete = hardDelete;
    }

    public DeleteRequest(final TestMail mail, final boolean hardDelete) {
        this.folderAndMailIds = new String[][] { mail.getFolderAndId() };
        this.hardDelete = hardDelete;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE), new Parameter("harddelete", hardDelete ? "1" : "0") };
    }

    @Override
    public AbstractAJAXParser<DeleteResponse> getParser() {
        return new AbstractAJAXParser<DeleteResponse>(failOnError) {

            @Override
            protected DeleteResponse createResponse(final Response response) {
                return new DeleteResponse(response);
            }
        };
    }

    @Override
    public Object getBody() throws JSONException {
        final JSONArray array = new JSONArray();
        for (final String[] folderAndObject : folderAndMailIds) {
            final JSONObject json = new JSONObject();
            json.put(AJAXServlet.PARAMETER_INFOLDER, folderAndObject[0]);
            json.put(DataFields.ID, folderAndObject[1]);
            array.put(json);
        }
        return array;
    }

    public DeleteRequest ignoreError() {
        failOnError = false;
        return this;
    }

    public DeleteRequest failOnError() {
        failOnError = true;
        return this;
    }
}
