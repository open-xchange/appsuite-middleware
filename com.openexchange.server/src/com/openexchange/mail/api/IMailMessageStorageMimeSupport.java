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

package com.openexchange.mail.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.mail.Message;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.utils.MimeMessageUtility;

/**
 * {@link IMailMessageStorageMimeSupport} - Extends basic message storage by MIME support.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IMailMessageStorageMimeSupport extends IMailMessageStorage {

    /**
     * Indicates if MIME messages are supported.
     *
     * @return <code>true</code> if supported; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isMimeSupported() throws OXException;

    /**
     * Appends given MIME messages to given folder.
     *
     * @param destFolder The destination folder
     * @param msgs The messages to append (<b>must</b> be completely pre-filled incl. content references)
     * @return The corresponding mail IDs in destination folder
     * @throws OXException If MIME messages cannot be appended.
     */
    String[] appendMimeMessages(String destFolder, Message[] msgs) throws OXException;

    /**
     * Gets the denoted MIME message.
     *
     * @param fullName The folder full name
     * @param id The message identifier
     * @param markSeen Whether to mark as seen or not
     * @return The denoted MIME message
     * @throws OXException If MIME message cannot be returned
     */
    Message getMimeMessage(String fullName, String id, boolean markSeen) throws OXException;

    /**
     * Output the denoted message as an RFC 822 format stream.
     *
     * @param fullName The folder full name
     * @param id The message identifier
     * @param os The stream to write to
     * @throws OXException If an error occurs writing to the stream
     */
    default void writeMimeMessage(String fullName, String id, OutputStream os) throws OXException {
        Message mimeMessage = getMimeMessage(fullName, id, false);
        if (mimeMessage == null) {
            throw MailExceptionCode.MAIL_NOT_FOUND.create(id, fullName);
        }

        try {
            mimeMessage.writeTo(os);
        } catch (IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Gets the MIME format stream corresponding to denoted message.
     *
     * @param fullName The folder full name
     * @param id The message identifier
     * @return The MIME format stream
     * @exception OXException for failures
     */
    default InputStream getMimeStream(String fullName, String id) throws OXException {
        Message mimeMessage = getMimeMessage(fullName, id, false);
        if (mimeMessage == null) {
            throw MailExceptionCode.MAIL_NOT_FOUND.create(id, fullName);
        }

        try {
            return MimeMessageUtility.getStreamFromPart(mimeMessage);
        } catch (IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

}
