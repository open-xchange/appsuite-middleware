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

package com.openexchange.ajax.appointment.recurrence;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.helper.ExceptionAssertion;
import com.openexchange.ajax.appointment.helper.PositiveAssertion;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.FolderTestManager;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ManagedAppointmentTest extends AppointmentTest {

    protected CalendarTestManager calendarManager;

    protected FolderTestManager folderManager;

    protected FolderObject folder;

    protected ExceptionAssertion exceptionAssertion;

    protected PositiveAssertion positiveAssertion;

    public ManagedAppointmentTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        calendarManager = new CalendarTestManager(getClient());
        folderManager = new FolderTestManager(getClient());
        UserValues values = getClient().getValues();
        this.folder = folderManager.generateFolder(
            "ManagedAppointmentTests",
            Module.CALENDAR.getFolderConstant(),
            values.getPrivateAppointmentFolder(),
            values.getUserId());
        folder = folderManager.insertFolderOnServer(folder);

        this.exceptionAssertion = new ExceptionAssertion(calendarManager, folder.getObjectID());
        this.positiveAssertion = new PositiveAssertion(calendarManager, folder.getObjectID());

    }

    @Override
    protected void tearDown() throws Exception {
        calendarManager.cleanUp();
        folderManager.cleanUp();
        super.tearDown();
    }

}
