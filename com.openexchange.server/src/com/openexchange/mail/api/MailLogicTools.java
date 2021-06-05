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

import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.processing.MimeForward;
import com.openexchange.mail.mime.processing.MimeReply;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.session.Session;

/**
 * {@link MailLogicTools} - Provides convenience methods to reply/forward a mail message.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailLogicTools {

    /**
     * The session providing user data.
     */
    protected final Session session;

    /**
     * The account ID.
     */
    protected final int accountId;

    /**
     * Initializes a new {@link MailLogicTools}
     *
     * @param session The session providing user data
     * @param accountId The account ID
     */
    public MailLogicTools(Session session, int accountId) {
        super();
        this.session = session;
        this.accountId = accountId;
    }

    /**
     * Creates a reply message for the message specified by <code>originalMail</code>.
     * <p>
     * If mailing system deals with common RFC822 messages, this convenience method only delegates its request to
     * {@link MimeReply#getReplyMail(MailMessage, boolean, Session)} and can be left unchanged. Otherwise an message data specific
     * implementation is needed.
     *
     * @param originalMail The original mail
     * @param replyAll <code>true</code> to reply to all recipients; otherwise <code>false</code>
     * @return An instance of {@link MailMessage} representing the reply message
     * @throws OXException If reply message cannot be generated
     */
    public MailMessage getReplyMessage(MailMessage originalMail, boolean replyAll) throws OXException {
        return getReplyMessage(originalMail, replyAll, false);
    }

    /**
     * Creates a reply message for the message specified by <code>originalMail</code>.
     * <p>
     * If mailing system deals with common RFC822 messages, this convenience method only delegates its request to
     * {@link MimeReply#getReplyMail(MailMessage, boolean, Session)} and can be left unchanged. Otherwise an message data specific
     * implementation is needed.
     *
     * @param originalMail The original mail
     * @param replyAll <code>true</code> to reply to all recipients; otherwise <code>false</code>
     * @param setFrom <code>true</code> to set 'From' header; otherwise <code>false</code> to leave it
     * @return An instance of {@link MailMessage} representing the reply message
     * @throws OXException If reply message cannot be generated
     */
    public MailMessage getReplyMessage(MailMessage originalMail, boolean replyAll, boolean setFrom) throws OXException {
        return MimeReply.getReplyMail(originalMail, replyAll, session, accountId, setFrom);
    }

    // ------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Creates a reply message for the message specified by <code>originalMail</code>.
     * <p>
     * If mailing system deals with common RFC822 messages, this convenience method only delegates its request to
     * {@link MimeReply#getReplyMail(MailMessage, boolean, Session)} and can be left unchanged. Otherwise an message data specific
     * implementation is needed.
     *
     * @param originalMail The original mail
     * @param replyAll <code>true</code> to reply to all recipients; otherwise <code>false</code>
     * @param usm The user mail settings to use; leave to <code>null</code> to obtain from specified session
     * @return An instance of {@link MailMessage} representing the reply message
     * @throws OXException If reply message cannot be generated
     */
    public MailMessage getReplyMessage(MailMessage originalMail, boolean replyAll, UserSettingMail usm) throws OXException {
        return getReplyMessage(originalMail, replyAll, usm, false);
    }

    /**
     * Creates a reply message for the message specified by <code>originalMail</code>.
     * <p>
     * If mailing system deals with common RFC822 messages, this convenience method only delegates its request to
     * {@link MimeReply#getReplyMail(MailMessage, boolean, Session)} and can be left unchanged. Otherwise an message data specific
     * implementation is needed.
     *
     * @param originalMail The original mail
     * @param replyAll <code>true</code> to reply to all recipients; otherwise <code>false</code>
     * @param usm The user mail settings to use; leave to <code>null</code> to obtain from specified session
     * @param setFrom <code>true</code> to set 'From' header; otherwise <code>false</code> to leave it
     * @return An instance of {@link MailMessage} representing the reply message
     * @throws OXException If reply message cannot be generated
     */
    public MailMessage getReplyMessage(MailMessage originalMail, boolean replyAll, UserSettingMail usm, boolean setFrom) throws OXException {
        return MimeReply.getReplyMail(originalMail, replyAll, session, accountId, usm, setFrom);
    }

    /**
     * Creates a reply message for the message specified by <code>originalMail</code>.
     * <p>
     * If mailing system deals with common RFC822 messages, this convenience method only delegates its request to
     * {@link MimeReply#getReplyMail(MailMessage, boolean, Session)} and can be left unchanged. Otherwise an message data specific
     * implementation is needed.
     *
     * @param originalMail The original mail
     * @param replyAll <code>true</code> to reply to all recipients; otherwise <code>false</code>
     * @param usm The user mail settings to use; leave to <code>null</code> to obtain from specified session
     * @param fromAddressProvider The provider for <code>"From"</code> address
     * @return An instance of {@link MailMessage} representing the reply message
     * @throws OXException If reply message cannot be generated
     */
    public MailMessage getReplyMessage(MailMessage originalMail, boolean replyAll, UserSettingMail usm, FromAddressProvider fromAddressProvider) throws OXException {
        return MimeReply.getReplyMail(originalMail, replyAll, session, accountId, usm, fromAddressProvider);
    }

    // ------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Creates a forward message for the messages specified by <code>originalMails</code>. If multiple messages are specified then these
     * messages are forwarded as <b>attachment</b> since no inline forward is possible.
     * <p>
     * If mailing system deals with common RFC822 messages, this convenience method only delegates its request to
     * {@link MimeForward#getFowardMail(MailMessage[], Session)} and can be left unchanged. Otherwise a message data specific implementation
     * is needed.
     *
     * @param originalMails The original mails
     * @param setFrom <code>true</code> to set 'From' header; otherwise <code>false</code> to leave it
     * @return An instance of {@link MailMessage} representing the forward message
     * @throws OXException If forward message cannot be generated
     */
    public MailMessage getFowardMessage(MailMessage[] originalMails, boolean setFrom) throws OXException {
        return MimeForward.getFowardMail(originalMails, session, accountId, setFrom);
    }

    // ------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Creates a forward message for the messages specified by <code>originalMails</code>. If multiple messages are specified then these
     * messages are forwarded as <b>attachment</b> since no inline forward is possible.
     * <p>
     * If mailing system deals with common RFC822 messages, this convenience method only delegates its request to
     * {@link MimeForward#getFowardMail(MailMessage[], Session)} and can be left unchanged. Otherwise a message data specific implementation
     * is needed.
     *
     * @param originalMails The original mails
     * @param usm The user mail settings to use; leave to <code>null</code> to obtain from specified session
     * @param setFrom <code>true</code> to set 'From' header; otherwise <code>false</code> to leave it
     * @return An instance of {@link MailMessage} representing the forward message
     * @throws OXException If forward message cannot be generated
     */
    public MailMessage getFowardMessage(MailMessage[] originalMails, UserSettingMail usm, boolean setFrom) throws OXException {
        return MimeForward.getFowardMail(originalMails, session, accountId, usm, setFrom);
    }

    /**
     * Creates a forward message for the messages specified by <code>originalMails</code>. If multiple messages are specified then these
     * messages are forwarded as <b>attachment</b> since no inline forward is possible.
     * <p>
     * If mailing system deals with common RFC822 messages, this convenience method only delegates its request to
     * {@link MimeForward#getFowardMail(MailMessage[], Session)} and can be left unchanged. Otherwise a message data specific implementation
     * is needed.
     *
     * @param originalMails The original mails
     * @param usm The user mail settings to use; leave to <code>null</code> to obtain from specified session
     * @param fromAddressProvider The provider for <code>"From"</code> address
     * @return An instance of {@link MailMessage} representing the forward message
     * @throws OXException If forward message cannot be generated
     */
    public MailMessage getFowardMessage(MailMessage[] originalMails, UserSettingMail usm, FromAddressProvider fromAddressProvider) throws OXException {
        return MimeForward.getFowardMail(originalMails, session, accountId, usm, fromAddressProvider);
    }

}
