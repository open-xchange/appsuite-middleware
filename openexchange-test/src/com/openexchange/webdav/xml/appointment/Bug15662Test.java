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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.webdav.xml.appointment;

import java.io.IOException;
import java.util.Date;
import org.xml.sax.SAXException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.webdav.xml.AppointmentTest;


/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class Bug15662Test extends AppointmentTest {

    private Appointment appointment;
    private Date now;
    private Date inAnHour;
    
    public Bug15662Test(String name) {
        super(name);
    }

    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        appointment = new Appointment();
        now = new Date();
        inAnHour = new Date( now.getTime() + 1000 * 60 * 60);
        appointment.setStartDate( now );
        appointment.setEndDate( inAnHour );
        appointment.setParentFolderID(appointmentFolderId);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if(appointment != null)
            deleteAppointment(getWebConversation(), appointment.getObjectID(), appointment.getParentFolderID(), getHostName(), getLogin(), getPassword());
            
    }


    
    
    public void testReserved() throws Exception{
        checkFreeBusy(appointment, Appointment.RESERVED);        
    }


    public void testTemporary() throws Exception{
        checkFreeBusy(appointment, Appointment.TEMPORARY);   
    }

    public void testAbsentOnBusiness() throws Exception{
        checkFreeBusy(appointment, Appointment.ABSENT);
    }
    
    public void testFree() throws Exception{
        checkFreeBusy(appointment, Appointment.FREE);
    }


    private void checkFreeBusy(Appointment app, int expectedState) throws OXException, Exception{
        app.setShownAs( expectedState );
        insertAppointment();
        int actualState = getFreeBusyState(app);
        assertEquals("Wrong free/busy state", expectedState, actualState);
    }
    
    private void insertAppointment() throws OXException, Exception {
        int objectId = insertAppointment(getWebConversation(), appointment , PROTOCOL + getHostName(), getLogin(), getPassword());
        appointment.setObjectID(objectId);
    }
    
    private int getFreeBusyState(Appointment app) throws IOException, SAXException {
        Date start = new Date(now.getTime() - 10);
        Date end = new Date(inAnHour.getTime() + 10);
        return getFreeBusyState(secondWebCon, "1337", getLogin(), "devel-mail.netline.de", start, end);
    }
}
