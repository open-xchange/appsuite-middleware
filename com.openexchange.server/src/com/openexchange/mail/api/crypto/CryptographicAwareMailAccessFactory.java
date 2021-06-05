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

package com.openexchange.mail.api.crypto;

import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.session.Session;

/**
 * {@link CryptographicAwareMailAccessFactory} decorates a {@link MailAccess} object in order to add cryptographic functionality.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public interface CryptographicAwareMailAccessFactory {

    /**
     * Decorates the given {@link MailAccess} in order to add cryptographic functionality.
     *
     * @param mailAccess The {@link MailAccess} object to decorate.
     * @param session The session
     * @param authentication The authentication value used for authentication, or <code>null</code> for falling back to use
     *            {@link #createAccess(MailAccess<IMailFolderStorage, IMailMessageStorage>, Session)} in order to obtain authentication information from the given session.
     * @return An {@link MailAccess} object with cryptographic functionality.
     * @throws OXException
     */
    MailAccess<IMailFolderStorage, IMailMessageStorage> createAccess(MailAccess<IMailFolderStorage, IMailMessageStorage> mailAccess, Session session, String authentication) throws OXException;

    /**
     * Decorates an {@link MailAccess} object in order to add cryptographic functionality.
     * <p>
     * The implementation has to obtain authentication from the given session, if necessary.
     * </p>
     *
     * @param mailAccess The {@link MailAccess} object to decorate.
     * @param session The session
     * @return An {@link MailAcces} object with cryptographic functionality.
     * @throws OXException
     */
    MailAccess<IMailFolderStorage, IMailMessageStorage> createAccess(MailAccess<IMailFolderStorage, IMailMessageStorage> mailAccess, Session session) throws OXException;

}
