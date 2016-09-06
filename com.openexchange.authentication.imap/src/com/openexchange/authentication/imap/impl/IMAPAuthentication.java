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

package com.openexchange.authentication.imap.impl;

import static com.openexchange.authentication.LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_CONTEXT_MAPPING;
import static com.openexchange.authentication.LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_USER_MAPPING;
import static com.openexchange.authentication.LoginExceptionCodes.UNKNOWN;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.idn.IDNA;
import javax.security.auth.login.LoginException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.java.Strings;
import com.openexchange.mail.api.MailConfig.LoginSource;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;

public class IMAPAuthentication implements AuthenticationService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IMAPAuthentication.class);

    private static enum PropertyNames {
        IMAP_TIMEOUT("IMAP_TIMEOUT"),
        IMAP_CONNECTIONTIMEOUT("IMAP_CONNECTIONTIMEOUT"),
        USE_FULL_LOGIN_INFO("USE_FULL_LOGIN_INFO"),
        USE_FULL_LOGIN_INFO_FOR_USER_LOOKUP("USE_FULL_LOGIN_INFO_FOR_USER_LOOKUP"),
        USE_FULL_LOGIN_INFO_FOR_CONTEXT_LOOKUP("USE_FULL_LOGIN_INFO_FOR_CONTEXT_LOOKUP"),
        IMAP_SERVER("IMAP_SERVER"),
        IMAP_PORT("IMAP_PORT"),
        USE_MULTIPLE("USE_MULTIPLE"),
        IMAP_USE_SECURE("IMAP_USE_SECURE"),
        IMAPAUTHENC("com.openexchange.authentication.imap.imapAuthEnc"),
        LOWERCASE_FOR_CONTEXT_USER_LOOKUP("LOWERCASE_FOR_CONTEXT_USER_LOOKUP");

        /** The name of the property */
        public final String name;

        private PropertyNames(final String name) {
            this.name = name;
        }
    }

    /**
     * The string for <code>ISO-8859-1</code> character encoding.
     */
    private static final String CHARENC_ISO8859 = "ISO-8859-1";

    // ----------------------------------------------------------------------------------------------------------------------

    private final ServiceLookup services;
    private final Cache<FailureKey, AuthenticationFailedException> failures;
    private final Properties props;

    /**
     * Default constructor.
     */
    public IMAPAuthentication(ServiceLookup services) {
        super();
        this.services = services;

        // Check whether to cache failed authentication attempts to quit subsequent tries in a fast manner
        ConfigurationService configService = services.getService(ConfigurationService.class);
        int failureCacheExpirySeconds = configService.getIntProperty("com.openexchange.authentication.imap.failureCacheExpirySeconds", 0);
        if (failureCacheExpirySeconds > 0) {
            failures = CacheBuilder.newBuilder().maximumSize(1024).expireAfterWrite(failureCacheExpirySeconds, TimeUnit.SECONDS).build();
        } else {
            failures = null;
        }

        // Initialize configuration properties
        props = configService.getFile("imapauth.properties");
    }

    @Override
    public Authenticated handleLoginInfo(final LoginInfo loginInfo) throws OXException {
        try {
            SplitResult splitResult = split(loginInfo.getUsername());

            String localPart = splitResult.localPart;
            String password = loginInfo.getPassword();
            if ("".equals(localPart.trim()) || "".equals(password.trim())) {
                throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
            }

            if (props.get(PropertyNames.IMAPAUTHENC.name) != null) {
                String authenc = (String) props.get(PropertyNames.IMAPAUTHENC.name);
                try {
                    password = new String(password.getBytes(authenc), CHARENC_ISO8859);
                } catch (UnsupportedEncodingException e) {
                    LOG.error("", e);
                    throw LoginExceptionCodes.COMMUNICATION.create(e);
                }
            }

            String imaptimeout = "4000";
            if (props.get(PropertyNames.IMAP_TIMEOUT.name) != null) {
                imaptimeout = (String) props.get(PropertyNames.IMAP_TIMEOUT.name);
            }

            String connectiontimeout = "4000";
            if (props.get(PropertyNames.IMAP_CONNECTIONTIMEOUT.name) != null) {
                connectiontimeout = (String) props.get(PropertyNames.IMAP_CONNECTIONTIMEOUT.name);
            }

            final Properties imapprops = new Properties();
            imapprops.put("mail.imap.connectiontimeout", connectiontimeout);
            imapprops.put("mail.imap.timeout", imaptimeout);

            boolean useFullLogin = true;
            if (props.get(PropertyNames.USE_FULL_LOGIN_INFO.name) != null) {
                useFullLogin = Boolean.parseBoolean(((String) props.get(PropertyNames.USE_FULL_LOGIN_INFO.name)).trim());
            }

            boolean useFullLoginForUserLookup = false;
            if (props.get(PropertyNames.USE_FULL_LOGIN_INFO_FOR_USER_LOOKUP.name) != null) {
                useFullLoginForUserLookup = Boolean.parseBoolean(((String) props.get(PropertyNames.USE_FULL_LOGIN_INFO_FOR_USER_LOOKUP.name)).trim());
            }

            boolean useFullLoginForContextLookup = false;
            if (props.get(PropertyNames.USE_FULL_LOGIN_INFO_FOR_CONTEXT_LOOKUP.name) != null) {
                useFullLoginForContextLookup = Boolean.parseBoolean(((String) props.get(PropertyNames.USE_FULL_LOGIN_INFO_FOR_CONTEXT_LOOKUP.name)).trim());
            }

            boolean lowerCaseForContextUserLookup = false;
            if (props.get(PropertyNames.LOWERCASE_FOR_CONTEXT_USER_LOOKUP.name) != null) {
                lowerCaseForContextUserLookup = Boolean.parseBoolean(((String) props.get(PropertyNames.LOWERCASE_FOR_CONTEXT_USER_LOOKUP.name)).trim());
            }

            String host = "localhost";
            if (props.get(PropertyNames.IMAP_SERVER.name) != null) {
                host = IDNA.toASCII((String) props.get(PropertyNames.IMAP_SERVER.name));
            }

            Integer port = Integer.valueOf(143);
            if (props.get(PropertyNames.IMAP_PORT.name) != null) {
                port = Integer.valueOf((String) props.get(PropertyNames.IMAP_PORT.name));
            }

            LOG.debug("Using imap server: {}", host);
            LOG.debug("Using imap port: {}", port);
            LOG.debug("Using full login info: {}", useFullLogin);
            LOG.debug("Using full login info for user look-up: {}", useFullLoginForUserLookup);

            // Set IMAP login
            String imapLogin = useFullLogin ? splitResult.fullLoginInfo : localPart;

            // Set user/context info
            String userInfo = useFullLoginForUserLookup ? splitResult.fullLoginInfo : localPart;
            String contextInfo = useFullLoginForContextLookup ? splitResult.fullLoginInfo : splitResult.domainPart;
            if (lowerCaseForContextUserLookup) {
                // Use JVM's default locale
                userInfo = userInfo.toLowerCase();
                contextInfo = contextInfo.toLowerCase();
            }

            // Support for multiple IMAP servers
            boolean secure = false;
            if ("true".equalsIgnoreCase(props.getProperty(PropertyNames.USE_MULTIPLE.name))) {
                int ctxId;
                Context ctx;
                {
                    ContextService contextService = services.getService(ContextService.class);
                    ctxId = contextService.getContextId(contextInfo);
                    if (ContextStorage.NOT_FOUND == ctxId) {
                        throw INVALID_CREDENTIALS_MISSING_CONTEXT_MAPPING.create(contextInfo);
                    }
                    ctx = contextService.getContext(ctxId);
                }

	            int userId;
	            {
	                UserService userService = services.getService(UserService.class);
	                try {
	                    userId = userService.getUserId(userInfo, ctx);
	                } catch (final OXException e) {
	                    throw INVALID_CREDENTIALS_MISSING_USER_MAPPING.create(loginInfo.getUsername());
	                }
	            }

	            /*
	             * Load primary account and check its protocol to be IMAP
	             */
	            MailAccount defaultMailAccount = services.getService(MailAccountStorageService.class).getDefaultMailAccount(userId, ctxId);
	            String mailProtocol = defaultMailAccount.getMailProtocol();
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
	                imapLogin = defaultMailAccount.getLogin();
	            }
	            if (LoginSource.PRIMARY_EMAIL.equals(loginSource)) {
	                imapLogin = defaultMailAccount.getPrimaryAddress();
	            }

	            /*
	             * Get IMAP server from primary account
	             */
	            host = IDNA.toASCII(defaultMailAccount.getMailServer());
	            port = Integer.valueOf(defaultMailAccount.getMailPort());
	            secure = defaultMailAccount.isMailSecure();
	            LOG.debug("Parsed IMAP Infos: {} {} {}  ({}@{})", (secure ? "imaps" : "imap"), host, port, userId, ctxId);


            } else {
                // ## ssl feature for single defined imap server
                // added by cutmasta
                if ("true".equalsIgnoreCase(props.getProperty(PropertyNames.IMAP_USE_SECURE.name))) {
                    secure = true;
                }
            }

            FailureKey failureKey = new FailureKey(host, port.intValue(), imapLogin, password);
            {
                AuthenticationFailedException authenticationFailed = null == failures ? null : failures.getIfPresent(failureKey);
                if (null != authenticationFailed) {
                    throw LoginExceptionCodes.INVALID_CREDENTIALS.create(authenticationFailed);
                }
            }

            ConfigurationService configuration = services.getService(ConfigurationService.class);
            final String socketFactoryClass = "com.openexchange.tools.ssl.TrustAllSSLSocketFactory";
            final String sPort = port.toString();
            if (secure) {
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
                /*
                 * Specify SSL protocols
                 */
                {
                    final String sslProtocols = configuration.getProperty("com.openexchange.imap.ssl.protocols", "SSLv3 TLSv1").trim();
                    imapprops.put("mail.imap.ssl.protocols", sslProtocols);
                }
                /*
                 * Specify SSL cipher suites
                 */
                {
                    final String cipherSuites = configuration.getProperty("com.openexchange.imap.ssl.ciphersuites", "").trim();
                    if (false == Strings.isEmpty(cipherSuites)) {
                        imapprops.put("mail.imap.ssl.ciphersuites", cipherSuites);
                    }
                }
            } else {
                /*
                 * Enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected connection.
                 */
                {
                    boolean enableTls = configuration.getBoolProperty("com.openexchange.imap.enableTls", true);
                    if (enableTls) {
                        imapprops.put("mail.imap.starttls.enable", "true");
                    }
                }
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
                {
                    final String sslProtocols = configuration.getProperty("com.openexchange.imap.ssl.protocols", "SSLv3 TLSv1").trim();
                    imapprops.put("mail.imap.ssl.protocols", sslProtocols);
                }
                /*
                 * Specify SSL cipher suites
                 */
                {
                    final String cipherSuites = configuration.getProperty("com.openexchange.imap.ssl.ciphersuites", "").trim();
                    if (false == Strings.isEmpty(cipherSuites)) {
                        imapprops.put("mail.imap.ssl.ciphersuites", cipherSuites);
                    }
                }
                // imapProps.put("mail.imap.ssl.enable", "true");
                /*
                 * Needed for JavaMail >= 1.4
                 */
                // Security.setProperty("ssl.SocketFactory.provider", socketFactoryClass);
            }

            Store imapconnection = null;
            try {
                Session session = Session.getInstance(imapprops, null);
                session.setDebug(false);

                imapconnection = session.getStore("imap");
                // try to connect with the credentials set above
                imapconnection.connect(host, port.intValue(), imapLogin, password);
                LOG.info("Imap authentication for user {} successful on host {}:{}", imapLogin, host, port);
            } catch (NoSuchProviderException e) {
                LOG.error("Error setup initial imap envorinment!", e);
                throw LoginExceptionCodes.COMMUNICATION.create(e);
            } catch (AuthenticationFailedException e) {
                Cache<FailureKey, AuthenticationFailedException> failures = this.failures;
                if (null != failures) {
                    failures.put(failureKey, e);
                }
                LOG.info("Authentication error on host {}:{} for user {}", host, port, imapLogin, e);
                LOG.debug("Debug imap authentication", e);
                throw LoginExceptionCodes.INVALID_CREDENTIALS.create(e);
            } catch (MessagingException e) {
                LOG.info("Messaging error on host {}:{} for user {}", host, port, imapLogin, e);
                LOG.debug("Debug imap error", e);
                throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
            } finally {
                if (imapconnection != null) {
                    try {
                        imapconnection.close();
                    } catch (Exception e) {
                        LOG.error("Error closing imap connection!", e);
                        throw LoginExceptionCodes.COMMUNICATION.create(e);
                    }
                }
            }

            /*
             * Set the context of the user, If full login was configured, we use the domain part as the context name/mapping entry. If NO
             * full login was configured, we assume that only 1 context is in the system which is named "defaultcontext".
             */
            if (useFullLogin) {
                LOG.debug("Using domain: {} as context name", splitResult.domainPart);
                return new AuthenticatedImpl(userInfo, contextInfo);
            }

            LOG.debug("Using \"defaultcontext\" as context name");
            return new AuthenticatedImpl(userInfo, "defaultcontext");
        } catch (ConfigurationException e) {
            LOG.error("Error reading auth plugin config!", e);
            throw LoginExceptionCodes.COMMUNICATION.create(e);
        }
    }

    @Override
    public Authenticated handleAutoLoginInfo(final LoginInfo loginInfo) throws OXException {
        throw LoginExceptionCodes.NOT_SUPPORTED.create(IMAPAuthentication.class.getName());
    }

    /**
     * Splits user name and context.
     *
     * @param loginInfo The composite information separated by an <code>'@'</code> sign
     * @return The split login info
     * @throws LoginException if no separator is found.
     */
    private SplitResult split(String loginInfo) {
        int pos = loginInfo.lastIndexOf('@');
        if (pos <= 0) {
            return new SplitResult(loginInfo, loginInfo, "defaultcontext");
        }

        // Split by '@' character
        return new SplitResult(loginInfo, loginInfo.substring(0, pos), loginInfo.substring(pos + 1));
    }

    // ------------------------------------------------ Helper classes ------------------------------------------------

    private static final class SplitResult {

        /** The local part; e.g. <code>"jane@somewhere.com"</code> */
        final String fullLoginInfo;

        /** The local part; e.g. <code>"jane"</code> from <code>"jane@somewhere.com"</code> */
        final String localPart;

        /** The domain part; e.g. <code>"somewhere.com"</code> from <code>"jane@somewhere.com"</code> */
        final String domainPart;

        SplitResult(String fullLoginInfo, String localPart, String domainPart) {
            super();
            this.fullLoginInfo = fullLoginInfo;
            this.localPart = localPart;
            this.domainPart = domainPart;
        }

    }

    private static final class FailureKey {

        private final String host;
        private final int port;
        private final String user;
        private final String password;
        private final int hash;

        FailureKey(String host, int port, String user, String password) {
            super();
            this.host = host;
            this.port = port;
            this.user = user;
            this.password = password;

            int prime = 31;
            int result = 1;
            result = prime * result + ((host == null) ? 0 : host.hashCode());
            result = prime * result + ((password == null) ? 0 : password.hashCode());
            result = prime * result + port;
            result = prime * result + ((user == null) ? 0 : user.hashCode());
            this.hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof FailureKey)) {
                return false;
            }
            FailureKey other = (FailureKey) obj;
            if (port != other.port) {
                return false;
            }
            if (host == null) {
                if (other.host != null) {
                    return false;
                }
            } else if (!host.equals(other.host)) {
                return false;
            }
            if (password == null) {
                if (other.password != null) {
                    return false;
                }
            } else if (!password.equals(other.password)) {
                return false;
            }
            if (user == null) {
                if (other.user != null) {
                    return false;
                }
            } else if (!user.equals(other.user)) {
                return false;
            }
            return true;
        }

    }

    private static final class AuthenticatedImpl implements Authenticated {

        private final String contextInfo;
        private final String userInfo;

        AuthenticatedImpl(String userInfo, String contextInfo) {
            super();
            this.userInfo = userInfo;
            this.contextInfo = contextInfo;
        }

        @Override
        public String getContextInfo() {
            return contextInfo;
        }

        @Override
        public String getUserInfo() {
            return userInfo;
        }
    }

}
