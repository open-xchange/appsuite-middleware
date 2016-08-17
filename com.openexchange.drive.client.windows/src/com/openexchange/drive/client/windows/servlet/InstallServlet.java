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

package com.openexchange.drive.client.windows.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.helper.BrowserDetector;
import com.openexchange.drive.client.windows.service.BrandingService;
import com.openexchange.drive.client.windows.service.DriveUpdateService;
import com.openexchange.drive.client.windows.service.internal.Utils;
import com.openexchange.exception.OXException;
import com.openexchange.tools.encoding.Helper;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link InstallServlet} is a servlet to initially download the drive client
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class InstallServlet extends SessionServlet {

    private static final long serialVersionUID = 6099648933040174331L;

    private static final Logger LOG = LoggerFactory.getLogger(InstallServlet.class);

    private final DriveUpdateService updateService;

    public InstallServlet(DriveUpdateService updateService) {
        super();
        this.updateService = updateService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ServerSession session = getSessionObject(req);
            if (null == session) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String branding = BrandingService.getBranding(session);
            String fileName = updateService.getExeFileName(branding);
            if (!Utils.hasPermissions(ServerSessionAdapter.valueOf(session).getUserConfiguration(), updateService.getNecessaryPermission())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            BrowserDetector detector = BrowserDetector.detectorFor(req.getHeader(Tools.HEADER_AGENT));
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/octet-stream");
            resp.setHeader("Content-disposition", "attachment; filename=\"" + Helper.escape(Helper.encodeFilename(fileName, "UTF-8", detector.isMSIE())) + "\"");
            Tools.removeCachingHeader(resp);

            InputStream is = updateService.getFile(fileName, branding);
            OutputStream out = resp.getOutputStream();
            byte[] buf = new byte[4096];
            int length = -1;
            while ((length = is.read(buf)) != -1) {
                out.write(buf, 0, length);
            }

            out.flush();
            is.close();
        } catch (OXException e) {
            LOG.error("", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
