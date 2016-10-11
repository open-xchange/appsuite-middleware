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

import static com.openexchange.mail.api.MailConfig.determinePasswordAndAuthType;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.api.AuthInfo;
import com.openexchange.mailaccount.KnownStatus;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.Status;
import com.openexchange.mailaccount.TransportAuth;
import com.openexchange.mailaccount.json.ActiveProviderDetector;
import com.openexchange.mailaccount.json.MailAccountFields;
import com.openexchange.mailaccount.json.MailAccountOAuthConstants;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link StatusAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@OAuthAction(MailAccountOAuthConstants.OAUTH_READ_SCOPE)
public final class StatusAction extends AbstractValidateMailAccountAction implements MailAccountFields {

    public static final String ACTION = "status";

    /**
     * Initializes a new {@link StatusAction}.
     */
    public StatusAction(ActiveProviderDetector activeProviderDetector) {
        super(activeProviderDetector);
    }

    @Override
    protected AJAXRequestResult innerPerform(final AJAXRequestData requestData, final ServerSession session, final JSONValue jVoid) throws OXException {
        try {

            MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            List<OXException> warnings = new LinkedList<>();

            {
                int id = optionalIntParameter(AJAXServlet.PARAMETER_ID, -1, requestData);
                if (id >= 0) {
                    if (!session.getUserPermissionBits().isMultipleMailAccounts() && MailAccount.DEFAULT_ID != id) {
                        throw MailAccountExceptionCodes.NOT_ENABLED.create(Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
                    }

                    Status status = determineAccountStatus(id, true, storageService, warnings, session);
                    String message = status.getMessage(session.getUser().getLocale());

                    JSONObject jStatus = new JSONObject(4).put("status", status.getId());
                    if (Strings.isNotEmpty(message)) {
                        jStatus.put("message", message);
                    }
                    return new AJAXRequestResult(new JSONObject(2).put(Integer.toString(id), jStatus), "json").addWarnings(warnings);
                }
            }

            // Get status for all mail accounts
            if (!session.getUserPermissionBits().isMultipleMailAccounts()) {
                throw MailAccountExceptionCodes.NOT_ENABLED.create(Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
            }

            MailAccount[] accounts = storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
            JSONObject jStatuses = new JSONObject(accounts.length);

            for (MailAccount account : accounts) {
                int id = account.getId();
                Status status = determineAccountStatus(id, false, storageService, warnings, session);
                if (null != status) {
                    String message = status.getMessage(session.getUser().getLocale());

                    JSONObject jStatus = new JSONObject(4).put("status", status.getId());
                    if (Strings.isNotEmpty(message)) {
                        jStatus.put("message", message);
                    }
                    jStatuses.put(Integer.toString(id), jStatus);
                }
            }
            return new AJAXRequestResult(jStatuses, "json").addWarnings(warnings);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private Status determineAccountStatus(int id, boolean singleRequested, MailAccountStorageService storageService, List<OXException> warnings, ServerSession session) throws OXException {
        MailAccount mailAccount = storageService.getMailAccount(id, session.getUserId(), session.getContextId());

        if (isUnifiedINBOXAccount(mailAccount)) {
            // Treat as no hit
            if (singleRequested) {
                throw MailAccountExceptionCodes.NOT_FOUND.create(Integer.valueOf(id), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
            }
            return null;
        }

        Boolean valid = actionValidateBoolean(mailAccount, session, false, warnings, false);
        return valid.booleanValue() ? KnownStatus.OK : KnownStatus.INVALID_CREDENTIALS;
    }

    /**
     * Validates specified account description.
     *
     * @param account The account to check
     * @param session The associated session
     * @param ignoreInvalidTransport
     * @param warnings The warnings list
     * @param errorOnDenied <code>true</code> to throw an error in case account description is denied (either by host or port); otherwise <code>false</code>
     * @return <code>true</code> for successful validation; otherwise <code>false</code>
     * @throws OXException If an severe error occurs
     */
    public static Boolean actionValidateBoolean(MailAccount account, ServerSession session, boolean ignoreInvalidTransport, List<OXException> warnings, boolean errorOnDenied) throws OXException {
        // Check for primary account
        if (MailAccount.DEFAULT_ID == account.getId()) {
            return Boolean.TRUE;
        }

        MailAccountDescription accountDescription = new MailAccountDescription();
        accountDescription.setMailServer(account.getMailServer());
        accountDescription.setMailPort(account.getMailPort());
        accountDescription.setMailOAuthId(account.getMailOAuthId());
        accountDescription.setMailSecure(account.isMailSecure());
        accountDescription.setMailProtocol(account.getMailProtocol());
        accountDescription.setMailStartTls(account.isMailStartTls());
        accountDescription.setLogin(account.getLogin());
        {
            AuthInfo authInfo = determinePasswordAndAuthType(account.getLogin(), session, account, true);
            accountDescription.setPassword(authInfo.getPassword());
            accountDescription.setAuthType(authInfo.getAuthType());
        }

        if (!isEmpty(account.getTransportServer())) {
            if (TransportAuth.NONE == account.getTransportAuth()) {
                return ValidateAction.actionValidateBoolean(accountDescription, session, ignoreInvalidTransport, warnings, errorOnDenied);
            }

            accountDescription.setTransportServer(account.getTransportServer());
            accountDescription.setTransportPort(account.getTransportPort());
            accountDescription.setTransportOAuthId(account.getTransportOAuthId());
            accountDescription.setTransportSecure(account.isTransportSecure());
            accountDescription.setTransportProtocol(account.getTransportProtocol());
            accountDescription.setTransportStartTls(account.isTransportStartTls());

            if (TransportAuth.MAIL == account.getTransportAuth()) {
                accountDescription.setTransportLogin(accountDescription.getLogin());
                accountDescription.setTransportPassword(accountDescription.getPassword());
                accountDescription.setTransportAuthType(accountDescription.getAuthType());
            } else {
                String transportLogin = account.getTransportLogin();
                accountDescription.setTransportLogin(transportLogin);
                AuthInfo authInfo = determinePasswordAndAuthType(transportLogin, session, account, false);
                accountDescription.setTransportPassword(authInfo.getPassword());
                accountDescription.setTransportAuthType(authInfo.getAuthType());
            }
        }

        return ValidateAction.actionValidateBoolean(accountDescription, session, ignoreInvalidTransport, warnings, errorOnDenied);
    }

}
