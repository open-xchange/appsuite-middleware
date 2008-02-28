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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.tools.oxfolder.OXFolderDeleteListener;
import com.openexchange.tools.oxfolder.TransactionConnection;
import com.openexchange.tools.oxfolder.deletelistener.sql.DetectCorruptPermissions;
import com.openexchange.tools.oxfolder.deletelistener.sql.GroupPermissionMerger;
import com.openexchange.tools.oxfolder.deletelistener.sql.UserPermissionMerger;

/**
 * {@link OXFolderDeleteListenerHelper} - Offers helper method related to
 * {@link OXFolderDeleteListener}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class OXFolderDeleteListenerHelper {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(OXFolderDeleteListenerHelper.class);

	/**
	 * Initializes a new {@link OXFolderDeleteListenerHelper}
	 */
	private OXFolderDeleteListenerHelper() {
		super();
	}

	/**
	 * Ensures folder data consistency after user/group delete operation
	 * 
	 * @param ctx
	 *            The context
	 * @throws DeleteFailedException
	 *             If checking folder data consistency fails
	 */
	public static void ensureConsistency(final Context ctx, final Connection writeCon) throws DeleteFailedException {
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
			throw new DeleteFailedException(DeleteFailedException.Code.SQL_ERROR, e, e.getMessage());
		} catch (final Exception e) {
			throw new DeleteFailedException(DeleteFailedException.Code.ERROR, e, e.getMessage());
		}
	}

	private static void checkUserPermissions(final int cid, final Connection writeCon) throws SQLException, Exception {
		/*
		 * Detect corrupt user permissions...
		 */
		CorruptPermission[] corruptPermissions = null;
		try {
			corruptPermissions = DetectCorruptPermissions.detectCorruptUserPermissions(cid, writeCon);
		} catch (final SQLException e) {
			LOG.error(e.getMessage(), e);
			throw e;
		}
		/*
		 * ... and handle them
		 */
		if (null != corruptPermissions && corruptPermissions.length > 0) {
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(64).append(corruptPermissions.length).append(
						" corrupt user permissions detected").toString());
			}
			final TransactionConnection transCon = new TransactionConnection(writeCon);
			try {
				UserPermissionMerger.handleCorruptUserPermissions(corruptPermissions, transCon);
				transCon.commit();
			} catch (final SQLException e) {
				LOG.error(e.getMessage(), e);
				transCon.rollback();
				throw e;
			} catch (final Throwable t) {
				LOG.error(t.getMessage(), t);
				transCon.rollback();
				throw t instanceof Exception ? (Exception) t : new Exception(t.getMessage(), t);
			} finally {
				transCon.setAutoCommit(true);
			}
		} else {
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(64).append("No corrupt user permissions detected").toString());
			}
		}
	}

	private static void checkGroupPermissions(final int cid, final Connection writeCon) throws SQLException, Exception {
		/*
		 * Detect corrupt group permissions...
		 */
		CorruptPermission[] corruptPermissions = null;
		try {
			corruptPermissions = DetectCorruptPermissions.detectCorruptGroupPermissions(cid, writeCon);
		} catch (final SQLException e) {
			LOG.error(e.getMessage(), e);
			throw e;
		}
		/*
		 * ... and handle them
		 */
		if (null != corruptPermissions && corruptPermissions.length > 0) {
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(64).append(corruptPermissions.length).append(
						" corrupt group permissions detected on host ").toString());
			}
			final TransactionConnection transCon = new TransactionConnection(writeCon);
			try {
				GroupPermissionMerger.handleCorruptGroupPermissions(corruptPermissions, transCon);
				transCon.commit();
			} catch (final SQLException e) {
				LOG.error(e.getMessage(), e);
				transCon.rollback();
				throw e;
			} catch (final Throwable t) {
				LOG.error(t.getMessage(), t);
				transCon.rollback();
				throw t instanceof Exception ? (Exception) t : new Exception(t.getMessage(), t);
			} finally {
				transCon.setAutoCommit(true);
			}
		} else {
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(64).append("No corrupt group permissions detected on host ").toString());
			}
		}
	}

}
