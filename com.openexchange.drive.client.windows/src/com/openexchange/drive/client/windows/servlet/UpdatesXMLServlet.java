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
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.drive.client.windows.service.BrandingService;
import com.openexchange.drive.client.windows.service.Constants;
import com.openexchange.drive.client.windows.service.DriveUpdateService;
import com.openexchange.drive.client.windows.service.internal.Services;
import com.openexchange.drive.client.windows.service.internal.Utils;
import com.openexchange.exception.OXException;
import com.openexchange.login.Interface;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateErrorMessage;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.webdav.OXServlet;

/**
 * 
 * {@link UpdatesXMLServlet} is a servlet which provides update informations about the drive client
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class UpdatesXMLServlet extends OXServlet {

    private static final long serialVersionUID = -7945036709270719526L;

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(UpdatesXMLServlet.class);

    private final TemplateService templateService;

    private final DriveUpdateService driveUpdate;

    private final static String HEAD = "<Products>";
    private final static String TAIL = "</Products>";

    public UpdatesXMLServlet(final TemplateService templateService, DriveUpdateService driveUpdate) {
        super();
        this.templateService = templateService;
        this.driveUpdate = driveUpdate;
    }

    @Override
    protected Interface getInterface() {
        return Interface.DRIVE_UPDATER;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        ServerSession session = null;
        PrintWriter writer = null;
        try {
            session = getServerSession(req);
            if (Utils.hasPermissions(session.getUserConfiguration(), driveUpdate.getNecessaryPermission())) {
                final String serverUrl = Utils.getServerUrl(req, session);
                ConfigurationService config = Services.getService(ConfigurationService.class);
                String updateTmplName = Constants.TMPL_UPDATER_DEFAULT;
                if (config != null) {
                    updateTmplName = config.getProperty(Constants.TMPL_UPDATER_CONFIG, Constants.TMPL_UPDATER_DEFAULT);
                }
                final OXTemplate productsTemplate = templateService.loadTemplate(updateTmplName);
                String branding = BrandingService.getBranding(session);
                Map<String, Object> map = null;
                try {
                    map = driveUpdate.getTemplateValues(serverUrl, Utils.getUserName(session), branding);
                } catch (NullPointerException e) {
                    LOG.error("Branding properties imcomplete!");
                    throw new BrandingException(BrandingException.MISSING_PROPERTIES);
                }
                writer = resp.getWriter();
                writeHead(writer);
                productsTemplate.process(map, writer);
                writeTail(writer);
            } else {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.setContentType("text/plain");
                return;
            }
        } catch (final OXException e) {
            if (!TemplateErrorMessage.UnderlyingException.equals(e)) {
                LOG.error(e.getMessage(), e);
            }

            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            resp.setContentType("text/html");
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }

            if (session != null) {
                logout(session, req, resp);
            }
        }
    }

    private void writeHead(PrintWriter writer) {
        writer.println(HEAD);
    }

    private void writeTail(PrintWriter writer) {
        writer.println(TAIL);
    }

    private ServerSession getServerSession(final HttpServletRequest req) throws OXException {
        return new ServerSessionAdapter(getSession(req));
    }

    private void logout(final ServerSession session, final HttpServletRequest req, final HttpServletResponse resp) {
        removeCookie(req, resp);
        try {
            LoginPerformer.getInstance().doLogout(session.getSessionID());
        } catch (final OXException e) {
            LOG.error("", e);
        }
    }

    private static final transient Tools.CookieNameMatcher COOKIE_MATCHER = new Tools.CookieNameMatcher() {
        @Override
        public boolean matches(final String cookieName) {
            return (COOKIE_SESSIONID.equals(cookieName) || Tools.JSESSIONID_COOKIE.equals(cookieName));
        }
    };

    private void removeCookie(final HttpServletRequest req, final HttpServletResponse resp) {
        Tools.deleteCookies(req, resp, COOKIE_MATCHER);
    }

    @Override
    protected void decrementRequests() {
        // Not used
    }

    @Override
    protected void incrementRequests() {
        // Not used
    }
}
