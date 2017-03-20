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

package com.openexchange.mail.autoconfig.sources;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.autoconfig.DefaultAutoconfig;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.tools.ConnectMode;
import com.openexchange.mail.autoconfig.tools.MailValidator;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.net.URIDefaults;

/**
 * {@link Guess}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Guess extends AbstractConfigSource {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Guess.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link Guess}.
     */
    public Guess(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public Autoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, int userId, int contextId) throws OXException {
        return getAutoconfig(emailLocalPart, emailDomain, password, userId, contextId, true);
    }

    @Override
    public DefaultAutoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, int userId, int contextId, boolean forceSecure) throws OXException {
        ConfigViewFactory configViewFactory = services.getService(ConfigViewFactory.class);
        ConfigView view = configViewFactory.getView(userId, contextId);
        ComposedConfigProperty<Boolean> property = view.property("com.openexchange.mail.autoconfig.allowGuess", boolean.class);
        if (property.isDefined() && !property.get().booleanValue()) {
            // Guessing is disabled
            return null;
        }

        // Guess the configuration...
        DefaultAutoconfig config = new DefaultAutoconfig();
        Map<String, Object> properties = new HashMap<String, Object>(2);

        // Check mail access (first IMAP, then POP3)
        boolean imapSuccess = fillProtocol(Protocol.IMAP, emailLocalPart, emailDomain, password, config, properties, forceSecure);
        boolean generalSuccess = imapSuccess;
        if (!imapSuccess) {
            generalSuccess = fillProtocol(Protocol.POP3, emailLocalPart, emailDomain, password, config, properties, forceSecure) || generalSuccess;
        }

        boolean mailSuccess = generalSuccess;

        // Check transport access (SMTP)
        generalSuccess = fillProtocol(Protocol.SMTP, emailLocalPart, emailDomain, password, config, properties, forceSecure) || generalSuccess;

        // Check for special SMTP that does not support authentication
        {
            Boolean smtpAuthSupported = (Boolean) properties.get("smtp.auth-supported");
            if (null != smtpAuthSupported && !smtpAuthSupported.booleanValue() && !mailSuccess) {
                // Neither IMAP nor POP3 reachable, but SMTP works as it does not support authentication
                // Therefore return null
                return null;
            }
        }


        return generalSuccess ? config : null;
    }

    private boolean fillProtocol(Protocol protocol, String emailLocalPart, String emailDomain, String password, DefaultAutoconfig config, Map<String, Object> properties, boolean forceSecure) {
        ConnectSettings connectSettings = guessHost(protocol, emailDomain);
        if (connectSettings == null) {
            return false;
        }

        String login = guessLogin(protocol, connectSettings.host, connectSettings.port, connectSettings.secure, forceSecure, emailLocalPart, emailDomain, password, properties);
        if (login == null) {
            return false;
        }

        if (Protocol.SMTP == protocol) {
            config.setTransportPort(connectSettings.port);
            config.setTransportProtocol(protocol.getProtocol());
            config.setTransportSecure(connectSettings.secure);
            config.setTransportServer(connectSettings.host);
            config.setTransportStartTls(forceSecure); // Take over since end-point has been checked with proper STARTTLS setting if we reach this point
            config.setUsername(login);
        } else {
            config.setMailPort(connectSettings.port);
            config.setMailProtocol(protocol.getProtocol());
            config.setMailSecure(connectSettings.secure);
            config.setMailServer(connectSettings.host);
            config.setMailStartTls(forceSecure); // Take over since end-point has been checked with proper STARTTLS setting if we reach this point
            config.setUsername(login);
        }
        return true;
    }

    private String guessLogin(Protocol protocol, String host, int port, boolean secure, boolean requireTls, String emailLocalPart, String emailDomain, String password, Map<String, Object> properties) {
        List<String> logins = Arrays.asList(emailLocalPart, emailLocalPart+"@"+emailDomain);
        ConnectMode connectMode = ConnectMode.connectModeFor(secure, requireTls);

        for (String login : logins) {
            switch (protocol) {
                case IMAP:
                    if (MailValidator.validateImap(host, port, connectMode, login, password)) {
                        return login;
                    }
                    break;
                case POP3:
                    if (MailValidator.validatePop3(host, port, connectMode, login, password)) {
                        return login;
                    }
                    break;
                case SMTP:
                    if (MailValidator.validateSmtp(host, port, connectMode, login, password, properties)) {
                        return login;
                    }
                    break;
            }
        }

        return null;
    }

    private static final List<String> IMAP_PREFIXES = Arrays.asList("", "imap.", "mail.");
    private static final List<String> SMTP_PREFIXES = Arrays.asList("", "smtp.", "mail.");
    private static final List<String> POP3_PREFIXES = Arrays.asList("", "pop3.", "mail.");

    private ConnectSettings guessHost(Protocol protocol, String emailDomain) {
        URIDefaults uriDefaults = null;
        List<String> prefixes = null;
        int altPort = 0;

        switch (protocol) {
            case IMAP:
                prefixes = IMAP_PREFIXES;
                uriDefaults = URIDefaults.IMAP;
                break;
            case POP3:
                prefixes = POP3_PREFIXES;
                uriDefaults = URIDefaults.POP3;
                break;
            case SMTP:
                prefixes = SMTP_PREFIXES;
                altPort = 587;
                uriDefaults = URIDefaults.SMTP;
                break;
            default:
                return null;
        }

        for (String prefix : prefixes) {
            String host = prefix + emailDomain;

            // Try SSL connect using default SSL port
            if (tryConnect(protocol, host, uriDefaults.getSSLPort(), true)) {
                return new ConnectSettings(host, true, uriDefaults.getSSLPort());
            }

            // Try SSL connect using default port
            if (tryConnect(protocol, host, uriDefaults.getPort(), true)) {
                return new ConnectSettings(host, true, uriDefaults.getPort());
            }

            // Try plain connect using alternative port
            if (altPort > 0 && tryConnect(protocol, host, altPort, false)) {
                return new ConnectSettings(host, false, altPort);
            }

            // Try plain connect using default port
            if (tryConnect(protocol, host, uriDefaults.getPort(), false)) {
                return new ConnectSettings(host, false, uriDefaults.getPort());
            }
        }

        return null;
    }

    private boolean tryConnect(Protocol protocol, String emailDomain, int port, boolean secure) {
        switch (protocol) {
            case IMAP:
                return MailValidator.tryImapConnect(emailDomain, port, secure);
            case POP3:
                return MailValidator.tryPop3Connect(emailDomain, port, secure);
            case SMTP:
                return MailValidator.trySmtpConnect(emailDomain, port, secure);
        }

        return false;
    }

    // ----------------------------------------------------- Helper classes -------------------------------------------------

    private static enum Protocol {
        IMAP("imap"), SMTP("smtp"), POP3("pop3");

        private final String protocol;

        private Protocol(String protocol) {
            this.protocol = protocol;
        }


        String getProtocol() {
            return protocol;
        }
    }

    private static class ConnectSettings {

        final String host;
        final int port;
        final boolean secure;

        ConnectSettings(String host, boolean secure, int port) {
            super();
            this.host = host;
            this.port = port;
            this.secure = secure;
        }
    }

}
