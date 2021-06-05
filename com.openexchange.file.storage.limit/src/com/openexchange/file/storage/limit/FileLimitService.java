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

package com.openexchange.file.storage.limit;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.limit.type.TypeLimitChecker;
import com.openexchange.session.Session;

/**
 * {@link FileLimitService} - checks folder related (e. g. quota) and request specific (e. g. size per file) limits for a file storage
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public interface FileLimitService {

    /**
     * Performs limit checks against all registered {@link TypeLimitChecker}
     *
     * @param session The session.
     * @param folderId The folderId of the change.
     * @param files The files to check.
     * @param type The type to check. This depends on the availability of one or more registered {@link TypeLimitChecker}
     * @return A listing of exceptions indicating possible limitation constraint violations or an empty list
     * @throws OXException If the check fails.
     */
    List<OXException> checkLimits(Session session, String folderId, List<LimitFile> files, String type) throws OXException;

}
