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

package com.openexchange.ajax.kata.appointments;

import junit.framework.Assert;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.kata.AbstractStep;
import com.openexchange.ajax.kata.IdentitySource;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.CalendarTestManager;


/**
 * {@link AppointmentCreateStep}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class AppointmentCreateStep extends AbstractStep implements IdentitySource<AppointmentObject> {

    private AppointmentObject entry;
    private CalendarTestManager manager;
    private boolean inserted;

    /**
     * Initializes a new {@link AppointmentCreateStep}.
     * @param entry
     */
    public AppointmentCreateStep(AppointmentObject entry, String name, String expectedError) {
        super(name, expectedError);
        this.entry = entry;
        this.expectedError = expectedError;
    }
    
    public void cleanUp() throws Exception {
        if(!inserted) {
            return;
        }
        manager.deleteAppointmentOnServer(entry);
    }

    public void perform(AJAXClient client) throws Exception {
        
        this.client = client;
        this.manager = new CalendarTestManager(client);
        
        int folderId = client.getValues().getPrivateAppointmentFolder();
        entry.setParentFolderID(folderId);
        
        InsertRequest insertRequest = new InsertRequest(entry, getTimeZone(), false);
        inserted = false;
        AppointmentInsertResponse insertResponse = execute(insertRequest);
        insertResponse.fillAppointment(entry);
        inserted = ! ( insertResponse.hasError() ||insertResponse.hasConflicts() );
        if(insertResponse.hasConflicts()){
            StringBuilder conflicts = new StringBuilder("Conflicting appointments: ");
            for(ConflictObject conflict: insertResponse.getConflicts()){
                conflicts.append( conflict.getTitle() );
                conflicts.append(", ");
            }
            Assert.fail(name + " " + conflicts);
        }
        checkError(insertResponse);
    }

    public void assumeIdentity(AppointmentObject newApp) {
        newApp.setObjectID( entry.getObjectID() );
        newApp.setParentFolderID( entry.getParentFolderID());
        newApp.setLastModified( entry.getLastModified());
    }

    public void rememberIdentityValues(AppointmentObject appointment) {
        entry.setLastModified(appointment.getLastModified());
    }

    public void forgetIdentity(AppointmentObject entry) {
        inserted = false;
    }
    
    public Class<AppointmentObject> getType() {
        return AppointmentObject.class;
    }
    
 
}
