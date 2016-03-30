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

package com.openexchange.authentication.service.osgi;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.authentication.BasicAuthenticationService;
import com.openexchange.authentication.basic.DefaultBasicAuthentication;
import com.openexchange.authentication.service.Authentication;
import com.openexchange.context.ContextService;
import com.openexchange.user.UserService;

/**
 * Dependently registers the AuthenticationService.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class BasicAuthenticationRegisterer implements ServiceTrackerCustomizer<Object, Object> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BasicAuthenticationRegisterer.class);

    private final BundleContext context;
    private final Lock lock;

    private ServiceRegistration<BasicAuthenticationService> registration;
    private ContextService contextService;
    private UserService userService;

    /**
     * Initializes a new {@link BasicAuthenticationRegisterer}.
     *
     * @param context The bundle context
     */
    public BasicAuthenticationRegisterer(BundleContext context) {
        super();
        this.context = context;
        lock = new ReentrantLock();
    }

    @Override
    public Object addingService(final ServiceReference<Object> reference) {
        Object obj = context.getService(reference);
        lock.lock();
        try {
            if (obj instanceof ContextService) {
                contextService = (ContextService) obj;
            }
            if (obj instanceof UserService) {
                userService = (UserService) obj;
            }
            boolean needsRegistration = null != contextService && null != userService && registration == null;
            if (needsRegistration) {
                LOG.info("Registering default basic authentication service.");
                DefaultBasicAuthentication basicAuthentication = new DefaultBasicAuthentication(contextService, userService);
                registration = context.registerService(BasicAuthenticationService.class, basicAuthentication, null);
                Authentication.setBasicService(basicAuthentication);
            }
        } finally {
            lock.unlock();
        }
        return obj;
    }

    @Override
    public void modifiedService(final ServiceReference<Object> reference, final Object service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<Object> reference, final Object service) {
        ServiceRegistration<?> unregister = null;
        lock.lock();
        try {
            if (service instanceof ContextService) {
                contextService = null;
            }
            if (service instanceof UserService) {
                userService = null;
            }
            if (registration != null && (contextService == null || userService == null)) {
                unregister = registration;
                this.registration = null;
            }
            if (null != unregister) {
                LOG.info("Unregistering default basic authentication service.");
                unregister.unregister();
                Authentication.setBasicService(null);
            }
        } finally {
            lock.unlock();
        }
        context.ungetService(reference);
    }

}
