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

package com.openexchange.ajax.requesthandler.rest;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.DispatcherServlet;
import com.openexchange.exception.OXException;
import com.openexchange.java.Reference;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractRestServlet} - The abstract Servlet to handle REST-like calls to the common Dispatcher framework.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractRestServlet extends DispatcherServlet {

    private static final long serialVersionUID = 3164103914795844167L;

    private final AJAXRequestDataTools ajaxRequestDataTools;

    /**
     * Initializes a new {@link AbstractRestServlet}.
     *
     * @param prefix The dispatcher servlet prefix
     */
    protected AbstractRestServlet(String prefix) {
        super(prefix);
        ajaxRequestDataTools = new RestRequestDataTools(this);
    }

    @Override
    protected AJAXRequestDataTools getAjaxRequestDataTools() {
        return ajaxRequestDataTools;
    }

    /**
     * Gets the method handler appropriate for specified method.
     *
     * @param method The HTTP method
     * @return The associated method handler or <code>null</code> if none appropriate
     */
    public abstract MethodHandler getMethodHandler(Method method);

    private static final class RestRequestDataTools extends AJAXRequestDataTools {

        private final AbstractRestServlet restServlet;

        /**
         * Initializes a new {@link RestRequestDataTools}.
         *
         * @param restServlet The REST end-point
         */
        RestRequestDataTools(final AbstractRestServlet restServlet) {
            super();
            this.restServlet = restServlet;
        }

        @Override
        public String getModule(String prefix, HttpServletRequest req) {
            // Obtain method's handler...
            MethodHandler methodHandler = getMethodHandler(req, restServlet);
            if (null == methodHandler) {
                return super.getModule(prefix, req);
            }

            return methodHandler.getModule();
        }

        @Override
        public String getAction(HttpServletRequest req) {
            // Obtain method's handler...
            MethodHandler methodHandler = getMethodHandler(req, restServlet);
            if (null == methodHandler) {
                return super.getAction(req);
            }

            // Split path
            String[] pathElements = splitPath(req);
            return methodHandler.getAction(pathElements, req);
        }

        @Override
        public AJAXRequestData parseRequest(HttpServletRequest req, boolean bPreferStream, boolean bIsFileUpload, boolean preLoadRequestBody, ServerSession session, String prefix, HttpServletResponse optResp) throws IOException, OXException {
            // Determine values for "preferStream" and "isFileUpload" argument
            boolean preferStream = false;
            boolean isFileUpload = false;
            if (bIsFileUpload) {
                // A file upload has already been detected...
                preferStream = true;
                isFileUpload = true;
            } else if (bPreferStream) {
                // Signals that a POST request is incoming. Check its Content-Type header (if available)
                String contentType = req.getContentType();
                if (null != contentType) {
                    contentType = Strings.asciiLowerCase(contentType.trim());
                    if (contentType.startsWith("multipart/form-data")) {
                        preferStream = true;
                        isFileUpload = true;
                    } else if (contentType.startsWith("application/x-www-form-urlencoded")) {
                        preferStream = true;
                        isFileUpload = false;
                    }
                }
            }

            // Parse to an instance of AJAXRequestData
            AJAXRequestData requestData = super.parseRequest(req, preferStream, isFileUpload, preLoadRequestBody, session, prefix, optResp);

            // Obtain method's handler...
            MethodHandler methodHandler = getMethodHandler(req, restServlet);
            if (null == methodHandler) {
                return requestData;
            }

            // ... and modify request appropriately to trigger the right Dispatcher call
            return methodHandler.modifyRequest(requestData, req);
        }
    }

    private static final String ATTR_PATH_ELEMS = "__ajax.rest.pathelems";

    /**
     * Splits given request's {@link HttpServletRequest#getPathInfo() extra path information} into tokens.
     *
     * @param restRequest The request
     * @return The split path or <code>null</code> if there is no extra path information available
     */
    protected static String[] splitPath(HttpServletRequest restRequest) {
        Reference<String[]> pathElementsRef = (Reference<String[]>) restRequest.getAttribute(ATTR_PATH_ELEMS);
        if (pathElementsRef == null) {
            String[] pathElements;
            String pathInfo = restRequest.getPathInfo();
            if (Strings.isEmpty(pathInfo)) {
                // No extra path information available
                pathElements = null;
            } else {
                if (pathInfo.charAt(0) == '/') {
                    // Drop starting slash character
                    pathInfo = pathInfo.substring(1);
                }
                pathElements = Strings.splitBy(pathInfo, '/', false);
            }
            pathElementsRef = new Reference<String[]>(pathElements);
            restRequest.setAttribute(ATTR_PATH_ELEMS, pathElementsRef);
        }
        return pathElementsRef.getValue();
    }

    private static final String ATTR_METHOD_HANDLER = "__ajax.rest.methodhandler";

    /**
     * Gets the method handler associated with given request.
     *
     * @param req The request
     * @param restServlet The REST servlet providing the look-up
     * @return The method handler
     */
    protected static MethodHandler getMethodHandler(HttpServletRequest req, AbstractRestServlet restServlet) {
        MethodHandler methodHandler = (MethodHandler) req.getAttribute(ATTR_METHOD_HANDLER);
        if (methodHandler == null) {
            // Determine the request's method
            Method method = Method.valueOf(req);
            if (null == method) {
                return null;
            }

            // Obtain method's handler...
            methodHandler = restServlet.getMethodHandler(method);
            if (null == methodHandler) {
                return null;
            }
            req.setAttribute(ATTR_METHOD_HANDLER, methodHandler);
        }
        return methodHandler;
    }

}
