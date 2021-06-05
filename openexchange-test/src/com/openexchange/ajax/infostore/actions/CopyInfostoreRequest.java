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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.file.storage.File;

/**
 * {@link CopyInfostoreRequest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class CopyInfostoreRequest extends AbstractInfostoreRequest<CopyInfostoreResponse> {

    private String id;
    private String folderId;
    private com.openexchange.file.storage.File metadata;
    private String version;
    private java.io.File upload;

    public CopyInfostoreRequest(String id, String folderId, File file) {
        this(id, folderId, file, null);
    }

    public CopyInfostoreRequest(String id, String folderId, File file, String version, java.io.File upload) {
        super();
        this.id = id;
        this.folderId = folderId;
        this.metadata = file;
        this.version = version;
        this.upload = upload;
    }

    public CopyInfostoreRequest(String id, String folderId, File file, String version) {
        this(id, folderId, file, version, null);
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> tmp = new ArrayList<Parameter>(4);
        tmp.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_COPY));
        tmp.add(new Parameter(AJAXServlet.PARAMETER_ID, id));
        tmp.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderId));
        tmp.add(new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, new Date()));
        if (version != null) {
            tmp.add(new Parameter(AJAXServlet.PARAMETER_VERSION, this.version));
        }
        if (null != upload) {
            tmp.add(new FieldParameter("json", getBody()));
            tmp.add(new FileParameter("file", metadata.getFileName(), new FileInputStream(upload), metadata.getFileMIMEType()));
        }
        return tmp.toArray(new Parameter[tmp.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends CopyInfostoreResponse> getParser() {
        return new CopyInfostoreParser(getFailOnError());
    }

    @Override
    public String getBody() throws JSONException {
        JSONObject jFile = prepareJFile();
        return jFile.toString();
    }

    public void setMetadata(com.openexchange.file.storage.File metadata) {
        this.metadata = metadata;
    }

    public com.openexchange.file.storage.File getMetadata() {
        return metadata;
    }

    private JSONObject prepareJFile() throws JSONException {
        final JSONObject originalObject = new JSONObject(writeJSON(getMetadata()));
        final JSONObject retVal = new JSONObject();
        final Set<String> set = originalObject.keySet();

        for (String string : set) {
            final Object test = originalObject.get(string);
            if (test != JSONObject.NULL) {
                if (test instanceof JSONArray) {
                    if (((JSONArray) test).length() > 0) {
                        retVal.put(string, test);
                    }
                } else {
                    retVal.put(string, test);
                }
            }
        }
        return retVal;
    }

}
