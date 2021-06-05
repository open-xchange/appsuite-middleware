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

import com.openexchange.ajax.appointment.helper.AbstractPositiveAssertion;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Changes;
import com.openexchange.groupware.container.Expectations;
import com.openexchange.test.CalendarTestManager;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class PositiveAssertionOnChangeException extends AbstractPositiveAssertion {

    private Appointment series, changeException;

    public Appointment getSeries() {
        return series;
    }

    public void setSeries(Appointment series) {
        this.series = series;
    }

    public Appointment getChangeException() {
        return changeException;
    }

    public void setChangeException(Appointment exception) {
        this.changeException = exception;
    }

    public PositiveAssertionOnChangeException(CalendarTestManager manager, int folder) {
        super(manager, folder);
    }

    @Override
    public void check(Appointment startAppointment, Changes changes, Expectations expectations) throws OXException {
        approachUsedForTest = "Create change exception";
        Appointment copy = startAppointment.clone();

        if (!startAppointment.containsObjectID()) {
            manager.insert(copy);
        }

        Appointment update = new Appointment();
        update.setLastModified(copy.getLastModified());
        update.setParentFolderID(copy.getParentFolderID());
        update.setObjectID(copy.getObjectID());

        changes.update(update);

        manager.update(update);
        checkViaGet(update.getParentFolderID(), update.getObjectID(), expectations);
        checkViaList(update.getParentFolderID(), update.getObjectID(), expectations);

        setChangeException(manager.get(update));
        setSeries(manager.get(copy));
    }

}
