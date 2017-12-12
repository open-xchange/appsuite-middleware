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

package com.openexchange.ajax.importexport;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.openexchange.test.concurrent.ParallelSuite;

/**
 * Test suite for iCal tests.
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    ICalImportTest.class,
    ICalTaskExportTest.class,
    ICalAppointmentExportTest.class,
    ICalSeriesTests.class,
    Bug9840Test.class,
    Bug11724Test.class,
    Bug11868Test.class,
    Bug11871Test.class,
    Bug11920Test.class,
    Bug11996Test.class,
    Bug12414Test.class,
    Bug12470Test.class,
    Bug17393Test.class,
    Bug17963Test_DateWithoutTime.class,
    Bug19046Test_SeriesWithExtraneousStartDate.class,
    Bug19089Test.class,
    Bug19463Test_TimezoneOffsetsWith4Digits.class,
    Bug19681_TimezoneForUtcProperties.class,
    Bug19915Test.class,
    Bug20132Test_WrongRecurrenceDatePosition.class,
    Bug20405Test_TaskWithoutDueDate.class,
    Bug20413Test_CompletelyWrongDTStart.class,
    Bug20453Test_emptyDTEND.class,
    Bug20498Test_ReminderJumpsAnHour.class,
    Bug20715Test_UidIsNotcaseSensitive.class,
    Bug20718Test_JumpDuringDstCrossing.class,
    Bug20896Test_AlarmsChange.class,
    Bug20945Test_UnexpectedError26.class,
    Bug22059Test.class,
    Bug27474Test.class,
    Bug28071Test.class,
    Bug56435Test_TaskStateRoundtrip.class
})
public final class ICalTestSuite {
}
