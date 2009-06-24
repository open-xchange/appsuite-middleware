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

package com.openexchange.groupware.notify;

import java.net.InetAddress;
import java.net.UnknownHostException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.tasks.Task;


public class Bug7507Test extends ParticipantNotifyTest {

 // Bug 7507
    public void testGenerateLink() {
        final EmailableParticipant p = new EmailableParticipant(0, 0, 0, null, "", "", null, null, 0, 23, 0, null, false); //FolderId: 23
        final Task task = new Task();
        task.setObjectID(42);
        final Appointment appointment = new Appointment();
        appointment.setObjectID(43);
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException e) {
            fail("Don't know my hostname");
        }
    
    
        final TestLinkableState state = new TestLinkableState();
        state.setTemplateString("[hostname]");
        state.setModule(Types.TASK);
        String link = state.generateLink(task, p);
        assertEquals(hostname,link);
        
        state.setTemplateString("[module]");
        link = state.generateLink(task, p);
        assertEquals("task",link);
        
        state.setTemplateString("[object]");
        link = state.generateLink(task, p);
        assertEquals("42",link);
        
        state.setTemplateString("[folder]");
        link = state.generateLink(task, p);
        assertEquals("23",link);
        
        state.setModule(Types.APPOINTMENT);
        state.setTemplateString("[hostname]");
        link = state.generateLink(appointment, p);
        assertEquals(hostname,link);

        state.setTemplateString("[module]");
        link = state.generateLink(appointment, p);
        assertEquals("calendar",link);
        
        state.setTemplateString("[object]");
        link = state.generateLink(appointment, p);
        assertEquals("43",link);
        
        state.setTemplateString("[folder]");
        link = state.generateLink(appointment, p);
        assertEquals("23",link);
        
        state.setTemplateString("http://[hostname]/ox6/#m=[module]&i=[object]&f=[folder]");
        link = state.generateLink(appointment, p);
        assertEquals("http://"+hostname+"/ox6/#m=calendar&i=43&f=23",link);
        
        p.folderId = -1;
        appointment.setParentFolderID(25);
        state.setTemplateString("[folder]");
        link = state.generateLink(appointment, p);
        assertEquals("25",link);
        
        
        
    }
}
