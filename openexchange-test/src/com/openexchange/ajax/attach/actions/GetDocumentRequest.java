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
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link GetDocumentRequest}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GetDocumentRequest extends AbstractAttachmentRequest<GetDocumentResponse> {

    private final int objectId;

    private final int moduleID;

    private final int folderID;

    private boolean failOnError;

    private String contentType;

    private final int attachmentID;

    private int off;

    private int len;

    public GetDocumentRequest(int folder, int objectId, int module, int attachment) {
        this(folder, objectId, module, attachment, null);
    }

    public GetDocumentRequest(int folder, int objectId, int module, int attachment, String lContentType) {
        this(folder, objectId, module, attachment, lContentType, -1, -1, false);
    }
    
    /**
     * Initializes a new {@link GetDocumentRequest}.
     */
    public GetDocumentRequest(int folder, int objectId, int module, int attachment, String lContentType, int off, int len, boolean lFailOnError) {
        super();
        this.objectId = objectId;
        this.moduleID = module;
        this.folderID = folder;
        this.failOnError = lFailOnError;
        this.contentType = lContentType;
        this.attachmentID = attachment;
        this.off = off;
        this.len = len;
    }
    
    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DOCUMENT));
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_MODULE, moduleID));
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_FOLDERID, folderID));
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_ATTACHEDID, attachmentID));
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_ID, objectId));
        if (contentType != null) {
            parameters.add(new URLParameter(AJAXServlet.PARAMETER_CONTENT_TYPE, contentType));
        }
        if (off != -1) {
            parameters.add(new URLParameter("off", off));
        }
        if (len != -1) {
            parameters.add(new URLParameter("len", len));
        }

        return parameters.toArray(new Parameter[parameters.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends GetDocumentResponse> getParser() {
        return new GetDocumentParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }
}
