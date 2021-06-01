/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.chronos;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.ajax.chronos.bugs.ChronosBugsTestSuite;
import com.openexchange.ajax.chronos.itip.ITipTestSuite;
import com.openexchange.ajax.chronos.schedjoules.SchedJoulesTestSuite;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * {@link ChronosTestSuite}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis
 *         Chouklis</a>
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
		// @formatter:off
		AcknowledgeAndSnoozeTest.class, BasicAlarmTest.class, BasicAlarmTriggerTest.class, MailAlarmTriggerTest.class,
		// BasicAvailabilityTest.class,
		BasicFreeBusyTest.class, SchedJoulesTestSuite.class, BasicSelfProtectionTest.class, BasicSeriesEventTest.class,
		BasicSingleEventTest.class, ChronosQuotaTest.class, TimezoneAlarmTriggerTest.class,
		ICalEventImportExportTest.class, RestrictedAttendeePermissionsTest.class, BasicICalCalendarProviderTest.class,
		ICalImportAccessTest.class, ChronosBugsTestSuite.class, ITipTestSuite.class, BirthdayCalendarExportTest.class,
		BasicCommentTest.class, ChangeOrganizerTest.class, AttendeePrivilegesTest.class, AlarmPropagationTests.class,
		NeedsActionActionTest.class
		// @formatter:on

})
public class ChronosTestSuite {
	// empty
}
