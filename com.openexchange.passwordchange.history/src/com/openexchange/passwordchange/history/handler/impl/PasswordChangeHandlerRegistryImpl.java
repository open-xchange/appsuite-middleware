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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.passwordchange.history.handler.PasswordChangeHandlerRegistry;
import com.openexchange.passwordchange.history.handler.PasswordHistoryHandler;

/**
 * {@link PasswordChangeTrackerRegistryImpl}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordChangeHandlerRegistryImpl implements ServiceTrackerCustomizer<PasswordHistoryHandler, PasswordHistoryHandler>, PasswordChangeHandlerRegistry {

    private static final org.slf4j.Logger       LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeHandlerRegistryImpl.class);
    private Map<String, PasswordHistoryHandler> handler;
    private final BundleContext                 context;

    /**
     * Initializes a new {@link PasswordChangeTrackerRegistryImpl}.
     */
    public PasswordChangeHandlerRegistryImpl(BundleContext context) {
        super();
        this.handler = new ConcurrentHashMap<>();
        this.context = context;
    }

    /**
     * Register a new {@link PasswordHistoryHandler}
     * 
     * @param symbolicName The name to search the handler for
     * @param handler The actual handler
     */
    public synchronized void register(String symbolicName, PasswordHistoryHandler handler) {
        if (checkSymbolic(symbolicName) || null != handler) {
            this.handler.put(symbolicName, handler);
        } else {
            LOG.debug("Could not add PasswordHistoryHandler for name {}", symbolicName);
        }
    }

    /**
     * Unregister a {@link PasswordHistoryHandler}
     * 
     * @param symbolicName The name of the {@link PasswordHistoryHandler}
     */
    public synchronized void unregister(String symbolicName) {
        if (checkSymbolic(symbolicName)) {
            LOG.debug("Try to remove PasswordChangeTracker with name {}", symbolicName);
            this.handler.remove(symbolicName);
        }
    }

    @Override
    public synchronized Map<String, PasswordHistoryHandler> getHandlers() {
        return this.handler;
    }

    @Override
    public synchronized PasswordHistoryHandler getHandler(String symbolicName) {
        if (checkSymbolic(symbolicName)) {
            return this.handler.get(symbolicName);
        }
        return null;
    }

    private boolean checkSymbolic(String symbolicName) {
        return null != symbolicName && false == symbolicName.isEmpty();
    }

    @Override
    public PasswordHistoryHandler addingService(ServiceReference<PasswordHistoryHandler> reference) {
        PasswordHistoryHandler tracker = context.getService(reference);
        register(tracker.getSymbolicName(), tracker);
        return tracker;
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
