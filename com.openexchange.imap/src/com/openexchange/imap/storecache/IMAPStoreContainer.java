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

import javax.mail.MessagingException;
import com.openexchange.imap.IMAPProvider;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPStore;


/**
 * {@link IMAPStoreContainer} - A container for connected {@link IMAPStore} instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IMAPStoreContainer {

    /**
     * The IMAP protocol name: <code>"imap"</code>.
     */
    public static final String PROTOCOL_NAME = IMAPProvider.PROTOCOL_IMAP.getName();

    /**
     * Gets a connected IMAP store.
     *
     * @param imapSession The IMAP session
     * @param login The login
     * @param pw The password
     * @param session The Groupware session
     * @return The connected IMAP store or <code>null</code> if currently impossible to do so
     * @throws MessagingException If returning a connected IMAP store fails
     * @throws InterruptedException If thread is interrupted when possibly waiting for free resources
     */
    IMAPStore getStore(javax.mail.Session imapSession, String login, String pw, Session session) throws MessagingException, InterruptedException;

    /**
     * Returns specified IMAP store to container.
     *
     * @param imapStore The IMAP store to return
     */
    void backStore(IMAPStore imapStore);

    /**
     * Close elapsed {@link IMAPStore} instances.
     *
     * @param stamp The stamp to check against
     * @param debugBuilder The optional debug builder
     */
    void closeElapsed(long stamp, StringBuilder debugBuilder);

    /**
     * Orderly clears this container.
     */
    void clear();

    /**
     * Gets the number of stores currently in-use.
     *
     * @return The number of stores currently in-use
     */
    int getInUseCount();

    /**
     * Determines whether the IMAPStoreContainer has elapsed
     *
     * @param millis
     * @return true if elapsed; false otherwise
     */
    boolean hasElapsed(long millis);
}
