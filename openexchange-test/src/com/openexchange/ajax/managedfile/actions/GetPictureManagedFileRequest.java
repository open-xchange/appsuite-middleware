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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link GetPictureManagedFileRequest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GetPictureManagedFileRequest extends AbstractManagedFileRequest<GetPictureManagedFileResponse> {

    private String uid;

    /**
     * Initializes a new {@link GetPictureManagedFileRequest}.
     * 
     * @param failOnError
     */
    public GetPictureManagedFileRequest(boolean failOnError) {
        super(failOnError);
    }

    /**
     * Initializes a new {@link GetPictureManagedFileRequest}.
     */
    public GetPictureManagedFileRequest(final String uid) {
        this(true);
        this.uid = uid;
    }

    @Override
    public String getServletPath() {
        return "/ajax/image/mfile/picture";
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> list = new ArrayList<Parameter>(1);
        list.add(new URLParameter("uid", uid));
        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends GetPictureManagedFileResponse> getParser() {
        return new GetPictureManagedFileParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

}
