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
