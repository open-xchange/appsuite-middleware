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

package com.openexchange.spamhandler.cloudmark.util;

import java.io.OutputStream;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailMessage;


/**
 * {@link MailMessageByteStream}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class MailMessageByteStream implements ByteStream {

    /**
     * Creates a new <code>MailMessageByteStream</code> for specified mail.
     *
     * @param mail The source mail
     * @return The instance or <code>null</code>
     */
    public static MailMessageByteStream newInstanceFrom(MailMessage mail) {
        return null == mail ? null : new MailMessageByteStream(mail);
    }

    // ----------------------------------------------------------------------------------------------------------------------------- //

    private final MailMessage mail;

    /**
     * Initializes a new {@link MailMessageByteStream}.
     */
    private MailMessageByteStream(MailMessage mail) {
        super();
        this.mail = mail;
    }

    @Override
    public void writeTo(OutputStream os) throws OXException {
        try {
            mail.writeTo(os);
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
