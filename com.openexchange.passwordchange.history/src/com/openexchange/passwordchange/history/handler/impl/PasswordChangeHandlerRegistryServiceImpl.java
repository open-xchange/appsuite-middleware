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

package com.openexchange.passwordchange.history.handler.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.java.Strings;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.passwordchange.history.handler.PasswordChangeHandlerRegistryService;
import com.openexchange.passwordchange.history.handler.PasswordHistoryHandler;

/**
 * {@link PasswordChangeTrackerRegistryImpl} - Implementation of {@link PasswordChangeHandlerRegistryService} as an {@link ServiceTrackerCustomizer}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
@SingletonService
public class PasswordChangeHandlerRegistryServiceImpl implements ServiceTrackerCustomizer<PasswordHistoryHandler, PasswordHistoryHandler>, PasswordChangeHandlerRegistryService {

    private static final org.slf4j.Logger       LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeHandlerRegistryServiceImpl.class);
    private final ConcurrentMap<String, PasswordHistoryHandler> handlers;
    private final BundleContext                 context;

    /**
     * Initializes a new {@link PasswordChangeTrackerRegistryImpl}.
     */
    public PasswordChangeHandlerRegistryServiceImpl(BundleContext context) {
        super();
        this.handlers = new ConcurrentHashMap<>();
        this.context = context;
    }

    /**
     * Register a new {@link PasswordHistoryHandler}
     *
     * @param handler The actual handler
     * @return <code>true</code> if successfully added to this registry; otherwise <code>false</code>
     */
    public boolean register(PasswordHistoryHandler handler) {
        if (null == handler) {
            return false;
        }

        String symbolicName = handler.getSymbolicName();
        if (Strings.isEmpty(symbolicName)) {
            LOG.debug("Could not add PasswordHistoryHandler for name {}", symbolicName);
            return false;
        }

        return null == this.handlers.putIfAbsent(symbolicName, handler);
    }

    /**
     * Unregister a {@link PasswordHistoryHandler}
     *
     * @param symbolicName The name of the {@link PasswordHistoryHandler}
     */
    public void unregister(String symbolicName) {
        if (false == Strings.isEmpty(symbolicName)) {
            LOG.debug("Try to remove PasswordChangeTracker with name {}", symbolicName);
            this.handlers.remove(symbolicName);
        }
    }

    @Override
    public Map<String, PasswordHistoryHandler> getHandlers() {
        return this.handlers;
    }

    @Override
    public PasswordHistoryHandler getHandler(String symbolicName) {
        if (false == Strings.isEmpty(symbolicName)) {
            return this.handlers.get(symbolicName);
        }
        return null;
    }

    // ----------------------------------------------------- ServiceTracker stuff ----------------------------------------------------------

    @Override
    public PasswordHistoryHandler addingService(ServiceReference<PasswordHistoryHandler> reference) {
        PasswordHistoryHandler handler = context.getService(reference);
        boolean added = register(handler);
        if (added) {
            return handler;
        }

        LOG.warn("Could not add {}. A handler with that symbolic name '{}' already exists", handler.getClass().getName(), handler.getSymbolicName());
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<PasswordHistoryHandler> reference, PasswordHistoryHandler service) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<PasswordHistoryHandler> reference, PasswordHistoryHandler service) {
        unregister(service.getSymbolicName());
        context.ungetService(reference);
    }
}
