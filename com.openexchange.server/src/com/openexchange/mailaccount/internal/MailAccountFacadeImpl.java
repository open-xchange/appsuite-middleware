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

package com.openexchange.mailaccount.internal;

import java.sql.Connection;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountFacade;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link MailAccountFacadeImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MailAccountFacadeImpl implements MailAccountFacade {

    /**
     * Initializes a new {@link MailAccountFacadeImpl}.
     */
    public MailAccountFacadeImpl() {
        super();

    }

    private MailAccountStorageService getStorageService() throws OXException {
        return ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
    }

    @Override
    public MailAccount getMailAccount(int id, int userId, int contextId) throws OXException {
        return getStorageService().getMailAccount(id, userId, contextId);
    }

    @Override
    public void deleteMailAccount(int id, Map<String, Object> properties, int userId, int contextId) throws OXException {
        getStorageService().deleteMailAccount(id, properties, userId, contextId);
    }

    @Override
    public int insertMailAccount(MailAccountDescription mailAccount, int userId, Context context, ServerSession session) throws OXException {
        return getStorageService().insertMailAccount(mailAccount, userId, context, session);
    }

    @Override
    public void updateMailAccount(MailAccountDescription mailAccount, Set<Attribute> fieldsToUpdate, int userId, int contextId, ServerSession session) throws OXException {
        getStorageService().updateMailAccount(mailAccount, userId, contextId, session);

    }

    @Override
    public MailAccount[] getUserMailAccounts(int userId, int contextId) throws OXException {
        return getStorageService().getUserMailAccounts(userId, contextId);
    }

    @Override
    public MailAccount getDefaultMailAccount(int userId, int contextId) throws OXException {
        return getStorageService().getDefaultMailAccount(userId, contextId);
    }

    @Override
    public void updateMailAccount(MailAccountDescription mailAccount, Set<Attribute> attributes, int userId, int contextId, ServerSession session, Connection wcon, boolean changePrimary) throws OXException {
        getStorageService().updateMailAccount(mailAccount, attributes, userId, contextId, session, wcon, changePrimary);

    }

    @Override
    public MailAccount getMailAccount(int accountId, int userId, int contextId, Connection con) throws OXException {
        return getStorageService().getMailAccount(accountId, userId, contextId, con);
    }

    @Override
    public int insertMailAccount(MailAccountDescription mailAccount, int userId, Context context, ServerSession session, Connection wcon) throws OXException {
        return getStorageService().insertMailAccount(mailAccount, userId, context, session, wcon);
    }

    @Override
    public void invalidateMailAccounts(int userId, int contextId) throws OXException {
        getStorageService().invalidateMailAccounts(userId, contextId);

    }

    @Override
    public void invalidateMailAccount(int accountId, int userId, int contextId) throws OXException {
        getStorageService().invalidateMailAccount(accountId, userId, contextId);

    }

    @Override
    public void clearFullNamesForMailAccount(int accountId, int userId, int contextId) throws OXException {
        getStorageService().clearFullNamesForMailAccount(accountId, userId, contextId);

    }

    @Override
    public void clearFullNamesForMailAccount(int accountId, int[] indexes, int userId, int contextId) throws OXException {
        getStorageService().clearFullNamesForMailAccount(accountId, indexes, userId, contextId);

    }

    @Override
    public MailAccount getRawMailAccount(int accountId, int userId, int contextId) throws OXException {
        return getStorageService().getRawMailAccount(accountId, userId, contextId);
    }

    @Override
    public int[] getByHostNames(Set<String> hostNames, int userId, int contextId) throws OXException {
        return getStorageService().getByHostNames(hostNames, userId, contextId);
    }

    @Override
    public void setFullNamesForMailAccount(int accountId, int[] indexes, String[] fullNames, int userId, int contextId) throws OXException {
        getStorageService().setFullNamesForMailAccount(accountId, indexes, fullNames, userId, contextId);

    }

    @Override
    public void setNamesForMailAccount(int accountId, int[] indexes, String[] names, int userId, int contextId) throws OXException {
        getStorageService().setNamesForMailAccount(accountId, indexes, names, userId, contextId);

    }

    @Override
    public int getByPrimaryAddress(String primaryAddress, int userId, int contextId) throws OXException {
        return getStorageService().getByPrimaryAddress(primaryAddress, userId, contextId);
    }

    @Override
    public MailAccount[] resolveLogin(String login, int contextId) throws OXException {
        return getStorageService().resolveLogin(login, contextId);
    }

    @Override
    public MailAccount[] resolvePrimaryAddr(String primaryAddress, int contextId) throws OXException {
        return getStorageService().resolvePrimaryAddr(primaryAddress, contextId);
    }

    @Override
    public MailAccount[] resolveLogin(String login, String serverUrl, int contextId) throws OXException {
        return getStorageService().resolveLogin(login, serverUrl, contextId);
    }

    @Override
    public void deleteMailAccount(int id, Map<String, Object> properties, int userId, int contextId, boolean deletePrimary) throws OXException {
        getStorageService().deleteMailAccount(id, properties, userId, contextId, deletePrimary);

    }

    @Override
    public void deleteMailAccount(int id, Map<String, Object> properties, int userId, int contextId, boolean deletePrimary, Connection con) throws OXException {
        getStorageService().deleteMailAccount(id, properties, userId, contextId, deletePrimary, con);

    }

    @Override
    public boolean hasAccounts(ServerSession session) throws OXException {
        return getStorageService().hasAccounts(session);
    }

    @Override
    public void migratePasswords(String oldSecret, String newSecret, ServerSession session) throws OXException {
        getStorageService().migratePasswords(oldSecret, newSecret, session);
    }

    @Override
    public void cleanUp(String secret, ServerSession session) throws OXException {
        getStorageService().cleanUp(secret, session);
    }

    @Override
    public void removeUnrecoverableItems(String secret, ServerSession session) throws OXException {
        getStorageService().removeUnrecoverableItems(secret, session);
    }

}
