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

package com.openexchange.ajp13.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.activation.FileTypeMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import com.openexchange.server.impl.Version;

/**
 * {@link ServletContextWrapper} - A wrapper class for {@link ServletContext} interface.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ServletContextWrapper implements ServletContext {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ServletContextWrapper.class));

    private static final int OX_SERVLET_MAJOR = 6;

    private static final int OX_SERVLET_MINOR = 14;

    // private static final int OX_SERVLET_PATCH = 0;

    /**
     * Return a context-relative path, beginning with a "/", that represents the canonical version of the specified path after ".." and "."
     * elements are resolved out. If the specified path attempts to go outside the boundaries of the current context (i.e. too many ".."
     * path elements are present), return <code>null</code> instead.
     *
     * @param path Path to be normalized
     */
    private static String normalize(final String path) {
        if (path == null) {
            return null;
        }
        String normalized = path;
        /*
         * Normalize the slashes and add leading slash if necessary
         */
        if (normalized.indexOf('\\') >= 0) {
            normalized = normalized.replace('\\', '/');
        }
        /*
         * Resolve occurrences of "/../" in the normalized path
         */
        final StringBuilder tmp = new StringBuilder(normalized.length());
        while (true) {
            final int index = normalized.indexOf("/../");
            if (index < 0) {
                break;
            }
            if (0 == index) {
                /*
                 * Trying to go outside our context
                 */
                return (null);
            }
            final int index2 = normalized.lastIndexOf('/', index - 1);
            tmp.setLength(0);
            normalized = tmp.append(normalized.substring(0, index2)).append(normalized.substring(index + 3)).toString();
        }
        /*
         * Return the normalized path
         */
        return (normalized);

    }

    /**
     * The context attributes for this context.
     */
    private final Map<String, Object> attributes = new HashMap<String, Object>();

    private String resourceDir;

    private final ServletConfigWrapper servletConfigWrapper;

    public ServletContextWrapper(final ServletConfigWrapper servletConfigWrapper) {
        this.servletConfigWrapper = servletConfigWrapper;
    }

    @Override
    public Object getAttribute(final String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<?> getAttributeNames() {
        return new Enumeration<String>() {

            private final Iterator<String> iterator = attributes.keySet().iterator();

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public String nextElement() {
                return iterator.next();
            }
        };
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public ServletContext getContext(final String uri) {
        if ((uri == null) || (uri.charAt(0) != '/')) {
            return null;
        }
        return null;
    }

    @Override
    public String getInitParameter(final String name) {
        return servletConfigWrapper.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return servletConfigWrapper.getInitParameterNames();
    }

    @Override
    public int getMajorVersion() {
        // E.g. 6.13.0-Rev5
        final String version = Version.getVersionString();
        final int pos = version.indexOf('.');
        if (pos > 0) {
            try {
                return Integer.parseInt(version.substring(0, pos));
            } catch (final NumberFormatException e) {
                return OX_SERVLET_MAJOR;
            }
        }
        return OX_SERVLET_MAJOR;
    }

    @Override
    public String getMimeType(final String fileName) {
        return FileTypeMap.getDefaultFileTypeMap().getContentType(fileName);
    }

    @Override
    public int getMinorVersion() {
        // E.g. 6.13.0-Rev5
        final String[] tokens = Version.getVersionString().split("\\.");
        if (tokens.length < 2) {
            return OX_SERVLET_MINOR;
        }
        try {
            return Integer.parseInt(tokens[1]);
        } catch (final NumberFormatException e) {
            return OX_SERVLET_MINOR;
        }
    }

    @Override
    public RequestDispatcher getNamedDispatcher(final String string) {
        return null;
    }

    @Override
    public String getRealPath(final String string) {
        return string;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(final String string) {
        return null;
    }

    @Override
    public URL getResource(final String string) throws MalformedURLException {
        return null;
    }

    @Override
    public InputStream getResourceAsStream(final String path) {
        return null;
    }

    @Override
    public Set<?> getResourcePaths(final String string) {
        return null;
    }

    @Override
    public String getServerInfo() {
        return "THE SERVER INFO";
    }

    @Override
    public Servlet getServlet(final String string) throws ServletException {
        return null;
    }

    @Override
    public String getServletContextName() {
        return servletConfigWrapper.getServletName();
    }

    @Override
    public Enumeration<?> getServletNames() {
        return null;
    }

    @Override
    public Enumeration<?> getServlets() {
        return null;
    }

    @Override
    public void log(final Exception exception, final String string) {
        if (LOG.isInfoEnabled()) {
            LOG.info(string, exception);
        }
    }

    @Override
    public void log(final String string) {
        if (LOG.isInfoEnabled()) {
            LOG.info(string);
        }
    }

    @Override
    public void log(final String string, final Throwable throwable) {
        if (LOG.isInfoEnabled()) {
            LOG.info(string, throwable);
        }
    }

    @Override
    public void removeAttribute(final String name) {
        attributes.remove(name);
    }

    @Override
    public void setAttribute(final String name, final Object value) {
        attributes.put(name, value);
    }

}
