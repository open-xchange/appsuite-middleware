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

import static com.openexchange.ajax.requesthandler.rest.AbstractRestServlet.splitPath;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.dispatcher.Parameterizable;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractMethodHandler} - The abstract method handler responsible for modifying <code>AJAXRequestData</code> instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractMethodHandler implements MethodHandler {

    /**
     * Initializes a new {@link AbstractMethodHandler}.
     */
    protected AbstractMethodHandler() {
        super();
    }

    private static final String ATTR_ACTION = "__ajax.rest.action";

    /**
     * Gets the action identifier dependent n given extra path information.
     *
     * @param restPathElements The extra path information or <code>null</code>
     * @param restRequest The REST request
     * @return The action identifier
     */
    @Override
    public String getAction(String[] restPathElements, HttpServletRequest restRequest) {
        String action = (String) restRequest.getAttribute(ATTR_ACTION);
        if (action == null) {
            action = doGetAction(restPathElements, restRequest);
            restRequest.setAttribute(ATTR_ACTION, action);
        }
        return action;
    }

    @Override
    public AJAXRequestData modifyRequest(AJAXRequestData requestData, HttpServletRequest restRequest) throws IOException, OXException {
        // Set the module
        requestData.setModule(getModule());

        // Set request URI
        requestData.setServletRequestURI("");
        requestData.setPathInfo("");
        requestData.putParameter(AJAXServlet.PARAM_PLAIN_JSON, "true"); // Avoid JavaScript call-backs

        // Split path
        String[] pathElements = splitPath(restRequest);

        // Invoke...
        modifyByPathInfo(requestData, pathElements, restRequest);

        // Take over action (if possible)
        String action = requestData.getAction();
        if (null != action && restRequest instanceof Parameterizable) {
            ((Parameterizable) restRequest).putParameter("action", action);
        }

        // Return modified AJAX request data
        return requestData;
    }

    /**
     * Modifies given AJAX request data by specified REST path info, which is the extra path information following the servlet path but
     * preceding the query string (without starting <code>"/"</code> character).
     *
     * @param requestData The AJAX request data to modify
     * @param restPathElements The split REST path info (w/o starting <code>"/"</code> character) or <code>null</code> if there is no extra path information available;<br>
     *                         <code>"/path/to/resource/123"</code> yields <code>["path", "to", "resource", "123"]</code>
     * @param restRequest The REST request
     */
    protected abstract void modifyByPathInfo(AJAXRequestData requestData, String[] restPathElements, HttpServletRequest restRequest) throws IOException, OXException;

    /**
     * Gets the action identifier dependent n given extra path information.
     *
     * @param restPathElements The extra path information or <code>null</code>
     * @param restRequest The REST request
     * @return The action identifier
     */
    protected abstract String doGetAction(String[] restPathElements, HttpServletRequest restRequest);

    // ---------------------------------------------------------------------------------------------------------------------------------

    /**
     * Checks if <b>no</b> extra path information is available.
     *
     * @param restPathElements The REST path information to check
     * @return <code>true</code> if <b>no</b> extra path information is available; otherwise <code>false</code> if not available
     */
    protected static boolean hasNoPathInfo(String[] restPathElements) {
        return false == hasPathInfo(restPathElements);
    }

    /**
     * Checks if extra path information is available.
     *
     * @param restPathElements The REST path information to check
     * @return <code>true</code> if extra path information is available; otherwise <code>false</code> if not available
     */
    protected static boolean hasPathInfo(String[] restPathElements) {
        return (restPathElements != null && restPathElements.length > 0);
    }
}
