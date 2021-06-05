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

package com.openexchange.chronos.recurrence;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.junit.runners.Parameterized.Parameters;

/**
 * {@link AbstractSingleTimeZoneTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public abstract class AbstractSingleTimeZoneTest extends RecurrenceServiceTest {

    /**
     * Does the dtstart value count towrds the "COUNT" rule of the recurrence rule if it is not in sync with the rule?
     */
    protected static final boolean COUNT_DTSTART = false;

    public AbstractSingleTimeZoneTest(String timeZone) {
        super(timeZone);
    }

    @Parameters(name = "{0}")
    public static List<Object[]> data() {
        List<Object[]> retval = new ArrayList<Object[]>();
        for (String tzId : TimeZone.getAvailableIDs()) {
            //for (String tzId : new String[] { "Europe/Berlin", "UTC" }) {
            retval.add(new Object[] { tzId });
        }
        return retval;
    }

}
