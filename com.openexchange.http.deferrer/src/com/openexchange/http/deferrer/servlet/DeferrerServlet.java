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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.http.deferrer.servlet;

import static com.openexchange.ajax.AJAXServlet.encodeUrl;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.http.deferrer.CustomRedirectURLDetermination;

/**
 * {@link DeferrerServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DeferrerServlet extends HttpServlet {

    private static final long serialVersionUID = 1358634554782437089L;

    /**
     * The listing for custom handlers.
     */
    public static final List<CustomRedirectURLDetermination> CUSTOM_HANDLERS = new CopyOnWriteArrayList<CustomRedirectURLDetermination>();

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // Create a new HttpSession if it's missing
        req.getSession(true);

        // Get the URL to defer to
        // Redirect
        final String redirectURL = determineRedirectURL(req);
        if (redirectURL == null) {
            return;
        }
        char concat = '?';
        if (redirectURL.indexOf('?') >= 0) {
            concat = '&';
        }

        final StringBuilder builder = new StringBuilder(encodeUrl(redirectURL, false, false));
        for (final Enumeration<?> parameterNames = req.getParameterNames(); parameterNames.hasMoreElements();) {
            final String name = (String) parameterNames.nextElement();
            if ("redirect".equals(name)) {
                continue;
            }
            final String parameter = req.getParameter(name);
            builder.append(concat);
            concat = '&';
            builder.append(name).append('=').append(encodeUrl(parameter, true, true));
        }
        resp.sendRedirect(builder.toString());

    }

    private String determineRedirectURL(final HttpServletRequest req) {
        for (final CustomRedirectURLDetermination determination : CUSTOM_HANDLERS) {
            final String url = determination.getURL(req);
            if (url != null) {
                return url;
            }
        }
        return req.getParameter("redirect");
    }

}
