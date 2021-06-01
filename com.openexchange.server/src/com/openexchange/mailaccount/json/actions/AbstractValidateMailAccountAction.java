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

package com.openexchange.mailaccount.json.actions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import javax.mail.MessagingException;
import javax.net.ssl.SSLException;
import com.openexchange.crypto.CryptoErrorMessage;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.AuthType;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.oauth.MailOAuthService;
import com.openexchange.mail.oauth.TokenInfo;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mail.utils.MailPasswordUtil;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.json.ActiveProviderDetector;
import com.openexchange.mailaccount.utils.MailAccountUtils;
import com.openexchange.net.ssl.exception.SSLExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;
import com.openexchange.tools.net.URITools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractValidateMailAccountAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractValidateMailAccountAction extends AbstractMailAccountAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractValidateMailAccountAction.class);

    /**
     * Initializes a new {@link AbstractValidateMailAccountAction}.
     */
    protected AbstractValidateMailAccountAction(ActiveProviderDetector activeProviderDetector) {
        super(activeProviderDetector);
    }

    protected static void checkForCommunicationProblem(List<OXException> warnings, boolean transport, MailAccountDescription accountDescription) {
        if (null != warnings && !warnings.isEmpty()) {
            OXException warning = warnings.get(0);
            if (indicatesCommunicationProblem(warning.getCause())) {
                OXException newWarning;
                if (transport) {
                    String login = accountDescription.getTransportLogin();
                    if (!seemsValid(login)) {
                        login = accountDescription.getLogin();
                    }
                    newWarning = MailAccountExceptionCodes.VALIDATE_FAILED_TRANSPORT.create(accountDescription.getTransportServer(), login);
                } else {
                    newWarning = MailAccountExceptionCodes.VALIDATE_FAILED_MAIL.create(accountDescription.getMailServer(), accountDescription.getLogin());
                }
                newWarning.setCategory(Category.CATEGORY_WARNING);
                warnings.clear();
                warnings.add(newWarning);
            }
        }
    }

    protected static boolean indicatesCommunicationProblem(Throwable cause) {
        if (MessagingException.class.isInstance(cause)) {
            Exception ne = ((MessagingException) cause).getNextException();
            return indicatesCommunicationProblem(ne);
        }
        return com.sun.mail.iap.ConnectionException.class.isInstance(cause) || java.net.SocketException.class.isInstance(cause);
    }

    /**
     * Checks the specified {@link OXException} for any indication about a potential SSL problem
     *
     * @param exception The {@link OXException} to check
     * @return <code>true</code> if the root cause indicates an SSL problem, <code>false</code> otherwise
     */
    protected static boolean indicatesSSLProblem(OXException exception) {
        return SSLExceptionCode.PREFIX.equals(exception.getPrefix()) || SSLException.class.isInstance(exception.getCause());
    }

    protected static boolean checkMailServerURL(MailAccountDescription accountDescription, ServerSession session, List<OXException> warnings, boolean errorOnDenied) throws OXException {
        if (MailAccountUtils.isDenied(accountDescription.getMailServer(), accountDescription.getMailPort())) {
            OXException oxe = MailAccountExceptionCodes.VALIDATE_FAILED_MAIL.create(accountDescription.getMailServer(), accountDescription.getLogin());
            if (errorOnDenied) {
                throw oxe;
            }
            warnings.add(oxe);
            return false;
        }

        try {
            fillMailServerCredentials(accountDescription, session, false);
        } catch (OXException e) {
            if (!CryptoErrorMessage.BadPassword.equals(e)) {
                throw e;
            }
            fillMailServerCredentials(accountDescription, session, true);
        }
        // Proceed
        final MailAccess<?, ?> mailAccess = getMailAccess(accountDescription, session, warnings);
        if (null == mailAccess) {
            return false;
        }
        try {
            // Now try to connect
            final boolean success = mailAccess.ping();
            // Add possible warnings
            {
                final Collection<OXException> currentWarnings = mailAccess.getWarnings();
                if (null != currentWarnings) {
                    warnings.addAll(currentWarnings);
                }
            }
            return success;
        } finally {
            mailAccess.close(false);
        }
    }

    protected static boolean checkTransportServerURL(MailAccountDescription accountDescription, ServerSession session, List<OXException> warnings, boolean errorOnDenied) throws OXException {
        // Now check transport server URL, if a transport server is present
        if (isEmpty(accountDescription.getTransportServer())) {
            return true;
        }

        if (MailAccountUtils.isDenied(accountDescription.getTransportServer(), accountDescription.getTransportPort())) {
            String login = accountDescription.getTransportLogin();
            if (!seemsValid(login)) {
                login = accountDescription.getLogin();
            }
            OXException oxe = MailAccountExceptionCodes.VALIDATE_FAILED_TRANSPORT.create(accountDescription.getTransportServer(), login);
            if (errorOnDenied) {
                throw oxe;
            }
            warnings.add(oxe);
            return false;
        }

        final String transportServerURL = accountDescription.generateTransportServerURL();
        // Get the appropriate transport provider by transport server URL
        final TransportProvider transportProvider = TransportProviderRegistry.getTransportProviderByURL(transportServerURL);
        if (null == transportProvider) {
            LOG.debug("Validating mail account failed. No transport provider found for URL: {}", transportServerURL);
            return false;
        }
        // Create a transport access instance
        final MailTransport mailTransport = transportProvider.createNewMailTransport(session);
        final TransportConfig transportConfig = mailTransport.getTransportConfig();
        // Set login and password
        try {
            fillTransportServerCredentials(accountDescription, session, false);
        } catch (OXException e) {
            if (!CryptoErrorMessage.BadPassword.equals(e)) {
                throw e;
            }
            fillTransportServerCredentials(accountDescription, session, true);
        }
        // Credentials
        {
            String login = accountDescription.getTransportLogin();
            String password = accountDescription.getTransportPassword();
            if (!seemsValid(login)) {
                login = accountDescription.getLogin();
            }
            if (!seemsValid(password)) {
                password = accountDescription.getPassword();
            }
            AuthType authType = accountDescription.getTransportAuthType();
            if (null == authType) {
                authType = accountDescription.getAuthType();
            }
            transportConfig.setAuthType(authType);
            transportConfig.setLogin(login);
            transportConfig.setPassword(password);
        }
        // Set server and port
        final URI uri;
        try {
            uri = URIParser.parse(transportServerURL, URIDefaults.SMTP);
        } catch (URISyntaxException e) {
            throw MailExceptionCode.URI_PARSE_FAILED.create(e, transportServerURL);
        }
        transportConfig.setServer(URITools.getHost(uri));
        transportConfig.setPort(uri.getPort());
        transportConfig.setSecure(accountDescription.isTransportSecure());
        transportConfig.setRequireTls(accountDescription.isTransportStartTls());
        boolean validated = true;
        // Now try to connect
        boolean close = false;
        try {
            mailTransport.ping();
            close = true;
        } catch (OXException e) {
            LOG.debug("Validating transport account failed.", e);
            Throwable cause = e.getCause();
            while ((null != cause) && (cause instanceof OXException)) {
                cause = cause.getCause();
            }
            if (null != cause) {
                warnings.add(MailAccountExceptionCodes.VALIDATE_FAILED_TRANSPORT.create(cause, transportConfig.getServer(), transportConfig.getLogin()));
            } else {
                e.setCategory(Category.CATEGORY_WARNING);
                warnings.add(e);
            }
            validated = false;
        } finally {
            if (close) {
                mailTransport.close();
            }
        }
        return validated;
    }

    private static void fillMailServerCredentials(MailAccountDescription accountDescription, ServerSession session, boolean invalidate) throws OXException {
        int accountId = accountDescription.getId();
        String login = accountDescription.getLogin();
        String password = accountDescription.getPassword();

        if (accountId >= 0 && (isEmpty(login) || isEmpty(password))) {
            /* ID is delivered, but password not set. Thus load from storage version. */
            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
            final MailAccount mailAccount = storageService.getMailAccount(accountDescription.getId(), session.getUserId(), session.getContextId());

            if (invalidate) {
                storageService.invalidateMailAccounts(session.getUserId(), session.getContextId());
            }
            accountDescription.setLogin(mailAccount.getLogin());
            if (mailAccount.isMailOAuthAble()) {
                // Do the OAuth dance...
                MailOAuthService mailOAuthService = ServerServiceRegistry.getInstance().getService(MailOAuthService.class);
                TokenInfo tokenInfo = mailOAuthService.getTokenFor(mailAccount.getMailOAuthId(), session);

                accountDescription.setAuthType(AuthType.parse(tokenInfo.getAuthMechanism()));
                accountDescription.setPassword(tokenInfo.getToken());
            } else {
                String encPassword = mailAccount.getPassword();
                accountDescription.setPassword(MailPasswordUtil.decrypt(encPassword, session, accountId, accountDescription.getLogin(), accountDescription.getMailServer()));
            }
        } else if (accountDescription.isMailOAuthAble()) {
            // Do the OAuth dance...
            MailOAuthService mailOAuthService = ServerServiceRegistry.getInstance().getService(MailOAuthService.class);
            TokenInfo tokenInfo = mailOAuthService.getTokenFor(accountDescription.getMailOAuthId(), session);

            accountDescription.setAuthType(AuthType.parse(tokenInfo.getAuthMechanism()));
            accountDescription.setPassword(tokenInfo.getToken());
        }

        checkNeededFields(accountDescription, false);
    }

    private static void fillTransportServerCredentials(MailAccountDescription accountDescription, ServerSession session, boolean invalidate) throws OXException {
        int accountId = accountDescription.getId();
        String login = accountDescription.getTransportLogin();
        String password = accountDescription.getTransportPassword();

        if (accountId >= 0 && (isEmpty(login) || isEmpty(password))) {
            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
            final MailAccount mailAccount = storageService.getMailAccount(accountId, session.getUserId(), session.getContextId());
            if (invalidate) {
                storageService.invalidateMailAccounts(session.getUserId(), session.getContextId());
            }
            if (isEmpty(login)) {
                login = mailAccount.getTransportLogin();
                if (isEmpty(login)) {
                    login = accountDescription.getLogin();
                    if (isEmpty(login)) {
                        login = mailAccount.getLogin();
                    }
                }
            }
            accountDescription.setTransportLogin(login);

            if (mailAccount.isTransportOAuthAble()) {
                // Do the OAuth dance...
                MailOAuthService mailOAuthService = ServerServiceRegistry.getInstance().getService(MailOAuthService.class);
                TokenInfo tokenInfo = mailOAuthService.getTokenFor(mailAccount.getTransportOAuthId(), session);

                accountDescription.setTransportAuthType(AuthType.parse(tokenInfo.getAuthMechanism()));
                accountDescription.setTransportPassword(tokenInfo.getToken());
            } else {
                if (isEmpty(password)) {
                    String encPassword = mailAccount.getTransportPassword();
                    accountId = mailAccount.getId();
                    password = MailPasswordUtil.decrypt(encPassword, session, accountId, login, mailAccount.getTransportServer());
                    if (isEmpty(password)) {
                        password = accountDescription.getPassword();
                        if (isEmpty(password)) {
                            encPassword = mailAccount.getPassword();
                            password = MailPasswordUtil.decrypt(encPassword, session, accountId, login, mailAccount.getTransportServer());
                        }
                    }
                }
                accountDescription.setTransportAuthType(AuthType.LOGIN);
                accountDescription.setTransportPassword(password);
            }
        } else if (accountDescription.isTransportOAuthAble()) {
            // Do the OAuth dance...
            MailOAuthService mailOAuthService = ServerServiceRegistry.getInstance().getService(MailOAuthService.class);
            TokenInfo tokenInfo = mailOAuthService.getTokenFor(accountDescription.getTransportOAuthId(), session);

            accountDescription.setTransportAuthType(AuthType.parse(tokenInfo.getAuthMechanism()));
            accountDescription.setTransportPassword(tokenInfo.getToken());
        }
    }

    static boolean seemsValid(final String str) {
        if (isEmpty(str)) {
            return false;
        }

        return !"null".equalsIgnoreCase(str);
    }

}
