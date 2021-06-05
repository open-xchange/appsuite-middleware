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

package com.openexchange.messaging;

import java.util.Arrays;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;


/**
 * {@link SimAccountManager}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimAccountManager implements MessagingAccountManager {

    private Session session;
    private MessagingAccount createdAccount;
    private OXException exception;
    private MessagingAccount updatedAccount;
    private MessagingAccount deletedAccount;
    private MessagingAccount accountToGet;
    private int id;
    private List<MessagingAccount> accounts;

    public MessagingAccount newAccount() {
        return new SimMessagingAccount();
    }

    @Override
    public int addAccount(final MessagingAccount account, final Session session) throws OXException {
        createdAccount = account;
        this.session = session;
        exception();
        return createdAccount.getId();
    }


    public MessagingAccount getCreatedAccount() {
        return createdAccount;
    }


    public Session getSession() {
        return session;
    }

    @Override
    public void deleteAccount(final MessagingAccount account, final Session session) throws OXException {
        deletedAccount = account;
        this.session = session;
        exception();
    }


    @Override
    public MessagingAccount getAccount(final int id, final Session session) throws OXException {
        this.id = id;
        this.session = session;
        exception();
        return accountToGet;
    }

    @Override
    public List<MessagingAccount> getAccounts(final Session session) throws OXException {
        exception();
        return accounts;
    }

    @Override
    public void updateAccount(final MessagingAccount account, final Session session) throws OXException {
        updatedAccount = account;
        this.session = session;
        exception();
    }

    public void setException(final OXException OXException) {
        exception = OXException;
    }

    private void exception() throws OXException {
        if (null != exception) {
            throw exception;
        }
    }


    public MessagingAccount getUpdatedAccount() {
        return updatedAccount;
    }


    public MessagingAccount getDeletedAccount() {
        return deletedAccount;
    }

    public void setAccountToGet(final MessagingAccount account) {
        accountToGet = account;
    }


    public int getId() {
        return id;
    }


    public void setAllAccounts(final MessagingAccount...list) {
        accounts = Arrays.asList(list);
    }

    @Override
    public void migrateToNewSecret(final String oldSecret, final String newSecret, final Session session) {
        // Nothing to do
    }

    @Override
    public boolean hasAccount(final Session session) throws OXException {
        // Nothing to do
        return false;
    }

    @Override
    public void cleanUp(String secret, Session session) throws OXException {
        // Ignore
    }

    @Override
    public void removeUnrecoverableItems(String secret, Session session) {
        // Ignore        
    }

}
