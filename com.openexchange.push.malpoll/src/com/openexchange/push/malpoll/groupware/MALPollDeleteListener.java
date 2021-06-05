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
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.push.malpoll.MALPollDBUtility;
import com.openexchange.push.malpoll.MALPollPushListenerRegistry;

/**
 * {@link MALPollDeleteListener} - Delete listener for MAL Poll bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MALPollDeleteListener implements DeleteListener {

    public MALPollDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_USER == event.getType()) {
            final int contextId = event.getContext().getContextId();
            final int userId = event.getId();
            MALPollPushListenerRegistry.getInstance().purgeUserPushListener(contextId, userId);

            MALPollDBUtility.deleteUserData(contextId, userId);
        } else if (DeleteEvent.TYPE_CONTEXT == event.getType()) {
            int contextId = event.getContext().getContextId();
            List<Integer> userIds = event.getUserIds();
            if (null != userIds) {
                for (Integer userId : userIds) {
                    MALPollPushListenerRegistry.getInstance().purgeUserPushListener(contextId, userId.intValue());
                }
            }

            MALPollDBUtility.deleteContextData(contextId);
        }
    }
}
