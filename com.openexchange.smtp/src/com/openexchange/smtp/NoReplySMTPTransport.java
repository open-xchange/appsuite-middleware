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

package com.openexchange.smtp;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.transport.config.ITransportProperties;
import com.openexchange.mail.transport.config.NoReplyConfig;
import com.openexchange.mail.transport.config.NoReplyConfig.SecureMode;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.transport.config.NoReplyConfigFactory;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.smtp.config.ISMTPProperties;
import com.openexchange.smtp.config.SMTPConfig;
import com.openexchange.smtp.config.SMTPProperties;
import com.openexchange.smtp.filler.SMTPMessageFiller;
import com.openexchange.smtp.services.Services;


/**
 * {@link NoReplySMTPTransport}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class NoReplySMTPTransport extends AbstractSMTPTransport {

    private final NoReplyConfig noReplyConfig;

    /**
     * Initializes a new {@link NoReplySMTPTransport}.
     *
     * @param contextId The context identifier
     * @throws OXException If initialization fails
     */
    public NoReplySMTPTransport(int contextId) throws OXException {
        super(contextId);
        NoReplyConfigFactory configFactory = Services.getService(NoReplyConfigFactory.class);
        if (configFactory == null) {
            throw ServiceExceptionCode.serviceUnavailable(NoReplyConfigFactory.class);
        }

        noReplyConfig = configFactory.getNoReplyConfig(contextId);
    }

    @Override
    protected void setReplyHeaders(MimeMessage mimeMessage, MailPath msgref) throws OXException, MessagingException {
        throw new OXException(new IllegalAccessException("Replies cannot be send by no-reply transport"));
    }

    @Override
    protected SMTPMessageFiller createSMTPMessageFiller(UserSettingMail optMailSettings) throws OXException {
        return new SMTPMessageFiller(getTransportConfig().getSMTPProperties(), new NoReplyCompositionParameters());
    }

    @Override
    protected SMTPConfig createSMTPConfig() throws OXException {
        final SMTPConfig smtpConfig = new SMTPConfig();
        smtpConfig.setLogin(noReplyConfig.getLogin());
        smtpConfig.setPassword(noReplyConfig.getPassword());
        smtpConfig.setServer(noReplyConfig.getServer());
        smtpConfig.setPort(noReplyConfig.getPort());
        SecureMode secureMode = noReplyConfig.getSecureMode();
        smtpConfig.setRequireTls(NoReplyConfig.SecureMode.TLS.equals(secureMode));
        smtpConfig.setSecure(NoReplyConfig.SecureMode.SSL.equals(secureMode));
        smtpConfig.setTransportProperties(new NoReplySMTPProperties(noReplyConfig));
        return smtpConfig;
    }

    @Override
    protected void processAddressHeader(MimeMessage mimeMessage) throws OXException, MessagingException {
        super.processAddressHeader(mimeMessage);
        mimeMessage.setFrom(noReplyConfig.getAddress());
        mimeMessage.setSender(null);
        mimeMessage.setReplyTo(null);
    }

    @Override
    protected Session getSMTPSession() throws OXException {
        return getSMTPSession(getTransportConfig(), false);
    }

    @Override
    public void sendReceiptAck(MailMessage srcMail, String fromAddr) throws OXException {
        throw new OXException(new IllegalAccessException("Receipts cannot be send by no-reply transport"));
    }

    @Override
    protected ITransportProperties createNewMailProperties() throws OXException {
        return TransportProperties.getInstance();
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    private static final class NoReplySMTPProperties implements ISMTPProperties {

        private final TransportProperties serverProperties;
        private final SMTPProperties smtpProperties;
        private final NoReplyConfig noReplyConfig;

        /**
         * Initializes a new {@link NoReplySMTPProperties}.
         *
         * @param noReplyConfig The no-reply configuration
         */
        NoReplySMTPProperties(NoReplyConfig noReplyConfig) {
            super();
            this.noReplyConfig = noReplyConfig;
            this.smtpProperties = SMTPProperties.getInstance();
            this.serverProperties = TransportProperties.getInstance();
        }

        @Override
        public int getReferencedPartLimit() {
            return serverProperties.getReferencedPartLimit();
        }

        @Override
        public boolean isSmtpEnvelopeFrom() {
            return false;
        }

        @Override
        public boolean isSmtpAuth() {
            return !Strings.isEmpty(noReplyConfig.getLogin()) && !Strings.isEmpty(noReplyConfig.getPassword());
        }

        @Override
        public boolean isLogTransport() {
            return true;
        }

        @Override
        public boolean isSendPartial() {
            return smtpProperties.isSendPartial();
        }

        @Override
        public int getSmtpTimeout() {
            return smtpProperties.getSmtpTimeout();
        }

        @Override
        public String getSmtpLocalhost() {
            return smtpProperties.getSmtpLocalhost();
        }

        @Override
        public int getSmtpConnectionTimeout() {
            return smtpProperties.getSmtpConnectionTimeout();
        }

        @Override
        public String getSmtpAuthEnc() {
            return smtpProperties.getSmtpAuthEnc();
        }

        @Override
        public String getSSLProtocols() {
            return smtpProperties.getSSLProtocols();
        }

        @Override
        public String getSSLCipherSuites() {
            return smtpProperties.getSSLCipherSuites();
        }

        @Override
        public boolean isEnforceSecureConnection() {
            return false;
        }

        @Override
        public void setEnforceSecureConnection(boolean enforceSecureConnection) {
            // Nothing
        }
    }

}
