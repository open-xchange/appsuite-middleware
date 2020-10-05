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
