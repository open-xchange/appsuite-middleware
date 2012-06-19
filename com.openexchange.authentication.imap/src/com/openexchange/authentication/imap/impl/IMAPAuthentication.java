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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.authentication.imap.impl;

import static com.openexchange.authentication.LoginExceptionCodes.INVALID_CREDENTIALS;
import static com.openexchange.authentication.LoginExceptionCodes.UNKNOWN;
import static com.openexchange.authentication.imap.osgi.ImapAuthServiceRegistry.getServiceRegistry;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.IDNA;
import javax.security.auth.login.LoginException;
import org.apache.commons.logging.Log;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.api.MailConfig.LoginSource;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.user.UserService;

public class IMAPAuthentication implements AuthenticationService {

    private enum PropertyNames {
        IMAP_TIMEOUT("IMAP_TIMEOUT"),
        IMAP_CONNECTIONTIMEOUT("IMAP_CONNECTIONTIMEOUT"),
        USE_FULL_LOGIN_INFO("USE_FULL_LOGIN_INFO"),
        IMAP_SERVER("IMAP_SERVER"),
        IMAP_PORT("IMAP_PORT"),
        USE_MULTIPLE("USE_MULTIPLE"),
        IMAP_USE_SECURE("IMAP_USE_SECURE"),
        IMAPAUTHENC("com.openexchange.authentication.imap.imapAuthEnc");

        public String name;

