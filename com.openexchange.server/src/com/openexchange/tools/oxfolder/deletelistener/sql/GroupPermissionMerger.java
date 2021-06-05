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

package com.openexchange.tools.oxfolder.deletelistener.sql;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.tools.oxfolder.deletelistener.CorruptPermission;
import com.openexchange.tools.oxfolder.deletelistener.Permission;

/**
 * {@link GroupPermissionMerger}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class GroupPermissionMerger {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GroupPermissionMerger.class);

	/**
	 * Initializes a new {@link GroupPermissionMerger}
	 */
	private GroupPermissionMerger() {
		super();
    }

    /**
     * Handles corrupt group permissions by re-assigning (merged permission) them to special "<i>all-groups-and-users</i>" group
     *
     * @param corruptPermissions The corrupt group permissions
     * @param con A connection to MySQL database
     * @throws SQLException If corrupt permissions cannot be handled due to a SQL error
     */
    public static void handleCorruptGroupPermissions(final CorruptPermission[] corruptPermissions, final Connection con) throws SQLException {
        final boolean[] delete = new boolean[1];
        Next: for (final CorruptPermission corruptPermission : corruptPermissions) {
            /*
             * Determine context's admin ID
             */
            final int admin = MergerUtility.getContextAdminID(corruptPermission.cid, con);
            /*
             * Yield merged permission and remember if admin already holds a permission on the folder in question. If he does group's
             * permission has to be deleted whereby admin's permission is set to merged permission. Otherwise group's permission is merged
             * and assigned to admin.
             */
            delete[0] = false;
            final Permission merged;
            try {
                merged = MergerUtility.getMergedPermission(corruptPermission.permission_id, admin, corruptPermission.fuid, corruptPermission.cid, con, delete);
            } catch (IllegalStateException e) {
                // Strange. Previously detected corrupt group permission does no more exist.
                continue Next;
            }
            if (delete[0]) {
                MergerUtility.deletePermission(corruptPermission.permission_id, corruptPermission.fuid, corruptPermission.cid, con);
                LOG.info("Permission deleted for group {} on folder {} in context {}", I(corruptPermission.permission_id), I(corruptPermission.fuid), I(corruptPermission.cid));
                MergerUtility.updatePermission(merged, admin, admin, corruptPermission.fuid, corruptPermission.cid, con);
                LOG.info("...and merged to context admin: {}", merged);
            } else {
                MergerUtility.updatePermission(merged, corruptPermission.permission_id, admin, corruptPermission.fuid, corruptPermission.cid, con);
                LOG.info("Permission re-assigned to context admin: {}", merged);
            }

        }
    }

}
