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

package com.openexchange.ajax.requesthandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.tools.session.ServerSession;


/**
 * An {@link AJAXRequestDataBuilder} is a fluent interface for constructing AJAXRequestData for use with the {@link Dispatcher}.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AJAXRequestDataBuilder {

    /**
     * Initializes a new {@code AJAXRequestDataBuilder} instance.
     * <p>
     * Best used as a static import.
     *
     * @return A new {@code AJAXRequestDataBuilder} instance
     */
    public static AJAXRequestDataBuilder request() {
        return new AJAXRequestDataBuilder();
    }

    /**
     * Initializes a new {@code AJAXRequestDataBuilder} instance.
     * <p>
     * Best used as a static import.
     *
     * @param action The action identifier
     * @param module The module identifier
     * @param session The associated session
     * @return A new {@code AJAXRequestDataBuilder} instance
     */
    public static AJAXRequestDataBuilder request(String action, String module, ServerSession session) {
        return new AJAXRequestDataBuilder().session(session).module(module).action(action);
    }

    // ------------------------------------------------------------------------------------------------------------------

    private final AJAXRequestData data;
    private boolean formatSpecified = false;

    /**
     * Initializes a new {@link AJAXRequestDataBuilder}.
     */
    private AJAXRequestDataBuilder() {
        super();
        data = new AJAXRequestData();
    }

    /**
     * Specify HTTP resources.
     */
    public AJAXRequestDataBuilder httpResources(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        data.setHttpServletRequest(httpRequest);
        data.setHttpServletResponse(httpResponse);
        return this;
    }

    /**
     * Specify the session
     */
    public AJAXRequestDataBuilder session(ServerSession session) {
        data.setSession(session);
        return this;
    }

    /**
     * Specify the module
     */
    public AJAXRequestDataBuilder module(String module) {
        data.setModule(module);
        return this;
    }

    /**
     * Specify the action
     */
    public AJAXRequestDataBuilder action(String action) {
        data.setAction(action);
        data.putParameter("action", action);
        return this;
    }

    /**
     * Specify parameters. Alternate parameter names and values. E.g. builder.params('id', '12', 'folder', '13')
     */
    public AJAXRequestDataBuilder params(String...params) {
        String name = null;
        for (String string : params) {
            if (name != null) {
                data.putParameter(name, string);
                name = null;
            } else {
                name = string;
            }
        }
        return this;
    }

    /**
     * Specify the body. Usually as a JSON formatted String.
     */
    public AJAXRequestDataBuilder data(Object body, String format) {
        data.setData(body, format);
        return this;
    }

    /**
     * Request a certain format in the results. Defaults to 'native';
     */
    public AJAXRequestDataBuilder format(String format) {
        formatSpecified = true;
        data.setFormat(format);
        return this;
    }

    public AJAXRequestDataBuilder pathInfo(String path) {
        data.setPathInfo(path);
        return this;
    }

    public AJAXRequestData build() {
    	return build(null);
    }

    public AJAXRequestData build(AJAXRequestData original) {
        if (!formatSpecified) {
            format("native");
        }
        if (null != original) {
            data.setHostname(original.getHostname());
            data.setPrefix(original.getPrefix());
            data.setRoute(original.getRoute());
            data.setServerPort(original.getServerPort());
        }
        return data;
    }

    public AJAXRequestDataBuilder hostname(String hostname) {
        data.setHostname(hostname);
        return this;
    }

}
