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

package com.openexchange.guest.impl.internal;

import java.sql.Connection;
import org.apache.commons.lang.Validate;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.contextgroup.DeleteContextGroupEvent;
import com.openexchange.groupware.delete.contextgroup.DeleteContextGroupListener;
import com.openexchange.guest.GuestService;
import com.openexchange.java.Strings;

/**
 * {@link GuestDeleteContextGroupListener}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class GuestDeleteContextGroupListener implements DeleteContextGroupListener {

    private final GuestService guestService;

    public GuestDeleteContextGroupListener(GuestService guestService) {
        Validate.notNull(guestService, "Required service GuestService is absent. Removing guests from mapping table not possible.");

        this.guestService = guestService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePerformed(DeleteContextGroupEvent event, Connection readCon, Connection writeCon) throws OXException {
        String contextGroupId = event.getContextGroupId();
        if (Strings.isEmpty(contextGroupId)) {
            return;
        }

        this.guestService.removeGuests(contextGroupId);
    }
}
