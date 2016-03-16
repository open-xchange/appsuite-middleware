/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.tools.oxfolder.deletelistener.sql;

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
            } catch (final IllegalStateException e) {
                // Strange. Previously detected corrupt group permission does no more exist.
                continue Next;
            }
            if (delete[0]) {
                MergerUtility.deletePermission(corruptPermission.permission_id, corruptPermission.fuid, corruptPermission.cid, con);
                LOG.info("Permission deleted for group {} on folder {} in context {}", corruptPermission.permission_id, corruptPermission.fuid, corruptPermission.cid);
                MergerUtility.updatePermission(merged, admin, admin, corruptPermission.fuid, corruptPermission.cid, con);
                LOG.info("...and merged to context admin: {}", merged);
            } else {
                MergerUtility.updatePermission(merged, corruptPermission.permission_id, admin, corruptPermission.fuid, corruptPermission.cid, con);
                LOG.info("Permission re-assigned to context admin: {}", merged);
            }

        }
    }

}
