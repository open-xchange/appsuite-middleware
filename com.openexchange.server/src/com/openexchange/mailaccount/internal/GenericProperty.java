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

package com.openexchange.mailaccount.internal;

import java.security.GeneralSecurityException;
import com.openexchange.exception.OXException;
import com.openexchange.mail.utils.MailPasswordUtil;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.secret.Decrypter;
import com.openexchange.session.Session;

/**
 * {@link GenericProperty}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GenericProperty implements Decrypter {

    /** The account identifier */
    public final int accountId;

    /** The associated session */
    public final Session session;

    /** The login identifier */
    public final String login;

    /** The server name */
    public final String server;

    /**
     * Initializes a new {@link GenericProperty}.
     */
    public GenericProperty(final int accountId, final Session session, final String login, final String server) {
        super();
        this.accountId = accountId;
        this.session = session;
        this.login = login;
        this.server = server;
    }

    @Override
    public String getDecrypted(final Session session, final String encrypted) throws OXException {
        if (null == encrypted || encrypted.length() == 0) {
            // Set to empty string
            return "";
        }
        // Decrypt mail account's password using session password
        try {
            return MailPasswordUtil.decrypt(encrypted, session.getPassword());
        } catch (GeneralSecurityException e) {
            throw MailAccountExceptionCodes.PASSWORD_DECRYPTION_FAILED.create(e, login, server, Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
        }
    }

}
