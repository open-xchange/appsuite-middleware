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
package com.openexchange.ajax.contact;

import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;

public class Bug19984Test extends ManagedAppointmentTest {
	
	public Bug19984Test(String name) {
		super(name);
	}

	String ical = "BEGIN:VCALENDAR\n" + 
			"BEGIN:VEVENT\n" + 
			"DTSTART:20110726T183000\n" + 
			"DTEND:20110726T200000\n" + 
			"LOCATION;ENCODING=QUOTED-PRINTABLE:DLRG-Heim\n" + 
			"CATEGORIES;ENCODING=QUOTED-PRINTABLE:DLRG WRD\n" + 
			"DESCRIPTION;CHARSET=ISO-8859-1;ENCODING=QUOTED-PRINTABLE:Liebe Einsatzkr\u00e4fte,=0A=0Awir laden ein zum Wasserretter-Treff. Dieser findet alle vier Wochen statt. Neben der Einteilung f\u00fcr den Wachdienst werden auch aktuelle Themen, wie Eins\u00e4tze, abgearbeitet oder auch nur kleine Ausbildungsinhalte aus dem Bereich Fachausbildung Wasserrettung vermittelt.=0A=0AWir freuen uns daher \u00fcber eine zahlreiche Teilnahme!=0A=0AEingeladen sind alle ab Rettungsschwimmabzeichen Bronze!!!\n" + 
			"SUMMARY;ENCODING=QUOTED-PRINTABLE:Wasserretter-Treff [OG\u00a0Hirschaid]\n" + 
			"PRIORITY:3\n" + 
			"END:VEVENT\n" + 
			"END:VCALENDAR";
	
	public void testIt() throws Exception {
		ICalImportRequest request = new ICalImportRequest(folder.getObjectID(), ical);
		ICalImportResponse response = getClient().execute(request);
		// System.out.println(response.getData());
		assertFalse(System.getProperty("line.separator")+response.getData(), response.hasError());
	}
}
