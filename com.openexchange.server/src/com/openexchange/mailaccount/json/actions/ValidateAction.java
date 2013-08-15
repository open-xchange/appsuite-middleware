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

package com.openexchange.mailaccount.json.actions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
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
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.json.parser.MailAccountParser;
import com.openexchange.server.services.ServerServiceRegistry;
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

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ValidateAction.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

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

        final MailAccountDescription accountDescription = new MailAccountDescription();
        final List<OXException> warnings = new LinkedList<OXException>();
        MailAccountParser.getInstance().parse(accountDescription, jData.toObject(), warnings);

        if (accountDescription.getId() >= 0 && null == accountDescription.getPassword()) {
            /*
             * ID is delivered, but password not set. Thus load from storage version.
             */
            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);

            try {
                final String password = storageService.getMailAccount(accountDescription.getId(), session.getUserId(), session.getContextId()).getPassword();
                accountDescription.setPassword(MailPasswordUtil.decrypt(password, session, accountDescription.getId(), accountDescription.getLogin(), accountDescription.getMailServer()));
            } catch (final OXException e) {
                if (!CryptoErrorMessage.BadPassword.equals(e)) {
                    throw e;
                }
                storageService.invalidateMailAccounts(session.getUserId(), session.getContextId());
                final String password = storageService.getMailAccount(accountDescription.getId(), session.getUserId(), session.getContextId()).getPassword();
                accountDescription.setPassword(MailPasswordUtil.decrypt(password, session, accountDescription.getId(), accountDescription.getLogin(), accountDescription.getMailServer()));
            }
        }

        checkNeededFields(accountDescription);
        if (isUnifiedINBOXAccount(accountDescription.getMailProtocol())) {
            // Deny validation of Unified Mail account
            throw MailAccountExceptionCodes.VALIDATION_FAILED.create();
        }
        // Check for tree parameter
        final boolean tree;
        {
            final String tmp = requestData.getParameter("tree");
            tree = AJAXRequestDataTools.parseBoolParameter(tmp);
        }
        // Check for ignoreInvalidTransport parameter
        final boolean ignoreInvalidTransport;
        {
            final String tmp = requestData.getParameter("ignoreInvalidTransport");
            ignoreInvalidTransport = AJAXRequestDataTools.parseBoolParameter(tmp);
        }
        if (tree) {
            return new AJAXRequestResult(actionValidateTree(accountDescription, session, ignoreInvalidTransport, warnings)).addWarnings(warnings);
        }
        return new AJAXRequestResult(actionValidateBoolean(accountDescription, session, ignoreInvalidTransport, warnings)).addWarnings(warnings);
    }

    private static Object actionValidateTree(final MailAccountDescription accountDescription, final ServerSession session, final boolean ignoreInvalidTransport, final List<OXException> warnings) throws JSONException, OXException {
        if (!actionValidateBoolean(accountDescription, session, ignoreInvalidTransport, warnings).booleanValue()) {
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
     * @return <code>true</code> for successful validation; otherwise <code>false</code>
     * @throws OXException If an severe error occurs
     */
    public static Boolean actionValidateBoolean(final MailAccountDescription accountDescription, final ServerSession session, final boolean ignoreInvalidTransport, final List<OXException> warnings) throws OXException {
        // Validate mail server
        boolean validated = checkMailServerURL(accountDescription, session, warnings);
        // Failed?
        if (!validated) {

            checkTransportServerURL(accountDescription, session, warnings);


            return Boolean.FALSE;
        }
        if (ignoreInvalidTransport) {
            // No need to check transport settings then
            return Boolean.TRUE;
        }
        // Now check transport server URL, if a transport server is present
        if (!isEmpty(accountDescription.getTransportServer())) {
            validated = checkTransportServerURL(accountDescription, session, warnings);
        }
        return Boolean.valueOf(validated);
    }

    static boolean checkMailServerURL(final MailAccountDescription accountDescription, final ServerSession session, final List<OXException> warnings) throws OXException {
        // Create a mail access instance
        final int accountId = accountDescription.getId();
        if (accountId > 0) { // External account
            final String password = accountDescription.getPassword();
            final String login = accountDescription.getLogin();
            if (isEmpty(password) || isEmpty(login)) {
                final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
                final MailAccount mailAccount = storageService.getMailAccount(accountId, session.getUserId(), session.getContextId());
                if (isEmpty(password)) {
                    accountDescription.setPassword(mailAccount.getPassword());
                }
                if (isEmpty(login)) {
                    accountDescription.setLogin(mailAccount.getLogin());
                }
            }
        }
        // Proceed
        final MailAccess<?, ?> mailAccess = getMailAccess(accountDescription, session, warnings);
        if (null == mailAccess) {
            return false;
        }
        // Now try to connect
        final boolean success = mailAccess.ping();
        if (success) {
            warnings.addAll(mailAccess.getWarnings());
        }
        return success;
    }

    static boolean checkTransportServerURL(final MailAccountDescription accountDescription, final ServerSession session, final List<OXException> warnings) throws OXException {
        // Now check transport server URL, if a transport server is present
        if (isEmpty(accountDescription.getTransportServer())) {
            return true;
        }
        final String transportServerURL = accountDescription.generateTransportServerURL();
        // Get the appropriate transport provider by transport server URL
        final TransportProvider transportProvider = TransportProviderRegistry.getTransportProviderByURL(transportServerURL);
        if (null == transportProvider) {
            if (DEBUG) {
                LOG.debug("Validating mail account failed. No transport provider found for URL: " + transportServerURL);
            }
            return false;
        }
        // Create a transport access instance
        final MailTransport mailTransport = transportProvider.createNewMailTransport(session);
        final TransportConfig transportConfig = mailTransport.getTransportConfig();
        // Set login and password
        String login = accountDescription.getTransportLogin();
        if (null == login) {
            login = accountDescription.getLogin();
        }
        transportConfig.setLogin(login);
        String password = accountDescription.getTransportPassword();
        if (null == password) {
            password = accountDescription.getPassword();
        }
        transportConfig.setPassword(password);
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
            if (DEBUG) {
                LOG.debug("Validating transport account failed.", e);
            }
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

}
