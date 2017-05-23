/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.image;

import java.io.IOException;
import java.net.URLDecoder;
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
     */
    public static ImageRequest parseFrom(String imageUri) {
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
            imageRequest.addParameter(URLDecoder.decode(nvp.substring(0, delim)), URLDecoder.decode(nvp.substring(delim + 1)));

            query = query.substring(i + 1);
        }
        {
            int delim = query.indexOf('=');
            imageRequest.addParameter(URLDecoder.decode(query.substring(0, delim)), URLDecoder.decode(query.substring(delim + 1)));
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
