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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link GetZippedDocumentsRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GetZippedDocumentsRequest extends AbstractAttachmentRequest<GetZippedDocumentsResponse> {

    private final int objectId;
    private final int moduleID;
    private final int folderID;
    private final int[] attachmentIds;

    private final boolean failOnError;

    public GetZippedDocumentsRequest(int folder, int objectId, int module, int[] attachmentIds) {
        this(folder, objectId, module, attachmentIds, false);
    }

    public GetZippedDocumentsRequest(int folder, int objectId, int module, int[] attachmentIds, boolean lFailOnError) {
        super();
        this.objectId = objectId;
        this.moduleID = module;
        this.folderID = folder;
        this.failOnError = lFailOnError;
        this.attachmentIds = attachmentIds;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_ACTION, "zipDocuments"));
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_MODULE, moduleID));
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_FOLDERID, folderID));
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_ATTACHEDID, objectId));
        return parameters.toArray(new Parameter[parameters.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends GetZippedDocumentsResponse> getParser() {
        return new GetZippedDocumentsParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        JSONArray array = new JSONArray(attachmentIds.length);
        for (int attachmentId : attachmentIds) {
            array.put(attachmentId);
        }
        return array.toString();
    }
}
