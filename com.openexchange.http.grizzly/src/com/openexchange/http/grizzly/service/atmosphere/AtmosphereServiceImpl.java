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

package com.openexchange.http.grizzly.service.atmosphere;

import java.util.EnumSet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import org.apache.commons.logging.Log;
import org.atmosphere.container.Grizzly2WebSocketSupport;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.Broadcaster;
import org.glassfish.grizzly.http.server.OXHttpServer;
import org.glassfish.grizzly.servlet.DispatcherType;
import org.glassfish.grizzly.servlet.FilterRegistration;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.osgi.framework.Bundle;
import com.openexchange.config.ConfigurationService;
import com.openexchange.http.grizzly.osgi.GrizzlyServiceRegistry;
import com.openexchange.http.grizzly.servletfilter.WrappingFilter;


/**
 * {@link AtmosphereServiceImpl}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class AtmosphereServiceImpl  implements AtmosphereService {

    private final static Log LOG = com.openexchange.log.Log.loggerFor(AtmosphereServiceImpl.class);
    private final AtmosphereFramework atmosphereFramework;
    private final String atmosphereServletMapping;

    public AtmosphereServiceImpl(OXHttpServer grizzly, Bundle bundle) throws ServletException {

        ConfigurationService configurationService = GrizzlyServiceRegistry.getInstance().getService(ConfigurationService.class);
        String realtimeContextPath = configurationService.getProperty("com.openexchange.http.realtime.contextPath", "/realtime");
        atmosphereServletMapping = configurationService.getProperty("com.openexchange.http.atmosphere.servletMapping", "/atmosphere/*");

        WebappContext realtimeContext = new WebappContext("Realtime context", realtimeContextPath);
        FilterRegistration filterRegistration = realtimeContext.addFilter(WrappingFilter.class.getName(), new WrappingFilter());
        filterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), "/*");

        AtmosphereServlet atmosphereServlet = new AtmosphereServlet(false, false);
        SynchronizedHttpServletWrapper synchronizedAtmosphereServlet = new SynchronizedHttpServletWrapper(atmosphereServlet);
        atmosphereFramework = atmosphereServlet.framework();
        ServletConfig config = FrameworkConfig.with("Atmosphere Servlet", realtimeContext)
            .and("org.atmosphere.cpr.broadcasterLifeCyclePolicy","NEVER")
            .build();
        atmosphereFramework.init(config);
        atmosphereFramework.setAsyncSupport(new Grizzly2WebSocketSupport(atmosphereFramework.getAtmosphereConfig()));

        ServletRegistration atmosphereRegistration = realtimeContext.addServlet("AtmosphereServlet", synchronizedAtmosphereServlet);
        atmosphereRegistration.addMapping(atmosphereServletMapping);
        atmosphereRegistration.setLoadOnStartup(0);

        //Deliver js lib matching the serverside lib
//        ServletRegistration atmosphereJSRegistration = realtimeContext.addServlet("AtmosphereJSServlet", new AtmosphereJSServlet(bundle));
//        atmosphereJSRegistration.addMapping("/atmosphere/jquery.atmosphere.js");
//        atmosphereJSRegistration.setLoadOnStartup(0);

        realtimeContext.deploy(grizzly);
    }

    /**
     * Strip the trailing "/*" characters if a path mapping is used for the
     * registration of the {@link AtmosphereServlet}.
     * @param mapping the configured mapping for the {@link AtmosphereServlet}
     * @return the servletPath configured for the {@link AtmosphereServlet}
     */
    private String getServletPathFromMapping(String mapping) {
        if(mapping == null) {
            throw new IllegalArgumentException();
        }
        if (!mapping.isEmpty() && mapping.startsWith("/") && mapping.endsWith("/*")) {
            // remove the last two characters iow. "/*"
            return mapping.substring(0, mapping.length()-2);
        } else {
            // return empty string to reach the default servlet
            return "";
        }
    }

    /**
     * Concatenate servletPath and handlerMapping to form a mapping the Atmosphere
     * framework can use to map Requests to AtmosphereHandlers.
     * @param mapping the mapping the user chose for this AtmosphereHandler e.g. "/chat"
     * @return  valid mapping that prepends the ServletPath of the
     *           AtmosphereServlet to the chosen handlerMapping e.g.
     *           /atmosphere/chat
     */
    private String generateHandlerMapping(String servletMapping, String handlerMapping) {
        if(servletMapping == null || handlerMapping == null) {
            throw new IllegalArgumentException();
        }
        if(!handlerMapping.startsWith("/")) {
            handlerMapping = "/" + handlerMapping;
        }
        return getServletPathFromMapping(servletMapping) + handlerMapping;
    }

    @Override
    public void addAtmosphereHandler(String handlerMapping, AtmosphereHandler handler) {
        atmosphereFramework.addAtmosphereHandler(generateHandlerMapping(atmosphereServletMapping, handlerMapping), handler);
        LOG.info("Added AtmosphereHandler " + handler + " with mapping " + handlerMapping);
    }

    @Override
    public void addAtmosphereHandler(String handlerMapping, AtmosphereHandler handler, Broadcaster broadcaster) {
        atmosphereFramework.addAtmosphereHandler(generateHandlerMapping(atmosphereServletMapping, handlerMapping), handler, broadcaster);
        LOG.info("Added AtmosphereHandler " + handler + " with mapping " + handlerMapping + " and Broadcaster " + broadcaster);
    }

    @Override
    public void unregister(String handlerMapping) {
        atmosphereFramework.removeAtmosphereHandler(generateHandlerMapping(atmosphereServletMapping, handlerMapping));
        LOG.info("Removed AtmosphereHandler with mapping " + handlerMapping);
    }

}
