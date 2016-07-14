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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.tools.MailValidator;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.net.URIDefaults;

/**
 * {@link Guess}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Guess extends AbstractConfigSource {

    private static final List<String> IMAP_PREFIXES = Arrays.asList("", "imap.", "mail.");

    private static final List<String> SMTP_PREFIXES = Arrays.asList("", "smtp.", "mail.");

    private static final List<String> POP3_PREFIXES = Arrays.asList("", "pop3.", "mail.");

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
    public Autoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, User user, Context context) throws OXException {
        return getAutoconfig(emailLocalPart, emailDomain, password, user, context, true);
    }

    @Override
    public Autoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, User user, Context context, boolean forceSecure) throws OXException {
        ConfigViewFactory configViewFactory = services.getService(ConfigViewFactory.class);
        ConfigView view = configViewFactory.getView(user.getId(), context.getContextId());
        ComposedConfigProperty<Boolean> property = view.property("com.openexchange.mail.autoconfig.allowGuess", boolean.class);
        if (property.isDefined() && !property.get().booleanValue()) {
            // Guessing is disabled
            return null;
        }

        // Proceed...
        Autoconfig config = new Autoconfig();

        final Map<String, Object> properties = new HashMap<String, Object>(2);
        boolean imapSuccess = fillProtocol(URIDefaults.IMAP, emailLocalPart, emailDomain, password, config, properties, forceSecure);
        boolean generalSuccess = imapSuccess;
        if (!imapSuccess) {
            generalSuccess = fillProtocol(URIDefaults.POP3, emailLocalPart, emailDomain, password, config, properties, forceSecure) || generalSuccess;
        }

        boolean preGeneralSuccess = generalSuccess;
        generalSuccess = fillProtocol(URIDefaults.SMTP, emailLocalPart, emailDomain, password, config, properties, forceSecure) || generalSuccess;

        if (properties.containsKey("smtp.auth-supported")) {
            final Boolean smtpAuthSupported = (Boolean) properties.get("smtp.auth-supported");
            if (!smtpAuthSupported.booleanValue() && !preGeneralSuccess) {
                // Neither IMAP nor POP3 reachable, but SMTP works as it does not support authentication
                // Therefore return null
                return null;
            }
        }


        return generalSuccess ? config : null;
    }

    private boolean fillProtocol(URIDefaults protocol, String emailLocalPart, String emailDomain, String password, Autoconfig config, Map<String, Object> properties, boolean forceSecure) {
        Object[] guessedHost = guessHost(protocol, emailDomain);
        if (guessedHost != null) {
            String host = (String) guessedHost[0];
            boolean secure = (Boolean) guessedHost[1];
            Integer port = (Integer) guessedHost[2];
            String login = guessLogin(protocol, host, port.intValue(), secure, forceSecure, emailLocalPart, emailDomain, password, properties);
            if (login == null) {
                return false;
            }
            if (protocol == URIDefaults.SMTP) {
                config.setTransportPort(port);
                config.setTransportProtocol(protocol.getProtocol());
                config.setTransportSecure(secure);
                config.setTransportServer(host);
                config.setUsername(login);
            } else {
                config.setMailPort(port);
                config.setMailProtocol(protocol.getProtocol());
                config.setMailSecure(secure);
                config.setMailServer(host);
                config.setUsername(login);
            }
            config.setMailStartTls(forceSecure);
            config.setTransportStartTls(forceSecure);
            return true;
        }
        return false;
    }

    private String guessLogin(URIDefaults protocol, String host, int port, boolean secure, boolean requireTls, String emailLocalPart, String emailDomain, String password, Map<String, Object> properties) {
        List<String> logins = Arrays.asList(emailLocalPart, emailLocalPart+"@"+emailDomain);

        for (String login : logins) {
            if (protocol == URIDefaults.IMAP) {
                if (MailValidator.validateImap(host, port, secure, requireTls, login, password)) {
                    return login;
                }
            } else if (protocol == URIDefaults.POP3) {
                if (MailValidator.validatePop3(host, port, secure, requireTls, login, password)) {
                    return login;
                }
            } else if (protocol == URIDefaults.SMTP) {
                if (MailValidator.validateSmtp(host, port, secure, requireTls, login, password, properties)) {
                    return login;
                }
            }
        }
        return null;
    }

    private Object[] guessHost(URIDefaults protocol, String emailDomain) {
        List<String> prefixes = null;
        int altPort = 0;
        if (protocol == URIDefaults.IMAP) {
            prefixes = IMAP_PREFIXES;
        } else if (protocol == URIDefaults.SMTP) {
            prefixes = SMTP_PREFIXES;
            altPort = 587;
        } else if (protocol == URIDefaults.POP3) {
            prefixes = POP3_PREFIXES;
        }

        if (prefixes == null) {
            return null;
        }

        for (String prefix : prefixes) {
            String host = prefix + emailDomain;
            if (checkSave(protocol, host, protocol.getSSLPort(), true)) {
                return new Object[] { host, true, protocol.getSSLPort() };
            } else if (checkSave(protocol, host, protocol.getPort(), true)) {
                return new Object[] { host, true, protocol.getPort() };
            } else if (altPort > 0 && checkSave(protocol, host, altPort, false)) {
                return new Object[] { host, false, altPort };
            } else if (checkSave(protocol, host, protocol.getPort(), false)) {
                return new Object[] { host, false, protocol.getPort() };
            }
        }
        return null;
    }

    private boolean checkSave(URIDefaults protocol, String emailDomain, int port, boolean secure) {
        try {
            if (protocol == URIDefaults.IMAP) {
                return MailValidator.checkForImap(emailDomain, port, secure);
            } else if (protocol == URIDefaults.SMTP) {
                return MailValidator.checkForSmtp(emailDomain, port, secure);
            } else if (protocol == URIDefaults.POP3) {
                return MailValidator.checkForPop3(emailDomain, port, secure);
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

}
