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

import java.io.IOException;
import java.lang.reflect.Method;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.config.ConfigurationService;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.CountingHttpServletRequest;
import com.openexchange.tools.servlet.ratelimit.RateLimitedException;

/**
 * {@link WebDavServlet} - An abstract class for servlets serving WebDAV requests
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class WebDavServlet extends HttpServlet {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = 4869234414872430531L;

    protected WebDavServlet() {
        super();
    }

    protected int getDavClass() {
        return 1;
    }

    /**
     * {@inheritDoc} TODO Improve discovery of methods.
     */
    @Override
    protected void doOptions(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final Method[] methods = getClass().getMethods();
        final Class<WebDavServlet> clazz = WebDavServlet.class;
        final Class<?> superClazz = WebDavServlet.class.getSuperclass();
        final StringBuilder allow = new StringBuilder(64);
        for (int i = 0; i < methods.length; i++) {
            if ("doGet".equals(methods[i].getName()) && !clazz.equals(methods[i].getDeclaringClass()) && !superClazz.equals(methods[i].getDeclaringClass())) {
                allow.append("GET,HEAD,");
            } else if ("doDelete".equals(methods[i].getName()) && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("DELETE,");
            } else if ("doPut".equals(methods[i].getName()) && !clazz.equals(methods[i].getDeclaringClass()) && !superClazz.equals(methods[i].getDeclaringClass())) {
                allow.append("PUT,");
            } else if ("doPost".equals(methods[i].getName()) && !clazz.equals(methods[i].getDeclaringClass()) && !superClazz.equals(methods[i].getDeclaringClass())) {
                allow.append("POST,");
            } else if ("doPropFind".equals(methods[i].getName()) && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("PROPFIND,");
            } else if ("doPropPatch".equals(methods[i].getName()) && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("PROPPATCH,");
            } else if ("doMkCol".equals(methods[i].getName()) && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("MKCOL,");
            } else if ("doCopy".equals(methods[i].getName()) && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("COPY,");
            } else if ("doMove".equals(methods[i].getName()) && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("MOVE,");
            } else if ("doLock".equals(methods[i].getName()) && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("LOCK,");
            } else if ("doUnLock".equals(methods[i].getName()) && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("UNLOCK,");
            } else if ("doReport".equals(methods[i].getName()) && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("REPORT,");
            } else if ("doMkCalendar".equals(methods[i].getName()) && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("MKCALENDAR,");
            } else if ("doAcl".equals(methods[i].getName()) && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("ACL,");
            }
        }
        allow.append("TRACE,OPTIONS");
        if (1 == getDavClass()) {
            resp.setHeader("DAV", "1");
        } else if (getDavClass() == 2) {
            resp.setHeader("DAV", "1,2");
        }
        resp.setHeader("Allow", allow.toString());
    }

    protected void doPropFind(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Method \"PROPFIND\" is not supported by this servlet");
    }

    protected void doPropPatch(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Method \"PROPPATCH\" is not supported by this servlet");
    }

    protected void doMkCol(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Method \"PROPPATCH\" is not supported by this servlet");
    }

    protected void doCopy(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Method \"COPY\" is not supported by this servlet");
    }

    protected void doMove(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Method \"MOVE\" is not supported by this servlet");
    }

    protected void doLock(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Method \"LOCK\" is not supported by this servlet");
    }

    protected void doUnLock(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Method \"UNLOCK\" is not supported by this servlet");
    }

    protected void doReport(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Method \"REPORT\" is not supported by this servlet");
    }

    protected void doMkCalendar(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Method \"MKCALENDAR\" is not supported by this servlet");
    }

    protected void doAcl(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Method \"ACL\" is not supported by this servlet");
    }

    protected boolean isDisabledByProperty() {
        ConfigurationService config = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        return config.getBoolProperty("com.openexchange.webdav.disabled", true);
    }

    protected String getDisabledMessage() {
        ConfigurationService config = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        String text = config.getText("webdav-disabled-message.txt");
        return text;
    }

    /**
     * Override this and return true to disable the servlet. In this case an error message will be sent to the client.
     * Whether WebDav servlets can be disabled at all has to be specified with "com.openexchange.webdav.disabled=true"
     * in server.properties. The error message may be customized within the property "com.openexchange.webdav.errorMessage".
     *
     * @return
     */
    protected boolean isServletDisabled() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse resp) throws ServletException, IOException {
        if (isServletDisabled() && isDisabledByProperty()) {
            resp.setContentType("text/html; charset=UTF-8");
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, getDisabledMessage());
            return;
        }
        incrementRequests();
        try {
            final HttpServletRequest req = new CountingHttpServletRequest(request);
            final String method = req.getMethod();
            if ("PROPFIND".equals(method)) {
                doPropFind(req, resp);
            } else if ("PROPPATCH".equals(method)) {
                doPropPatch(req, resp);
            } else if ("MKCOL".equals(method)) {
                doMkCol(req, resp);
            } else if ("COPY".equals(method)) {
                doCopy(req, resp);
            } else if ("MOVE".equals(method)) {
                doMove(req, resp);
            } else if ("LOCK".equals(method)) {
                doLock(req, resp);
            } else if ("UNLOCK".equals(method)) {
                doUnLock(req, resp);
            } else if ("REPORT".equals(method)) {
                doReport(req, resp);
            } else if ("MKCALENDAR".equals(method)) {
                doMkCalendar(req, resp);
            } else if ("ACL".equals(method)) {
                doAcl(req, resp);
            } else {
                super.service(req, resp);
            }
        } catch (RateLimitedException e) {
            e.send(resp);
        } finally {
            decrementRequests();
        }
    }

    /**
     * Increments the number of requests to servlet at the very beginning of {@link #service(HttpServletRequest, HttpServletResponse)
     * service} method. Default implementation uses type {@link MonitoringInfo#WEBDAV_USER}, override if applicable.
     */
    protected void incrementRequests() {
        MonitoringInfo.incrementNumberOfConnections(MonitoringInfo.WEBDAV_USER);
    }

    /**
     * Decrements the number of requests to servlet at the very end of {@link #service(HttpServletRequest, HttpServletResponse) service}
     * method. Default implementation uses type {@link MonitoringInfo#WEBDAV_USER}, override if applicable.
     */
    protected void decrementRequests() {
        MonitoringInfo.decrementNumberOfConnections(MonitoringInfo.WEBDAV_USER);
    }

    /**
     * Status code (207) indicating that the returned content contains XML for WebDAV.
     */
    public static final int SC_MULTISTATUS = 207;

    /**
     * Status code (423) indicating that the requested resource is locked.
     */
    public static final int SC_LOCKED = 423;
}
