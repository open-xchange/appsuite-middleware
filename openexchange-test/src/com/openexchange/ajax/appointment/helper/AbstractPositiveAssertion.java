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
public abstract class AbstractPositiveAssertion extends AbstractAssertion{


    public AbstractPositiveAssertion(CalendarTestManager manager, int folder) {
        this.manager = manager;
        this.folder = folder;
    }

    public void check(Changes changes, Expectations expectations) throws Exception{
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
        if(null == approachUsedForTest) {
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
        if(manager.hasLastException()) {
            fail2("Could not create appointment, error: " + manager.getLastException());
        }
        checkViaGet(copy.getParentFolderID(), copy.getObjectID(), expectations);
        checkViaList(copy.getParentFolderID(), copy.getObjectID(), expectations);
    }

    protected void checkViaList(int folderId, int appointmentId, Expectations expectations){
        methodUsedForTest = "List";
        try {
            List<Appointment> appointments = manager.list(new ListIDs(folderId, appointmentId), expectations.getKeys());
            Appointment actual = find(appointments, folderId, appointmentId);
            if(manager.hasLastException()) {
                fail2("Exception occured: " + manager.getLastException(), manager.getLastException());
            }
            expectations.verify(state(),actual);
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
            if(manager.hasLastException()) {
                fail2("Exception occured: " + manager.getLastException());
            }
        } catch (Exception e) {
            fail2("Exception occurred: " + e);
            return;
        }
        expectations.verify( state(), actual);
    }

    protected void checkViaGet(int folderId, int appointmentId, int recurrencePos, Expectations expectations) {
        methodUsedForTest = "Get recurrence";
        Appointment actual;
        try {
            actual = manager.get(folderId, appointmentId, recurrencePos);
            if(manager.hasLastException()) {
                fail2("Exception occured: " + manager.getLastException());
            }
        } catch (Exception e) {
            fail2("Exception occurred: " + e);
            return;
        }
        expectations.verify( state(), actual);
    }
}
