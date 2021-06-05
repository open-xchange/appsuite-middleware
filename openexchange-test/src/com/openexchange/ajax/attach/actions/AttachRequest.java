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

import java.io.InputStream;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Attachment;
import com.openexchange.ajax.attach.AttachmentTools;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.container.CommonObject;

/**
 * {@link AttachRequest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class AttachRequest extends AbstractAttachmentRequest<AttachResponse> {

    private final int moduleId;

    private final int folderId;

    private final int attachedId;

    private final String fileName;

    private final InputStream data;

    private final String mimeType;

    public AttachRequest(int folderId, int objectId, int moduleId, String fileName, InputStream data, String mimeType) {
        super();
        this.moduleId = moduleId;
        this.folderId = folderId;
        this.attachedId = objectId;
        this.fileName = fileName;
        this.data = data;
        this.mimeType = mimeType;
    }

    public AttachRequest(CommonObject obj, String fileName, InputStream data, String mimeType) {
        super();
        moduleId = AttachmentTools.determineModule(obj);
        folderId = obj.getParentFolderID();
        attachedId = obj.getObjectID();
        this.fileName = fileName;
        this.data = data;
        this.mimeType = mimeType;
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.UPLOAD;
    }

    private String writeJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(AttachmentField.MODULE_ID_LITERAL.getName(), moduleId);
        json.put(AttachmentField.FOLDER_ID_LITERAL.getName(), folderId);
        json.put(AttachmentField.ATTACHED_ID_LITERAL.getName(), attachedId);
        return json.toString();
    }

    @Override
    public Parameter[] getParameters() throws JSONException {
        return new Parameter[] { new URLParameter(AJAXServlet.PARAMETER_ACTION, Attachment.ACTION_ATTACH), new FieldParameter("json_0", writeJSON()), new FileParameter("file_0", fileName, data, mimeType)
        };
    }

    @Override
    public AttachParser getParser() {
        return new AttachParser(false);
    }
}
