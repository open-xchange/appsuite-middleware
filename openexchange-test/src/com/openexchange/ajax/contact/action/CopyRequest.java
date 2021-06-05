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

import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link CopyRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class CopyRequest extends AbstractContactRequest<CopyResponse> {

    private final int sourceFolderID;
    private final int destinationFolderID;
    private final int contactID;
    private final boolean failOnError;

    public CopyRequest(int contactID, int sourceFolderID, int destinationFolderID, boolean failOnError) {
        super();
        this.contactID = contactID;
        this.sourceFolderID = sourceFolderID;
        this.destinationFolderID = destinationFolderID;
        this.failOnError = failOnError;
    }

    @Override
    public Object getBody() throws JSONException {
        JSONObject jso = new JSONObject();
        jso.put("folder_id", destinationFolderID);
        return jso;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        List<Parameter> list = new LinkedList<Parameter>();

        list.add(new Parameter("action", "copy"));
        list.add(new Parameter("folder", sourceFolderID));
        list.add(new Parameter("id", contactID));

        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public CopyParser getParser() {
        return new CopyParser(failOnError);
    }
}
