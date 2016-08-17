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

package com.openexchange.ajax.folder;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.openexchange.ajax.folder.api2.Bug15752Test;
import com.openexchange.ajax.folder.api2.Bug15980Test;
import com.openexchange.ajax.folder.api2.Bug15995Test;
import com.openexchange.ajax.folder.api2.Bug16163Test;
import com.openexchange.ajax.folder.api2.Bug16243Test;
import com.openexchange.ajax.folder.api2.Bug16284Test;
import com.openexchange.ajax.folder.api2.Bug16303Test;
import com.openexchange.ajax.folder.api2.Bug17225Test;
import com.openexchange.ajax.folder.api2.Bug17261Test;
import com.openexchange.ajax.folder.api2.Bug29853Test;
import com.openexchange.ajax.folder.api2.Bug44895Test;
import com.openexchange.ajax.folder.api2.ChangePermissionsTest;
import com.openexchange.ajax.folder.api2.ClearTest;
import com.openexchange.ajax.folder.api2.CreateTest;
import com.openexchange.ajax.folder.api2.GetTest;
import com.openexchange.ajax.folder.api2.MoveTest;
import com.openexchange.ajax.folder.api2.PathTest;
import com.openexchange.ajax.folder.api2.SubscribeTest;
import com.openexchange.ajax.folder.api2.UpdateTest;
import com.openexchange.ajax.folder.api2.UpdatesTest;
import com.openexchange.ajax.folder.api2.VisibleFoldersTest;
import com.openexchange.ajax.infostore.test.Bug37211Test;
import com.openexchange.ajax.infostore.test.InfostoreObjectCountTest;

/**
 * Suite for all folder tests.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class FolderTestSuite {

    /**
     * Prevent instantiation.
     */
    private FolderTestSuite() {
        super();
    }

    /**
     * Generates the task test suite.
     *
     * @return the task tests suite.
     */
    public static Test suite() {
        final TestSuite tests = new TestSuite("com.openexchange.ajax.folder.FolderTestSuite");
        // First the function tests.
        tests.addTestSuite(FunctionTests.class);
        tests.addTestSuite(com.openexchange.ajax.folder.ShareFolderTest.class);
        tests.addTestSuite(com.openexchange.ajax.folder.DeleteFolderTest.class);

        // Now several single function tests.
        tests.addTestSuite(GetMailInboxTest.class);
        tests.addTestSuite(GetVirtualTest.class);
        // tests.addTestSuite(GetSortedMailFolderTest.class);
        tests.addTestSuite(ExemplaryFolderTestManagerTest.class);

        // New folder API
        tests.addTestSuite(ClearTest.class);
        tests.addTestSuite(CreateTest.class);
        tests.addTestSuite(GetTest.class);
        tests.addTestSuite(ListTest.class);
        tests.addTestSuite(MoveTest.class);
        tests.addTestSuite(PathTest.class);
        tests.addTestSuite(UpdatesTest.class);
        tests.addTestSuite(UpdateTest.class);
        tests.addTestSuite(VisibleFoldersTest.class);
        tests.addTestSuite(SubscribeTest.class);
        tests.addTestSuite(DefaultMediaFoldersTest.class);

        // Test for object counts for database folder
        tests.addTest(new JUnit4TestAdapter(ContactObjectCountTest.class));
        tests.addTest(new JUnit4TestAdapter(TaskObjectCountTest.class));
        tests.addTest(new JUnit4TestAdapter(AppointmentObjectCountTest.class));
        tests.addTest(new JUnit4TestAdapter(InfostoreObjectCountTest.class));

        // EAS subscribe
        tests.addTestSuite(com.openexchange.ajax.folder.eas.SubscribeTest.class);
        tests.addTestSuite(com.openexchange.ajax.folder.eas.MultipleSubscribeTest.class);
        tests.addTestSuite(com.openexchange.ajax.folder.eas.MultipleSubscribeWithoutParentTest.class);

        // And finally bug tests.
        tests.addTestSuite(Bug12393Test.class);
        tests.addTestSuite(Bug16899Test.class);

        // New folder API bug tests
        tests.addTestSuite(Bug15752Test.class);
        tests.addTestSuite(Bug15995Test.class);
        tests.addTestSuite(Bug15980Test.class);
        tests.addTestSuite(Bug16163Test.class);
        tests.addTestSuite(Bug16243Test.class);
        tests.addTestSuite(Bug16284Test.class);
        tests.addTestSuite(Bug16303Test.class);
        tests.addTestSuite(Bug16724Test.class);
        tests.addTestSuite(Bug17027Test.class);
        tests.addTestSuite(Bug17225Test.class);
        tests.addTestSuite(Bug17261Test.class);
        tests.addTestSuite(Bug29853Test.class);
        tests.addTestSuite(ChangePermissionsTest.class);
        tests.addTestSuite(Bug37211Test.class);
        tests.addTestSuite(Bug44895Test.class);

        tests.addTestSuite(PermissionsCascadeTest.class);

        return tests;
    }
}
