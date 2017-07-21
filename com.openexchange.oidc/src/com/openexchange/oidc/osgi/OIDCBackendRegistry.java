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
package com.openexchange.oidc.osgi;

import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.java.ConcurrentList;
import com.openexchange.java.Strings;
import com.openexchange.oidc.OIDCConfig;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.OIDCWebSSOProvider;
import com.openexchange.oidc.http.InitService;
import com.openexchange.oidc.impl.OIDCWebSSOProviderImpl;
import com.openexchange.oidc.spi.OIDCBackend;
import com.openexchange.oidc.spi.OIDCExceptionHandler;
import com.openexchange.oidc.state.CoreStateManagement;
import com.openexchange.oidc.tools.OIDCCoreValidator;
import com.openexchange.oidc.tools.OIDCValidatorImpl;
import com.openexchange.server.ServiceLookup;

/**
 * Registers and stores all OpenID backends and their servlets to handle future requests.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class OIDCBackendRegistry extends ServiceTracker<OIDCBackend, OIDCBackend>{
    
    private ServiceLookup services;
    private final ConcurrentList<OIDCBackend> backends;
    private ConcurrentHashMap<OIDCBackend, Stack<String>> backendServlets;
    private ConcurrentHashMap<OIDCBackend, Stack<ServiceRegistration<?>>> backendServiceRegistrations;

    //TODO QS-VS: comment
    public OIDCBackendRegistry(BundleContext context, ServiceLookup services) {
        super(context, OIDCBackend.class, null);
        this.services = services;
        this.backends = new ConcurrentList<>();
        this.backendServlets = new ConcurrentHashMap<>();
        this.backendServiceRegistrations = new ConcurrentHashMap<>();

    }
    
    @Override
    public OIDCBackend addingService(ServiceReference<OIDCBackend> reference) {
        final OIDCBackend oidcBackend = context.getService(reference);
        final Stack<String> servlets = new Stack<>();
        HttpService httpService = services.getService(HttpService.class);
        final Stack<ServiceRegistration<?>> serviceRegistrations = new Stack<ServiceRegistration<?>>();

        if (backends.addIfAbsent(oidcBackend)) {
            try {
                OIDCConfig config = oidcBackend.getOIDCConfig();
                String path = oidcBackend.getPath();
                if (config == null) {
                    throw OIDCExceptionCode.MISSING_BACKEND_CONFIGURATION.create(path.isEmpty() ? "No path available" : path);
                }
                if (!Strings.isEmpty(path)) {
                    validatePath(path);
                }
                OIDCWebSSOProvider ssoProvider = new OIDCWebSSOProviderImpl(oidcBackend, new CoreStateManagement(services.getService(HazelcastInstance.class)), new OIDCCoreValidator(oidcBackend));
                OIDCExceptionHandler exceptionHandler = oidcBackend.getExceptionHandler();
                
                registerServlet(servlets, httpService, getPrefix(oidcBackend), new InitService(ssoProvider, exceptionHandler, services, config), "init");
                
                return oidcBackend;
            } catch (OXException | ServletException | NamespaceException e) {
                
                while (!servlets.isEmpty()) {
                    httpService.unregister(servlets.pop());
                }
                while (!serviceRegistrations.isEmpty()) {
                    ServiceRegistration<?> pop = serviceRegistrations.pop();
                    if (null != pop) {
                        pop.unregister();
                    }
                }
                backends.remove(oidcBackend);
                context.ungetService(reference);
            } finally {
                if (!servlets.isEmpty()) {
                    backendServlets.putIfAbsent(oidcBackend, servlets);
                }
                if (!serviceRegistrations.isEmpty()) {
                    backendServiceRegistrations.putIfAbsent(oidcBackend, serviceRegistrations);
                }

            }
        }
        return null;
    }
    
    private String getPrefix(final OIDCBackend oidcBackend) {
        StringBuilder prefixBuilder = new StringBuilder();
        prefixBuilder.append(services.getService(DispatcherPrefixService.class).getPrefix());
        prefixBuilder.append("oidc/");
        String path = oidcBackend.getPath();
        if (!Strings.isEmpty(path)) {
            prefixBuilder.append(path).append("/");
        }
        return prefixBuilder.toString();
    }

    /**
     * Helper method that validates the path to only contain allowed characters
     * @param path The path to be checked.
     * @return
     */
    private void validatePath(String path) throws OXException{
        if (path.matches(".*[^a-zA-Z0-9].*")) {
            throw OIDCExceptionCode.INVALID_BACKEND_PATH.create(path);
        }
    }
    
    /**
     * Helper method to register a servlet
     * @param servlets the servlets stack of a SAMLBackend
     * @param httpService the HttpService where to register the servlet
     * @param prefix prefix of this SAMLBackend
     * @param servlet the servlet to be registered
     * @param part additional servlet path information
     * @throws ServletException if the servlet's init method throws an exception, or the given servlet object has already been registered at a different alias.
     * @throws NamespaceException if the registration fails because the alias is already in use.
     */
    private void registerServlet(final Stack<String> servlets, HttpService httpService, String prefix, Servlet servlet, String part) throws ServletException, NamespaceException {
        String servletName = prefix + part;
        httpService.registerServlet(servletName, servlet, null, null);
        servlets.push(servletName);
    }

    //TODO QS-VS: comment
    public void stop() {
        
    }
}
