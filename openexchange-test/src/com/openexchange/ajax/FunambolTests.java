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

package com.openexchange.ajax;

import com.openexchange.ajax.appointment.CalendarTestManagerTest;
import com.openexchange.ajax.contact.BasicManagedContactTests;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.task.BasicManagedTaskTests;

/**
 * A summary of tests written to fulfill Funambol QA requirements. These tests do exist in the test system, yet they were not collected in
 * one suite before.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class FunambolTests extends AbstractAJAXSession {

    public FunambolTests(String name) {
        super(name);
    }

    public void testLoginAndLogout() {
        /*
         * Login Request: http://oxptftest.schlund.de/ajax/login?action=login Logout Request: http://192.168.0.76/ajax/login?
         * action=logout&session=887e15e1497fddf59e67ba0450a346e0
         */

        // not implemented: Every single test does log in and out
    }

    public void testGetAllContactsWithColumns() throws Exception {
        /*
         * Request: http://192.168.0.76/ajax/contacts? action=all&session=3b0b1d04035e8d1cfa19918228ca69b7&folder=38&columns=1%2C4%2C5% 2C20
         * -> request columns 1,4,5,20
         */

        BasicManagedContactTests test = new BasicManagedContactTests("funambol: all contacts");
        try {
            test.setUp();
            test.testGetAllContactsWithColumns();
        } finally {
            test.tearDown();
        }

    }

    public void testGetAllAppointmentsWithStartAndEndDate() throws Exception {
        /*
         * Request: http://oxptftest.schlund.de/ajax/calendar?
         * action=all&session=f9d94265fea7c0fc57a4f550269904c3&folder=256&start=10000000&end=252
         * 4608000000&columns=1%2C4%2C5%2C20%2C209%2C207
         */

        CalendarTestManagerTest test = new CalendarTestManagerTest("funambol: all appointments");
        try {
            test.setUp();
            test.testGetAllInFolder();
        } finally {
            test.tearDown();
        }
    }

    public void testGetAllTasksWithStartAndEndDate() throws Exception {
        /*
         * Request: http://192.168.0.76/ajax/tasks? action=all&session=0e7c0ac11129ae69ea4e6335ab7c3f67&folder=39&start=10000000&end=2524
         * 608000000&columns=1%2C4%2C5%2C20%2C209
         */
        //start and end dates are not specified in the HTTP EnumAPI.
        BasicManagedTaskTests test = new BasicManagedTaskTests("funambol: all tasks");
        try {
            test.setUp();
            test.testAll();
        } finally {
            test.tearDown();
        }
    }

    public void testGetContactUpdates() throws Exception {
        /*
         * Request: http://192.168.0.76/ajax/contacts? action=updates&session=e4533629a58e8de2d550ee0a0a78cd33&folder=38&timestamp=12061116
         * 47532&ignore=none&columns=1%2C4%2C5
         */
        BasicManagedContactTests test = new BasicManagedContactTests("funambol: updates for contact");
        try {
            test.setUp();
            test.testUpdateContactAndGetUpdates();
        } finally {
            test.tearDown();
        }
    }

    public void testGetAppointmentUpdates() throws Exception {
        /*
         * Request: http://192.168.0.76/ajax/calendar? action=updates&session=f75cd157f27e5b756c663e8121bd6ee7&folder=37&timestamp=12127611
         * 90221&ignore=none&columns=1%2C4%2C5%2C209% 2C207&start=10000000&end=2524608000000
         */

        CalendarTestManagerTest test = new CalendarTestManagerTest("funambol: updates for appointments");
        try {
            test.setUp();
            test.testUpdates();
        } finally {
            test.tearDown();
        }
    }

    public void testGetTaskUpdates() throws Exception {
        /*
         * Request: http://192.168.0.76/ajax/tasks? action=updates&session=cfbaa1d35d0aab24f0145f60b62c8302&folder=39&timestamp=12127617
         * 18843&ignore=none&columns=1%2C4%2C5%2C209 Notice that this request is very important; the Funambol platform requires all the
         * updated items since the time <time stamp>. This time is the start time of the last sync for this user. During the last sync the
         * Funambol platform stores the Ids of the items handled by the sync, in this way we can understand the items handled between the
         * last sync and the current sync session.
         */
        BasicManagedTaskTests test = new BasicManagedTaskTests("funambol: updates for tasks");
        try {
            test.setUp();
            test.testUpdateAndReceiveUpdates();
        } finally {
            test.tearDown();
        }
    }

    public void testGetSingleContact() throws Exception {
        /*
         * Request: http://192.168.0.76/ajax/contacts? action=get&session=092644bdf45610f622b6b631e74c0d17&folder=38&id=3301&columns=1 Note:
         * Parameter "column" is not used, 'get' always return the whole object.
         */

        BasicManagedContactTests test = new BasicManagedContactTests("funambol: add contact");
        try {
            test.setUp();
            test.testCreateAndGetContact();
        } finally {
            test.tearDown();
        }
    }

    public void testGetSingleAppointment() throws Exception {
        /*
         * Request: http://oxptftest.schlund.de/ajax/calendar? action=get&session=f9d94265fea7c0fc57a4f550269904c3&folder=256&id=1436
         */

        CalendarTestManagerTest test = new CalendarTestManagerTest("funambol: get appointment");
        try {
            test.setUp();
            test.testGet();
        } finally {
            test.tearDown();
        }

    }

    public void getSingleTask() throws Exception {
        /*
         * Request: http://192.168.0.76/ajax/tasks? action=get&session=cfbaa1d35d0aab24f0145f60b62c8302&folder=39&id=215&columns=1
         */
        //columns is a useless param here - get always return everything
        BasicManagedTaskTests test = new BasicManagedTaskTests("funambol: get task");
        try {
            test.setUp();
            test.testCreateAndGet();
        } finally {
            test.tearDown();
        }
    }

    public void testUpdateContact() throws Exception {
        /*
         * Request: http://192.168.0.76/ajax/contacts?action=update&session=092644bdf45610f622b6b631e74c0d17&id=3301&folder=38&timestamp=
         * 1212760797891
         */

        BasicManagedContactTests test = new BasicManagedContactTests("funambol: update contact");
        try {
            test.setUp();
            test.testUpdateContactAndGetUpdates();
        } finally {
            test.tearDown();
        }

    }

    public void testUpdateAppointment() throws Exception {
        /*
         * Request: http://oxptftest.schlund.de/ajax/calendar?
         * action=update&session=f9d94265fea7c0fc57a4f550269904c3&id=1436&folder=256&timestamp= 1206016523153
         */

        CalendarTestManagerTest test = new CalendarTestManagerTest("funambol: update appointment");
        try {
            test.setUp();
            test.testUpdate();
        } finally {
            test.tearDown();
        }

    }

    public void testUpdateTask() throws Exception {
        /*
         * Request: http://192.168.0.76/ajax/tasks? action=update&session=cfbaa1d35d0aab24f0145f60b62c8302&id=215&folder=39&timestamp=12
         * 12762011129 Following is a sample of a request body for the update calendar item: REQUEST BODY: {"alarm":
         * 15,"ignore_conflicts":true,"private_flag":false,"title":"testing","end_date":
         * 1196420400000,"start_date":1196416800000,"location":"","note":"","recurrence_type": 0,"full_time":false}
         */
        BasicManagedTaskTests test = new BasicManagedTaskTests("funambol: update tasks");
        try {
            test.setUp();
            test.testUpdateAndReceiveUpdates();
        } finally {
            test.tearDown();
        }
    }

    public void testAddContact() throws Exception {
        /*
         * Request: http://192.168.0.76/ajax/contacts? action=new&session=092644bdf45610f622b6b631e74c0d17
         */
        BasicManagedContactTests test = new BasicManagedContactTests("funambol: add contact");
        try {
            test.setUp();
            test.testCreateAndGetContact();
        } finally {
            test.tearDown();
        }

    }

    public void testAddAppointment() throws Exception {
        /*
         * Request: http://oxptftest.schlund.de/ajax/calendar? action=new&session=f3abe0fc28b004289575a4a01887ef6e
         */

        CalendarTestManagerTest test = new CalendarTestManagerTest("funambol: add appointment");
        try {
            test.setUp();
            test.testCreate();
        } finally {
            test.tearDown();
        }

    }

    public void testAddTask() throws Exception {
        /*
         * Request: http://192.168.0.76/ajax/tasks?action=new&session=cfbaa1d35d0aab24f0145f60b62c8302 Following is a sample request body
         * for an add calendar item: REQUEST BODY: {"alarm":
         * 15,"folder_id":"256","ignore_conflicts":true,"private_flag":false,"title":"1","end_date":
         * 1198054800000,"start_date":1198051200000,"location":"","note":"","recurrence_type": 0,"full_time":false}
         */
        BasicManagedTaskTests test = new BasicManagedTaskTests("funambol: add task");
        try {
            test.setUp();
            test.testCreateAndGet();
        } finally {
            test.tearDown();
        }

    }

    public void testDeleteContact() throws Exception {
        /*
         * Request: http://192.168.0.76/ajax/contacts? action=delete&session=887e15e1497fddf59e67ba0450a346e0&timestamp=1206111782257
         */
        BasicManagedContactTests test = new BasicManagedContactTests("funambol: delete contact");
        try {
            test.setUp();
            test.testDeleteContact();
        } finally {
            test.tearDown();
        }
    }

    public void testDeleteAppointment() throws Exception {
        /*
         * Request: http://192.168.0.76/ajax/calendar? action=delete&session=f75cd157f27e5b756c663e8121bd6ee7&timestamp=1212761477393
         */
        CalendarTestManagerTest test = new CalendarTestManagerTest("funambol:delete appointment");
        try {
            test.setUp();
            test.testRemove();
        } finally {
            test.tearDown();
        }

    }

    public void testDeleteTask() throws Exception {
        /*
         * Request: http://192.168.0.76/ajax/tasks? action=delete&session=cfbaa1d35d0aab24f0145f60b62c8302&timestamp=1212762011919 Following
         * is a sample request body for a delete calendar item: REQUEST BODY: {"folder":38,"id":293}
         */
        BasicManagedTaskTests test = new BasicManagedTaskTests("funambol: delete task");
        try {
            test.setUp();
            test.testCreateAndDelete();
        } finally {
            test.tearDown();
        }
    }
}
