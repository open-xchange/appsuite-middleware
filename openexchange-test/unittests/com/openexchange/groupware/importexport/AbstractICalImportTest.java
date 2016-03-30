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

package com.openexchange.groupware.importexport;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.importers.ICalImporter;
import com.openexchange.test.AjaxInit;
import com.openexchange.tools.session.ServerSessionFactory;

public abstract class AbstractICalImportTest extends AbstractContactTest {

	public final Format format = Format.ICAL;
	

    @BeforeClass
	public static void initialize() throws Exception {
		Init.startServer();
		final UserStorage uStorage = UserStorage.getInstance();
        ctx = ContextStorage.getInstance().getContext(ContextStorage.getInstance().getContextId(AjaxInit.getAJAXProperty("contextName")));
        userId = uStorage.getUserId(AjaxInit.getAJAXProperty("login"), ctx);
	    sessObj = ServerSessionFactory.createServerSession(userId, ctx.getContextId(), "csv-tests");
		userId = sessObj.getUserId();
		imp = new ICalImporter(null);
	}

    @AfterClass
    public static void shutdown() throws Exception {
        Init.stopServer();
    }

    public AbstractICalImportTest() {
		super();
	}

	public String generateRecurringICAL(final int interval, final String frequency) {
		return
		"BEGIN:VCALENDAR\n" +
		"VERSION:2.0\n" +
		"PRODID:-//The Horde Project//Horde_iCalendar Library//EN\n" +
		"METHOD:PUBLISH\n" +
		"BEGIN:VEVENT\n" +
		"DTSTART;VALUE=DATE:20070616\n" +
		"DTEND;VALUE=DATE:20070617\n" +
		"DTSTAMP:20070530T200206Z\n" +
		"UID:20070530220126.23mszu01hoo0@www.klein-intern.de\n" +
		"SUMMARY:Marc beim Umzug helfen\n" +
		"TRANSP:OPAQUE\nORGANIZER;CN=Marcus Klein:MAILTO:m.klein@sendung-mit-der-maus.com\n" +
		"LOCATION:Olpe\n" +
		"RRULE:FREQ="+frequency+";INTERVAL="+interval+";UNTIL=20070627\n" +
		"END:VEVENT\n" +
		"END:VCALENDAR\n";
	}

}
