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

package com.openexchange.ui7;

import java.io.File;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UI7Servlet extends FileServlet {

    private static final long serialVersionUID = -87595969177959884L;

    private File zoneinfo;

    private static String ZONEINFO = "apps/io.ox/core/date/tz/zoneinfo/";

    public UI7Servlet(FileCache cache, File root, File zoneinfo) {
        super(cache, root);
        this.zoneinfo = zoneinfo;
    }

    private static Pattern versionRE = Pattern.compile("/v=[^/]+/");

    @Override
    protected File getFile(HttpServletRequest req, HttpServletResponse resp, String path) {
        if (path == null) {
            return new File(root, "core");
        }
        Matcher m = versionRE.matcher(path);
        if (m.lookingAt()) {
            path = path.substring(m.end());
        }
        if (path.startsWith(ZONEINFO)) {
            resp.setContentType("text/plain;charset=ISO-8859-1");
            return new File(zoneinfo, path.substring(ZONEINFO.length()));
        }
        return new File(root, path);
    }

    @Override
    protected void writeHeaders(HttpServletRequest req, HttpServletResponse resp, File file, String path) {
        if (path == null || path.startsWith("/core") || path.startsWith("/signin")) {
            resp.setContentType("text/html");
            resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
            resp.setHeader("Expires", "0");
            resp.setHeader("Pragma", "no-cache");
            return;
        }
        if ("/src/online.js".equals(path)) {
            resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
            return;
        }
        resp.setDateHeader("Expires", (new Date()).getTime() + (long) 3e10); // in almost a year
        super.writeHeaders(req, resp, file, path);
    }
}
