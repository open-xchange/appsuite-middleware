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

package com.openexchange.ajax.redirect;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link RedirectServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RedirectServlet extends HttpServlet {

    private static final long serialVersionUID = 6111473866164506367L;

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // create a new HttpSession if it's missing
        req.getSession(true);

        String location = req.getParameter("location");
        if (location == null) {
            Tools.sendErrorPage(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing \"location\" parameter");
            return;
        }

        if (!isRelative(location)) {
            Tools.sendErrorPage(resp, HttpServletResponse.SC_BAD_REQUEST, "Specified location must not be absolute.");
            return;
        }

        if (isServerRelative(location)) {
            resp.sendRedirect(AJAXUtility.encodeUrl(location, true));
            return;
        }

        String referer = purgeHost(req.getHeader("referer"));
        if (referer == null) {
            Tools.sendErrorPage(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing \"referer\" header");
            return;
        }

        location = assumeRelative(referer, location);
        resp.sendRedirect(AJAXUtility.encodeUrl(location, true, true));
    }

    private static final Pattern PROTOCOL_PATTERN = Pattern.compile("^(\\w*:)?//");

    private boolean isRelative(final String location) {
        final Matcher matcher = PROTOCOL_PATTERN.matcher(location);
        return !matcher.find();
    }

    private static final Pattern HOST_PATTERN = Pattern.compile("^(\\w*:)?//\\w*/");

    private String purgeHost(final String location) {
        if (location == null) {
            return null;
        }
        return HOST_PATTERN.matcher(location).replaceAll("");
    }

    private boolean isServerRelative(final String location) {
        return location.length() > 0 && location.charAt(0) == '/';
    }

    private String assumeRelative(String referer, String location) {
        int index = referer.lastIndexOf('/');
        if (index >= 0) {
            return "/" + referer.substring(0, index) + "/" + location;
        }

        return "/" + referer + "/" + location;
    }

}
