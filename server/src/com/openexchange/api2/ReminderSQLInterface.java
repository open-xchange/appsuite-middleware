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



package com.openexchange.api2;

import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.groupware.reminder.ReminderDeleteInterface;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.tools.iterator.SearchIterator;
import java.sql.Connection;
import java.util.Date;

/**
 * ReminderSQLInterface
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public interface ReminderSQLInterface {
	
	public void setReminderDeleteInterface(ReminderDeleteInterface reminderDeleteInterface);
	
	public int insertReminder(ReminderObject reminderObj) throws OXMandatoryFieldException, OXConflictException, OXException;

	public int insertReminder(ReminderObject reminderObj, Connection writeCon) throws OXMandatoryFieldException, OXConflictException, OXException;
	
	public void updateReminder(ReminderObject reminderObj) throws OXMandatoryFieldException, OXConflictException, OXException;

	public void updateReminder(ReminderObject reminderObj, Connection writeCon) throws OXMandatoryFieldException, OXConflictException, OXException;
	
	public void deleteReminder(int objectId) throws OXException;
	
	public void deleteReminder(int targetId, int module) throws OXException;

	public void deleteReminder(int targetId, int module, Connection writeCon) throws OXException;

	public void deleteReminder(int targetId, int userId, int module) throws OXException;

	public void deleteReminder(int targetId, int userId, int module, Connection writeCon) throws OXException;
	
	public boolean existsReminder(int targetId, int userId, int module) throws OXException;
	
	public ReminderObject loadReminder(int targetId, int userId, int module) throws OXException;
	
	public ReminderObject loadReminder(int objectId) throws OXMandatoryFieldException, OXConflictException, OXException;

    /**
     * This method loads the reminder for several target objects.
     * @param targetIds unique identifier of several target objects.
     * @param userId unique identifier of the user.
     * @param module module type of target objects.
     * @return an array of found reminders.
     * @throws OXException if reading the reminder fails.
     */
    ReminderObject[] loadReminder(int[] targetIds, int userId, int module)
        throws OXException;

	public SearchIterator listReminder(int targetId) throws OXException;
	
	public SearchIterator listReminder(int userId, Date end) throws OXException;
	
	public SearchIterator listModifiedReminder(int userId, Date lastModified) throws OXException;

}
