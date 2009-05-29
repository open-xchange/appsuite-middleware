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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.contact.ContactInterfaceProvider;
import com.openexchange.groupware.contact.ContactInterfaceProviderRegistry;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ContactInterfaceDiscoveryServiceImpl} - The {@link ContactInterfaceDiscoveryService} implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ContactInterfaceDiscoveryServiceImpl implements ContactInterfaceDiscoveryService {

    /**
     * Initializes a new {@link ContactInterfaceDiscoveryServiceImpl}.
     */
    public ContactInterfaceDiscoveryServiceImpl() {
        super();
    }

    public ContactInterfaceProvider getContactInterfaceProvider(final int folderId, final int contextId) throws OXException {
        final ContactInterfaceProvider provider = ContactInterfaceProviderRegistry.getInstance().getService(folderId, contextId);
        if (provider == null) {
            try {
                return new RdbContactInterfaceProvider(ContextStorage.getStorageContext(contextId));
            } catch (final ContextException e) {
                throw new OXException(e);
            }
        }
        return provider;
    }

    public ContactInterface getContactInterface(final int folderId, final Session session) throws OXException {
        final int contextId = session.getContextId();
        final ContactInterfaceProvider provider = ContactInterfaceProviderRegistry.getInstance().getService(folderId, contextId);
        if (provider == null) {
            if (session instanceof ServerSession) {
                return new RdbContactInterfaceProvider(((ServerSession) session).getContext()).newContactInterface(session);
            }
            try {
                return new RdbContactInterfaceProvider(ContextStorage.getStorageContext(contextId)).newContactInterface(session);
            } catch (final ContextException e) {
                throw new OXException(e);
            }
        }
        return provider.newContactInterface(session);
    }

    private static final class RdbContactInterfaceProvider implements ContactInterfaceProvider {

        private final Context ctx;

        public RdbContactInterfaceProvider(final Context ctx) {
            super();
            this.ctx = ctx;
        }

        public ContactInterface newContactInterface(final Session session) throws OXException {
            return new RdbContactSQLInterface(session, ctx);
        }
    }

}
