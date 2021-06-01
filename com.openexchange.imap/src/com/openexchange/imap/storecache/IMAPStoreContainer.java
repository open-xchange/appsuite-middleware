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
     * @param session The Open-Xchange session
     * @return The connected IMAP store or empty if currently impossible to do so
     * @throws IMAPStoreContainerInvalidException If this container has already been invalidated
     * @throws MessagingException If returning a connected IMAP store fails
     * @throws InterruptedException If thread is interrupted when possibly waiting for free resources
     */
    IMAPStore getStore(javax.mail.Session imapSession, String login, String pw, Session session) throws IMAPStoreContainerInvalidException, MessagingException, InterruptedException;

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
     */
    void closeElapsed(long stamp);

    /**
     * Orderly clears this container.
     */
    void clear();

    /**
     * Determines whether the IMAPStoreContainer has elapsed IMAP store instances.
     *
     * @param millis
     * @return true if it contains elapsed ones; false otherwise
     */
    boolean hasElapsed(long millis);

}
