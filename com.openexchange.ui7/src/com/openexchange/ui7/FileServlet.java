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
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.mail.mime.MimeType2ExtMap;

/**
 * {@link FileServlet}
 * 
 * @author <a href="mailto:viktor.pracht@open-xchange.com">Viktor Pracht</a>
 */
public class FileServlet extends HttpServlet {

    private static final long serialVersionUID = 5984953578534687847L;

    private static org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(FileServlet.class));

    protected File root;

    private FileCache cache;

    public FileServlet(FileCache cache, File root) {
        super();
        this.cache = cache;
        this.root = root;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        File file = getFile(req, resp, path);
        LOG.debug("Serving " + file);
        byte[] data = cache.get(file);
        if (data == null) {
            resp.reset();
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            writeHeaders(req, resp, file, path);
            resp.getOutputStream().write(data);
        }
    }

    protected File getFile(HttpServletRequest req, HttpServletResponse resp, String path) {
        return path == null ? root : new File(root, path);
    }

    protected void writeHeaders(HttpServletRequest req, HttpServletResponse resp, File file, String path) {
        if (!resp.containsHeader("Content-Type")) {
            resp.setContentType(MimeType2ExtMap.getContentType(path));
        }
    }

}
