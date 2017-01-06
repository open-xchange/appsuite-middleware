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

package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug30142Test}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug30142Test extends AbstractAJAXSession {

    private FolderObject folder;

    private Appointment appointment;

    public Bug30142Test() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        folder = ftm.generatePrivateFolder("Bug 30142 " + UUID.randomUUID().toString(), FolderObject.CALENDAR, getClient().getValues().getPrivateAppointmentFolder(), getClient().getValues().getUserId());
        ftm.insertFolderOnServer(folder);

        appointment = new Appointment();
        appointment.setTitle("Bug 30142");
        appointment.setStartDate(D("01.12.2013 08:00"));
        appointment.setEndDate(D("01.12.2013 09:00"));
        appointment.setParentFolderID(folder.getObjectID());
        appointment.setIgnoreConflicts(true);
        catm.insert(appointment);

    }

    @Test
    public void testBug30142() throws Exception {
        Appointment update = catm.createIdentifyingCopy(appointment);
        update.setCategories("Test");
        catm.update(update);

        Appointment loaded = catm.get(folder.getObjectID(), appointment.getObjectID());
        assertEquals("Missing category.", "Test", loaded.getCategories());
        assertEquals("Bad folder id.", loaded.getParentFolderID(), folder.getObjectID());
    }
}
