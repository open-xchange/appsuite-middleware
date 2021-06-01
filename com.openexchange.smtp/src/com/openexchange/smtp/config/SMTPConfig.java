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

package com.openexchange.smtp.config;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import javax.mail.internet.idn.IDNA;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.AuthType;
import com.openexchange.mail.api.MailCapabilities;
import com.openexchange.mail.api.UrlInfo;
import com.openexchange.mail.oauth.MailOAuthService;
import com.openexchange.mail.oauth.TokenInfo;
import com.openexchange.mail.transport.config.ITransportProperties;
import com.openexchange.mail.transport.config.TransportAuthSupportAware;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mail.utils.MailPasswordUtil;
import com.openexchange.mailaccount.Account;
import com.openexchange.mailaccount.TransportAuth;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.smtp.SMTPExceptionCode;
import com.openexchange.smtp.services.Services;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;

/**
 * {@link SMTPConfig} - The SMTP configuration.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SMTPConfig extends TransportConfig implements TransportAuthSupportAware {

    private static final String PROTOCOL_SMTP_SECURE = "smtps";

    /*
     * +++++++++ User-specific fields +++++++++
     */

    private boolean secure;

    private int smtpPort;

    private String smtpServer;
    private InetAddress smtpServerAddress;

    private ISMTPProperties transportProperties;

    /**
     * Default constructor
     */
    public SMTPConfig() {
        super();
    }

    @Override
    public MailCapabilities getCapabilities() {
        return MailCapabilities.EMPTY_CAPS;
    }

    /**
     * Gets the smtpPort
     *
     * @return the smtpPort
     */
    @Override
    public int getPort() {
        return smtpPort;
    }

    /**
     * Gets the smtpServer
     *
     * @return the smtpServer
     */
    @Override
    public String getServer() {
        return smtpServer;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    protected void parseServerURL(final UrlInfo urlInfo) throws OXException {
        final URI uri;
        try {
            uri = URIParser.parse(urlInfo.getServerURL(), URIDefaults.SMTP);
        } catch (URISyntaxException e) {
            throw SMTPExceptionCode.URI_PARSE_FAILED.create(e, urlInfo.getServerURL());
        }
        secure = PROTOCOL_SMTP_SECURE.equals(uri.getScheme());
        smtpServer = uri.getHost();
        smtpPort = uri.getPort();
        requireTls = urlInfo.isRequireStartTls();
    }

    @Override
    public void setPort(final int smtpPort) {
        this.smtpPort = smtpPort;
    }

    @Override
    public void setSecure(final boolean secure) {
        this.secure = secure;
    }

    @Override
    public void setServer(final String smtpServer) {
        this.smtpServer = null == smtpServer ? null : IDNA.toUnicode(smtpServer);
    }

    /**
     * Gets the Internet Protocol (IP) address of the SMTP server.
     *
     * @return The Internet Protocol (IP) address of the SMTP server.
     * @throws OXException If SMTP server cannot be resolved
     */
    public InetAddress getSmtpServerAddress() throws OXException {
        if (null == smtpServerAddress) {
            try {
                smtpServerAddress = InetAddress.getByName(getServer());
            } catch (UnknownHostException e) {
                throw SMTPExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        }
        return smtpServerAddress;
    }

    @Override
    public ITransportProperties getTransportProperties() {
        return transportProperties;
    }

    @Override
    public boolean isAuthSupported() throws OXException {
        ISMTPProperties transportProperties = this.transportProperties;
        if (null == transportProperties) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create("SMTP config not yet initialized");
        }
        return transportProperties.isSmtpAuth();
    }

    public ISMTPProperties getSMTPProperties() {
        return transportProperties;
    }

    @Override
    public void setTransportProperties(final ITransportProperties transportProperties) {
        this.transportProperties = (ISMTPProperties) transportProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCustomParsing(final Account account, final Session session) throws OXException {
        if (!account.isDefaultAccount()) {
            TransportAuth transportAuth = account.getTransportAuth();
            if (transportAuth == null) {
                return true;
            }
            switch (transportAuth) {
            case CUSTOM:
                int oAuthAccontId = assumeOauthFor(account, false);
                if (oAuthAccontId >= 0) {
                    // Do the OAuth dance...
                    MailOAuthService mailOAuthService = Services.optService(MailOAuthService.class);
                    if (null == mailOAuthService) {
                        throw ServiceExceptionCode.absentService(MailOAuthService.class);
                    }

                    TokenInfo tokenInfo = mailOAuthService.getTokenFor(oAuthAccontId, session);
                    login = saneLogin(account.getTransportLogin());
                    password = tokenInfo.getToken();
                    authType = AuthType.parse(tokenInfo.getAuthMechanism());
                } else {
                    login = saneLogin(account.getTransportLogin());
                    password = MailPasswordUtil.decrypt(account.getTransportPassword(), session, account.getId(), login, account.getTransportServer());
                }
                break;
            case NONE:
                login = null;
                password = null;
                break;
            case MAIL:
                /* fall-through */
            default:
                /* Do nothing as login/password already set by calling MailConfig.fillLoginAndPassword() in TransportConfig.getTransportConfig() */
                break;
            }

            return true;
        }
        return false;
    }

}
