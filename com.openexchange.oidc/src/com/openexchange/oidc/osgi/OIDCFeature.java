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
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.oidc.OIDCConfig;
import com.openexchange.oidc.hz.PortableAuthenticationRequestFactory;
import com.openexchange.oidc.hz.PortableLogoutRequestFactory;
import com.openexchange.oidc.impl.OIDCSessionInspectorService;
import com.openexchange.osgi.DependentServiceStarter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.inspector.SessionInspectorService;

/**
 * Starts the OpenID feature.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class OIDCFeature extends DependentServiceStarter{

    private static final Logger LOG = LoggerFactory.getLogger(OIDCFeature.class);
    private final OIDCConfig config;
    private OIDCBackendRegistry oidcBackends;
    
    private final Stack<ServiceRegistration<?>> serviceRegistrations = new Stack<ServiceRegistration<?>>();

    private final static Class<?>[] OPTIONAL_SERVICES = new Class[] {
        HostnameService.class
    };
    
    public OIDCFeature(BundleContext context, Class<?>[] neededServices, OIDCConfig config) throws InvalidSyntaxException {
        super(context, neededServices, OPTIONAL_SERVICES);
        this.config = config;
    }

    @Override
    protected void start(ServiceLookup services) throws Exception {
        if(config.isEnabled()) {
            LOG.info("Starting core OpenID Connect support... ");
            getOIDCBackends(services);
            serviceRegistrations.push(context.registerService(SessionInspectorService.class, new OIDCSessionInspectorService(oidcBackends, context), null));
            serviceRegistrations.push(context.registerService(CustomPortableFactory.class, new PortableAuthenticationRequestFactory(), null));
            serviceRegistrations.push(context.registerService(CustomPortableFactory.class, new PortableLogoutRequestFactory(), null));
        } else {
            LOG.info("OpenID Connect support is disabled by configuration. Skipping initialization...");
        }
    }

    private void getOIDCBackends(ServiceLookup services) {
        if (this.oidcBackends == null) {
            this.oidcBackends = new OIDCBackendRegistry(context, services);
        }
        this.oidcBackends.open();
    }

    @Override
    protected void stop(ServiceLookup services) throws Exception {
        LOG.info("Stopping core OpenID Connect support... ");
        
        while (!serviceRegistrations.isEmpty()) {
            serviceRegistrations.pop().unregister();
        }
        if (this.oidcBackends != null) {
            this.oidcBackends.close();
            this.oidcBackends.stop();
            this.oidcBackends = null;
        }
    }
}
