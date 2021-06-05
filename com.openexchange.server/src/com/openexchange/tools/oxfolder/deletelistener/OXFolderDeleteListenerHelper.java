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

package com.openexchange.tools.oxfolder.deletelistener;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.tools.oxfolder.OXFolderDeleteListener;
import com.openexchange.tools.oxfolder.deletelistener.sql.DetectCorruptPermissions;
import com.openexchange.tools.oxfolder.deletelistener.sql.GroupPermissionMerger;
import com.openexchange.tools.oxfolder.deletelistener.sql.UserPermissionMerger;

/**
 * {@link OXFolderDeleteListenerHelper} - Offers helper method related to {@link OXFolderDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXFolderDeleteListenerHelper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXFolderDeleteListenerHelper.class);

    /**
     * Initializes a new {@link OXFolderDeleteListenerHelper}
     */
    private OXFolderDeleteListenerHelper() {
        super();
    }

    /**
     * Ensures folder data consistency after user/group delete operation
     *
     * @param ctx The context
     * @throws OXException If checking folder data consistency fails
     */
    public static void ensureConsistency(final Context ctx, final Connection writeCon) throws OXException {
        try {
            /*
             * Check user permissions
             */
            checkUserPermissions(ctx.getContextId(), writeCon);
            /*
             * Check group permissions
             */
            checkGroupPermissions(ctx.getContextId(), writeCon);
        } catch (SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (Exception e) {
            throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }

    private static void checkUserPermissions(final int cid, final Connection writeCon) throws SQLException, Exception {
        /*
         * Detect corrupt user permissions, that is a permission entry holds a reference to a user which no more exists in corresponding
         * user table.
         */
        CorruptPermission[] corruptPermissions = null;
        try {
            corruptPermissions = DetectCorruptPermissions.detectCorruptUserPermissions(cid, writeCon);
        } catch (SQLException e) {
            LOG.error("", e);
            throw e;
        }
        /*
         * ... and handle them
         */
        if (null != corruptPermissions && corruptPermissions.length > 0) {
            LOG.info("{} corrupt user permissions detected", corruptPermissions.length);
            final boolean performTransaction = writeCon.getAutoCommit();
            if (performTransaction) {
                writeCon.setAutoCommit(false);
            }
            try {
                UserPermissionMerger.handleCorruptUserPermissions(corruptPermissions, writeCon);
                if (performTransaction) {
                    writeCon.commit();
                }
            } catch (SQLException e) {
                LOG.error("", e);
                if (performTransaction) {
                    writeCon.rollback();
                }
                throw e;
            } catch (Throwable t) {
                LOG.error("", t);
                if (performTransaction) {
                    writeCon.rollback();
                }
                throw t instanceof Exception ? (Exception) t : new Exception(t.getMessage(), t);
            } finally {
                if (performTransaction) {
                    writeCon.setAutoCommit(true);
                }
            }
        } else {
            LOG.info("No corrupt user permissions detected");
        }
    }

    private static void checkGroupPermissions(final int cid, final Connection writeCon) throws SQLException, Exception {
        /*
         * Detect corrupt group permissions, that is a permission entry holds a reference to a group which no more exists in corresponding
         * group table.
         */
        CorruptPermission[] corruptPermissions = null;
        try {
            corruptPermissions = DetectCorruptPermissions.detectCorruptGroupPermissions(cid, writeCon);
        } catch (SQLException e) {
            LOG.error("", e);
            throw e;
        }
        /*
         * ... and handle them
         */
        if (null != corruptPermissions && corruptPermissions.length > 0) {
            LOG.info("{} corrupt group permissions detected on host ", corruptPermissions.length);
            final boolean performTransaction = writeCon.getAutoCommit();
            if (performTransaction) {
                writeCon.setAutoCommit(false);
            }
            try {
                GroupPermissionMerger.handleCorruptGroupPermissions(corruptPermissions, writeCon);
                if (performTransaction) {
                    writeCon.commit();
                }
            } catch (SQLException e) {
                LOG.error("", e);
                if (performTransaction) {
                    writeCon.rollback();
                }
                throw e;
            } catch (Throwable t) {
                LOG.error("", t);
                if (performTransaction) {
                    writeCon.rollback();
                }
                throw t instanceof Exception ? (Exception) t : new Exception(t.getMessage(), t);
            } finally {
                if (performTransaction) {
                    writeCon.setAutoCommit(true);
                }
            }
        } else {
            LOG.info("No corrupt group permissions detected on host ");
        }
    }

}
