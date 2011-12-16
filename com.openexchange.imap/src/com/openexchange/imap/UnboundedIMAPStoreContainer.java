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

package com.openexchange.imap;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import javax.mail.MessagingException;
import com.sun.mail.imap.IMAPStore;


/**
 * {@link UnboundedIMAPStoreContainer} - The unbounded {@link IMAPStoreContainer}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UnboundedIMAPStoreContainer extends AbstractIMAPStoreContainer {

    protected final BlockingQueue<IMAPStoreWrapper> queue;

    protected final String server;

    protected final int port;

    protected final String login;

    protected final String pw;

    /**
     * Initializes a new {@link UnboundedIMAPStoreContainer}.
     */
    public UnboundedIMAPStoreContainer(final String name, final String server, final int port, final String login, final String pw) {
        super(name);
        queue = new PriorityBlockingQueue<IMAPStoreWrapper>();
        this.login = login;
        this.port = port;
        this.pw = pw;
        this.server = server;
    }

    @Override
    public IMAPStore getStore(final javax.mail.Session imapSession) throws MessagingException, InterruptedException {
        /*
         * Retrieve and remove the head of this queue
         */
        final IMAPStoreWrapper imapStoreWrapper = queue.poll();
        IMAPStore imapStore = null == imapStoreWrapper ? null : imapStoreWrapper.imapStore;
        if (null == imapStore) {
            imapStore = newStore(server, port, login, pw, imapSession);
        }
        return imapStore;
    }

    @Override
    public void backStore(final IMAPStore imapStore) {
        if (!queue.offer(new IMAPStoreWrapper(imapStore))) {
            closeSafe(imapStore);
        }
    }

    @Override
    public void closeElapsed(final long stamp, final StringBuilder debugBuilder) {
        for (final Iterator<IMAPStoreWrapper> iter = queue.iterator(); iter.hasNext();) {
            final IMAPStoreWrapper imapStoreWrapper = iter.next();
            if (imapStoreWrapper.lastAccessed >= stamp) {
                try {
                    iter.remove();
                    if (null == debugBuilder) {
                        closeSafe(imapStoreWrapper.imapStore);
                    } else {
                        final String info = imapStoreWrapper.imapStore.toString();
                        closeSafe(imapStoreWrapper.imapStore);
                        debugBuilder.setLength(0);
                        LOG.debug(debugBuilder.append("Closed elapsed IMAP store: ").append(info).toString());
                    }
                } catch (final IllegalStateException e) {
                    // Ignore
                }
            }
        }
    }

    @Override
    public void clear() {
        for (final Iterator<IMAPStoreWrapper> iter = queue.iterator(); iter.hasNext();) {
            final IMAPStoreWrapper imapStoreWrapper = iter.next();
            iter.remove();
            closeSafe(imapStoreWrapper.imapStore);
        }
    }

}
