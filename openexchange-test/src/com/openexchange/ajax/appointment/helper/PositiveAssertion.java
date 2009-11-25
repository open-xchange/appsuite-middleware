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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.io.IOException;
import java.util.List;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.tools.servlet.AjaxException;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class PositiveAssertion extends AbstractAssertion{


    public PositiveAssertion(CalendarTestManager manager, int folder) throws AjaxException, IOException, SAXException, JSONException {
        this.manager = manager;
        this.folder = folder;
    }
    
    public void check(Changes changes, Expectations expectations) throws AjaxException, IOException, SAXException, JSONException {
        check(generateDefaultAppointment(), changes, expectations);
    }
    
    public void check(Appointment startAppointment, Changes changes, Expectations expectations) throws AjaxException, IOException, SAXException, JSONException{        
        if(!startAppointment.containsParentFolderID())
            startAppointment.setParentFolderID(folder);
        
        Appointment startAppointmentCopy = (Appointment) startAppointment.clone();
        try {
            createAndCheck(startAppointment, changes, expectations);
        } finally {
            manager.cleanUp();
        }
        try {
            updateAndCheck(startAppointmentCopy, changes, expectations);
        } finally {
            manager.cleanUp();
        }

    }
    
    protected void updateAndCheck(Appointment startAppointment, Changes changes, Expectations expectations) {
        approachUsedForTest = "Create, then update";
        create(startAppointment);
        update(startAppointment, changes);
        checkViaGet(startAppointment.getParentFolderID(), startAppointment.getObjectID(), expectations);
        checkViaList(startAppointment.getParentFolderID(), startAppointment.getObjectID(), expectations);
    }

    protected void createAndCheck(Appointment startAppointment, Changes changes, Expectations expectations) {
        approachUsedForTest = "Create directly";
        changes.update(startAppointment);
        create(startAppointment);
        if(manager.hasLastException())
            fail2("Could not create appointment, error: " + manager.getLastException());
        checkViaGet(startAppointment.getParentFolderID(), startAppointment.getObjectID(), expectations);
        checkViaList(startAppointment.getParentFolderID(), startAppointment.getObjectID(), expectations);
    }

    protected void checkViaList(int folder, int id, Expectations expectations){
        methodUsedForTest = "List";
        try {
            List<Appointment> appointments = manager.list(new ListIDs(folder, id), expectations.getKeys());
            Appointment actual = find(appointments, folder, id);
            if(manager.hasLastException())
                fail2("Exception occured: " + manager.getLastException());
            expectations.verify(state(),actual);
        } catch (Exception e) {
            fail2("Exception occurred: " + e);
            return;
        }
        
    }

    protected void checkViaGet(int folder, int id, Expectations expectations) {
        methodUsedForTest = "Get";
        Appointment actual;
        try {
            actual = manager.getAppointmentFromServer(folder, id);
            if(manager.hasLastException())
                fail2("Exception occured: " + manager.getLastException());
        } catch (Exception e) {
            fail2("Exception occurred: " + e);
            return;
        }
        expectations.verify( state(), actual);
    }

}
