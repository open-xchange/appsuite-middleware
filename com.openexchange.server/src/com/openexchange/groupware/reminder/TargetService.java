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

package com.openexchange.groupware.reminder;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * This interface must be implemented to remove reminder information from
 * objects if the reminder is deleted. Additionally the last modified timestamp
 * on the object should be actualized.
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public interface TargetService {

    String MODULE_PROPERTY = "MODULE";

    TargetService EMPTY = new TargetService() {
        @Override
        public void updateTargetObject(final Context ctx, final Connection con, final int targetId, final int userId) throws OXException {
            // Nothing to do.
        }
        @Override
        public void updateTargetObject(final Context ctx, final Connection con, final int targetId) throws OXException {
            // Nothing to do.
        }
    };

    /**
     * All reminder information for every participant must be removed.
     * @param ctx Context.
     * @param con writable database connection.
     * @param targetId identifier of the object to actualize.
     * @throws OXException If some problem occurs actualizing the object
     */
    void updateTargetObject(Context ctx, Connection con, int targetId) throws OXException;

    /**
     * The reminder information for a specific participant must be removed.
     * @param ctx Context.
     * @param con writeable database connection.
     * @param targetId identifier of the object to actualize.
     * @param userId identifier of the user that deleted his reminder.
     * @throws OXException If some problem occurs actualizing the object
     */
    void updateTargetObject(Context ctx, Connection con, int targetId, int userId) throws OXException;

}





