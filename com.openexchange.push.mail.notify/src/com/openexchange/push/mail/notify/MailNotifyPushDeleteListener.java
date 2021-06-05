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

package com.openexchange.push.mail.notify;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;

/**
 * {@link MailNotifyPushDeleteListener} - Delete listener for the push bundle.
 * Listens for user delete events and performs actions
 *
 */
public final class MailNotifyPushDeleteListener implements DeleteListener {

    private final MailNotifyPushListenerRegistry registry;

    /**
     * Initializes a new {@link MailNotifyPushDeleteListener}.
     */
    public MailNotifyPushDeleteListener(final MailNotifyPushListenerRegistry registry) {
        super();
        this.registry = registry;
    }

    @Override
    public void deletePerformed(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_USER == event.getType()) {
            registry.purgeUserPushListener(event.getContext().getContextId(), event.getId());
        }
    }

}
