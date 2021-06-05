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

package com.openexchange.mail.compose;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link AttachmentStorageService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
@SingletonService
public interface AttachmentStorageService {

    /**
     * Gets the applicable attachment storage for session-associated user
     *
     * @param session The session
     * @return The applicable attachment storage
     * @throws OXException If attachment storage cannot be returned
     */
    AttachmentStorage getAttachmentStorageFor(Session session) throws OXException;

    /**
     * Gets the attachment storage matching specified storage type.
     *
     * @param storageType The attachment storage type to look-up by
     * @return The matching attachment storage
     * @throws OXException If no such attachment storage exists or it cannot be returned
     */
    AttachmentStorage getAttachmentStorageByType(AttachmentStorageType storageType) throws OXException;
}
