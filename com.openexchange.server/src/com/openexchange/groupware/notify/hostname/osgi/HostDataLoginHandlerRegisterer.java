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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.groupware.notify.hostname.osgi;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.groupware.notify.hostname.internal.HostDataLoginHandler;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.systemname.SystemNameService;

/**
 * {@link HostDataLoginHandlerRegisterer}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class HostDataLoginHandlerRegisterer implements ServiceTrackerCustomizer<SystemNameService, SystemNameService> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HostDataLoginHandlerRegisterer.class);

    private final Lock lock = new ReentrantLock();
    private final BundleContext context;
    private final HostDataLoginHandler loginHandler;

    private SystemNameService service = null;
    private ServiceRegistration<LoginHandlerService> registration = null;

    public HostDataLoginHandlerRegisterer(BundleContext context, HostDataLoginHandler loginHandler) {
        super();
        this.context = context;
        this.loginHandler = loginHandler;
    }

    @Override
    public SystemNameService addingService(ServiceReference<SystemNameService> reference) {
        SystemNameService foundService = context.getService(reference);
        boolean needsRegistration = false;
        lock.lock();
        try {
            if (null == service) {
                service = foundService;
                needsRegistration = null == registration;
            }
        } finally {
            lock.unlock();
        }
        if (!needsRegistration && registration != null) {
            LOG.warn("Found multiple SysteNameService instances. Ignoring {}", service.getClass().getName());
            foundService = null;
            context.ungetService(reference);
        }
        if (needsRegistration) {
            loginHandler.setSystemNameService(service);
            registration = context.registerService(LoginHandlerService.class, loginHandler, null);
        }
        return foundService;
    }

    @Override
    public void modifiedService(ServiceReference<SystemNameService> reference, SystemNameService foundService) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<SystemNameService> reference, SystemNameService foundService) {
        ServiceRegistration<LoginHandlerService> unregister = null;
        lock.lock();
        try {
            if (null != foundService && service == foundService) {
                loginHandler.setSystemNameService(null);
                service = null;
                unregister = registration;
                registration = null;
            }
        } finally {
            lock.unlock();
        }
        if (null != unregister) {
            unregister.unregister();
            context.ungetService(reference);
        }
    }
}
