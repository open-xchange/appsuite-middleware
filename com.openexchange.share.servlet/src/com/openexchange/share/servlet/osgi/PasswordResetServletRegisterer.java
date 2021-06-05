/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.share.servlet.osgi;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.osgi.service.http.HttpServices;
import com.openexchange.share.servlet.internal.PasswordResetServlet;

/**
 * Dependently registers the share servlet.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PasswordResetServletRegisterer implements ServiceTrackerCustomizer<Object, Object> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordResetServletRegisterer.class);
    private static final String ALIAS_APPENDIX = "share/reset/password";

    private final BundleContext context;
    private final Lock lock;
    private final byte[] hashSalt;

    private String alias;
    private HttpService httpService;
    private DispatcherPrefixService prefixService;

    /**
     * Initializes a new {@link PasswordResetServletRegisterer}.
     *
     * @param context The bundle context
     * @param hashSalt The hash salt to use
     */
    public PasswordResetServletRegisterer(BundleContext context, byte[] hashSalt) {
        super();
        this.context = context;
        this.hashSalt = hashSalt;
        this.lock = new ReentrantLock();
    }

    @Override
    public Object addingService(final ServiceReference<Object> reference) {
        Object service = context.getService(reference);
        lock.lock();
        try {
            if (service instanceof HttpService) {
                httpService = (HttpService) service;
            }
            if (service instanceof DispatcherPrefixService) {
                prefixService = (DispatcherPrefixService) service;
            }
            boolean needsRegistration = null != httpService && null != prefixService && null == alias;
            if (needsRegistration) {
                alias = prefixService.getPrefix() + ALIAS_APPENDIX;
                if (false == registerServlet(alias, httpService)) {
                    alias = null;
                }
            }
        } finally {
            lock.unlock();
        }
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<Object> reference, final Object service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<Object> reference, final Object service) {
        lock.lock();
        try {
            HttpService hs;
            if (service instanceof HttpService) {
                httpService = null;
                hs = (HttpService) service;
            } else {
                hs = httpService;
            }
            if (service instanceof DispatcherPrefixService) {
                prefixService = null;
            }
            if (alias != null && (httpService == null || prefixService == null)) {
                String unregister = alias;
                this.alias = null;
                unregisterServlet(unregister, hs);
            }
        } finally {
            lock.unlock();
        }
        context.ungetService(reference);
    }

    private boolean registerServlet(String alias, HttpService httpService) {
        try {
            httpService.registerServlet(alias, new PasswordResetServlet(hashSalt), null, null);
            LOG.info("PasswordResetServlet successfully registered");
            return true;
        } catch (Exception e) {
            LOG.info("Failed to register PasswordResetServlet", e);
            return false;
        }
    }

    private void unregisterServlet(String alias, HttpService httpService) {
        HttpServices.unregister(alias, httpService);
        LOG.info("PasswordResetServlet successfully unregistered");
    }

}
