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

package com.openexchange.messaging.generic;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.generic.internal.CachingMessagingAccountStorage;
import com.openexchange.messaging.generic.internal.Modifier;
import com.openexchange.session.Session;

/**
 * {@link DefaultMessagingAccountManager} - The default messaging account manager.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class DefaultMessagingAccountManager implements MessagingAccountManager {

    private static final class DefaultModifier implements Modifier {

        private final DefaultMessagingAccountManager manager;

        public DefaultModifier(final DefaultMessagingAccountManager manager) {
            super();
            this.manager = manager;
        }

        @Override
        public MessagingAccount modifyIncoming(final MessagingAccount account) throws OXException {
            return manager.modifyIncoming(account);
        }

        @Override
        public MessagingAccount modifyOutgoing(final MessagingAccount account) throws OXException {
            return manager.modifyOutgoing(account);
        }

    }

    /**
     * The messaging account storage cache.
     */
    private static final CachingMessagingAccountStorage CACHE = CachingMessagingAccountStorage.getInstance();

    /**
     * The identifier of associated messaging service.
     */
    private final String serviceId;

    private final MessagingService service;

    private final Modifier modifier;

    /**
     * Initializes a new {@link DefaultMessagingAccountManager}.
     *
     * @param service The messaging service
     */
    public DefaultMessagingAccountManager(final MessagingService service) {
        super();
        serviceId = service.getId();
        this.service = service;
        modifier = new DefaultModifier(this);
    }

    protected MessagingAccount modifyIncoming(final MessagingAccount account) throws OXException {
        return account;
    }

    protected MessagingAccount modifyOutgoing(final MessagingAccount account) throws OXException {
        return account;
    }

    @Override
    public MessagingAccount getAccount(final int id, final Session session) throws OXException {
        return CACHE.getAccount(serviceId, id, session, modifier);
    }

    @Override
    public List<MessagingAccount> getAccounts(final Session session) throws OXException {
        return CACHE.getAccounts(serviceId, session, modifier);
    }

    @Override
    public int addAccount(final MessagingAccount account, final Session session) throws OXException {
        return CACHE.addAccount(serviceId, account, session, modifier);
    }

    @Override
    public void deleteAccount(final MessagingAccount account, final Session session) throws OXException {
        CACHE.deleteAccount(serviceId, account, session, modifier);
    }

    @Override
    public void updateAccount(final MessagingAccount account, final Session session) throws OXException {
        CACHE.updateAccount(serviceId, account, session, modifier);
    }

    @Override
    public void migrateToNewSecret(final String oldSecret, final String newSecret, final Session session) throws OXException {
        CACHE.migrateToNewSecret(service, oldSecret, newSecret, session);
    }

    @Override
    public boolean hasAccount(final Session session) throws OXException {
        return CACHE.hasAccount(service, session);
    }

    @Override
    public void cleanUp(String secret, Session session) throws OXException {
        CACHE.cleanUp(service, secret, session);
    }
    
    @Override
    public void removeUnrecoverableItems(String secret, Session session) throws OXException {
        CACHE.removeUnrecoverableItems(service, secret, session);        
    }
}
