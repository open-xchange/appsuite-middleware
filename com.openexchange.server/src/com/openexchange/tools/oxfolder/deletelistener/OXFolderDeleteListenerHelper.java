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
        } catch (final SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
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
        } catch (final SQLException e) {
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
            } catch (final SQLException e) {
                LOG.error("", e);
                if (performTransaction) {
                    writeCon.rollback();
                }
                throw e;
            } catch (final Throwable t) {
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
        } catch (final SQLException e) {
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
            } catch (final SQLException e) {
                LOG.error("", e);
                if (performTransaction) {
                    writeCon.rollback();
                }
                throw e;
            } catch (final Throwable t) {
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
