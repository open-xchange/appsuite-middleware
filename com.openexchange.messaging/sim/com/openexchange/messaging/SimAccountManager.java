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

    public MessagingAccount newAccount() throws OXException {
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
        if(null != exception) {
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
