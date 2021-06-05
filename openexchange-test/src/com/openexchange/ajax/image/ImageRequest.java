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

package com.openexchange.ajax.image;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.Header;

/**
 * {@link ImageRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ImageRequest implements AJAXRequest<ImageResponse> {

    /**
     * Parses the appropriate image request from given image URI
     *
     * @param imageUri The image UIR to parse
     * @return The image request
     * @throws UnsupportedEncodingException
     */
    public static ImageRequest parseFrom(String imageUri) throws UnsupportedEncodingException {
        if (null == imageUri) {
            return null;
        }

        // "/ajax/image/user/picture?id=273&timestamp=1468497596841"
        int pos = imageUri.indexOf('?');
        String path = imageUri.substring(0, pos);

        ImageRequest imageRequest = new ImageRequest(null);
        imageRequest.setPath(path);

        String query = imageUri.substring(pos + 1);
        int i;
        while ((i = query.indexOf('&')) > 0) {
            String nvp = query.substring(0, i);
            int delim = nvp.indexOf('=');
            imageRequest.addParameter(URLDecoder.decode(nvp.substring(0, delim), UTF_8.name()), URLDecoder.decode(nvp.substring(delim + 1), UTF_8.name()));

            query = query.substring(i + 1);
        }
        {
            int delim = query.indexOf('=');
            imageRequest.addParameter(URLDecoder.decode(query.substring(0, delim), StandardCharsets.UTF_8.name()), URLDecoder.decode(query.substring(delim + 1), UTF_8.name()));
        }
        return imageRequest;
    }

    // --------------------------------------------------------------------

    private final String uid;
    private String path;
    private List<Parameter> params;

    public ImageRequest(final String uid) {
        super();
        this.uid = uid;
    }

    /**
     * Sets the path; e.g <code>"/ajax/image/user/picture"</code>
     *
     * @param path The path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Adds specified parameter
     *
     * @param name The name
     * @param value The value
     */
    public void addParameter(String name, String value) {
        List<Parameter> params = this.params;
        if (null == params) {
            params = new ArrayList<>();
            this.params = params;
        }
        params.add(new Parameter(name, value));
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    @Override
    public String getServletPath() {
        return null == path ? "/ajax/image" : path;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> params = new ArrayList<>();

        if (null != uid) {
            params.add(new Parameter("uid", uid));
        }
        if (null != this.params) {
            params.addAll(this.params);
        }

        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public ImageParser getParser() {
        return new ImageParser(true);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

}
