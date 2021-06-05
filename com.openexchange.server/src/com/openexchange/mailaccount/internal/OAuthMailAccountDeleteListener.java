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

package com.openexchange.mailaccount.internal;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.MailAccounts;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;


/**
 * {@link OAuthMailAccountDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class OAuthMailAccountDeleteListener implements MailAccountDeleteListener {

    /**
     * Initializes a new {@link OAuthMailAccountDeleteListener}.
     */
    public OAuthMailAccountDeleteListener() {
        super();
    }

    @Override
    public void onBeforeMailAccountDeletion(int id, Map<String, Object> eventProps, int userId, int contextId, Connection con) throws OXException {
        if ("oauth".equals(eventProps.get("com.openexchange.mailaccount.deleteOrigin"))) {
            return;
        }

        MailAccountStorageService mass = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
        if (null != mass) {
            MailAccount mailAccount = mass.getMailAccount(id, userId, contextId, con);
            if (false == mailAccount.isDefaultAccount() && false == isUnifiedINBOXAccount(mailAccount) && mailAccount.isMailOAuthAble()) {
                int oauthAccountId = mailAccount.getMailOAuthId();
                if (oauthAccountId > 0) {
                    eventProps.put("com.openexchange.mailaccount.oauthAccountId", I(oauthAccountId));
                }
            }
        }
    }

    @Override
    public void onAfterMailAccountDeletion(int id, Map<String, Object> eventProps, int userId, int contextId, Connection con) throws OXException {
        Integer iOAuthAccountId = (Integer) eventProps.get("com.openexchange.mailaccount.oauthAccountId");
        if (iOAuthAccountId == null) {
            // No OAuth account identifier
            return;
        }

        OAuthService oauthService = ServerServiceRegistry.getInstance().getService(OAuthService.class);
        if (null == oauthService) {
            // Missing OAuth service
            return;
        }

        Optional<Session> optionalSession = MailAccounts.tryGetSession(eventProps, userId, contextId);
        if (!optionalSession.isPresent()) {
            // No session available
            return;
        }

        Session session = optionalSession.get();
        session.setParameter("__connection", con);
        try {
            int oauthAccountId = iOAuthAccountId.intValue();
            OAuthAccount oauthAccount = oauthService.getAccount(session, oauthAccountId);

            // Get the enabled scopes...
            Set<OAuthScope> scopes = new HashSet<>();
            for (OAuthScope scope : oauthAccount.getEnabledScopes()) {
                scopes.add(scope);
            }
            // ...and remove the 'mail' scope.
            for (Iterator<OAuthScope> it = scopes.iterator(); it.hasNext();) {
                if (OXScope.mail == it.next().getOXScope()) {
                    it.remove();
                }
            }
            // Check if any scope is left
            if (scopes.isEmpty()) {
                // No scopes anymore. Delete the OAuth account.
                oauthService.deleteAccount(session, oauthAccountId);
                return;
            }
            // Update OAuth the account to drop scope
            eventProps.put(OAuthConstants.ARGUMENT_SCOPES, scopes);
            oauthService.updateAccount(session, oauthAccountId, eventProps);
        } finally {
            session.setParameter("__connection", null);
        }
    }

    /**
     * Checks if specified mail account represents the Unified Mail account
     *
     * @param mailAccount The mail account to check
     * @return <code>true</code> if specified mail account represents the Unified Mail account; otherwise <code>false</code>
     */
    private static boolean isUnifiedINBOXAccount(final MailAccount mailAccount) {
        return isUnifiedINBOXAccount(mailAccount.getMailProtocol());
    }

    /**
     * Checks if specified mail protocol denotes the Unified Mail protocol identifier
     *
     * @param mailProtocol The mail protocol to check
     * @return <code>true</code> if specified mail protocol denotes the Unified Mail protocol identifier; otherwise <code>false</code>
     */
    private static boolean isUnifiedINBOXAccount(final String mailProtocol) {
        return UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(mailProtocol);
    }

}
