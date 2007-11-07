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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.openexchange.database.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;

/**
 * DelFolderTreeTableUpdateTask
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = Component.UPDATE)
public class DelFolderTreeTableUpdateTask implements UpdateTask {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(DelFolderTreeTableUpdateTask.class);
	
	private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(DelFolderTreeTableUpdateTask.class);

	/* (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.update.UpdateTask#addedWithVersion()
	 */
	public int addedWithVersion() {
		return 4;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.groupware.update.UpdateTask#getPriority()
	 */
	public int getPriority() {
		/*
		 * Modification on database: highest priority.
		 */
		return UpdateTask.UpdateTaskPriority.HIGHEST.priority;
	}
	
	private static final String STR_INFO = "Performing update task 'DelFolderTreeTableUpdateTask'";
	
	private static final String SQL_MODIFY = "ALTER TABLE del_oxfolder_tree MODIFY `fname` VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL";

	/* (non-Javadoc)
	 * @see com.openexchange.groupware.update.UpdateTask#perform(com.openexchange.groupware.update.Schema, int)
	 */
	@OXThrowsMultiple(category = { Category.CODE_ERROR }, desc = { "" }, exceptionId = { 1 }, msg = { "An SQL error occurred while performing task DelFolderTreeTableUpdateTask: %1$s." })
	public void perform(final Schema schema, final int contextId) throws AbstractOXException {
		if (LOG.isInfoEnabled()) {
			LOG.info(STR_INFO);
		}
		Connection writeCon = null;
		PreparedStatement stmt = null;
		try {
			writeCon = Database.get(contextId, true);
			try {
				stmt = writeCon.prepareStatement(SQL_MODIFY);
				stmt.executeUpdate();
			} catch (SQLException e) {
				throw EXCEPTION.create(1, e, e.getMessage());
			}
		} finally {
			closeSQLStuff(null, stmt);
			if (writeCon != null) {
				Database.back(contextId, true, writeCon);
			}
		}
	}

}
