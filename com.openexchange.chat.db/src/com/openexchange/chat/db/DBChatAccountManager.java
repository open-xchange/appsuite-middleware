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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.chat.db;

import java.util.Collections;
import java.util.List;
import com.openexchange.chat.ChatAccount;
import com.openexchange.chat.ChatAccountManager;
import com.openexchange.chat.ChatExceptionCodes;
import com.openexchange.chat.ChatService;
import com.openexchange.chat.util.ChatAccountImpl;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link DBChatAccountManager}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DBChatAccountManager implements ChatAccountManager {

    private static final String DEFAULT_ACCOUNT = ChatService.DEFAULT_ACCOUNT;

    private final ChatAccountImpl defaultAccount;

    private volatile List<ChatAccount> accounts;

    /**
     * Initializes a new {@link DBChatAccountManager}.
     */
    public DBChatAccountManager() {
        super();
        defaultAccount = new ChatAccountImpl();
        defaultAccount.setDisplayName("OX7 Chat");
        defaultAccount.setId(DEFAULT_ACCOUNT);
    }

    /**
     * Gets the default account
     *
     * @return The default account
     */
    public ChatAccountImpl getDefaultAccount() {
        return defaultAccount;
    }

    @Override
    public String addAccount(final ChatAccount account, final Session session) throws OXException {
        throw ChatExceptionCodes.UNSUPPORTED_OPERATION.create();
    }

    @Override
    public void updateAccount(final ChatAccount account, final Session session) throws OXException {
        throw ChatExceptionCodes.UNSUPPORTED_OPERATION.create();
    }

    @Override
    public void deleteAccount(final ChatAccount account, final Session session) throws OXException {
        throw ChatExceptionCodes.UNSUPPORTED_OPERATION.create();
    }

    @Override
    public List<ChatAccount> getAccounts(final Session session) throws OXException {
        List<ChatAccount> tmp = accounts;
        if (null == tmp) {
            synchronized (this) {
                tmp = accounts;
                if (null == tmp) {
                    tmp = Collections.<ChatAccount> singletonList(defaultAccount);
                    accounts = tmp;
                }
            }
        }
        return tmp;
    }

    @Override
    public ChatAccount getAccount(final String id, final Session session) throws OXException {
        if (!DEFAULT_ACCOUNT.equals(id)) {
            throw ChatExceptionCodes.ACCOUNT_NOT_FOUND.create(id);
        }
        return defaultAccount;
    }

    @Override
    public String checkSecretCanDecryptStrings(final Session session, final String secret) throws OXException {
        return null;
    }

    @Override
    public void migrateToNewSecret(final String oldSecret, final String newSecret, final Session session) throws OXException {
        // Nope
    }

}
