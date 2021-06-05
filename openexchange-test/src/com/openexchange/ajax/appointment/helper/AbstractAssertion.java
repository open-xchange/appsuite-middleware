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

import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import java.util.List;
import java.util.TimeZone;
import org.junit.Assert;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Changes;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link AbstractAssertion}
 *
 * @author <a href="mailto:firstname.lastname@open-xchange.com">Firstname Lastname</a>
 */
public class AbstractAssertion extends Assert {

    protected String approachUsedForTest; // for example "create and update" or only "create"

    protected String methodUsedForTest; // for example "list" or "get"

    protected int folder;

    protected CalendarTestManager manager;

    public AbstractAssertion() {
        super();
    }

    protected String state() {
        return "[" + approachUsedForTest + " | " + methodUsedForTest + "]";
    }

    protected void fail2(String message) {
        Assert.fail(state() + message);
    }

    protected void fail2(String message, Throwable t) {
        t.printStackTrace();
        Assert.fail(state() + message);
    }

    protected Appointment find(List<Appointment> appointments, int folderToSearch, int id) {
        for (Appointment app : appointments) {
            if (app.getParentFolderID() == folderToSearch && app.getObjectID() == id) {
                return app;
            }
        }
        return null;
    }

    public static Appointment generateDefaultAppointment(int folder) {
        Appointment app = generateDefaultAppointment();
        app.setParentFolderID(folder);
        return app;
    }

    public static Appointment generateDefaultAppointment() {
        Appointment app = new Appointment();
        app.setTitle("Generic recurrence test appointment");
        TimeZone utc = TimeZone.getTimeZone("UTC");
        app.setStartDate(D("1/1/2008 1:00", utc));
        app.setEndDate(D("1/1/2008 2:00", utc));
        app.setIgnoreConflicts(true);
        return app;
    }

    protected Appointment create(Appointment app) {
        return manager.insert(app);
    }

    protected void update(Appointment app, Changes changes) {
        Appointment update = new Appointment();
        update.setParentFolderID(app.getParentFolderID());
        update.setObjectID(app.getObjectID());
        update.setLastModified(app.getLastModified());
        update.setIgnoreConflicts(app.getIgnoreConflicts());

        changes.update(update);
        manager.update(update);
    }
}
