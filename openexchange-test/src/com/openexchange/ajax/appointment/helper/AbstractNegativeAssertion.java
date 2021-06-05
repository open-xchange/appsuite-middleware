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

package com.openexchange.ajax.appointment.helper;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Changes;
import com.openexchange.test.CalendarTestManager;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class AbstractNegativeAssertion extends AbstractAssertion {

    public AbstractNegativeAssertion(CalendarTestManager manager, int folderToWorkIn) {
        super();
        this.manager = manager;
        manager.setFailOnError(false);
        this.folder = folderToWorkIn;
    }

    public void check(Changes changes, OXException expectedError) {
        check(generateDefaultAppointment(), changes, expectedError);
    }

    public abstract void check(Appointment startWith, Changes changes, OXException expectedError);

    protected void createAndCheck(Appointment startWith, Changes changes, OXException expectedError) {
        approachUsedForTest = "Create directly";

        changes.update(startWith);
        create(startWith);

        checkForError(expectedError);
    }

    protected void updateAndCheck(Appointment startWith, Changes changes, OXException expectedError) {
        approachUsedForTest = "Create and update";

        create(startWith);
        update(startWith, changes);

        checkForError(expectedError);
    }

    private void checkForError(OXException expectedError) {
        methodUsedForTest = "Check lastException field";
        assertTrue(state() + " Expecting exception, did not get one", manager.hasLastException());
        try {
            OXException lastException = (OXException) manager.getLastException();
            assertTrue(state() + " Expected error: " + expectedError + ", actual error: " + lastException.getErrorCode(), expectedError.similarTo(lastException));
        } catch (ClassCastException e) {
            fail2("Should have an OXException, but could not cast it into one");
        }
    }
}
