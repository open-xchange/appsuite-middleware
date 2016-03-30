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

package com.openexchange.jslob.json.rest;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.DispatcherServlet;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractRestServlet} - The abstract Servlet to handle REST-like requests.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractRestServlet extends DispatcherServlet {

    private static final long serialVersionUID = 3164103914795844167L;

    private final AJAXRequestDataTools ajaxRequestDataTools;

    /**
     * Initializes a new {@link AbstractRestServlet}.
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

        private final AbstractRestServlet servlet;

        public RestRequestDataTools(final AbstractRestServlet servlet) {
            super();
            this.servlet = servlet;
        }

        @Override
        public AJAXRequestData parseRequest(final HttpServletRequest req, final boolean preferStream, final boolean isFileUpload, final ServerSession session, final String prefix, final HttpServletResponse optResp) throws IOException, OXException {
            if (isFileUpload) {
                return super.parseRequest(req, preferStream, isFileUpload, session, prefix, optResp);
            }
            /*
             * Parse dependent on HTTP method and/or servlet path
             */
            final Method method = Method.valueOf(req);
            if (null == method) {
                return super.parseRequest(req, preferStream, isFileUpload, session, prefix, optResp);
            }
            final MethodHandler methodHandler = servlet.getMethodHandler(method);
            if (null == methodHandler) {
                return super.parseRequest(req, preferStream, isFileUpload, session, prefix, optResp);
            }
            return methodHandler.parseRequest(req, session, servlet);
        }
    }

}
