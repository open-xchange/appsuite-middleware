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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
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
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * Suite for all folder tests.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    FunctionTests.class,
    com.openexchange.ajax.folder.ShareFolderTest.class,
    com.openexchange.ajax.folder.DeleteFolderTest.class,

    // Now several single function tests.
    GetMailInboxTest.class,
    GetVirtualTest.class,
    // GetSortedMailFolderTest.class,
    ExemplaryFolderTestManagerTest.class,

    // New folder API
    ClearTest.class,
    CreateTest.class,
    GetTest.class,
    ListTest.class,
    MoveTest.class,
    PathTest.class,
    UpdatesTest.class,
    UpdateTest.class,
    VisibleFoldersTest.class,
    SubscribeTest.class,
    DefaultMediaFoldersTest.class,

    // Test for object counts for database folder
    ContactObjectCountTest.class,
    TaskObjectCountTest.class,
    AppointmentObjectCountTest.class,
    InfostoreObjectCountTest.class,
                    // EAS subscribe
    com.openexchange.ajax.folder.eas.SubscribeTest.class,
    com.openexchange.ajax.folder.eas.MultipleSubscribeTest.class,
    com.openexchange.ajax.folder.eas.MultipleSubscribeWithoutParentTest.class,

    // And finally bug tests.
    Bug12393Test.class,
    Bug16899Test.class,

    // New folder API bug tests
    Bug15752Test.class,
    Bug15995Test.class,
    Bug15980Test.class,
    Bug16163Test.class,
    Bug16243Test.class,
    Bug16284Test.class,
    Bug16303Test.class,
    Bug16724Test.class,
    Bug17027Test.class,
    Bug17225Test.class,
    Bug17261Test.class,
    Bug29853Test.class,
    ChangePermissionsTest.class,
    Bug37211Test.class,
    Bug44895Test.class,
    PermissionsCascadeTest.class,

    PublicFolderMovePermissionTest.class

})
public final class FolderTestSuite {


}
