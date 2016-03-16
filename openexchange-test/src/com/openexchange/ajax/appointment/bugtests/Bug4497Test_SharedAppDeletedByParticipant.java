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

import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.helper.AbstractAssertion;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;

public class Bug4497Test_SharedAppDeletedByParticipant extends	ManagedAppointmentTest {

	public Bug4497Test_SharedAppDeletedByParticipant(String name) {
		super(name);
	}
	
	public void testIt() throws Exception{
		AJAXClient client2 = new AJAXClient(User.User2);
		
		UserParticipant other = new UserParticipant(client2.getValues().getUserId());
		assertTrue(other.getIdentifier() > 0);
		int fid1 = folder.getObjectID();
		
		Appointment app = AbstractAssertion.generateDefaultAppointment(fid1);
		app.addParticipant(other);
		
		calendarManager.insert(app);
		
		int appId = app.getObjectID();
		int fid2 = client2.getValues().getPrivateAppointmentFolder();
		
		GetResponse getResponse = client2.execute(new GetRequest(fid2,appId));
		Date lastMod = getResponse.getTimestamp();

		CommonDeleteResponse deleteResponse = client2.execute(new DeleteRequest(appId, fid2, lastMod));
		
		assertFalse(deleteResponse.hasError() || deleteResponse.hasConflicts());
	}

}