        private PropertyNames(final String name) {
            this.name = name;
        }
    }

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(IMAPAuthentication.class));

    private static Properties props;

    private final static String IMAP_AUTH_PROPERTY_FILE = "/opt/open-xchange/etc/groupware/imapauth.properties";

    /**
     * The string for <code>ISO-8859-1</code> character encoding.
     */
    private static final String CHARENC_ISO8859 = "ISO-8859-1";

    /**
     * Default constructor.
     */
    public IMAPAuthentication() {
        super();
    }

    // /**
    // * Default constructor.
    // */
    // public IMAPAuthentication() {
    // super();
    // }

    /**
     * {@inheritDoc}
     */
    @Override
    public Authenticated handleLoginInfo(final LoginInfo loginInfo) throws OXException {
        // IMAPConnection def = null;

        Session session = null;
        Store imapconnection = null;

        String host = "localhost";
        int port = 143;
        boolean use_full_login = true;
        String user = null;
        String connectiontimeout = "4000";
        String imaptimeout = "4000";

        try {
            if (props == null) {
                initConfig();
            }

            final String[] splitted = split(loginInfo.getUsername());

            final String uid = splitted[1];
            String password = loginInfo.getPassword();
            if ("".equals(uid.trim()) || "".equals(password.trim())) {
                throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
            }

            if (props.get(PropertyNames.IMAPAUTHENC.name) != null) {
                final String authenc = (String) props.get(PropertyNames.IMAPAUTHENC.name);
                try {
                    password = new String(password.getBytes(authenc), CHARENC_ISO8859);
                } catch (final UnsupportedEncodingException e) {
                    LOG.error(e.getMessage(), e);
                    throw LoginExceptionCodes.COMMUNICATION.create(e);
                }
            }

            if (props.get(PropertyNames.IMAP_TIMEOUT.name) != null) {
                imaptimeout = (String) props.get(PropertyNames.IMAP_TIMEOUT.name);
            }

            if (props.get(PropertyNames.IMAP_CONNECTIONTIMEOUT.name) != null) {
                connectiontimeout = (String) props.get(PropertyNames.IMAP_CONNECTIONTIMEOUT.name);
            }

            final Properties imapprops = new Properties();
            imapprops.put("mail.imap.connectiontimeout", connectiontimeout);
            imapprops.put("mail.imap.timeout", imaptimeout);

            if (props.get(PropertyNames.USE_FULL_LOGIN_INFO.name) != null) {
                use_full_login = Boolean.parseBoolean((String) props.get(PropertyNames.USE_FULL_LOGIN_INFO.name));
            }

            if (props.get(PropertyNames.IMAP_SERVER.name) != null) {
                host = IDNA.toASCII((String) props.get(PropertyNames.IMAP_SERVER.name));
            }

            if (props.get(PropertyNames.IMAP_PORT.name) != null) {
                port = Integer.parseInt((String) props.get(PropertyNames.IMAP_PORT.name));
            }

            LOG.debug("Using imap server: " + host);
            LOG.debug("Using imap port: " + port);
            LOG.debug("Using full login info: " + use_full_login);

            // set imap username
            if (use_full_login) {
                user = uid + "@" + splitted[0];
            } else {
                user = uid;
            }

            // multiple imap server support
            // Added by cutmasta
            boolean USE_IMAPS = false;
            if ("true".equalsIgnoreCase(props.getProperty(PropertyNames.USE_MULTIPLE.name))) {
	            final ContextService contextService = getServiceRegistry().getService(ContextService.class, true);
	
	            final int ctxId = contextService.getContextId(splitted[0]);
	            if (ContextStorage.NOT_FOUND == ctxId) {
	                throw INVALID_CREDENTIALS.create();
	            }
	            final Context ctx = contextService.getContext(ctxId);
	
	            final UserService userService = getServiceRegistry().getService(UserService.class, true);
	            final int userId;
	            try {
	                userId = userService.getUserId(uid, ctx);
	            } catch (final OXException e) {
	                throw INVALID_CREDENTIALS.create();
	            }
	            // final User user2 = userService.getUser(userId, ctx);
	            
	            /*
	             * Load primary account and check its protocol to be IMAP
	             */
	            final MailAccount defaultMailAccount =
	                getServiceRegistry().getService(MailAccountStorageService.class, true).getDefaultMailAccount(userId, ctxId);
	            final String mailProtocol = defaultMailAccount.getMailProtocol();
	            if (!mailProtocol.toLowerCase().startsWith("imap")) {
	                throw UNKNOWN.create(new StringBuilder(128).append(
	                    "IMAP authentication failed: Primary account's protocol is not IMAP but ").append(mailProtocol).append(
	                    " for user ").append(userId).append(" in context ").append(ctxId).toString());
	            }
	
	            /*
	             * Set user according to configured login source if different from LoginSource.USER_NAME
	             */
	            final LoginSource loginSource = MailProperties.getInstance().getLoginSource();
	            if (LoginSource.USER_IMAPLOGIN.equals(loginSource)) {
	                user = defaultMailAccount.getLogin();
	            }
	            if (LoginSource.PRIMARY_EMAIL.equals(loginSource)) {
	                user = defaultMailAccount.getPrimaryAddress();
	            }
	
	            /*
	             * Get IMAP server from primary account
	             */
	            host = IDNA.toASCII(defaultMailAccount.getMailServer());
	            port = defaultMailAccount.getMailPort();
	            USE_IMAPS = defaultMailAccount.isMailSecure();
	            LOG.debug("Parsed IMAP Infos: " + (USE_IMAPS ? "imaps" : "imap") + " " + host + " " + port + "  (" + userId + "@" + ctxId + ")");


            } else {
                // ## ssl feature for single defined imap server
                // added by cutmasta
                if ("true".equalsIgnoreCase(props.getProperty(PropertyNames.IMAP_USE_SECURE.name))) {
                    USE_IMAPS = true;
                }
            }

            final String socketFactoryClass = "com.openexchange.tools.ssl.TrustAllSSLSocketFactory";
            final String sPort = String.valueOf(port);
            if (USE_IMAPS) {
                /*
                 * Enables the use of the STARTTLS command.
                 */
                // imapProps.put("mail.imap.starttls.enable", "true");
                /*
                 * Set main socket factory to a SSL socket factory
                 */
                imapprops.put("mail.imap.socketFactory.class", socketFactoryClass);
                imapprops.put("mail.imap.socketFactory.port", sPort);
                imapprops.put("mail.imap.socketFactory.fallback", "false");
                /*
                 * Needed for JavaMail >= 1.4
                 */
                // Security.setProperty("ssl.SocketFactory.provider", socketFactoryClass);
            } else {
                /*
                 * Enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected connection.
                 */
                imapprops.put("mail.imap.starttls.enable", "true");
                /*
                 * Specify the javax.net.ssl.SSLSocketFactory class, this class will be used to create IMAP SSL sockets if TLS handshake says
                 * so.
                 */
                imapprops.put("mail.imap.socketFactory.port", sPort);
                imapprops.put("mail.imap.ssl.socketFactory.class", socketFactoryClass);
                imapprops.put("mail.imap.ssl.socketFactory.port", sPort);
                imapprops.put("mail.imap.socketFactory.fallback", "false");
                /*
                 * Specify SSL protocols
                 */
                imapprops.put("mail.imap.ssl.protocols", "SSLv3 TLSv1");
                // imapProps.put("mail.imap.ssl.enable", "true");
                /*
                 * Needed for JavaMail >= 1.4
                 */
                // Security.setProperty("ssl.SocketFactory.provider", socketFactoryClass);
            }

            session = Session.getInstance(imapprops, null);
            session.setDebug(false);

            imapconnection = session.getStore("imap");
            // try to connect with the credentials set above
            imapconnection.connect(host, port, user, password);
            LOG.info("Imap authentication for user " + user + " successful on host " + host + ":" + port);

            /*
             * Set the context of the user, If full login was configured, we use the domain part as the context name/mapping entry. If NO
             * full login was configured, we assume that only 1 context is in the system which is named "defaultcontext".
             */
            if (use_full_login) {
                LOG.debug("Using domain: " + splitted[0] + " as context name!");
            } else {
                LOG.debug("Using \"defaultcontext\" as context name!");
                splitted[0] = "defaultcontext";
            }
            return new Authenticated() {

                @Override
                public String getContextInfo() {
                    return splitted[0];
                }

                @Override
                public String getUserInfo() {
                    return splitted[1];
                }
            };
        } catch (final ConfigurationException e) {
            LOG.error("Error reading auth plugin config!", e);
            throw LoginExceptionCodes.COMMUNICATION.create(e);
        } catch (final NoSuchProviderException e) {
            LOG.error("Error setup initial imap envorinment!", e);
            throw LoginExceptionCodes.COMMUNICATION.create(e);
        } catch (final AuthenticationFailedException e) {
            LOG.info("Authentication error on host " + host + ":" + port + " for user " + user, e);
            LOG.debug("Debug imap authentication", e);
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create(e);
        } catch (final MessagingException e) {
            LOG.info("Messaging error on host " + host + ":" + port + " for user " + user, e);
            LOG.debug("Debug imap error", e);
            throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
        } finally {
            try {
                if (imapconnection != null) {
                    imapconnection.close();
                }
            } catch (final MessagingException e) {
                LOG.error("Error closing imap connection!", e);
                throw LoginExceptionCodes.COMMUNICATION.create(e);
            }
        }
    }

    @Override
    public Authenticated handleAutoLoginInfo(final LoginInfo loginInfo) throws OXException {
        throw LoginExceptionCodes.NOT_SUPPORTED.create(IMAPAuthentication.class.getName());
    }

    private static void initConfig() throws OXException {
        synchronized (IMAPAuthentication.class) {
            if (null == props) {
                final File file = new File(IMAP_AUTH_PROPERTY_FILE);
                if (!file.exists()) {
                    throw com.openexchange.configuration.ConfigurationExceptionCodes.FILE_NOT_FOUND.create(file.getAbsolutePath());
                }
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    props = new Properties();
                    props.load(fis);
                } catch (final IOException e) {
                    throw com.openexchange.configuration.ConfigurationExceptionCodes.NOT_READABLE.create(file.getAbsolutePath());
                } finally {
                    if (null != fis) {
                        try {
                            fis.close();
                        } catch (final IOException e) {
                            LOG.error("Error closing file inputstream for file " + IMAP_AUTH_PROPERTY_FILE + " ", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Splits user name and context.
     * 
     * @param loginInfo combined information separated by an @ sign.
     * @return a string array with context and user name (in this order).
     */
    private String[] split(final String loginInfo) {
        return split(loginInfo, '@');
    }

    /**
     * Splits user name and context.
     * 
     * @param loginInfo combined information separated by an @ sign.
     * @param separator for splitting user name and context.
     * @return a string array with context and user name (in this order).
     * @throws LoginException if no separator is found.
     */
    private String[] split(final String loginInfo, final char separator) {
        final int pos = loginInfo.lastIndexOf(separator);
        final String[] splitted = new String[2];
        if (-1 == pos) {
            splitted[1] = loginInfo;
            splitted[0] = "defaultcontext";
        } else {
            splitted[1] = loginInfo.substring(0, pos);
            splitted[0] = loginInfo.substring(pos + 1);
        }
        return splitted;
    }

}
