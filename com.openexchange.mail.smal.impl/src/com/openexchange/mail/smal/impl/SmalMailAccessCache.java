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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.impl;

import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.cache.EnqueueingMailAccessCache;
import com.openexchange.mail.cache.IMailAccessCache;
import com.openexchange.mail.cache.SingletonMailAccessCache;
import com.openexchange.session.Session;

/**
 * {@link SmalMailAccessCache}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SmalMailAccessCache implements IMailAccessCache {

    private static volatile SmalMailAccessCache cacheInstance;

    /**
     * Gets the singleton instance.
     *
     * @return The singleton instance
     * @throws OXException If instance initialization fails
     */
    public static IMailAccessCache getInstance() throws OXException {
        IMailAccessCache tmp = cacheInstance;
        if (null == tmp) {
            synchronized (SingletonMailAccessCache.class) {
                tmp = cacheInstance;
                if (null == tmp) {
                    final int max = MailAccess.MAX_PER_USER;
                    tmp = cacheInstance = new SmalMailAccessCache(1 == max ? SingletonMailAccessCache.newInstance() : EnqueueingMailAccessCache.newInstance(max));
                }
            }
        }
        return tmp;
    }

    /**
     * Releases the singleton instance.
     */
    public static void releaseInstance() {
        if (null != cacheInstance) {
            synchronized (SingletonMailAccessCache.class) {
                if (null != cacheInstance) {
                    cacheInstance.close();
                    cacheInstance = null;
                }
            }
        }
    }

    private final IMailAccessCache delegate;

    private SmalMailAccessCache(final IMailAccessCache delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public int numberOfMailAccesses(Session session, int accountId) throws OXException {
        return delegate.numberOfMailAccesses(session, accountId);
    }

    @Override
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> removeMailAccess(final Session session, final int accountId) {
        return delegate.removeMailAccess(session, accountId);
    }

    @Override
    public boolean putMailAccess(final Session session, final int accountId, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) {
        return delegate.putMailAccess(session, accountId, mailAccess);
    }

    @Override
    public boolean containsMailAccess(final Session session, final int accountId) {
        return delegate.containsMailAccess(session, accountId);
    }

    @Override
    public void clearUserEntries(final Session session) throws OXException {
        delegate.clearUserEntries(session);
    }

    @Override
    public void close() {
        delegate.close();
    }

}
