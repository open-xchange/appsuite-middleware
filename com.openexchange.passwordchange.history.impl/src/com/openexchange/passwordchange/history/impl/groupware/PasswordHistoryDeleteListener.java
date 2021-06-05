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

package com.openexchange.passwordchange.history.impl.groupware;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.passwordchange.history.PasswordChangeRecorder;
import com.openexchange.passwordchange.history.PasswordChangeRecorderRegistryService;
import com.openexchange.passwordchange.history.impl.events.PasswordChangeHelper;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 *
 * {@link PasswordHistoryDeleteListener} - Listener to delete all password change history entries on deletion of user or context
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordHistoryDeleteListener implements DeleteListener {

    private final PasswordChangeRecorderRegistryService registry;
    private final UserService userService;

    /**
     * Initializes a new {@link PasswordHistoryDeleteListener}.
     *
     * @param registry The {@link PasswordChangeRecorderRegistryService} to get the {@link PasswordChangeRecorder} from
     */
    public PasswordHistoryDeleteListener(PasswordChangeRecorderRegistryService registry, UserService userService) {
        super();
        this.registry = registry;
        this.userService = userService;
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        // Only context and user are relevant
        switch (event.getType()) {
            case DeleteEvent.TYPE_CONTEXT:
                {
                    // Get users in context and remove password for them
                    for (User user : userService.getUser(event.getContext())) {
                        if (false == user.isGuest()) {
                            PasswordChangeHelper.clearSafeFor(event.getContext().getContextId(), user.getId(), -1, registry);
                        }
                    }
                }

                break;
            case DeleteEvent.TYPE_USER:
                {
                    User user = userService.getUser(event.getId(), event.getContext());
                    if (false == user.isGuest()) {
                        PasswordChangeHelper.clearSafeFor(event.getContext().getContextId(), event.getId(), -1, registry);
                    }
                }

                break;
            default:
                // Ignore all other
                return;
        }
    }

}
