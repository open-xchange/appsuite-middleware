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
package com.openexchange.ajax.appointment.bugtests;

import java.util.Date;

import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.groupware.container.Appointment;

public class Bug20980Test_DateOnMissingDSTHour extends ManagedAppointmentTest {
	public Bug20980Test_DateOnMissingDSTHour(String name) {
		super(name);
	}

	public void testBugWithDST() throws Exception{
		int fid = folder.getObjectID();
		Appointment series = generateDailyAppointment();
		series.setStartDate(D("30/3/2008 01:00",utc));
		series.setEndDate(D("30/3/2008 02:00", utc));
		series.setTitle("A daily series");
		series.setParentFolderID(fid);
		calendarManager.insert(series);

		Date lastMod = series.getLastModified();
		for(int i = 1; i < 3; i++){
			Appointment changeEx = new Appointment();
			changeEx.setParentFolderID(series.getParentFolderID());
			changeEx.setObjectID(series.getObjectID());
			changeEx.setLastModified(lastMod);
			changeEx.setRecurrencePosition(i);
			changeEx.setTitle("Element # "+i+" of series that has different name");
			calendarManager.update(changeEx);
			assertNull("Problem with update #"+i, calendarManager.getLastException());
			lastMod = new Date(calendarManager.getLastModification().getTime() +1);
		}
	}

}
