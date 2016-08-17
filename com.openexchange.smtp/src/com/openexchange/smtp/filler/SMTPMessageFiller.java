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

package com.openexchange.smtp.filler;

import java.io.IOException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
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
            if (!Strings.isEmpty(envelopeFrom)) {
                try {
                    smtpMessage.setFrom(new QuotedInternetAddress(envelopeFrom));
                } catch (MessagingException e) {
                    // Failed to parse envelope-from
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
