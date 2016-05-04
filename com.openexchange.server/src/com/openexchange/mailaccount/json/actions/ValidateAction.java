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

package com.openexchange.mailaccount.json.actions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.mail.MessagingException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.crypto.CryptoErrorMessage;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mail.utils.MailPasswordUtil;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountFacade;
import com.openexchange.mailaccount.TransportAuth;
import com.openexchange.mailaccount.json.parser.MailAccountParser;
import com.openexchange.mailaccount.utils.MailAccountUtils;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;
import com.openexchange.tools.net.URITools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ValidateAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "validate", description = "Validate a mail account (which shall be created)", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "tree", optional=true, description = "An optional boolean parameter which indicates whether on successful validation the folder tree shall be returned (NULL on failure) or if set to \"false\" or missing only a boolean is returned which indicates validation result.")
}, requestBody = "A JSON object describing the new account to validate. See mail account data.",
    responseDescription = "Dependent on optional \"tree\" parameter a JSON folder object or a boolean value indicating the validation result.")
public final class ValidateAction extends AbstractMailAccountTreeAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ValidateAction.class);

    public static final String ACTION = AJAXServlet.ACTION_VALIDATE;

    /**
     * Initializes a new {@link ValidateAction}.
     */
    public ValidateAction() {
        super();
    }

    @Override
    protected AJAXRequestResult innerPerform(final AJAXRequestData requestData, final ServerSession session, final JSONValue jData) throws OXException, JSONException {
        if (!session.getUserPermissionBits().isMultipleMailAccounts()) {
            throw
            MailAccountExceptionCodes.NOT_ENABLED.create(
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(session.getContextId()));
        }

        MailAccountDescription accountDescription = new MailAccountDescription();
        List<OXException> warnings = new LinkedList<OXException>();
        Set<Attribute> availableAttributes = MailAccountParser.getInstance().parse(accountDescription, jData.toObject(), warnings);

        if (accountDescription.getId() == MailAccount.DEFAULT_ID) {
            return new AJAXRequestResult(Boolean.TRUE);
        }

        if (!availableAttributes.contains(Attribute.TRANSPORT_AUTH_LITERAL)) {
            accountDescription.setTransportAuth(TransportAuth.MAIL);
            availableAttributes.add(Attribute.TRANSPORT_AUTH_LITERAL);
        }

        // Check for tree parameter
        boolean tree;
        {
            String tmp = requestData.getParameter("tree");
            tree = AJAXRequestDataTools.parseBoolParameter(tmp);
        }

        if (accountDescription.getId() >= 0) {
            final MailAccountFacade mailAccountFacade = getMailAccountFacade();
            MailAccount storageMailAccount = mailAccountFacade.getMailAccount(accountDescription.getId(), session.getUserId(), session.getContextId());

            boolean checkPassword = true;
            if (null == accountDescription.getPassword()) {
                checkPassword = false;
                // Identifier is given, but password not set. Thus load from storage version.
                try {
                    String password = storageMailAccount.getPassword();
                    if (null != password) {
                        String decryptedPassword = MailPasswordUtil.decrypt(password, session, accountDescription.getId(), accountDescription.getLogin(), accountDescription.getMailServer());
                        accountDescription.setPassword(decryptedPassword);
                    }
                } catch (OXException e) {
                    if (!CryptoErrorMessage.BadPassword.equals(e)) {
                        throw e;
                    }
                    mailAccountFacade.invalidateMailAccounts(session.getUserId(), session.getContextId());
                    storageMailAccount = mailAccountFacade.getMailAccount(accountDescription.getId(), session.getUserId(), session.getContextId());
                    String decryptedPassword = MailPasswordUtil.decrypt(storageMailAccount.getPassword(), session, accountDescription.getId(), accountDescription.getLogin(), accountDescription.getMailServer());
                    accountDescription.setPassword(decryptedPassword);
                }
            }

            // Check for any modifications that would justify validation
            if (!tree && !hasValidationReason(accountDescription, storageMailAccount, checkPassword, session)) {
                return new AJAXRequestResult(Boolean.TRUE);
            }
        }

        checkNeededFields(accountDescription);
        if (isUnifiedINBOXAccount(accountDescription.getMailProtocol())) {
            // Deny validation of Unified Mail account
            throw MailAccountExceptionCodes.UNIFIED_INBOX_ACCOUNT_VALIDATION_FAILED.create();
        }

        // Check for ignoreInvalidTransport parameter
        boolean ignoreInvalidTransport;
        {
            String tmp = requestData.getParameter("ignoreInvalidTransport");
            ignoreInvalidTransport = AJAXRequestDataTools.parseBoolParameter(tmp);
        }
        if (tree) {
            return new AJAXRequestResult(actionValidateTree(accountDescription, session, ignoreInvalidTransport, warnings)).addWarnings(warnings);
        }
        return new AJAXRequestResult(actionValidateBoolean(accountDescription, session, ignoreInvalidTransport, warnings, false)).addWarnings(warnings);
    }

    private static boolean hasValidationReason(MailAccountDescription accountDescription, MailAccount storageMailAccount, boolean checkPassword, ServerSession session) throws OXException {
        String s1 = storageMailAccount.generateMailServerURL();
        String s2 = accountDescription.generateMailServerURL();
        if (null == s1) {
            if (null != s2) {
                return true;
            }
        } else if (!s1.equals(s2)) {
            return true;
        }

        s1 = storageMailAccount.generateTransportServerURL();
        s2 = accountDescription.generateTransportServerURL();
        if (null == s1) {
            if (null != s2) {
                return true;
            }
        } else if (!s1.equals(s2)) {
            return true;
        }

        s1 = storageMailAccount.getLogin();
        s2 = accountDescription.getLogin();
        if (null == s1) {
            if (null != s2) {
                return true;
            }
        } else if (!s1.equals(s2)) {
            return true;
        }

        if (checkPassword) {
            s1 = MailPasswordUtil.decrypt(storageMailAccount.getPassword(), session, accountDescription.getId(), accountDescription.getLogin(), accountDescription.getMailServer());
            s2 = accountDescription.getPassword();
            if (null == s1) {
                if (null != s2) {
                    return true;
                }
            } else if (!s1.equals(s2)) {
                return true;
            }
        }

        s2 = accountDescription.getTransportLogin();
        if (null != s2) {
            s1 = storageMailAccount.getTransportLogin();
            if (!s2.equals(s1)) {
                return true;
            }
        }

        s2 = accountDescription.getTransportPassword();
        if (null != s2) {
            s1 = storageMailAccount.getTransportPassword();
            if (null != s1) {
                s1 = MailPasswordUtil.decrypt(s1, session, accountDescription.getId(), accountDescription.getLogin(), accountDescription.getMailServer());
                if (!s2.equals(s1)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static Object actionValidateTree(final MailAccountDescription accountDescription, final ServerSession session, final boolean ignoreInvalidTransport, final List<OXException> warnings) throws JSONException, OXException {
        if (!actionValidateBoolean(accountDescription, session, ignoreInvalidTransport, warnings, false).booleanValue()) {
            // TODO: How to indicate error if folder tree requested?
            return null;
        }
        // Create a mail access instance
        final MailAccess<?, ?> mailAccess = getMailAccess(accountDescription, session, warnings);
        if (null == mailAccess) {
            return JSONObject.NULL;
        }
        return actionValidateTree0(mailAccess, session);
    }

    /**
     * Validates specified account description.
     *
     * @param accountDescription The account description
     * @param session The associated session
     * @param ignoreInvalidTransport
     * @param warnings The warnings list
     * @param errorOnDenied <code>true</code> to throw an error in case account description is denied (either by host or port); otherwise <code>false</code>
     * @return <code>true</code> for successful validation; otherwise <code>false</code>
     * @throws OXException If an severe error occurs
     */
    public static Boolean actionValidateBoolean(MailAccountDescription accountDescription, ServerSession session, boolean ignoreInvalidTransport, List<OXException> warnings, boolean errorOnDenied) throws OXException {
        // Check for primary account
        if (MailAccount.DEFAULT_ID == accountDescription.getId()) {
            return Boolean.TRUE;
        }
        // Validate mail server
        boolean validated = checkMailServerURL(accountDescription, session, warnings, errorOnDenied);
        // Failed?
        if (!validated) {
            checkForCommunicationProblem(warnings, false, accountDescription);
            return Boolean.FALSE;
        }
        if (ignoreInvalidTransport) {
            // No need to check transport settings then
            return Boolean.TRUE;
        }
        // Now check transport server URL, if a transport server is present
        if (!isEmpty(accountDescription.getTransportServer())) {
            validated = checkTransportServerURL(accountDescription, session, warnings, errorOnDenied);
            if (!validated) {
                checkForCommunicationProblem(warnings, true, accountDescription);
                return Boolean.FALSE;
            }
        }
        return Boolean.valueOf(validated);
    }

    static void checkForCommunicationProblem(List<OXException> warnings, boolean transport, MailAccountDescription accountDescription) {
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

    private static boolean indicatesCommunicationProblem(Throwable cause) {
        if (MessagingException.class.isInstance(cause)) {
            Exception ne = ((MessagingException) cause).getNextException();
            return indicatesCommunicationProblem(ne);
        }
        return com.sun.mail.iap.ConnectionException.class.isInstance(cause) || java.net.SocketException.class.isInstance(cause);
    }

    static boolean checkMailServerURL(MailAccountDescription accountDescription, ServerSession session, List<OXException> warnings, boolean errorOnDenied) throws OXException {
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
            transportConfig.setLogin(login);
            transportConfig.setPassword(password);
        }
        // Set server and port
        final URI uri;
        try {
            uri = URIParser.parse(transportServerURL, URIDefaults.SMTP);
        } catch (final URISyntaxException e) {
            throw MailExceptionCode.URI_PARSE_FAILED.create(e, transportServerURL);
        }
        transportConfig.setServer(URITools.getHost(uri));
        transportConfig.setPort(uri.getPort());
        transportConfig.setSecure(accountDescription.isTransportSecure());
        boolean validated = true;
        // Now try to connect
        boolean close = false;
        try {
            mailTransport.ping();
            close = true;
        } catch (final OXException e) {
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
            /* ID is delivered, but password not set. Thus load from storage version.*/
            final MailAccountFacade mailAccountFacade = getMailAccountFacade();
            final MailAccount mailAccount = mailAccountFacade.getMailAccount(accountDescription.getId(), session.getUserId(), session.getContextId());

            if (invalidate) {
                mailAccountFacade.invalidateMailAccounts(session.getUserId(), session.getContextId());
            }
            accountDescription.setLogin(mailAccount.getLogin());
            String encPassword = mailAccount.getPassword();
            accountDescription.setPassword(MailPasswordUtil.decrypt(encPassword, session, accountId, accountDescription.getLogin(), accountDescription.getMailServer()));
        }

        checkNeededFields(accountDescription);
    }

    private static void fillTransportServerCredentials(MailAccountDescription accountDescription, ServerSession session, boolean invalidate) throws OXException {
        int accountId = accountDescription.getId();
        String login = accountDescription.getTransportLogin();
        String password = accountDescription.getTransportPassword();

        if (accountId >= 0 && (isEmpty(login) || isEmpty(password))) {
            final MailAccountFacade mailAccountFacade = getMailAccountFacade();
            final MailAccount mailAccount = mailAccountFacade.getMailAccount(accountId, session.getUserId(), session.getContextId());
            if (invalidate) {
                mailAccountFacade.invalidateMailAccounts(session.getUserId(), session.getContextId());
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
            accountDescription.setTransportPassword(password);
        }
    }

    private static boolean seemsValid(final String str) {
        if (isEmpty(str)) {
            return false;
        }

        return !"null".equalsIgnoreCase(str);
    }

}
