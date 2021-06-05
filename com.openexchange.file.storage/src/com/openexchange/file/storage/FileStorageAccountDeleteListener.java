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

package com.openexchange.file.storage;

import java.sql.Connection;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link FileStorageAccountDeleteListener} - Listener interface for file storage account deletion.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface FileStorageAccountDeleteListener {

    /**
     * Handles the event <i>before</i> the denoted file storage account is deleted.
     * 
     * @param session The session
     * @param id The file storage account identfier
     * @param eventProps Optional properties for delete event
     * @param connection The used connection <i>in transactional state</i>
     *
     * @throws OXException If a critical error occurs which should abort file storage account deletion
     */
    public void onBeforeFileStorageAccountDeletion(Session session, int id, Map<String, Object> eventProps, Connection connection) throws OXException;

    /**
     * Handles the event <i>after</i> the denoted OAuth account is deleted.
     * 
     * @param session The session
     * @param id The file storage account identifier
     * @param eventProps Optional properties for delete event
     * @param connection The used connection <i>in transactional state</i>
     *
     * @throws OXException If a critical error occurs which should abort OAuth account deletion
     */
    public void onAfterFileStorageAccountDeletion(Session session, int id, Map<String, Object> eventProps, Connection connection) throws OXException;
}
