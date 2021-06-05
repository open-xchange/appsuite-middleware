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

package com.openexchange.groupware.delete;

import java.sql.Connection;
import java.util.EventListener;
import com.openexchange.exception.OXException;

/**
 * {@link DeleteListener} - Performs the action(s) related to a received delete event
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface DeleteListener extends EventListener {

    /**
     * Performs the action(s) related to received delete event
     *
     * @param event the delete event
     * @param readCon a read-only connection
     * @param writeCon a writable connection
     * @throws OXException If an error occurs handling the delete event for this listener
     */
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException;

}
