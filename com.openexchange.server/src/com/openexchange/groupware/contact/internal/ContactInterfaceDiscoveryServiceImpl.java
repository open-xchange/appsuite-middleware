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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.contact.internal;

import java.util.List;
import com.openexchange.api2.RdbContactSQLImpl;
import com.openexchange.concurrent.TimeoutConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.contact.ContactInterfaceProvider;
import com.openexchange.groupware.contact.ContactInterfaceProviderRegistration;
import com.openexchange.groupware.contact.ContactInterfaceProviderRegistry;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ContactInterfaceDiscoveryServiceImpl} - The {@link ContactInterfaceDiscoveryService} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ContactInterfaceDiscoveryServiceImpl implements ContactInterfaceDiscoveryService {

    private static volatile ContactInterfaceDiscoveryServiceImpl instance;

    /**
     * Gets the {@link ContactInterfaceDiscoveryService} implementation.
     *
     * @return The {@link ContactInterfaceDiscoveryService} implementation
     */
    public static ContactInterfaceDiscoveryServiceImpl getInstance() {
        return instance;
    }

    /**
     * Initializes the {@link ContactInterfaceDiscoveryService} implementation.
     *
     * @throws OXException If initialization fails
     */
    static void initInstance() throws OXException {
        instance = new ContactInterfaceDiscoveryServiceImpl();
    }

    /**
     * Releases the {@link ContactInterfaceDiscoveryService} implementation.
     */
    static void releaseInstance() {
        instance.dispose();
        instance = null;
    }

    /*-
     * Member section
     */

    private final RdbContactInterfaceProviderCache rdbProviderCache;

    /**
     * Initializes a new {@link ContactInterfaceDiscoveryServiceImpl}.
     *
     * @throws OXException If initialization fails
     */
    private ContactInterfaceDiscoveryServiceImpl() throws OXException {
        super();
        try {
            rdbProviderCache = new RdbContactInterfaceProviderCache();
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    /**
     * Disposes this {@link ContactInterfaceDiscoveryService} implementation.
     */
    private void dispose() {
        rdbProviderCache.dispose();
    }

    @Override
    public ContactInterfaceProvider getContactInterfaceProvider(final int folderId, final int contextId) throws OXException {
        final ContactInterfaceProvider provider = ContactInterfaceProviderRegistry.getInstance().getService(folderId, contextId);
        if (provider == null) {
            try {
                return rdbProviderCache.getProvider(ContextStorage.getStorageContext(contextId));
            } catch (final OXException e) {
                throw new OXException(e);
            }
        }
        return provider;
    }

    @Override
    public ContactInterface newContactInterface(final int folderId, final Session session) throws OXException {
        final int contextId = session.getContextId();
        final ContactInterfaceProvider provider = ContactInterfaceProviderRegistry.getInstance().getService(folderId, contextId);
        if (provider == null) {
            return rdbBySession(session, contextId);
        }
        return provider.newContactInterface(session);
    }

    @Override
    public boolean hasSpecificContactInterface(final int folderId, final int contextId) {
        return (null != ContactInterfaceProviderRegistry.getInstance().getService(folderId, contextId));
    }

    @Override
    public ContactInterface newDefaultContactInterface(final Session session) throws OXException {
        return rdbBySession(session, session.getContextId());
    }

    private ContactInterface rdbBySession(final Session session, final int contextId) throws OXException {
        if (session instanceof ServerSession) {
            return rdbProviderCache.getProvider(((ServerSession) session).getContext()).newContactInterface(session);
        }
        try {
            return rdbProviderCache.getProvider(ContextStorage.getStorageContext(contextId)).newContactInterface(session);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    @Override
    public List<ContactInterfaceProviderRegistration> getRegistrations(final int contextId) {
        return ContactInterfaceProviderRegistry.getInstance().getRegistrations(contextId);
    }


    private static final class RdbContactInterfaceProvider implements ContactInterfaceProvider {

        private final Context ctx;

        public RdbContactInterfaceProvider(final Context ctx) {
            super();
            this.ctx = ctx;
        }

        @Override
        public ContactInterface newContactInterface(final Session session) {
            return new RdbContactSQLImpl(session, ctx);
        }
    } // End of RdbContactInterfaceProvider

    private static final class RdbContactInterfaceProviderCache {

        private final TimeoutConcurrentMap<Integer, RdbContactInterfaceProvider> map;

        public RdbContactInterfaceProviderCache() throws OXException {
            super();
            this.map = new TimeoutConcurrentMap<Integer, RdbContactInterfaceProvider>(20);
        }

        public RdbContactInterfaceProvider getProvider(final Context ctx) {
            final Integer key = Integer.valueOf(ctx.getContextId());
            RdbContactInterfaceProvider retval = map.get(key);
            if (null == retval) {
                retval = new RdbContactInterfaceProvider(ctx);
                final RdbContactInterfaceProvider prev = map.putIfAbsent(key, retval, 300);
                if (prev != null) {
                    retval = prev;
                }
            }
            return retval;
        }

        public void dispose() {
            map.dispose();
        }
    } // End of RdbContactInterfaceProviderCache


}
