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

package com.openexchange.push.imapidle;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.Map;
import org.slf4j.Logger;
import com.openexchange.mailaccount.MailAccountDeleteListener;

/**
 * {@link ImapIdleMailAccountDeleteListener} - The {@link MailAccountDeleteListener} for IMAP IDLE bundle.
 *
 */
public final class ImapIdleMailAccountDeleteListener implements MailAccountDeleteListener {

    /**
     * Initializes a new {@link ImapIdleMailAccountDeleteListener}.
     */
    public ImapIdleMailAccountDeleteListener() {
        super();
    }

    @Override
    public void onAfterMailAccountDeletion(final int id, final Map<String, Object> eventProps, final int user, final int cid, final Connection con) {
        // Nothing to do
    }

    @Override
    public void onBeforeMailAccountDeletion(final int id, final Map<String, Object> eventProps, final int user, final int cid, final Connection con) {
        ImapIdlePushManagerService instance = ImapIdlePushManagerService.getInstance();
        if (null == instance) {
            return;
        }

        if (instance.getAccountId() == id) {
            try {
                instance.stopListener(false, true, user, cid);
            } catch (Exception e) {
                Logger logger = org.slf4j.LoggerFactory.getLogger(ImapIdleMailAccountDeleteListener.class);
                logger.warn("Failed to stop IMAP-IDLE listener for user {} in context {}", I(user), I(cid));
            }
        }
    }

}
