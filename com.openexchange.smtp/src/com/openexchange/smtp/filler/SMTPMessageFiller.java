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

package com.openexchange.smtp.filler;

import java.io.IOException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.filler.CompositionParameters;
import com.openexchange.mail.mime.filler.MimeMessageFiller;
import com.openexchange.mail.mime.filler.SessionCompositionParameters;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.session.Session;
import com.openexchange.smtp.config.ISMTPProperties;
import com.openexchange.smtp.dataobjects.SMTPMailMessage;
import com.sun.mail.smtp.SMTPMessage;

/**
 * {@link SMTPMessageFiller} - Fills an instance of {@link SMTPMessage} with headers/contents given through an instance of
 * {@link SMTPMailMessage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SMTPMessageFiller extends MimeMessageFiller {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SMTPMessageFiller.class);
    }

    private final ISMTPProperties smtpProperties;

    /**
     * Constructor
     *
     * @param smtpProperties
     * @param session The session
     * @param ctx The context
     * @param usm The user's mail settings
     */
    public SMTPMessageFiller(ISMTPProperties smtpProperties, Session session, Context ctx, UserSettingMail usm) {
        super(new SessionCompositionParameters(session, ctx, usm));
        this.smtpProperties = smtpProperties;
    }

    /**
     * Constructor
     *
     * @param smtpProperties
     * @param compositionParameters
     */
    public SMTPMessageFiller(ISMTPProperties smtpProperties, CompositionParameters compositionParameters) {
        super(compositionParameters);
        this.smtpProperties = smtpProperties;
    }

    /**
     * Fills given instance of {@link SMTPMessage}
     *
     * @param mail The source mail
     * @param smtpMessage The SMTP message to fill
     * @param type The compose type
     * @param accountId The identifier of the associated account
     * @param session The session
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If a mail error occurs
     * @throws IOException If an I/O error occurs
     */
    public void fillMail(ComposedMailMessage mail, SMTPMessage smtpMessage, ComposeType type) throws MessagingException, OXException, IOException {
        if (null != type) {
            mail.setSendType(type);
        }

        // Set headers
        setMessageHeaders(mail, smtpMessage);
        if (!mail.containsFrom() || mail.getFrom().length == 0) {
            String envelopeFrom = compositionParameters.getEnvelopeFrom();
            if (Strings.isNotEmpty(envelopeFrom)) {
                try {
                    smtpMessage.setFrom(new QuotedInternetAddress(envelopeFrom));
                } catch (MessagingException e) {
                    // Failed to parse envelope-from
                    LoggerHolder.LOG.trace("Failed to parse envelope-from", e);
                }
            }
        }

        // Set common headers
        setCommonHeaders(smtpMessage);

        // Fill body
        fillMailBody(mail, smtpMessage, type);
    }

    @Override
    public void setCommonHeaders(final MimeMessage mimeMessage) throws MessagingException, OXException {
        super.setCommonHeaders(mimeMessage);
        /*
         * ENVELOPE-FROM
         */
        if (smtpProperties.isSmtpEnvelopeFrom() && (mimeMessage instanceof SMTPMessage)) {
            /*
             * Set ENVELOPE-FROM in SMTP message to user's primary email address
             */
            ((SMTPMessage) mimeMessage).setEnvelopeFrom(compositionParameters.getEnvelopeFrom());
        }
    }

}
