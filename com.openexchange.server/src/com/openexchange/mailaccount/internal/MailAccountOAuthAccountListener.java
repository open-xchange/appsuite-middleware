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

import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.UnifiedInboxManagement;

/**
 * {@link MailAccountOAuthAccountListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public abstract class MailAccountOAuthAccountListener {

    /**
     * Initializes a new {@link MailAccountOAuthAccountListener}.
     */
    protected MailAccountOAuthAccountListener() {
        super();
    }

    /**
     * Checks if specified mail account represents the Unified Mail account
     *
     * @param mailAccount The mail account to check
     * @return <code>true</code> if specified mail account represents the Unified Mail account; otherwise <code>false</code>
     */
    protected static boolean isUnifiedINBOXAccount(final MailAccount mailAccount) {
        return isUnifiedINBOXAccount(mailAccount.getMailProtocol());
    }

    /**
     * Checks if specified mail protocol denotes the Unified Mail protocol identifier
     *
     * @param mailProtocol The mail protocol to check
     * @return <code>true</code> if specified mail protocol denotes the Unified Mail protocol identifier; otherwise <code>false</code>
     */
    protected static boolean isUnifiedINBOXAccount(final String mailProtocol) {
        return UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(mailProtocol);
    }

}
