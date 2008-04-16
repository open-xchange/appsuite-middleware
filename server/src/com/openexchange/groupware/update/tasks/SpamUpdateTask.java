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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.openexchange.database.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;
import com.openexchange.mail.usersetting.UserSettingMail;

/**
 * SpamUpdateTask - Inserts columns <tt>confirmed_spam</tt> and
 * <tt>confirmed_ham</tt> to table <tt>user_setting_mail</tt>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = EnumComponent.UPDATE)
public class SpamUpdateTask implements UpdateTask {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(SpamUpdateTask.class);

	private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(SpamUpdateTask.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.update.UpdateTask#addedWithVersion()
	 */
	public int addedWithVersion() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.update.UpdateTask#getPriority()
	 */
	public int getPriority() {
		/*
		 * Modification on database: highest priority.
		 */
		return UpdateTask.UpdateTaskPriority.HIGHEST.priority;
	}

	private static final String SQL_MODIFY = "ALTER TABLE user_setting_mail "
			+ "ADD COLUMN confirmed_spam VARCHAR(128) character set utf8 collate utf8_unicode_ci NOT NULL, "
			+ "ADD COLUMN confirmed_ham VARCHAR(128) character set utf8 collate utf8_unicode_ci NOT NULL";
	
	private static final String SQL_UPDATE = "UPDATE user_setting_mail SET confirmed_spam = ?, confirmed_ham = ?";
	
	private static final String STR_INFO = "Performing update task 'SpamUpdateTask'";
	
	private static final String CONFIRMED_SPAM = "confirmed_spam";
	
	private static final String CONFIRMED_HAM = "confirmed_ham";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.update.UpdateTask#perform(com.openexchange.groupware.update.Schema,
	 *      int)
	 */
	@OXThrowsMultiple(
			category = { Category.CODE_ERROR },
			desc = { "" },
			exceptionId = { 1 },
			msg = { "An SQL error occurred while performing task SpamUpdateTask: %1$s." }
		)
	public void perform(final Schema schema, final int contextId) throws AbstractOXException {
		if (LOG.isInfoEnabled()) {
			LOG.info(STR_INFO);
		}
		if (checkExistence(CONFIRMED_SPAM, contextId) && checkExistence(CONFIRMED_HAM, contextId)) {
			return;
		}
		Connection writeCon = null;
		PreparedStatement stmt = null;
		try {
			writeCon = Database.get(contextId, true);
			try {
				stmt = writeCon.prepareStatement(SQL_MODIFY);
				stmt.executeUpdate();
				stmt.close();
				stmt = writeCon.prepareStatement(SQL_UPDATE);
				stmt.setString(1, UserSettingMail.STD_CONFIRMED_SPAM);
				stmt.setString(2, UserSettingMail.STD_CONFIRMED_HAM);
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
	
	private static final String SQL_SELECT_ALL = "SELECT * FROM user_setting_mail";
	
	@OXThrowsMultiple(
			category = { Category.CODE_ERROR },
			desc = { "" },
			exceptionId = { 2 },
			msg = { "An SQL error occurred while performing task PasswordMechUpdateTask: %1$s." }
	)
	private static final boolean checkExistence(final String colName, final int contextId) throws AbstractOXException {
		Connection readCon = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			readCon = Database.get(contextId, false);
			try {
				stmt = readCon.createStatement();
				rs = stmt.executeQuery(SQL_SELECT_ALL);
				final ResultSetMetaData meta = rs.getMetaData();
				final int length = meta.getColumnCount();
				boolean found = false;
				for (int i = 1; i <= length && !found; i++) {
					found = colName.equals(meta.getColumnName(i));
				}
				return found;
			} catch (SQLException e) {
				throw EXCEPTION.create(2, e, e.getMessage());
			}
		} finally {
			closeSQLStuff(rs, stmt);
			if (readCon != null) {
				Database.back(contextId, false, readCon);
			}
		}
	}

}
