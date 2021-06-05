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

package com.openexchange.ajax.snippet.actions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.framework.Header.SimpleHeader;

/**
 * {@link AttachSnippetRequest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AttachSnippetRequest extends AbstractSnippetRequest<AttachSnippetResponse> {

    private byte[] body;

    private int sid;

    /**
     * Initializes a new {@link AttachSnippetRequest}.
     * 
     * @param failOnError
     */
    protected AttachSnippetRequest(boolean failOnError) {
        super(failOnError);
    }

    /**
     * Initializes a new {@link AttachSnippetRequest}.;
     * 
     * @param body
     * @param failOnError
     */
    public AttachSnippetRequest(final byte[] body, final int sid, final boolean failOnError) {
        super(failOnError);
        this.body = body;
        this.sid = sid;
    }

    public AttachSnippetRequest(final byte[] body, final int sid) {
        this(body, sid, true);
    }

    @Override
    public Method getMethod() {
        return Method.POST;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> params = new LinkedList<Parameter>();
        params.add(new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ATTACH));
        params.add(new URLParameter("id", Integer.toString(sid)));
        params.add(new URLParameter("type", "image"));
        params.add(new FileParameter("file", "blah", new ByteArrayInputStream(body), "image/png"));
        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends AttachSnippetResponse> getParser() {
        return new AttachSnippetParser(failOnError);
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
