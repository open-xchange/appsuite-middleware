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

package com.openexchange.groupware.attach;

import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

public interface AttachmentAuthorization {

    /**
     * Checks whether a user is allowed to add attachments to objects in a folder or not, throwing an appropriate exception if the
     * permission check fails.
     *
     * @param session The user's session
     * @param folderId The parent folder ID
     * @param objectId The object ID
     * @throws OXException If adding attachments is not permitted
     */
	void checkMayAttach(ServerSession session, int folderId, int objectId) throws OXException;

    /**
     * Checks whether a user is allowed to remove attachments from objects in a folder or not, throwing an appropriate exception if the
     * permission check fails.
     *
     * @param session The user's session
     * @param folderId The parent folder ID
     * @param objectId The object ID
     * @throws OXException If remvoing attachments is not permitted
     */
	void checkMayDetach(ServerSession session, int folderId, int objectId) throws OXException;

    /**
     * Checks whether a user is allowed to read attachments of objects in a folder or not, throwing an appropriate exception if the
     * permission check fails.
     *
     * @param session The user's session
     * @param folderId The parent folder ID
     * @param objectId The object ID
     * @throws OXException If reading attachments is not permitted
     */
	void checkMayReadAttachments(ServerSession session, int folderId, int objectId) throws OXException;

}
