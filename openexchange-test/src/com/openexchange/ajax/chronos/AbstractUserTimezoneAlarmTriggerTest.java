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

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * {@link AbstractUserTimezoneAlarmTriggerTest} runs the tests with different user timezone configurations.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
@RunWith(Parameterized.class)
public abstract class AbstractUserTimezoneAlarmTriggerTest extends AbstractAlarmTriggerTest {

    @Parameter(value = 0)
    public TimeZone timeZone;

    @Parameters(name = "TimeZone={0}")
    public static Iterable<Object[]> params() {
        List<Object[]> timeZones = new ArrayList<>(3);
        timeZones.add(new Object[] { TimeZone.getTimeZone("UTC") });
        timeZones.add(new Object[] { TimeZone.getTimeZone("Europe/Berlin") });
        timeZones.add(new Object[] { TimeZone.getTimeZone("America/New_York") });
        return timeZones;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        changeTimezone(timeZone);
    }

}
