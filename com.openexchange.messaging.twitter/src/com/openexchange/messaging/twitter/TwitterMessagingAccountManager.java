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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.messaging.twitter;

import java.util.List;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingException;
import com.openexchange.session.Session;

/**
 * {@link TwitterMessagingAccountManager}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterMessagingAccountManager implements MessagingAccountManager {

    private final MessagingAccountManager delegate;

    /**
     * Initializes a new {@link TwitterMessagingAccountManager}.
     */
    public TwitterMessagingAccountManager(final MessagingAccountManager delegate) {
        super();
        this.delegate = delegate;
    }

    public int addAccount(MessagingAccount account, Session session) throws MessagingException {
        return delegate.addAccount(account, session);
    }

    public void updateAccount(MessagingAccount account, Session session) throws MessagingException {
        delegate.updateAccount(account, session);
    }

    public void deleteAccount(MessagingAccount account, Session session) throws MessagingException {
        delegate.deleteAccount(account, session);
    }

    public List<MessagingAccount> getAccounts(Session session) throws MessagingException {
        return delegate.getAccounts(session);
    }

    public MessagingAccount getAccount(int id, Session session) throws MessagingException {
        return new TwitterMessagingAccount(delegate.getAccount(id, session));
    }

}
