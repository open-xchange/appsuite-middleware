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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.jsieve.export;

import java.net.URI;
import java.net.URISyntaxException;
import javax.mail.internet.idn.IDNA;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mailfilter.Credentials;
import com.openexchange.mailfilter.MailFilterProperties;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.mailfilter.services.Services;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;

/**
 * {@link SieveHandlerFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class SieveHandlerFactory {

    /**
     * Connect to the Sieve server and return a handler
     *
     * @param creds credentials
     * @return a sieve handler
     * @throws OXException
     */
    public static SieveHandler getSieveHandler(Credentials creds) throws OXException {
        return getSieveHandler(creds, false);
    }

    /**
     * Connect to the Sieve server and return a handler
     *
     * @param creds credentials
     * @param onlyWelcome <code>true</code> if only server's welcome message is of interest; otherwise <code>false</code> for full login round-trip
     * @return a sieve handler
     * @throws OXException
     */
    public static SieveHandler getSieveHandler(Credentials creds, boolean onlyWelcome) throws OXException {
        ConfigurationService config = Services.getService(ConfigurationService.class);

        // Determine & parse host and port
        int sieve_port;
        String sieve_server;
        User user = null;
        {
            String logintype = config.getProperty(MailFilterProperties.Values.SIEVE_LOGIN_TYPE.property);
            if (MailFilterProperties.LoginTypes.GLOBAL.name.equals(logintype)) {
                sieve_server = config.getProperty(MailFilterProperties.Values.SIEVE_SERVER.property);
                if (null == sieve_server) {
                    throw MailFilterExceptionCode.PROPERTY_ERROR.create(MailFilterProperties.Values.SIEVE_SERVER.property);
                }
                try {
                    sieve_port = Integer.parseInt(config.getProperty(MailFilterProperties.Values.SIEVE_PORT.property));
                } catch (final RuntimeException e) {
                    throw MailFilterExceptionCode.PROPERTY_ERROR.create(e, MailFilterProperties.Values.SIEVE_PORT.property);
                }
            } else if (MailFilterProperties.LoginTypes.USER.name.equals(logintype)) {
                user = getUser(creds, null);
                {
                    String mailServerURL = user.getImapServer();
                    try {
                        URI uri = URIParser.parse(IDNA.toASCII(mailServerURL), URIDefaults.IMAP);
                        sieve_server = uri.getHost();
                    } catch (final URISyntaxException e) {
                        throw MailFilterExceptionCode.NO_SERVERNAME_IN_SERVERURL.create(e, mailServerURL);
                    }
                }

                try {
                    sieve_port = Integer.parseInt(config.getProperty(MailFilterProperties.Values.SIEVE_PORT.property));
                } catch (final RuntimeException e) {
                    throw MailFilterExceptionCode.PROPERTY_ERROR.create(e, MailFilterProperties.Values.SIEVE_PORT.property);
                }
            } else {
                throw MailFilterExceptionCode.NO_VALID_LOGIN_TYPE.create();
            }
        }

        if (onlyWelcome) {
            // Host name and port are sufficient...
            return new SieveHandler(sieve_server, sieve_port);
        }

        // Get SIEVE_AUTH_ENC property
        String authEnc = config.getProperty(MailFilterProperties.Values.SIEVE_AUTH_ENC.property, MailFilterProperties.Values.SIEVE_AUTH_ENC.def);

        // Determine & parse login and password dependent on configured credentials source
        switch (getCredSrc(config)) {
            case IMAP_LOGIN:
                {
                    String authname = getUser(creds, user).getImapLogin();
                    return newSieveHandlerUsing(sieve_server, sieve_port, creds.getUsername(), authname, getRightPassword(config, creds), authEnc, creds.getOauthToken());
                }
            case MAIL:
                {
                    String authname = getUser(creds, user).getMail();
                    return newSieveHandlerUsing(sieve_server, sieve_port, creds.getUsername(), authname, getRightPassword(config, creds), authEnc, creds.getOauthToken());
                }
            case SESSION:
                // fall-through
            case SESSION_FULL_LOGIN:
                return newSieveHandlerUsing(sieve_server, sieve_port, creds.getUsername(), creds.getAuthname(), getRightPassword(config, creds), authEnc, creds.getOauthToken());
            default:
                throw MailFilterExceptionCode.NO_VALID_CREDSRC.create();

        }
    }

    private static SieveHandler newSieveHandlerUsing(String host, int port, String userName, String authName, String password, String authEncoding, String oauthToken) {
        return new SieveHandler(null == userName ? authName : userName, authName, password, host, port, authEncoding, oauthToken);
    }

    private static User getUser(Credentials creds, User user) throws OXException {
        if (null != user) {
            return user;
        }

        User storageUser = UserStorage.getInstance().getUser(creds.getUserid(), creds.getContextid());
        if (null == storageUser) {
            throw MailFilterExceptionCode.INVALID_CREDENTIALS.create("Could not get a valid user object for uid " + creds.getUserid() + " and contextid " + creds.getContextid());
        }
        return storageUser;
    }

    private static MailFilterProperties.CredSrc getCredSrc(ConfigurationService config) throws OXException {
        String sCredSrc = config.getProperty(MailFilterProperties.Values.SIEVE_CREDSRC.property);
        MailFilterProperties.CredSrc credSrc = MailFilterProperties.CredSrc.credSrcFor(sCredSrc);
        if (null == credSrc) {
            // Unknown credsrc
            throw MailFilterExceptionCode.NO_VALID_CREDSRC.create();
        }
        return credSrc;
    }

    /**
     * Get the correct password according to the credentials
     *
     * @param config
     * @param creds
     * @return
     * @throws OXException
     */
    public static String getRightPassword(final ConfigurationService config, final Credentials creds) throws OXException {
        String sPasswordsrc = config.getProperty(MailFilterProperties.Values.SIEVE_PASSWORDSRC.property);
        MailFilterProperties.PasswordSource passwordSource = MailFilterProperties.PasswordSource.passwordSourceFor(sPasswordsrc);
        if (null == passwordSource) {
            throw MailFilterExceptionCode.NO_VALID_PASSWORDSOURCE.create();
        }

        switch (passwordSource) {
            case GLOBAL:
                {
                    String masterpassword = config.getProperty(MailFilterProperties.Values.SIEVE_MASTERPASSWORD.property);
                    if (null == masterpassword || masterpassword.length() == 0) {
                        throw MailFilterExceptionCode.NO_MASTERPASSWORD_SET.create();
                    }
                    return masterpassword;
                }
            case SESSION:
                return creds.getPassword();
            default:
                throw MailFilterExceptionCode.NO_VALID_PASSWORDSOURCE.create();
        }
    }
}
