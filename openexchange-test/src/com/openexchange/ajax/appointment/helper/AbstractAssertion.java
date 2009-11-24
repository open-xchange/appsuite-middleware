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
import java.util.Calendar;
import java.util.List;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.tools.servlet.AjaxException;
import junit.framework.Assert;

/**
 * {@link AbstractAssertion}
 * 
 * @author <a href="mailto:firstname.lastname@open-xchange.com">Firstname Lastname</a>
 */
public class AbstractAssertion extends Assert {

    protected CalendarTestManager manager;

    public AbstractAssertion() {
        super();
    }

    protected Appointment find(List<Appointment> appointments, int folder, int id) {
        for (Appointment app : appointments)
            if (app.getParentFolderID() == folder && app.getObjectID() == id)
                return app;
        return null;
    }

    protected static Appointment generateDefaultAppointment(int folder) {
        Appointment app = generateDefaultAppointment();
        app.setParentFolderID(folder);
        return app;
    }

    protected static Appointment generateDefaultAppointment() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 1);

        Appointment app = new Appointment();
        app.setTitle("Generic recurrence test appointment");
        app.setStartDate(cal.getTime());
        cal.add(Calendar.HOUR, 1);
        app.setEndDate(cal.getTime());

        return app;
    }

    protected Appointment create(Appointment app) {
        return manager.insertAppointmentOnServer(app);
    }

    protected void update(Appointment app, Changes changes) {
        Appointment update = new Appointment();
        update.setParentFolderID(app.getParentFolderID());
        update.setObjectID(app.getObjectID());
        update.setLastModified(app.getLastModified());

        changes.update(update);
        manager.updateAppointmentOnServer(update);
    }

    protected int getPrivateAppointmentFolder() throws AjaxException, IOException, SAXException, JSONException {
        return manager.getClient().getValues().getPrivateAppointmentFolder();
    }

}
