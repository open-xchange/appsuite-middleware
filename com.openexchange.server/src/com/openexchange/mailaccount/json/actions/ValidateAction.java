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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.crypto.CryptoErrorMessage;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.utils.MailPasswordUtil;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.TransportAuth;
import com.openexchange.mailaccount.json.ActiveProviderDetector;
import com.openexchange.mailaccount.json.parser.DefaultMailAccountParser;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ValidateAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ValidateAction extends AbstractMailAccountTreeAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ValidateAction.class);

    public static final String ACTION = AJAXServlet.ACTION_VALIDATE;

    /**
     * Initializes a new {@link ValidateAction}.
     */
    public ValidateAction(ActiveProviderDetector activeProviderDetector) {
        super(activeProviderDetector);
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
        Set<Attribute> availableAttributes = DefaultMailAccountParser.getInstance().parse(accountDescription, jData.toObject(), warnings);

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
            if (accountDescription.getId() != MailAccount.DEFAULT_ID && existsSeparateActionProviderSupportingMethod(GetAction.ACTION, session)) {
                // Unable to validate existing account hosted by another provider
                return new AJAXRequestResult(Boolean.TRUE);
            }

            MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
            MailAccount storageMailAccount = storageService.getMailAccount(accountDescription.getId(), session.getUserId(), session.getContextId());

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
                    storageService.invalidateMailAccounts(session.getUserId(), session.getContextId());
                    storageMailAccount = storageService.getMailAccount(accountDescription.getId(), session.getUserId(), session.getContextId());
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

}
