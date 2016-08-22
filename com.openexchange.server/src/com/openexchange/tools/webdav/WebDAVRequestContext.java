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

package com.openexchange.tools.webdav;

import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.framework.request.DefaultRequestContext;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link WebDAVRequestContext}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.3
 */
public class WebDAVRequestContext extends DefaultRequestContext {

    private final HttpServletRequest request;
    private final Session session;

    /**
     * Initializes a new {@link WebDAVRequestContext}.
     *
     * @param request The underlying HTTP servlet request
     * @param session The session
     */
    public WebDAVRequestContext(HttpServletRequest request, Session session) {
        super();
        this.request = request;
        this.session = session;
        if (null != session) {
            boolean isGuest = Boolean.TRUE.equals(session.getParameter(Session.PARAM_GUEST));
            setHostData(Tools.createHostData(request, session.getContextId(), session.getUserId(), isGuest));
        } else {
            setHostData(Tools.createHostData(request, -1, -1, false));
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
