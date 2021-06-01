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
                    throw BrandingExceptionCodes.MISSING_PROPERTIES.create();
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
        } catch (OXException e) {
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
        } catch (OXException e) {
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
