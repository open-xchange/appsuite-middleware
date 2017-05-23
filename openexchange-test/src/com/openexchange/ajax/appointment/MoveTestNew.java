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

package com.openexchange.ajax.appointment;

import static com.openexchange.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.TestManager;

/**
 * {@link MoveTestNew} This test describes the current status of the calendar implementation. It does not cover any user stories or expected
 * behaviours. This is just to ensure, no unintended side effects occur during calendar changes. It's subject to change, if the behaviour
 * should change.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class MoveTestNew extends AbstractAppointmentTest {

    private AJAXClient clientC;

    private UserValues valuesC;

    private CalendarTestManager ctmB, ctmC;

    private FolderTestManager ftmB, ftmC;

    private FolderObject folderA, folderA1, folderB, folderB1, folderB2, folderC, folderC1;

    private Set<TestManager> tm = new HashSet<TestManager>();

    private int idB, idC;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        clientC = new AJAXClient(testContext.acquireUser());

        valuesC = clientC.getValues();

        idB = getClient2().getValues().getUserId();
        idC = valuesC.getUserId();

        ctmB = new CalendarTestManager(getClient2());
        ctmC = new CalendarTestManager(clientC);

        tm.add(ctmB);
        tm.add(ctmC);

        ftmB = new FolderTestManager(getClient2());
        ftmC = new FolderTestManager(clientC);

        tm.add(ftmB);
        tm.add(ftmC);

        for (TestManager manager : tm) {
            manager.setFailOnError(true);
        }

        folderA = ftm.getFolderFromServer(getClient().getValues().getPrivateAppointmentFolder());
        folderA1 = createPrivateFolder("SubfolderA1" + UUID.randomUUID().toString(), ftm, getClient());
        folderA1 = ftm.insertFolderOnServer(folderA1);

        // 
        folderB = ftmB.getFolderFromServer(getClient2().getValues().getPrivateAppointmentFolder());
        addAuthorPermissions(folderB, getClient().getValues().getUserId(), ftmB);
        folderB1 = createPrivateFolder("SubfolderB1" + UUID.randomUUID().toString(), ftmB, getClient2(), getClient());
        folderB1 = ftmB.insertFolderOnServer(folderB1);
        folderB2 = createPrivateFolder("SubfolderB2" + UUID.randomUUID().toString(), ftmB, getClient2(), getClient());
        folderB2 = ftmB.insertFolderOnServer(folderB2);

        folderC = ftmC.getFolderFromServer(valuesC.getPrivateAppointmentFolder());
        addAuthorPermissions(folderC, getClient().getValues().getUserId(), ftmC);
        folderC1 = createPrivateFolder("SubfolderC1" + UUID.randomUUID().toString(), ftmC, clientC, getClient());
        folderC1 = ftmC.insertFolderOnServer(folderC1);
    }

    private void addAuthorPermissions(FolderObject folder, int userId, FolderTestManager actor) {
        OCLPermission authorPermissions = getAuthorPermissions(userId, folder.getObjectID());
        List<OCLPermission> newPermissions = new ArrayList<OCLPermission>();
        for (OCLPermission ocl : folder.getPermissions()) {
            if (ocl.getEntity() != userId) {
                newPermissions.add(ocl);
            }
        }
        newPermissions.add(authorPermissions);
        FolderObject folderUpdate = new FolderObject(folder.getObjectID());
        folderUpdate.setPermissions(newPermissions);
        folderUpdate.setLastModified(new Date(Long.MAX_VALUE));
        actor.updateFolderOnServer(folderUpdate);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            for (TestManager manager : tm) {
                manager.cleanUp();
            }
            
            if(null != clientC) {
                clientC.logout();
                clientC = null;
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testOwnPrivateToSubfolder() throws Exception {
        Appointment app = generateAppointment("testOwnPrivateToSubfolder", folderA);
        catm.insert(app);
        Appointment loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", getClient().getValues().getUserId(), loaded.getParticipants()[0].getIdentifier());

        move(app, folderA, folderA1, catm);
        loaded = get(app, folderA1, catm);
        assertEquals("Wrong folder id.", folderA1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", getClient().getValues().getUserId(), loaded.getParticipants()[0].getIdentifier());
    }

    @Test
    public void testOwnPrivateToSubfolderWithParticipants() throws Exception {
        Appointment app = generateAppointment("testOwnPrivateToSubfolderWithParticipants", folderA, getClient().getValues().getUserId(), idB, idC);
        catm.insert(app);
        Appointment loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB, ctmB);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        move(app, folderA, folderA1, catm);
        loaded = get(app, folderA1, catm);
        assertEquals("Wrong folder id.", folderA1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB, ctmB);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);
    }

    @Test
    public void testOwnPrivateToOtherPrivate() throws Exception {
        Appointment app = generateAppointment("testOwnPrivateToOtherPrivate", folderA);
        catm.insert(app);
        Appointment loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", getClient().getValues().getUserId(), loaded.getParticipants()[0].getIdentifier());

        move(app, folderA, folderB, catm);
        loaded = get(app, folderB, ctmB);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", idB, loaded.getParticipants()[0].getIdentifier());
    }

    @Test
    public void testOwnPrivateToOtherPrivateWithParticipants() throws Exception {
        Appointment app = generateAppointment("testOwnPrivateToOtherPrivateWithParticipants", folderA, getClient().getValues().getUserId(), idB, idC);
        catm.insert(app);
        Appointment loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB, ctmB);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        move(app, folderA, folderB, catm);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB, ctmB);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);
    }

    @Test
    public void testOwnPrivateToOtherSubfolder() throws Exception {
        Appointment app = generateAppointment("testOwnPrivateToOtherSubfolder", folderA);
        catm.insert(app);
        Appointment loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", getClient().getValues().getUserId(), loaded.getParticipants()[0].getIdentifier());

        move(app, folderA, folderB1, catm);

        loaded = get(app, folderB1, ctmB);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", idB, loaded.getParticipants()[0].getIdentifier());
    }

    @Test
    public void testOwnPrivateToOtherSubfolderWithParticipants() throws Exception {
        Appointment app = generateAppointment("testOwnPrivateToOtherSubfolderWithParticipants", folderA, getClient().getValues().getUserId(), idB, idC);
        catm.insert(app);
        Appointment loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB, ctmB);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        move(app, folderA, folderB1, catm);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB1, ctmB);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);
    }

    @Test
    public void testOtherPrivateToOwnPrivate() throws Exception {
        Appointment app = generateAppointment("testOtherPrivateToOwnPrivate", folderB);
        catm.insert(app);
        Appointment loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", idB, loaded.getParticipants()[0].getIdentifier());

        move(app, folderB, folderA, catm);
        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);

        loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);
    }

    @Test
    public void testOtherPrivateToOwnPrivateWithParticipants() throws Exception {
        Appointment app = generateAppointment("testOtherPrivateToOwnPrivateWithParticipants", folderB, getClient().getValues().getUserId(), idB, idC);
        catm.insert(app);
        Appointment loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        move(app, folderB, folderA, catm);
        loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);
    }

    @Test
    public void testOtherPrivateToOtherSubfolder() throws Exception {
        Appointment app = generateAppointment("testOtherPrivateToOtherSubfolder", folderB, idB);
        catm.insert(app);
        Appointment loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", idB, loaded.getParticipants()[0].getIdentifier());

        move(app, folderB, folderB1, catm);
        loaded = get(app, folderB1, catm);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", idB, loaded.getParticipants()[0].getIdentifier());
    }

    @Test
    public void testOtherPrivateToOtherSubfolderWithParticipants() throws Exception {
        Appointment app = generateAppointment("testOtherPrivateToOtherSubfolderWithParticipants", folderB, getClient().getValues().getUserId(), idB, idC);
        catm.insert(app);
        Appointment loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB, ctmB);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        move(app, folderB, folderB1, catm);
        loaded = get(app, folderB1, catm);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB1, ctmB);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

    }

    @Test
    public void testOtherPrivateToThirdPartyPrivateWithParticipants() throws Exception {
        Appointment app = generateAppointment("testOtherPrivateToOtherSubfolder", folderB, getClient().getValues().getUserId(), idB, idC);
        catm.insert(app);
        Appointment loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB, ctmB);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        move(app, folderB, folderC, catm);
        loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB, ctmB);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);
    }

    @Test
    public void testOtherSubfolderToOwnPrivate() throws Exception {
        Appointment app = generateAppointment("testOtherSubfolderToOwnPrivate", folderB1);
        catm.insert(app);
        Appointment loaded = get(app, folderB1, catm);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", idB, loaded.getParticipants()[0].getIdentifier());

        move(app, folderB1, folderA, catm);
        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);

        loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);
    }

    @Test
    public void testOtherSubfolderToOwnSubfolder() throws Exception {
        Appointment app = generateAppointment("testOtherSubfolderToOwnPrivate", folderB1);
        catm.insert(app);
        Appointment loaded = get(app, folderB1, catm);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", idB, loaded.getParticipants()[0].getIdentifier());

        move(app, folderB1, folderA1, catm);
        loaded = get(app, folderA1, catm);
        assertEquals("Wrong folder id.", folderA1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);

        loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);
    }

    @Test
    public void testOtherSubfolderToOwnPrivateWithParticipants() throws Exception {
        Appointment app = generateAppointment("testOtherPrivateToOwnPrivateWithParticipants", folderB1, getClient().getValues().getUserId(), idB, idC);
        catm.insert(app);
        Appointment loaded = get(app, folderB1, catm);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        move(app, folderB1, folderA, catm);
        loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);
    }

    @Test
    public void testOtherSubfolderToOwnSubfolderWithParticipants() throws Exception {
        Appointment app = generateAppointment("testOtherPrivateToOwnPrivateWithParticipants", folderB1, getClient().getValues().getUserId(), idB, idC);
        catm.insert(app);
        Appointment loaded = get(app, folderB1, catm);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        move(app, folderB1, folderA1, catm);
        loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderA1, catm);
        assertEquals("Wrong folder id.", folderA1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);
    }

    @Test
    public void testOtherSubfolderToOtherSubfolder() throws Exception {
        Appointment app = generateAppointment("testOtherSubfolderToOwnPrivate", folderB1);
        catm.insert(app);
        Appointment loaded = get(app, folderB1, catm);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", idB, loaded.getParticipants()[0].getIdentifier());

        loaded = get(app, folderB1, ctmB);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", idB, loaded.getParticipants()[0].getIdentifier());

        move(app, folderB1, folderB2, catm);
        loaded = get(app, folderB2, catm);
        assertEquals("Wrong folder id.", folderB2.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", idB, loaded.getParticipants()[0].getIdentifier());

        loaded = get(app, folderB2, ctmB);
        assertEquals("Wrong folder id.", folderB2.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", idB, loaded.getParticipants()[0].getIdentifier());
    }

    @Test
    public void testOtherSubfolderToOtherSubfolderWithParticipants() throws Exception {
        Appointment app = generateAppointment("testOtherSubfolderToOtherSubfolderWithParticipants", folderB1, getClient().getValues().getUserId(), idB, idC);
        catm.insert(app);
        Appointment loaded = get(app, folderB1, catm);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB1, ctmB);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        move(app, folderB1, folderB2, catm);
        loaded = get(app, folderB2, catm);
        assertEquals("Wrong folder id.", folderB2.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB2, ctmB);
        assertEquals("Wrong folder id.", folderB2.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);
    }

    /*
     * The following tests are not yet fully supported.
     */

    // TODO: Fix!
    public void _testOtherPrivateToThirdPartySubfolder() throws Exception {
        Appointment app = generateAppointment("testOtherPrivateToThirdPartySubfolder", folderB, idB);
        catm.insert(app);
        Appointment loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", idB, loaded.getParticipants()[0].getIdentifier());

        move(app, folderB, folderC1, catm);
        loaded = get(app, folderC1, catm);
        assertEquals("Wrong folder id.", folderC1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);

        loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);
    }

    // TODO: Fix!
    @Test
    public void testOtherSubfolderToThirdPartySubfolder() throws Exception {
        Appointment app = generateAppointment("testOtherSubfolderToThirdPartySubfolder", folderB1);
        catm.insert(app);
        Appointment loaded = get(app, folderB1, catm);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", idB, loaded.getParticipants()[0].getIdentifier());

        loaded = get(app, folderB1, ctmB);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", idB, loaded.getParticipants()[0].getIdentifier());

        move(app, folderB1, folderC1, catm);
        loaded = get(app, folderC1, catm);
        assertEquals("Wrong folder id.", folderC1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);

        loaded = get(app, folderC1, ctmC);
        assertEquals("Wrong folder id.", folderC1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);

        //        loaded = get(app, folderB, ctmB);
        //        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        //        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);

        loaded = get(app, folderB1, ctmB);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);
    }

    // TODO: Fix!
    @Test
    public void testOtherPrivateToThirdPartySubfolderWithParticipants() throws Exception {
        Appointment app = generateAppointment("testOtherPrivateToThirdPartySubfolderWithParticipants", folderB, getClient().getValues().getUserId(), idB, idC);
        catm.insert(app);
        Appointment loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB, ctmB);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        move(app, folderB, folderC1, catm);
        loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB, ctmB);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        //        loaded = get(app, folderC1, ctmC);
        //        assertEquals("Wrong folder id.", folderC1.getObjectID(), loaded.getParentFolderID());
        //        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);
    }

    // TODO: Fix!
    @Test
    public void testOtherSubfolderToThirdPartyPrivate() throws Exception {
        Appointment app = generateAppointment("testOtherSubfolderToThirdPartyPrivate", folderB1);
        catm.insert(app);
        Appointment loaded = get(app, folderB1, catm);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", idB, loaded.getParticipants()[0].getIdentifier());

        loaded = get(app, folderB1, ctmB);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", idB, loaded.getParticipants()[0].getIdentifier());

        move(app, folderB1, folderC, catm);
        loaded = get(app, folderC, catm);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);

        //        loaded = get(app, folderB, ctmB);
        //        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        //        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);

        loaded = get(app, folderB1, ctmB);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);
    }

    // TODO: Fix!
    public void _testOtherPrivateToThirdPartyPrivate() throws Exception {
        Appointment app = generateAppointment("testOtherPrivateToOtherSubfolder", folderB, idB);
        catm.insert(app);
        Appointment loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 1, loaded.getParticipants().length);
        assertEquals("Wrong participant.", idB, loaded.getParticipants()[0].getIdentifier());

        move(app, folderB, folderC, catm);
        loaded = get(app, folderC, catm);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);

        loaded = get(app, folderB, catm);
        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 2, loaded.getParticipants().length);
    }

    // TODO: Fix!
    @Test
    public void testOtherSubfolderToThirdPartySubfolderWithParticipants() throws Exception {
        Appointment app = generateAppointment("testOtherPrivateToOtherSubfolder", folderB1, getClient().getValues().getUserId(), idB, idC);
        catm.insert(app);
        Appointment loaded = get(app, folderB1, catm);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB1, ctmB);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        move(app, folderB1, folderC1, catm);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        //        loaded = get(app, folderC1, ctmC);
        //        assertEquals("Wrong folder id.", folderC1.getObjectID(), loaded.getParentFolderID());
        //        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        //        loaded = get(app, folderB, catm);
        //        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        //        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB1, catm);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        //        loaded = get(app, folderB, ctmB);
        //        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        //        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB1, ctmB);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);
    }

    // TODO: Fix!
    @Test
    public void testOtherSubfolderToThirdPartyPrivateWithParticipants() throws Exception {
        Appointment app = generateAppointment("testOtherSubfolderToThirdPartyPrivateWithParticipants", folderB1, getClient().getValues().getUserId(), idB, idC);
        catm.insert(app);
        Appointment loaded = get(app, folderB1, catm);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB1, ctmB);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        move(app, folderB1, folderC, catm);
        loaded = get(app, folderC, ctmC);
        assertEquals("Wrong folder id.", folderC.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderA, catm);
        assertEquals("Wrong folder id.", folderA.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        //        loaded = get(app, folderB, ctmB);
        //        assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        //        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB1, ctmB);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        //      loaded = get(app, folderB, catm);
        //      assertEquals("Wrong folder id.", folderB.getObjectID(), loaded.getParentFolderID());
        //      assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);

        loaded = get(app, folderB1, catm);
        assertEquals("Wrong folder id.", folderB1.getObjectID(), loaded.getParentFolderID());
        assertEquals("Wrong amount of participants.", 3, loaded.getParticipants().length);
    }

    private Appointment get(Appointment app, FolderObject inFolder, CalendarTestManager actor) throws Exception {
        return actor.get(inFolder.getObjectID(), app.getObjectID(), true);
    }

    private void move(Appointment app, FolderObject from, FolderObject to, CalendarTestManager ctm) {
        app.setParentFolderID(to.getObjectID());
        ctm.update(from.getObjectID(), app);
    }

    private FolderObject createPrivateFolder(String name, FolderTestManager ftm, AJAXClient... client) throws Exception {
        FolderObject folder = ftm.generatePrivateFolder(name, FolderObject.CALENDAR, client[0].getValues().getPrivateAppointmentFolder(), client[0].getValues().getUserId());

        if (client.length > 1) {
            for (int i = 1; i < client.length; i++) {
                folder.addPermission(getAuthorPermissions(client[i].getValues().getUserId(), folder.getObjectID()));
            }
        }

        return folder;
    }

    private OCLPermission getAuthorPermissions(int userId, int folderId) {
        OCLPermission permissions = new OCLPermission();
        permissions.setEntity(userId);
        permissions.setGroupPermission(false);
        permissions.setFolderAdmin(false);
        permissions.setFuid(folderId);
        permissions.setAllPermission(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);

        return permissions;
    }

    private Appointment generateAppointment(String title, FolderObject folder, int... userIds) {
        Appointment retval = new Appointment();
        retval.setTitle(title);
        retval.setStartDate(D("01.09.2014 08:00"));
        retval.setEndDate(D("01.09.2014 09:00"));
        retval.setParentFolderID(folder.getObjectID());
        retval.setIgnoreConflicts(true);
        if (userIds != null && userIds.length > 0) {
            for (int userId : userIds) {
                retval.addParticipant(new UserParticipant(userId));
            }
        }

        return retval;
    }
}
