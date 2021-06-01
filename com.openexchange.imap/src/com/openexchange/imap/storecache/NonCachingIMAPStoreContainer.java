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
import com.openexchange.imap.IMAPAccess;
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

    /**
     * Initializes a new {@link NonCachingIMAPStoreContainer}.
     */
    public NonCachingIMAPStoreContainer(int accountId, Session session, String server, int port, boolean propagateClientIp) {
        super(accountId, session, propagateClientIp);
        this.port = port;
        this.server = server;
    }

    @Override
    public IMAPStore getStore(javax.mail.Session imapSession, String login, String pw, Session session) throws MessagingException, InterruptedException {
        return newStore(server, port, login, pw, imapSession, session);
    }

    @Override
    public void backStore(IMAPStore imapStore) {
        backStoreNoValidityCheck(imapStore);
    }

    protected void backStoreNoValidityCheck(IMAPStore imapStore) {
        IMAPAccess.closeSafely(imapStore);
    }

    @Override
    public void closeElapsed(long stamp) {
        // Nothing to do
    }

    @Override
    public void clear() {
        // Nothing to do
    }

    @Override
    public boolean hasElapsed(long millis) {
        return false;
    }

}
