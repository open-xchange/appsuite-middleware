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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

/**
 * Abstrakte Klasse mit Funktionen, die jedes Servlet fuer WebDAV braucht.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class WebDavServlet extends HttpServlet {

    protected int getDavClass() {
        return 1;
    }

    /**
     * {@inheritDoc}
     * TODO Improve discovery of methods.
     */
    @Override
    protected void doOptions(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException, IOException {
        final Method[] methods = getClass().getMethods();
        final Class clazz = WebDavServlet.class;
        final Class superClazz = WebDavServlet.class.getSuperclass();
        final StringBuilder allow = new StringBuilder();
        for (int i = 0; i < methods.length; i++) {
            if ("doGet".equals(methods[i].getName())
                && !clazz.equals(methods[i].getDeclaringClass())
                && !superClazz.equals(methods[i].getDeclaringClass())) {
                allow.append("GET,HEAD,");
            } else if ("doDelete".equals(methods[i].getName())
                && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("DELETE,");
            } else if ("doPut".equals(methods[i].getName())
                && !clazz.equals(methods[i].getDeclaringClass())
                && !superClazz.equals(methods[i].getDeclaringClass())) {
                allow.append("PUT,");
            } else if ("doPost".equals(methods[i].getName())
                && !clazz.equals(methods[i].getDeclaringClass())
                && !superClazz.equals(methods[i].getDeclaringClass())) {
                allow.append("POST,");
            } else if ("doPropFind".equals(methods[i].getName())
                && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("PROPFIND,");
            } else if ("doPropPatch".equals(methods[i].getName())
                && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("PROPPATCH,");
            } else if ("doMkCol".equals(methods[i].getName())
                && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("MKCOL,");
            } else if ("doCopy".equals(methods[i].getName())
                && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("COPY,");
            } else if ("doMove".equals(methods[i].getName())
                && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("MOVE,");
            } else if ("doLock".equals(methods[i].getName())
                && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("LOCK,");
            } else if ("doUnLock".equals(methods[i].getName())
                && !clazz.equals(methods[i].getDeclaringClass())) {
                allow.append("UNLOCK,");
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

    protected void doPropFind(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "Method \"PROPFIND\" is not supported by this servlet");
    }

    protected void doPropPatch(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "Method \"PROPPATCH\" is not supported by this servlet");
    }

    protected void doMkCol(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "Method \"PROPPATCH\" is not supported by this servlet");
    }

    protected void doCopy(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "Method \"COPY\" is not supported by this servlet");
    }

    protected void doMove(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "Method \"MOVE\" is not supported by this servlet");
    }

    protected void doLock(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "Method \"LOCK\" is not supported by this servlet");
    }

    protected void doUnLock(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "Method \"UNLOCK\" is not supported by this servlet");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void service(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException, IOException {
        final String method = req.getMethod();
        if ("GET".equals(method) || "HEAD".equals(method)
            || "POST".equals(method) || "DELETE".equals(method)
            || "OPTIONS".equals(method) || "PUT".equals(method)
            || "TRACE".equals(method)) {
            super.service(req, resp);
        } else if ("PROPFIND".equals(method)) {
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
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Method \""
                + method + "\" is not supported by this servlet");
        }
    }

   /**
    * Status code (207) indicating that the returned content contains XML for
    * WebDAV.
    */
   public static final int SC_MULTISTATUS = 207;

   /**
    * Status code (423) indicating that the requested ressource is locked.
    */
   public static final int SC_LOCKED = 423;
}
