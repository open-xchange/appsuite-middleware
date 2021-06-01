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

package com.openexchange.ajax.appointment.recurrence;

import com.openexchange.ajax.appointment.helper.AbstractNegativeAssertion;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Changes;
import com.openexchange.test.CalendarTestManager;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class NegativeAssertionOnChangeException extends AbstractNegativeAssertion {

    public NegativeAssertionOnChangeException(CalendarTestManager manager, int folderToWorkIn) {
        super(manager, folderToWorkIn);
    }

    @Override
    public void check(Appointment startWith, Changes changes, OXException expectedError) {
        Appointment copy = startWith.clone();
        if (!startWith.containsObjectID()) {
            manager.insert(copy);
        }

        Appointment update = new Appointment();
        update.setParentFolderID(copy.getParentFolderID());
        update.setObjectID(copy.getObjectID());
        update.setLastModified(copy.getLastModified());
        changes.update(update);

        manager.update(update);
        assertTrue("Expected error " + expectedError + " but got nothing", manager.hasLastException());
        OXException actual = (OXException) manager.getLastException();
        assertTrue("Actual error" + actual + " should match expected error " + expectedError, expectedError.similarTo(actual));
    }

}
