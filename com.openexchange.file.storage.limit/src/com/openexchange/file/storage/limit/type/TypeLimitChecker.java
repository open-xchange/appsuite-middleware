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

package com.openexchange.file.storage.limit.type;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.limit.LimitFile;
import com.openexchange.session.Session;

/**
 * {@link TypeLimitChecker}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public interface TypeLimitChecker {

    /**
     * Returns the type the implementation will check the limits for.
     * 
     * @return {@link String} defining the type the implementation will check the limits for.
     */
    String getType();

    /**
     * Returns a {@link List} of {@link OXException}s containing the limits that will be exceeded (for the handled type) if the list of provided files will be uploaded for the given folder.
     * 
     * @param session The current session
     * @param folderId The id of the folder to check the limits for
     * @param files The files that should be uploaded.
     * @return A {@link List} of {@link OXException}s containing exceeded limits for the given folder/list of files combination
     * @throws OXException In case an error occurred while checking the limits
     */
    List<OXException> check(Session session, String folderId, List<LimitFile> files) throws OXException;

}
