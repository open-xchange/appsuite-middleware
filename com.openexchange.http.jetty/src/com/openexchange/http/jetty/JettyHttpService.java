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

package com.openexchange.http.jetty;

import java.util.Dictionary;
import java.util.Enumeration;
import javax.servlet.Servlet;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

/**
 * {@link JettyHttpService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class JettyHttpService implements HttpService {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(JettyHttpService.class));

    private Server server;

    private ServletContextHandler root;

    public JettyHttpService(int port) {
        server = new Server();

        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port);
        connector.setForwarded(true);

        server.setConnectors(new Connector[]{ connector });

        root = new ServletContextHandler(ServletContextHandler.SESSIONS);
        root.setContextPath("/");
        server.setHandler(root);

    }

    @Override
    public HttpContext createDefaultHttpContext() {
        return null; // Let's see if we can get away with this
    }

    @Override
    public void registerResources(String alias, String name, HttpContext context) {
        // TODO
    }

    @Override
    public void registerServlet(String alias, Servlet servlet, Dictionary initparams, HttpContext context)  {
        try {
            if (!alias.startsWith("/")) {
                alias = "/" + alias;
            }

            ServletHolder holder = new ServletHolder(servlet);
            if (initparams != null) {
                Enumeration elements = initparams.elements();
                while (elements.hasMoreElements()) {
                    Object key = elements.nextElement();
                    if(key == null) {
                        continue;
                    }

                    Object value = initparams.get(key);



                    holder.setInitParameter((String)key, (String)value);
                }
            }
            root.addServlet(holder, alias);
            if(!alias.endsWith("/*")) {
                root.addServlet(holder, alias+"/*");
            }
        } catch (Throwable t) {
            // TODO: Better Exceptionhandling
            LOG.fatal(t.getMessage(), t);
        }
    }

    @Override
    public void unregister(String alias) {

    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
        server = null;
        root = null;
    }

}
