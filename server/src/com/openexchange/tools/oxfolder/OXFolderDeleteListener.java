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

package com.openexchange.tools.oxfolder;

import java.sql.Connection;
import java.sql.SQLException;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;
import com.openexchange.tools.oxfolder.deletelistener.OXFolderDeleteListenerHelper;

/**
 * Implements interface
 * <code>com.openexchange.groupware.delete.DeleteListener</code>.
 * 
 * <p>
 * In case of a normal user all his private permissions (working & backup) are
 * going to be deleted first, whereby his public permissions (working & backup)
 * are reassigned to context's admin. In next step all private folders owned by
 * this user are going to be completely deleted while checking any left
 * references in corresponding permission table AND 'oxfolder_specialfolders'
 * table. All public folders owned by this user are reassigned to context's
 * admin. Finally folder table is checked if any references in column
 * 'changed_from' points to this user. If any, they are going to be reassigned
 * to context's admin, too.
 * </p>
 * 
 * <p>
 * In case of a group, only permission references are examined, since a group
 * cannot occur in folder fields 'owner' or 'modifedBy'
 * </p>
 * 
 * <p>
 * In case of context's admin, every reference located in any folder or
 * permission table (working & backup) are removed, that either points to admin
 * hisself or point to virtual group 'All Groups & Users'
 * </p>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class OXFolderDeleteListener implements DeleteListener {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(OXFolderDeleteListener.class);

	private static final String TABLE_WORKING_FOLDER = "oxfolder_tree";

	private static final String TABLE_WORKING_PERMS = "oxfolder_permissions";

	private static final String TABLE_BACKUP_FOLDER = "del_oxfolder_tree";

	private static final String TABLE_BACKUP_PERMS = "del_oxfolder_permissions";

	public OXFolderDeleteListener() {
		super();
	}

	public void deletePerformed(final DeleteEvent delEvent, final Connection readConArg, final Connection writeConArg)
			throws DeleteFailedException {
		Connection readCon = readConArg;
		Connection writeCon = writeConArg;
		final Context ctx = delEvent.getContext();
		final long lastModified = System.currentTimeMillis();
		/*
		 * User deletion
		 */
		if (delEvent.getType() == DeleteEvent.TYPE_USER) {
			boolean performTransaction = true;
			try {
				final boolean createReadCon = (readCon == null);
				boolean closeWriteCon = false;
				try {
					if (createReadCon) {
						readCon = DBPool.pickup(ctx);
					}
					if (writeCon == null) {
						writeCon = DBPool.pickupWriteable(ctx);
						closeWriteCon = true;
					}
					performTransaction = writeCon.getAutoCommit();
					if (performTransaction) {
						writeCon.setAutoCommit(false);
					}
					final int userId = delEvent.getId();
					/*
					 * Get context's mailadmin
					 */
					int mailadmin = ctx.getMailadmin();
					if (mailadmin == -1) {
						mailadmin = OXFolderSQL.getContextMailAdmin(readCon, ctx);
						if (mailadmin == -1) {
							throw new OXFolderException(FolderCode.NO_ADMIN_USER_FOUND_IN_CONTEXT, Integer.valueOf(ctx
									.getContextId()));
						}
					}
					final boolean isMailAdmin = (mailadmin == userId);
					/*
					 * Hander user's permissions
					 */
					if (isMailAdmin) {
						/*
						 * Working
						 */
						OXFolderSQL.handleMailAdminPermissions(userId, TABLE_WORKING_FOLDER, TABLE_WORKING_PERMS,
								readCon, writeCon, ctx);
						/*
						 * Backup
						 */
						OXFolderSQL.handleMailAdminPermissions(userId, TABLE_BACKUP_FOLDER, TABLE_BACKUP_PERMS,
								readCon, writeCon, ctx);
					} else {
						/*
						 * Working
						 */
						OXFolderSQL.handleEntityPermissions(userId, mailadmin, lastModified, TABLE_WORKING_FOLDER,
								TABLE_WORKING_PERMS, readCon, writeCon, ctx);
						/*
						 * Backup
						 */
						OXFolderSQL.handleEntityPermissions(userId, mailadmin, lastModified, TABLE_BACKUP_FOLDER,
								TABLE_BACKUP_PERMS, readCon, writeCon, ctx);
					}
					/*
					 * Handle user's folders
					 */
					if (isMailAdmin) {
						/*
						 * Working
						 */
						OXFolderSQL.handleMailAdminFolders(userId, TABLE_WORKING_FOLDER, TABLE_WORKING_PERMS, readCon,
								writeCon, ctx);
						/*
						 * Backup
						 */
						OXFolderSQL.handleMailAdminFolders(userId, TABLE_BACKUP_FOLDER, TABLE_BACKUP_PERMS, readCon,
								writeCon, ctx);
					} else {
						/*
						 * Working
						 */
						OXFolderSQL.handleEntityFolders(userId, mailadmin, lastModified, TABLE_WORKING_FOLDER,
								TABLE_WORKING_PERMS, readCon, writeCon, ctx);
						/*
						 * Backup
						 */
						OXFolderSQL.handleEntityFolders(userId, mailadmin, lastModified, TABLE_BACKUP_FOLDER,
								TABLE_BACKUP_PERMS, readCon, writeCon, ctx);
					}
					if (performTransaction) {
						writeCon.commit();
						writeCon.setAutoCommit(true);
					}
				} finally {
					if (createReadCon && readCon != null) {
						DBPool.closeReaderSilent(ctx, readCon);
					}
					if (closeWriteCon && writeCon != null) {
						DBPool.closeWriterSilent(ctx, writeCon);
					}
				}
			} catch (final OXException e) {
				try {
					if (performTransaction && writeCon != null) {
						writeCon.rollback();
						writeCon.setAutoCommit(true);
					}

				} catch (final SQLException e1) {
					LOG.warn(e1.getMessage(), e1);
				}
				LOG.error(e.getMessage(), e);
				throw new DeleteFailedException(e);
			} catch (final SQLException e) {
				try {
					if (performTransaction && writeCon != null) {
						writeCon.rollback();
						writeCon.setAutoCommit(true);
					}

				} catch (final SQLException e1) {
					LOG.warn(e1.getMessage(), e1);
				}
				LOG.error(e.getMessage(), e);
				throw new DeleteFailedException(DeleteFailedException.Code.SQL_ERROR, e, e.getLocalizedMessage());
			} catch (final DBPoolingException e) {
				try {
					if (performTransaction && writeCon != null) {
						writeCon.rollback();
						writeCon.setAutoCommit(true);
					}

				} catch (final SQLException e1) {
					LOG.warn(e1.getMessage(), e1);
				}
				LOG.error(e.getMessage(), e);
				throw new DeleteFailedException(e);
			}
		} else if (delEvent.getType() == DeleteEvent.TYPE_GROUP) {
			boolean performTransaction = true;
			try {
				final boolean createReadCon = (readCon == null);
				boolean closeWriteCon = false;
				try {
					if (createReadCon) {
						readCon = DBPool.pickup(ctx);
					}
					if (writeCon == null) {
						writeCon = DBPool.pickupWriteable(ctx);
						closeWriteCon = true;
					}
					performTransaction = writeCon.getAutoCommit();
					if (performTransaction) {
						writeCon.setAutoCommit(false);
					}
					final int groupId = delEvent.getId();
					/*
					 * Get context's mailadmin
					 */
					int mailadmin = ctx.getMailadmin();
					if (mailadmin == -1) {
						mailadmin = OXFolderSQL.getContextMailAdmin(readCon, ctx);
						if (mailadmin == -1) {
							throw new OXFolderException(FolderCode.NO_ADMIN_USER_FOUND_IN_CONTEXT, Integer.valueOf(ctx
									.getContextId()));
						}
					}
					/*
					 * Hander group's permissions
					 */
					OXFolderSQL.handleEntityPermissions(groupId, mailadmin, lastModified, TABLE_WORKING_FOLDER,
							TABLE_WORKING_PERMS, readCon, writeCon, ctx);
					/*
					 * Backup
					 */
					OXFolderSQL.handleEntityPermissions(groupId, mailadmin, lastModified, TABLE_BACKUP_FOLDER,
							TABLE_BACKUP_PERMS, readCon, writeCon, ctx);
					if (performTransaction) {
						writeCon.commit();
						writeCon.setAutoCommit(true);
					}
				} finally {
					if (createReadCon && readCon != null) {
						DBPool.closeReaderSilent(ctx, readCon);
					}
					if (closeWriteCon && writeCon != null) {
						DBPool.closeWriterSilent(ctx, writeCon);
					}
				}
			} catch (final OXException e) {
				try {
					if (performTransaction && writeCon != null) {
						writeCon.rollback();
						writeCon.setAutoCommit(true);
					}

				} catch (final SQLException e1) {
					LOG.warn(e1.getMessage(), e1);
				}
				LOG.error(e.getMessage(), e);
				throw new DeleteFailedException(e);
			} catch (final SQLException e) {
				try {
					if (performTransaction && writeCon != null) {
						writeCon.rollback();
						writeCon.setAutoCommit(true);
					}

				} catch (final SQLException e1) {
					LOG.warn(e1.getMessage(), e1);
				}
				LOG.error(e.getMessage(), e);
				throw new DeleteFailedException(DeleteFailedException.Code.SQL_ERROR, e, e.getLocalizedMessage());
			} catch (final DBPoolingException e) {
				try {
					if (performTransaction && writeCon != null) {
						writeCon.rollback();
						writeCon.setAutoCommit(true);
					}

				} catch (final SQLException e1) {
					LOG.warn(e1.getMessage(), e1);
				}
				LOG.error(e.getMessage(), e);
				throw new DeleteFailedException(e);
			}
		}
		Connection wc = writeConArg;
		try {
			boolean closeWriteCon = false;
			if (wc == null) {
				wc = DBPool.pickupWriteable(ctx);
				closeWriteCon = true;
			}
			try {
				OXFolderDeleteListenerHelper.ensureConsistency(ctx, wc);
			} finally {
				if (closeWriteCon && wc != null) {
					DBPool.closeWriterSilent(ctx, wc);
				}
			}
		} catch (final DBPoolingException e) {
			LOG.error(e.getMessage(), e);
			throw new DeleteFailedException(e);
		}
	}

}
