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

package com.openexchange.authentication.kerberos.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.kerberos.impl.KerberosAuthentication;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.kerberos.KerberosService;
import com.openexchange.user.UserService;

/**
 * Dependently registers the AuthenticationService.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class AuthenticationRegisterer implements ServiceTrackerCustomizer<Object, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationRegisterer.class);

    private final BundleContext context;
    private ServiceRegistration<AuthenticationService> registration;
    private KerberosService kerberosService;
    private ContextService contextService;
    private UserService userService;
    private ConfigurationService configService;

    public AuthenticationRegisterer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public synchronized Object addingService(ServiceReference<Object> reference) {
        final Object obj = context.getService(reference);
        final boolean needsRegistration;
        {
            if (obj instanceof KerberosService) {
                kerberosService = (KerberosService) obj;
            }
            if (obj instanceof ContextService) {
                contextService = (ContextService) obj;
            }
            if (obj instanceof UserService) {
                userService = (UserService) obj;
            }
            if (obj instanceof ConfigurationService) {
                configService = (ConfigurationService) obj;
            }
            needsRegistration = null != kerberosService && null != contextService && null != userService && null != configService && registration == null;
        }
        if (needsRegistration) {
            LOG.info("Registering Kerberos authentication service.");
            registration = context.registerService(AuthenticationService.class, new KerberosAuthentication(
                kerberosService,
                contextService,
                userService,
                configService), null);
        }
        return obj;
    }

    @Override
    public void modifiedService(ServiceReference<Object> reference, Object service) {
        // Nothing to do.
    }

    @Override
    public synchronized void removedService(ServiceReference<Object> reference, Object service) {
        ServiceRegistration<AuthenticationService> unregister = null;
        {
            if (service instanceof ContextService) {
                contextService = null;
            }
            if (service instanceof UserService) {
                userService = null;
            }
            if (service instanceof KerberosService) {
                kerberosService = null;
            }
            if (service instanceof ConfigurationService) {
                configService = null;
            }
            if (registration != null && (null == contextService || null == userService || null == kerberosService)) {
                unregister = registration;
                registration = null;
            }
        }
        if (null != unregister) {
            LOG.info("Unregistering Kerberos authentication service.");
            unregister.unregister();
        }
        context.ungetService(reference);
    }
}
