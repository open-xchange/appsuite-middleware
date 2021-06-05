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

package com.openexchange.ajax.contact.action;

import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.groupware.container.Contact;

/**
 * Stores parameters for the delete request.
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class DeleteRequest extends AbstractContactRequest<CommonDeleteResponse> {

    private final int folderId;

    private final int objectId;

    private final int[] objectIds;

    private final Date lastModified;

    private final boolean failOnError;

    public DeleteRequest(final int folderId, final int objectId, final Date lastModified, boolean failOnError) {
        super();
        this.folderId = folderId;
        this.objectId = objectId;
        this.objectIds = null;
        this.lastModified = lastModified;
        this.failOnError = failOnError;
    }

    public DeleteRequest(final int folderId, final int[] objectIds, final Date lastModified, boolean failOnError) {
        super();
        this.folderId = folderId;
        this.objectId = 0;
        this.objectIds = objectIds;
        this.lastModified = lastModified;
        this.failOnError = failOnError;
    }

    public DeleteRequest(final Contact contact, boolean failOnError) {
        this(contact.getParentFolderID(), contact.getObjectID(), contact.getLastModified(), failOnError);
    }

    public DeleteRequest(final int folderId, final int[] objectIds, final Date lastModified) {
        this(folderId, objectIds, lastModified, true);
    }

    public DeleteRequest(final int folderId, final int objectId, final Date lastModified) {
        this(folderId, objectId, lastModified, true);
    }

    public DeleteRequest(final Contact contact) {
        this(contact.getParentFolderID(), contact.getObjectID(), contact.getLastModified(), true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getBody() throws JSONException {
        if (objectIds == null) {
            JSONObject json = new JSONObject();
            json.put(DataFields.ID, objectId);
            json.put(AJAXServlet.PARAMETER_INFOLDER, folderId);
            return json;
        }
        JSONArray jsonArray = new JSONArray();
        for (final int id : objectIds) {
            JSONObject json = new JSONObject();
            json.put(DataFields.ID, id);
            json.put(AJAXServlet.PARAMETER_INFOLDER, folderId);
        }
        return jsonArray;
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
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE), new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, lastModified)
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeleteParser getParser() {
        return new DeleteParser(failOnError);
    }
}
