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

package com.openexchange.push.malpoll.groupware;

import java.sql.Connection;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.push.malpoll.MALPollPushListener;
import com.openexchange.push.malpoll.MALPollPushListenerRegistry;

/**
 * {@link MALPollMailAccountDeleteListener} - The {@link MailAccountDeleteListener} for MAL Poll bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MALPollMailAccountDeleteListener implements MailAccountDeleteListener {

    /**
     * Initializes a new {@link MALPollMailAccountDeleteListener}.
     */
    public MALPollMailAccountDeleteListener() {
        super();
    }

    @Override
    public void onAfterMailAccountDeletion(final int id, final Map<String, Object> eventProps, final int user, final int cid, final Connection con) throws OXException {
        // Nothing to do
    }

    @Override
    public void onBeforeMailAccountDeletion(final int id, final Map<String, Object> eventProps, final int user, final int cid, final Connection con) throws OXException {
        if (MALPollPushListener.getAccountId() == id) {
            MALPollPushListenerRegistry.getInstance().purgeUserPushListener(cid, user);
        }
    }

}
