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

package com.openexchange.share.servlet.osgi;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.dispatcher.DispatcherPrefixService;
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
        httpService.unregister(alias);
        LOG.info("PasswordResetServlet successfully unregistered");
    }

}
