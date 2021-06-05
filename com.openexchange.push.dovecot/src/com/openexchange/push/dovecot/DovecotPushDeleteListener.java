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

package com.openexchange.push.dovecot;

import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.push.PushUser;
import com.openexchange.push.dovecot.osgi.Services;
import com.openexchange.user.UserService;

/**
 * {@link DovecotPushDeleteListener} - Delete listener for Dovecot Push bundle.
 */
public final class DovecotPushDeleteListener implements DeleteListener {

    private static final Logger LOG = LoggerFactory.getLogger(DovecotPushDeleteListener.class);

    private final AbstractDovecotPushManagerService pushManager;

    /**
     * Initializes a new {@link DovecotPushDeleteListener}.
     */
    public DovecotPushDeleteListener(AbstractDovecotPushManagerService pushManager) {
        super();
        this.pushManager = pushManager;
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_USER == event.getType()) {
            dropListenerFor(event.getId(), event.getContext().getContextId());
        } else if (DeleteEvent.TYPE_CONTEXT == event.getType()) {
            UserService userService = Services.getServiceLookup().getServiceSafe(UserService.class);
            int contextId = event.getContext().getContextId();
            for (int userId : userService.listAllUser(contextId, false, false)) {
                dropListenerFor(userId, contextId);
            }
        }
    }

    /**
     * Drops the listener associated with given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If listener cannot be dropped
     */
    private void dropListenerFor(int userId, int contextId) {
        try {
            pushManager.unregisterForDeletedUser(new PushUser(userId, contextId));
        } catch (OXException e) {
            LOG.warn("Unable to stop push listener for deleted user", e);
        }
    }

}
