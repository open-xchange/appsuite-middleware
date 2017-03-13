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
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mailfilter.Credentials;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.mailfilter.properties.CredentialSource;
import com.openexchange.mailfilter.properties.LoginType;
import com.openexchange.mailfilter.properties.MailFilterProperty;
import com.openexchange.mailfilter.properties.PasswordSource;
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
        LeanConfigurationService mailFilterConfig = Services.getService(LeanConfigurationService.class);

        int userId = creds.getUserid();
        int contextId = creds.getContextid();

        // Determine & parse host and port
        int sievePort;
        String sieveServer;
        User user = null;
        {
            String sCredSrc = mailFilterConfig.getProperty(userId, contextId, MailFilterProperty.loginType);
            LoginType loginType = LoginType.loginTypeFor(sCredSrc);

            switch (loginType) {
                case GLOBAL:
                    sieveServer = mailFilterConfig.getProperty(userId, contextId, MailFilterProperty.server);
                    if (null == sieveServer) {
                        throw MailFilterExceptionCode.PROPERTY_ERROR.create(MailFilterProperty.server.getFQPropertyName());
                    }
                    sievePort = getPort(mailFilterConfig, userId, contextId);
                    break;
                case USER:
                    user = getUser(creds);
                    String mailServerURL = user.getImapServer();
                    try {
                        URI uri = URIParser.parse(IDNA.toASCII(mailServerURL), URIDefaults.IMAP);
                        sieveServer = uri.getHost();
                    } catch (final URISyntaxException e) {
                        throw MailFilterExceptionCode.NO_SERVERNAME_IN_SERVERURL.create(e, mailServerURL);
                    }
                    sievePort = getPort(mailFilterConfig, userId, contextId);

                    break;
                default:
                    throw MailFilterExceptionCode.NO_VALID_LOGIN_TYPE.create();
            }
        }

        if (onlyWelcome) {
            // Host name and port are sufficient...
            return new SieveHandler(sieveServer, sievePort);
        }

        // Get the 'authenticationEncoding' property
        String authEnc = mailFilterConfig.getProperty(userId, contextId, MailFilterProperty.authenticationEncoding);

        // Determine & parse login and password dependent on configured credentials source
        String sCredSrc = mailFilterConfig.getProperty(userId, contextId, MailFilterProperty.credentialSource);
        CredentialSource credentialSource = CredentialSource.credentialSourceFor(sCredSrc);
        switch (credentialSource) {
            case IMAP_LOGIN: {
                String authname = getUser(creds, user).getImapLogin();
                return newSieveHandlerUsing(sieveServer, sievePort, creds.getUsername(), authname, getRightPassword(mailFilterConfig, creds), authEnc, creds.getOauthToken(), creds.getUserid(), creds.getContextid());
            }
            case MAIL: {
                String authname = getUser(creds, user).getMail();
                return newSieveHandlerUsing(sieveServer, sievePort, creds.getUsername(), authname, getRightPassword(mailFilterConfig, creds), authEnc, creds.getOauthToken(), creds.getUserid(), creds.getContextid());
            }
            case SESSION:
                // fall-through
            case SESSION_FULL_LOGIN:
                return newSieveHandlerUsing(sieveServer, sievePort, creds.getUsername(), creds.getAuthname(), getRightPassword(mailFilterConfig, creds), authEnc, creds.getOauthToken(), creds.getUserid(), creds.getContextid());
            default:
                throw MailFilterExceptionCode.NO_VALID_CREDSRC.create();
        }
    }

    /**
     * Creates an new {@link SieveHandler} with the specified properties
     * 
     * @param host The host
     * @param port The port
     * @param userName The username
     * @param authName The authentication name
     * @param password The password
     * @param authEncoding The authentication encoding
     * @param oauthToken The oauth token
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The {@link SieveHandler}
     */
    private static SieveHandler newSieveHandlerUsing(String host, int port, String userName, String authName, String password, String authEncoding, String oauthToken, int userId, int contextId) {
        return new SieveHandler(null == userName ? authName : userName, authName, password, host, port, authEncoding, oauthToken, userId, contextId);
    }

    /**
     * Get the user
     * 
     * @param credentials The {@link Credentials}
     * @return the user
     * @throws OXException
     */
    private static User getUser(Credentials credentials) throws OXException {
        return getUser(credentials, null);
    }

    /**
     * Get the user
     * 
     * @param creds The {@link Credentials}
     * @param user The optional {@link User}
     * @return the user
     * @throws OXException
     */
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

    /**
     * Get the port from the configuration service
     * 
     * @param mailFilterConfig The {@link MailFilterConfigurationService}
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The sieve port
     * @throws OXException if an error is occurred
     */
    private static int getPort(LeanConfigurationService mailFilterConfig, int userId, int contextId) throws OXException {
        try {
            return mailFilterConfig.getIntProperty(userId, contextId, MailFilterProperty.port);
        } catch (final RuntimeException e) {
            throw MailFilterExceptionCode.PROPERTY_ERROR.create(e, MailFilterProperty.port.getFQPropertyName());
        }
    }

    /**
     * Get the correct password according to the credentials
     *
     * @param config
     * @param creds
     * @return
     * @throws OXException
     */
    public static String getRightPassword(final LeanConfigurationService config, final Credentials creds) throws OXException {
        int userId = creds.getUserid();
        int contextId = creds.getContextid();
        String sPasswordsrc = config.getProperty(userId, contextId, MailFilterProperty.passwordSource);
        PasswordSource passwordSource = PasswordSource.passwordSourceFor(sPasswordsrc);

        switch (passwordSource) {
            case GLOBAL: {
                String masterpassword = config.getProperty(userId, contextId, MailFilterProperty.masterPassword);
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
