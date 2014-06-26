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

import com.openexchange.chat.ChatAccess;
import com.openexchange.chat.ChatAccountManager;
import com.openexchange.chat.ChatExceptionCodes;
import com.openexchange.chat.ChatService;
import com.openexchange.chat.util.ChatAccountImpl;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;


/**
 * {@link DBChatService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DBChatService implements ChatService {

    /**
     * Gets a new service instance.
     *
     * @return A new service instance
     */
    public static DBChatService newDbChatService() {
        final DBChatService service = new DBChatService();
        final ChatAccountImpl defaultAccount = service.accountManager.getDefaultAccount();
        defaultAccount.setChatService(service);
        return service;
    }

    private static final String IDENTIFIER = ChatService.DEFAULT_SERVICE;

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public static String getIdentifier() {
        return IDENTIFIER;
    }

    private final DBChatAccountManager accountManager;

    private final String displayName;

    /**
     * Initializes a new {@link DBChatService}.
     */
    private DBChatService() {
        super();
        accountManager = new DBChatAccountManager();
        displayName = "Default OX7 Chat Service";
    }

    @Override
    public String getId() {
        return IDENTIFIER;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public ChatAccess access(final String accountId, final Session session) throws OXException {
        if (!DEFAULT_ACCOUNT.equals(accountId)) {
            throw ChatExceptionCodes.ACCOUNT_NOT_FOUND.create(accountId);
        }
        return DBChatAccess.getDbChatAccess(session);
    }

    @Override
    public ChatAccountManager getAccountManager() {
        return accountManager;
    }

}
