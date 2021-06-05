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

package com.openexchange.smtp;

import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.transport.config.ITransportProperties;
import com.openexchange.mail.transport.config.NoReplyConfig;
import com.openexchange.mail.transport.config.NoReplyConfig.SecureMode;
import com.openexchange.mail.transport.config.NoReplyConfigFactory;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.mail.usersetting.UserSettingMail;
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
    private final boolean useNoReplyAddress;

    /**
     * Initializes a new {@link NoReplySMTPTransport}.
     *
     * @param contextId The context identifier
     * @throws OXException If initialization fails
     */
    public NoReplySMTPTransport(int contextId) throws OXException {
        this(contextId, true);
    }

    /**
     * Initializes a new {@link NoReplySMTPTransport}.
     *
     * @param contextId The context identifier
     * @param useNoReplyAddress <code>true</code> to use configured no-reply address; otherwise <code>false</code> to keep existing "From" header (if any)
     * @throws OXException If initialization fails
     */
    public NoReplySMTPTransport(int contextId, boolean useNoReplyAddress) throws OXException {
        super(contextId);
        this.useNoReplyAddress = useNoReplyAddress;
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
        if (useNoReplyAddress) {
            InternetAddress noReplyAddress = noReplyConfig.getAddress();
            String noReplyPersonal = mimeMessage.getHeader(NoReplyConfig.HEADER_NO_REPLY_PERSONAL, null);
            if (Strings.isNotEmpty(noReplyPersonal)) {
                mimeMessage.removeHeader(NoReplyConfig.HEADER_NO_REPLY_PERSONAL);
                try {
                    QuotedInternetAddress newAddr = new QuotedInternetAddress();
                    newAddr.setAddress(noReplyAddress.getAddress());
                    newAddr.setPersonal(noReplyPersonal, "UTF-8");
                    noReplyAddress = newAddr;
                } catch (UnsupportedEncodingException e) {
                    // Cannot occur
                }
            }
            mimeMessage.setFrom(noReplyAddress);
            mimeMessage.setSender(null);
            mimeMessage.setReplyTo(null);
        }
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
            return Strings.isNotEmpty(noReplyConfig.getLogin()) && Strings.isNotEmpty(noReplyConfig.getPassword());
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
        public String getPrimaryAddressHeader() {
            // Not needed for special no-reply MTA
            return null;
        }
    }

}
