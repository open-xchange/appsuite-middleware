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

package com.openexchange.tools.webdav;

import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.framework.request.DefaultRequestContext;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link WebDAVRequestContext}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.3
 */
public class WebDAVRequestContext extends DefaultRequestContext {

    private static final Logger LOG = LoggerFactory.getLogger(WebDAVRequestContext.class);
    private final HttpServletRequest request;
    private final Session session;

    /**
     * Initializes a new {@link WebDAVRequestContext}.
     *
     * @param request The underlying HTTP servlet request
     * @param session The session
     * @throws ServletException
     */
    public WebDAVRequestContext(HttpServletRequest request, Session session) {
        super();
        this.request = request;
        this.session = session;
        setUserAgent(request.getHeader("user-agent"));
        if (null != session) {
            boolean isGuest = Boolean.TRUE.equals(session.getParameter(Session.PARAM_GUEST));
            setHostData(Tools.createHostData(request, session.getContextId(), session.getUserId(), isGuest));
        } else {
            setHostData(Tools.createHostData(request, -1, -1, false));
        }
        try {
            setSession(ServerSessionAdapter.valueOf(session));
        } catch (OXException e) {
            // Failed to set session
            LOG.debug("", e);
        }
    }

    /**
     * Returns the value of the specified request header as a <code>String</code>. If the request did not include a header of the
     * specified name, this method returns <code>null</code>. If there are multiple headers with the same name, this method returns the
     * first head in the request. The header name is case insensitive. You can use this method with any request header.
     *
     * @param name a <code>String</code> specifying the header name
     * @return a <code>String</code> containing the value of the requested header, or <code>null</code> if the request does not have a
     *         header of that name
     * @see HttpServletRequest#getHeader(String)
     */
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    /**
     * Returns all the values of the specified request header as an <code>Enumeration</code> of <code>String</code> objects.
     *
     * <p>Some headers, such as <code>Accept-Language</code> can be sent by clients as several headers each with a different value rather
     * than sending the header as a comma separated list.
     *
     * <p>If the request did not include any headers of the specified name, this method returns an empty <code>Enumeration</code>. The
     * header name is case insensitive. You can use this method with any request header.
     *
     * @param name a <code>String</code> specifying the header name
     * @return an <code>Enumeration</code> containing the values of the requested header. If the request does not have any headers of that
     *         name return an empty enumeration. If the container does not allow access to header information, return null
     * @see HttpServletRequest#getHeaders(String)
     */
    public Enumeration<?> getHeaders(String name) {
        return request.getHeaders(name);
    }

    /**
     * Returns an enumeration of all the header names this request contains. If the request has no headers, this method returns an empty
     * enumeration.
     *
     * <p>Some servlet containers do not allow servlets to access headers using this method, in which case this method returns
     * <code>null</code>
     *
     * @return an enumeration of all the header names sent with this request; if the request has no headers, an empty enumeration; if the
     *         servlet container does not allow servlets to use this method, <code>null</code>
     * @see HttpServletRequest#getHeaderNames()
     */
    public Enumeration<?> getHeaderNames() {
        return request.getHeaderNames();
    }

    @Override
    public String toString() {
        return "DAVRequestContext [session=" + session + ", hostData()=" + getHostData() + "]";
    }
}
