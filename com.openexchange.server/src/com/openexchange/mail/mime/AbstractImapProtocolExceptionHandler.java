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

package com.openexchange.mail.mime;

import javax.mail.Folder;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.session.Session;
import com.sun.mail.iap.ProtocolException;


/**
 * {@link AbstractImapProtocolExceptionHandler} - The abstract handler for {@link com.sun.mail.iap.ProtocolException IMAP protocol exceptions}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public abstract class AbstractImapProtocolExceptionHandler implements MimeMailExceptionHandler {

    /**
     * Initializes a new {@link AbstractImapProtocolExceptionHandler}.
     */
    protected AbstractImapProtocolExceptionHandler() {
        super();
    }

    @Override
    public OXException handle(MessagingException me, MailConfig mailConfig, Session session, Folder folder) {
        if (null == me) {
            return null;
        }

        Exception nextException = me.getNextException();
        if (!(nextException instanceof com.sun.mail.iap.ProtocolException)) {
            return null;
        }

        return handleProtocolException((com.sun.mail.iap.ProtocolException) nextException, mailConfig, session, folder);
    }

    /**
     * Handles given IMAP protocol exception.
     *
     * @param protocolException The IMAP protocol exception to handle
     * @param mailConfig The optional mail config
     * @param session The optional session
     * @param folder The optional folder
     * @return The handled exception or <code>null</code>
     */
    protected abstract OXException handleProtocolException(ProtocolException protocolException, MailConfig mailConfig, Session session, Folder folder);

}
