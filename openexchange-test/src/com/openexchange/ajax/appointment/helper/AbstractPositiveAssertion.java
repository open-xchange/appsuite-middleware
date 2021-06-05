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

import java.util.List;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Changes;
import com.openexchange.groupware.container.Expectations;
import com.openexchange.test.CalendarTestManager;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class AbstractPositiveAssertion extends AbstractAssertion {

    public AbstractPositiveAssertion(CalendarTestManager manager, int folder) {
        this.manager = manager;
        this.folder = folder;
    }

    public void check(Changes changes, Expectations expectations) throws Exception {
        approachUsedForTest = null;
        check(generateDefaultAppointment(), changes, expectations);
    }

    public abstract void check(Appointment startAppointment, Changes changes, Expectations expectations) throws Exception;

    protected void createAndUpdateAndCheck(Appointment startAppointment, Changes changes, Expectations expectations) {
        approachUsedForTest = "Create, then update";
        Appointment copy = startAppointment.clone();
        create(copy); //TODO replace remaining code with updateAndCheck(copy,changes,expectations); and test that
        update(copy, changes);
        checkViaGet(copy.getParentFolderID(), copy.getObjectID(), expectations);
        checkViaList(copy.getParentFolderID(), copy.getObjectID(), expectations);
    }

    protected void updateAndCheck(Appointment startAppointment, Changes changes, Expectations expectations) {
        if (null == approachUsedForTest) {
            approachUsedForTest = "Update existing";
        }
        Appointment base = new Appointment();
        base.setLastModified(startAppointment.getLastModified());
        base.setObjectID(startAppointment.getObjectID());
        base.setParentFolderID(startAppointment.getParentFolderID());

        update(base, changes);
        checkViaGet(base.getParentFolderID(), base.getObjectID(), expectations);
        checkViaList(base.getParentFolderID(), base.getObjectID(), expectations);
    }

    protected void createAndCheck(Appointment startAppointment, Changes changes, Expectations expectations) {
        approachUsedForTest = "Create directly";
        Appointment copy = startAppointment.clone();
        changes.update(copy);
        create(copy);
        if (manager.hasLastException()) {
            fail2("Could not create appointment, error: " + manager.getLastException());
        }
        checkViaGet(copy.getParentFolderID(), copy.getObjectID(), expectations);
        checkViaList(copy.getParentFolderID(), copy.getObjectID(), expectations);
    }

    protected void checkViaList(int folderId, int appointmentId, Expectations expectations) {
        methodUsedForTest = "List";
        try {
            List<Appointment> appointments = manager.list(new ListIDs(folderId, appointmentId), expectations.getKeys());
            Appointment actual = find(appointments, folderId, appointmentId);
            if (manager.hasLastException()) {
                fail2("Exception occured: " + manager.getLastException(), manager.getLastException());
            }
            expectations.verify(state(), actual);
        } catch (Exception e) {
            fail2("Exception occurred: ", e);
            return;
        }

    }

    protected void checkViaGet(int folderId, int appointmentId, Expectations expectations) {
        methodUsedForTest = "Get";
        Appointment actual;
        try {
            actual = manager.get(folderId, appointmentId);
            if (manager.hasLastException()) {
                fail2("Exception occured: " + manager.getLastException());
            }
        } catch (Exception e) {
            fail2("Exception occurred: " + e);
            return;
        }
        expectations.verify(state(), actual);
    }

    protected void checkViaGet(int folderId, int appointmentId, int recurrencePos, Expectations expectations) {
        methodUsedForTest = "Get recurrence";
        Appointment actual;
        try {
            actual = manager.get(folderId, appointmentId, recurrencePos);
            if (manager.hasLastException()) {
                fail2("Exception occured: " + manager.getLastException());
            }
        } catch (Exception e) {
            fail2("Exception occurred: " + e);
            return;
        }
        expectations.verify(state(), actual);
    }
}
