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

package com.openexchange.subscribe.google;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.ContactTestManager;
import com.openexchange.test.FolderTestManager;

/**
 * {@link AbstractGoogleSubscribeTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.6.1
 */
public abstract class AbstractGoogleSubscribeTest extends AbstractAJAXSession {

    private CalendarTestManager calendarMgr;

    private ContactTestManager contactMgr;

    private FolderTestManager folderMgr;

    /**
     * Initializes a new {@link AbstractGoogleSubscribeTest}.
     *
     * @param name
     */
    protected AbstractGoogleSubscribeTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        calendarMgr = new CalendarTestManager(client);
        contactMgr = new ContactTestManager(client);
        folderMgr = new FolderTestManager(client);
    }

    /**
     * Get the calendar test manager
     *
     * @return
     */
    public CalendarTestManager getCalendarManager() {
        return calendarMgr;
    }

    /**
     * Get the contact test manager
     *
     * @return
     */
    public ContactTestManager getContactManager() {
        return contactMgr;
    }

    public FolderTestManager getFolderManager() {
        return folderMgr;
    }

    public static void assertFieldIsNull(String fieldDesc, Object valueToCheck) {
        assertNull("The field " + fieldDesc + " should be empty, but is not ", valueToCheck);
    }

    public static void assertNotNullAndEquals(String fieldDesc, Object expected, Object actual) {
        assertNotNull("Could not find expected mapping for " + fieldDesc, actual);
        assertEquals("Mapping for field '" + fieldDesc + "' differs -->", expected, actual);
    }

    public static void assertFieldNotNull(String fieldDesc, Object expected, Object actual) {
        assertNotNull("Could not find expected mapping for " + fieldDesc, actual);
    }

    /**
     * Gets date / time with the default time zone Europe / Berlin.
     */
    protected Date getDateTime(int day, int month, int year, int hour, int minute) {
        return getDateTime(day, month, year, hour, minute, 0, TimeZone.getTimeZone("Europe/Berlin"));
    }

    /**
     * Gets date / time with precisions seconds with the default time zone Europe / Berlin.
     */
    protected Date getDateTime(int day, int month, int year, int hour, int minute, int seconds) {
        return getDateTime(day, month, year, hour, minute, seconds, TimeZone.getTimeZone("Europe/Berlin"));
    }

    protected Date getDateTime(int day, int month, int year, int hour, int minute, int seconds, TimeZone timezone) {
        Calendar cal = Calendar.getInstance(timezone);
        cal.clear();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, seconds);
        return cal.getTime();
    }

    private int getTestFolderID(final String id) {
        return GoogleSubscribeTestEnvironment.getInstance().getTestFolders().get(id);
    }

    protected int getCalendarTestFolderID() {
        return getTestFolderID(GoogleSubscribeTestEnvironment.CALENDAR_SOURCE_ID);
    }

    protected int getContactTestFolderID() {
        return getTestFolderID(GoogleSubscribeTestEnvironment.CONTACT_SOURCE_ID);
    }

}
