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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.osgi;

import static com.openexchange.osgi.Tools.requireService;
import java.io.ByteArrayInputStream;
import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Stack;
import javax.servlet.ServletException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.caching.CacheService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.OAuthResourceService;
import com.openexchange.oauth.provider.internal.OAuthProviderServiceImpl;
import com.openexchange.oauth.provider.internal.OAuthResourceServiceImpl;
import com.openexchange.oauth.provider.internal.authcode.AbstractAuthorizationCodeProvider;
import com.openexchange.oauth.provider.internal.authcode.portable.PortableAuthCodeInfoFactory;
import com.openexchange.oauth.provider.internal.client.CachingOAuthClientStorage;
import com.openexchange.oauth.provider.internal.client.OAuthClientStorage;
import com.openexchange.oauth.provider.internal.client.RdbOAuthClientStorage;
import com.openexchange.oauth.provider.internal.grant.InMemoryGrantStorage;
import com.openexchange.oauth.provider.internal.grant.OAuthGrantStorage;
import com.openexchange.oauth.provider.internal.rmi.OAuthClientRmiImpl;
import com.openexchange.oauth.provider.rmi.OAuthClientRmi;
import com.openexchange.oauth.provider.servlets.AuthorizationEndpoint;
import com.openexchange.oauth.provider.servlets.TokenEndpoint;


/**
 * {@link OAuthProvider}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthProvider.class);

    private final OAuthProviderActivator activator;

    private final BundleContext context;

    private Stack<ServiceRegistration<?>> registeredServices;

    private Stack<String> registeredServlets;

    private boolean started;

    public OAuthProvider(OAuthProviderActivator activator, BundleContext context) {
        super();
        this.activator = activator;
        this.context = context;
    }

    /**
     * Initializes all services and servlets and takes care of their registration. Calling this method on an already started instance
     * is a no-op.
     *
     * @param authCodeProvider
     * @throws Exception
     */
    public synchronized void start(AbstractAuthorizationCodeProvider authCodeProvider) throws Exception {
        if (!started) {
            try {
                registeredServices = new Stack<>();
                registeredServlets = new Stack<>();

                OAuthClientStorage clientStorage = initClientStorage();
                OAuthGrantStorage grantStorage = new InMemoryGrantStorage();

                OAuthProviderServiceImpl oAuthProvider = new OAuthProviderServiceImpl(authCodeProvider, clientStorage, grantStorage, activator);
                OAuthResourceServiceImpl resourceService = new OAuthResourceServiceImpl(clientStorage, grantStorage);
                registeredServices.add(context.registerService(OAuthResourceService.class, resourceService, null));
                registeredServices.add(context.registerService(CustomPortableFactory.class, new PortableAuthCodeInfoFactory(), null));

                Dictionary<String, Object> props = new Hashtable<String, Object>(2);
                props.put("RMIName", OAuthClientRmi.RMI_NAME);
                registeredServices.add(context.registerService(Remote.class, new OAuthClientRmiImpl(oAuthProvider), props));

                registerServlets(oAuthProvider);
                started = true;
            } catch (Exception e) {
                LOG.error("Could not initialize OAuth provider", e);
                unregisterServlets();
                unregisterServices();
                throw e;
            }
        }
    }

    /**
     * Closes all open resources and un-registeres all services and servlets. Calling this method on a stopped instance
     * is a no-op.
     *
     * @throws Exception
     */
    public synchronized void stop() throws Exception {
        if (started) {
            try {
                unregisterServlets();
                unregisterServices();
                registeredServlets = null;
                registeredServices = null;
            } finally {
                started = false;
            }
        }
    }

    private void unregisterServices() {
        while (!registeredServices.isEmpty()) {
            ServiceRegistration<?> service = registeredServices.pop();
            try {
                service.unregister();
            } catch (Exception e) {
                LOG.error("Could not unregister service '{}'", service, e);
            }
        }
    }

    private void registerServlets(OAuthProviderService oAuthProvider) throws ServletException, NamespaceException, OXException {
        AuthorizationEndpoint authorizationEndpoint = new AuthorizationEndpoint(oAuthProvider, activator);
        TokenEndpoint tokenEndpoint = new TokenEndpoint(oAuthProvider);

        HttpService httpService = requireService(HttpService.class, activator);
        DispatcherPrefixService dispatcherPrefixService = requireService(DispatcherPrefixService.class, activator);
        String authorizationEndpointAlias = dispatcherPrefixService.getPrefix() + OAuthProviderConstants.AUTHORIZATION_SERVLET_ALIAS;
        httpService.registerServlet(authorizationEndpointAlias, authorizationEndpoint, null, httpService.createDefaultHttpContext());
        registeredServlets.add(authorizationEndpointAlias);
        String tokenEndpointAlias = dispatcherPrefixService.getPrefix() + OAuthProviderConstants.ACCESS_TOKEN_SERVLET_ALIAS;
        httpService.registerServlet(tokenEndpointAlias, tokenEndpoint, null, httpService.createDefaultHttpContext());
        registeredServlets.add(tokenEndpointAlias);
    }

    private void unregisterServlets() {
        ServiceReference<HttpService> httpServiceRef = context.getServiceReference(HttpService.class);
        if (httpServiceRef != null) {
            try {
                HttpService httpService = context.getService(httpServiceRef);
                if (httpService != null) {
                    while (!registeredServlets.isEmpty()) {
                        String alias = registeredServlets.pop();
                        try {
                            httpService.unregister(alias);
                        } catch (Exception e) {
                            LOG.error("Could not unregister servlet '{}'", alias, e);
                        }
                    }
                }
            } finally {
                context.ungetService(httpServiceRef);
            }
        }
    }

    private OAuthClientStorage initClientStorage() throws Exception {
        String regionName = CachingOAuthClientStorage.REGION_NAME;
        byte[] ccf = ("jcs.region."+regionName+"=LTCP\n" +
            "jcs.region."+regionName+".cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes\n" +
            "jcs.region."+regionName+".cacheattributes.MaxObjects=100000\n" +
            "jcs.region."+regionName+".cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache\n" +
            "jcs.region."+regionName+".cacheattributes.UseMemoryShrinker=true\n" +
            "jcs.region."+regionName+".cacheattributes.MaxMemoryIdleTimeSeconds=360\n" +
            "jcs.region."+regionName+".cacheattributes.ShrinkerIntervalSeconds=60\n" +
            "jcs.region."+regionName+".elementattributes=org.apache.jcs.engine.ElementAttributes\n" +
            "jcs.region."+regionName+".elementattributes.IsEternal=false\n" +
            "jcs.region."+regionName+".elementattributes.MaxLifeSeconds=-1\n" +
            "jcs.region."+regionName+".elementattributes.IdleTime=360\n" +
            "jcs.region."+regionName+".elementattributes.IsSpool=false\n" +
            "jcs.region."+regionName+".elementattributes.IsRemote=false\n" +
            "jcs.region."+regionName+".elementattributes.IsLateral=false\n").getBytes();
        activator.getService(CacheService.class).loadConfiguration(new ByteArrayInputStream(ccf), true);
        return new CachingOAuthClientStorage(new RdbOAuthClientStorage(activator), activator);
    }

}
