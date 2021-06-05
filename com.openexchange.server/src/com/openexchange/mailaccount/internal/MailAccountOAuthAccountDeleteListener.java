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

import java.sql.Connection;
import java.util.Collections;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link MailAccountOAuthAccountDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class MailAccountOAuthAccountDeleteListener extends MailAccountOAuthAccountListener implements OAuthAccountDeleteListener {

    /**
     * Initializes a new {@link MailAccountOAuthAccountDeleteListener}.
     */
    public MailAccountOAuthAccountDeleteListener() {
        super();
    }

    @Override
    public void onBeforeOAuthAccountDeletion(int oauthAccountId, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException {
        // Ignore
    }

    @Override
    public void onAfterOAuthAccountDeletion(int oauthAccountId, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException {
        MailAccountStorageService mass = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
        if (null != mass) {
            MailAccount[] userMailAccounts = mass.getUserMailAccounts(user, cid, con);
            for (MailAccount mailAccount : userMailAccounts) {
                if (false == mailAccount.isDefaultAccount() && false == isUnifiedINBOXAccount(mailAccount)) {
                    deleteAccount(mailAccount, oauthAccountId, user, cid, con, mass);
                }
            }
        }
    }

    private void deleteAccount(MailAccount mailAccount, int oauthAccountId, int user, int cid, Connection con, MailAccountStorageService mass) throws OXException {
        boolean deleted = false;
        if (mailAccount.isMailOAuthAble()) {
            if (mailAccount.getMailOAuthId() == oauthAccountId) {
                mass.deleteMailAccount(mailAccount.getId(), Collections.singletonMap("com.openexchange.mailaccount.deleteOrigin", "oauth"), user, cid, false, con);
                deleted = true;
            }
        }
        if (!deleted && mailAccount.isTransportOAuthAble()) {
            if (mailAccount.getTransportOAuthId() == oauthAccountId) {
                mass.deleteTransportAccount(mailAccount.getId(), user, cid, con);
            }
        }
    }

}
