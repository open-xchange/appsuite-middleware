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

package com.openexchange.messaging.rss;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.MessagingAddressHeader;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingFolderAccess;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.session.Session;
import com.sun.syndication.fetcher.FeedFetcher;


/**
 * {@link RSSFeedOperations}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RSSFeedOperations implements MessagingAccountAccess, MessagingAccountTransport {

    private final int accountId;
    private final RSSMessageAccess messageAccess;
    private final RSSFolderAccess folderAccess;
    private boolean connected;

    public RSSFeedOperations(final int accountId, final Session session, final FeedFetcher fetcher, final MessagingAccountManager accounts) {
        super();
        this.accountId = accountId;

        folderAccess = new RSSFolderAccess(accountId, session);
        messageAccess = new RSSMessageAccess(accountId, session, fetcher, accounts);
    }

    @Override
    public int getAccountId() {
        return accountId;
    }

    @Override
    public MessagingFolderAccess getFolderAccess() throws OXException {
        return folderAccess;
    }

    @Override
    public MessagingMessageAccess getMessageAccess() throws OXException {
        return messageAccess;
    }

    @Override
    public MessagingFolder getRootFolder() throws OXException {
        return folderAccess.getRootFolder();
    }

    @Override
    public void close() {
        connected = false;
    }

    @Override
    public void connect() throws OXException {
        connected = true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean ping() throws OXException {
        return true;
    }

    @Override
    public void transport(final MessagingMessage message, final Collection<MessagingAddressHeader> recipients) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public boolean cacheable() {
        // Nothing to cache
        return false;
    }

}
