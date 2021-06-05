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

package com.openexchange.ajax.managedfile.actions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.framework.Header.SimpleHeader;

/**
 * {@link NewManagedFileRequest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class NewManagedFileRequest extends AbstractManagedFileRequest<NewManagedFileResponse> {

    private String module;

    private String type;

    private Object body;

    /**
     * Initializes a new {@link NewManagedFileRequest}.
     * 
     * @param failOnError
     */
    public NewManagedFileRequest(boolean failOnError) {
        super(failOnError);
    }

    public NewManagedFileRequest(final String module, final String type, final Object body) {
        this(true);
        this.module = module;
        this.type = type;
        this.body = body;

    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.POST;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> list = new ArrayList<Parameter>();
        list.add(new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW));
        list.add(new URLParameter(AJAXServlet.PARAMETER_MODULE, module));
        list.add(new URLParameter(AJAXServlet.PARAMETER_TYPE, type));
        list.add(new FileParameter("file", "5161.png", new ByteArrayInputStream((byte[]) body), "image/png"));
        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends NewManagedFileResponse> getParser() {
        return new NewManagedFileParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return body;
    }

    @Override
    public Header[] getHeaders() {
        return new Header[] { new SimpleHeader("Content-Type", "multipart/form-data") };
    }

}
