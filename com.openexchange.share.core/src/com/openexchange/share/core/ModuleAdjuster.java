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

package com.openexchange.share.core;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.spi.ModuleExtension;


/**
 * {@link ModuleAdjuster}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public interface ModuleAdjuster extends ModuleExtension {

    /**
     * Adjusts the IDs of a target to reflect the view of the the target user (i.e. the new permission entity).
     *
     * @param target The target from the sharing users point of view
     * @param session The sharing users session
     * @param targetUserId The ID of the user to adjust the target for
     */
    default ShareTarget adjustTarget(ShareTarget target, Session session, int targetUserId) throws OXException {
        return adjustTarget(target, session, targetUserId, null);
    }

    /**
     * Adjusts the IDs of a target to reflect the view of the the target user (i.e. the new permission entity).
     *
     * @param target The target from the sharing users point of view
     * @param session The sharing users session
     * @param targetUserId The ID of the user to adjust the target for
     * @param connection The underlying shared database connection, or <code>null</code> to acquire one dynamically if needed
     */
    ShareTarget adjustTarget(ShareTarget target, Session session, int targetUserId, Connection connection) throws OXException;

    /**
     * Adjusts the IDs of a target to reflect the view of the the target user (i.e. the new permission entity).
     *
     * @param target The target from the sharing users point of view
     * @param contextId The context ID
     * @param requestUserId The requesting users ID
     * @param targetUserId The ID of the user to adjust the target for
     */
    default ShareTarget adjustTarget(ShareTarget target, int contextId, int requestUserId, int targetUserId) throws OXException {
        return adjustTarget(target, contextId, requestUserId, targetUserId, null);
    }

    /**
     * Adjusts the IDs of a target to reflect the view of the the target user (i.e. the new permission entity).
     *
     * @param target The target from the sharing users point of view
     * @param contextId The context ID
     * @param requestUserId The requesting users ID
     * @param targetUserId The ID of the user to adjust the target for
     * @param connection The underlying shared database connection, or <code>null</code> to acquire one dynamically if needed
     */
    ShareTarget adjustTarget(ShareTarget target, int contextId, int requestUserId, int targetUserId, Connection connection) throws OXException;

}
