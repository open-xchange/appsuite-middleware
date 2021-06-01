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

package com.openexchange.ajax.reminder;

import static org.junit.Assert.assertEquals;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.test.common.test.OXTestToolkit;

public abstract class ReminderTest extends AbstractAJAXSession {

    public static void compareReminder(final ReminderObject reminderObj1, final ReminderObject reminderObj2) {
        assertEquals("id", reminderObj1.getObjectId(), reminderObj2.getObjectId());
        assertEquals("folder", reminderObj1.getFolder(), reminderObj2.getFolder());
        OXTestToolkit.assertEqualsAndNotNull("alarm", reminderObj1.getDate(), reminderObj2.getDate());
    }
}
