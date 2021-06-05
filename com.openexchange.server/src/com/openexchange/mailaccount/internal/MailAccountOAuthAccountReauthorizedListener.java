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
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.oauth.OAuthAccountReauthorizedListener;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link MailAccountOAuthAccountReauthorizedListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class MailAccountOAuthAccountReauthorizedListener extends MailAccountOAuthAccountListener implements OAuthAccountReauthorizedListener {

    /**
     * Initializes a new {@link MailAccountOAuthAccountReauthorizedListener}.
     */
    public MailAccountOAuthAccountReauthorizedListener() {
        super();
    }

    @Override
    public void onAfterOAuthAccountReauthorized(int oauthAccountId, Map<String, Object> eventProps, int user, int contextId, Connection con) throws OXException {
        MailAccountStorageService mass = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
        if (null != mass) {
            MailAccount[] userMailAccounts = mass.getUserMailAccounts(user, contextId, con);
            for (MailAccount mailAccount : userMailAccounts) {
                if (false == mailAccount.isDefaultAccount() && false == isUnifiedINBOXAccount(mailAccount) && mailAccount.getMailOAuthId() == oauthAccountId) {
                    enbledMailAccount(mailAccount, oauthAccountId, user, contextId, con, mass);
                }
            }
        }
    }

    private void enbledMailAccount(MailAccount mailAccount, int oauthAccountId, int userId, int contextId, Connection con, MailAccountStorageService mass) throws OXException {
        boolean updated = false;
        if (mailAccount.isMailOAuthAble()) {
            if (mailAccount.getMailOAuthId() == oauthAccountId) {
                mass.enableMailAccount(mailAccount.getId(), userId, contextId, con);
                updated = true;
            }
        }
        if (!updated && mailAccount.isTransportOAuthAble()) {
            if (mailAccount.getTransportOAuthId() == oauthAccountId) {
                mass.enableMailAccount(mailAccount.getId(), userId, contextId, con);
            }
        }
    }

}
