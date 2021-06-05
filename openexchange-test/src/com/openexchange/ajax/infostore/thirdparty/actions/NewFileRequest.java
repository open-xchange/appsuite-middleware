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

package com.openexchange.ajax.infostore.thirdparty.actions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.framework.Header.SimpleHeader;

/**
 * {@link NewFileRequest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class NewFileRequest extends AbstractFileRequest<NewFileResponse> {

    private File file;

    private byte[] bytes;

    private String mimeType;

    private JSONObject metadata;

    private NewFileRequest(boolean failOnError) {
        super(failOnError);
    }

    /**
     * Initializes a new {@link NewFileRequest} with sequence of byte data.
     *
     * @param byteData - The size of the file in kilobytes
     * @param metadata - The additional metadata to be sent as json object in the multipart request
     * @param mimeType - the mime type of the file
     */
    public NewFileRequest(byte[] byteData, final JSONObject metadata, String mimeType) {
        this(true);
        this.bytes = byteData;
        this.mimeType = mimeType;
        this.metadata = metadata;
    }

    /**
     * Initializes a new {@link NewFileRequest}.
     *
     * @param size - The file to be sent
     * @param metadata - The additional metadata to be sent as json object in the multipart request
     * @param mimeType - the mime type of the file
     */
    public NewFileRequest(final File file, final JSONObject metadata, String mimeType) {
        this(true);
        this.file = file;
        this.mimeType = mimeType;
        this.metadata = metadata;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.POST;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> list = new ArrayList<Parameter>();
        list.add(new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW));
        if (file != null) {
            list.add(new FileParameter("file", file.getName(), new FileInputStream(file), mimeType));
        } else if (bytes != null) {
            list.add(new FileParameter("file", "random", new ByteArrayInputStream(bytes), mimeType));
        }
        list.add(new FieldParameter("json", metadata.toString()));
        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends NewFileResponse> getParser() {
        return new NewFileParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return new Header[] { new SimpleHeader("Content-Type", "multipart/form-data") };
    }

}
