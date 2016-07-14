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

package com.openexchange.imap.storecache;

import java.util.concurrent.atomic.AtomicInteger;
import javax.mail.MessagingException;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link NonCachingIMAPStoreContainer} - The non-caching {@link IMAPStoreContainer}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class NonCachingIMAPStoreContainer extends AbstractIMAPStoreContainer {

    protected final String server;
    protected final int port;
    private final AtomicInteger inUseCount;

    /**
     * Initializes a new {@link NonCachingIMAPStoreContainer}.
     */
    public NonCachingIMAPStoreContainer(int accountId, Session session, String server, int port, boolean propagateClientIp) {
        super(accountId, session, propagateClientIp);
        this.port = port;
        this.server = server;
        inUseCount = new AtomicInteger();
    }

    @Override
    public int getInUseCount() {
        return inUseCount.get();
    }

    @Override
    public IMAPStore getStore(javax.mail.Session imapSession, String login, String pw, Session session) throws MessagingException, InterruptedException {
        inUseCount.incrementAndGet();
        return newStore(server, port, login, pw, imapSession, session);
    }

    @Override
    public void backStore(final IMAPStore imapStore) {
        backStoreNoValidityCheck(imapStore);
        inUseCount.decrementAndGet();
    }

    protected void backStoreNoValidityCheck(final IMAPStore imapStore) {
        closeSafe(imapStore);
    }

    @Override
    public void closeElapsed(final long stamp, final StringBuilder debugBuilder) {
        // Nothing to do
    }

    @Override
    public void clear() {
        // Nothing to do
    }

    /* (non-Javadoc)
     * @see com.openexchange.imap.storecache.IMAPStoreContainer#hasElapsed(long)
     */
    @Override
    public boolean hasElapsed(long millis) {
        return false;
    }
}
