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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.net.ssl.config.impl.osgi;

import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.jslob.JSlobEntry;
import com.openexchange.net.ssl.config.UserAwareSSLConfigurationService;
import com.openexchange.net.ssl.config.impl.internal.UserAwareSSLConfigurationImpl;
import com.openexchange.net.ssl.config.impl.jslob.AcceptUntrustedCertificatesJSLobEntry;
import com.openexchange.osgi.Tools;
import com.openexchange.user.UserService;

/**
 * {@link UserAwareSSLConfigurationServiceRegisterer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class UserAwareSSLConfigurationServiceRegisterer implements ServiceTrackerCustomizer<Object, Object> {

    private final BundleContext context;
    private final ConfigViewFactory factory;

    private UserService userService = null;
    private ContextService contextService = null;
    private List<ServiceRegistration<?>> registrations = null;

    /**
     * Initializes a new {@link UserAwareSSLConfigurationServiceRegisterer}.
     *
     * @param factory The config-cascade service
     * @param context The bundle context
     */
    public UserAwareSSLConfigurationServiceRegisterer(ConfigViewFactory factory, BundleContext context) {
        super();
        this.factory = factory;
        this.context = context;
    }

    /**
     * Gets the filter expression for this registerer.
     *
     * @return The filter expression
     */
    public Filter getFilter() throws InvalidSyntaxException {
        return Tools.generateServiceFilter(context, UserService.class, ContextService.class);
    }

    @Override
    public synchronized Object addingService(ServiceReference<Object> reference) {
        // Exclusively accessed...
        Object service = context.getService(reference);
        if (service instanceof UserService) {
            this.userService = (UserService) service;
            update();
        } else if (service instanceof ContextService) {
            this.contextService = (ContextService) service;
            update();
        } else {
            // Of no need
            context.ungetService(reference);
            return null;
        }

        return service;
    }

    @Override
    public void modifiedService(ServiceReference<Object> reference, Object service) {
        // Ignore
    }

    @Override
    public synchronized void removedService(ServiceReference<Object> reference, Object service) {
        // Exclusively accessed...
        if (service instanceof UserService) {
            this.userService = null;
            update();
        } else if (service instanceof ContextService) {
            this.contextService = null;
            update();
        }

        context.ungetService(reference);
    }

    private void update() {
        ContextService contextService = this.contextService;
        if (null == contextService) {
            // Not all needed service available
            optUnregister();
            return;
        }

        UserService userService = this.userService;
        if (null == userService) {
            // Not all needed service available
            optUnregister();
            return;
        }

        List<ServiceRegistration<?>> registrations = this.registrations;
        if (null != registrations) {
            // Already registered
            return;
        }

        UserAwareSSLConfigurationImpl userAwareSSLConfigurationImpl = new UserAwareSSLConfigurationImpl(userService, contextService, factory);
        AcceptUntrustedCertificatesJSLobEntry jsLobEntry = new AcceptUntrustedCertificatesJSLobEntry(contextService, userAwareSSLConfigurationImpl);

        registrations = new ArrayList<>(2);
        this.registrations = registrations;
        registrations.add(context.registerService(UserAwareSSLConfigurationService.class, userAwareSSLConfigurationImpl, null));
        registrations.add(context.registerService(JSlobEntry.class, jsLobEntry, null));
    }

    private void optUnregister() {
        List<ServiceRegistration<?>> registrations = this.registrations;
        if (null != registrations) {
            this.registrations = null;
            for (ServiceRegistration<?> registration : registrations) {
                registration.unregister();
            }
        }
    }

}
