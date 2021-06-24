/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.ajax.folder.api2.MWB1030Test;
import com.openexchange.ajax.folder.api2.MoveTest;
import com.openexchange.ajax.folder.api2.PathTest;
import com.openexchange.ajax.folder.api2.SubscribeTest;
import com.openexchange.ajax.folder.api2.UpdateTest;
import com.openexchange.ajax.folder.api2.UpdatesTest;
import com.openexchange.ajax.folder.api2.VisibleFoldersTest;
import com.openexchange.ajax.folder.api_client.MailFolderCountTest;
import com.openexchange.ajax.folder.api_client.PermissionLimitTest;
import com.openexchange.ajax.infostore.test.Bug37211Test;
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
    Bug17027Test.class,
    Bug17225Test.class,
    Bug17261Test.class,
    Bug29853Test.class,
    ChangePermissionsTest.class,
    Bug37211Test.class,
    Bug44895Test.class,
    PermissionsCascadeTest.class,

    PublicFolderMovePermissionTest.class,

    // API Client tests
    MailFolderCountTest.class,
    PermissionLimitTest.class,
    CheckLimitsTest.class,
    SubscribeTest.class,

    // Merge/inherit parent folder permissions on move tests
    InheritPermissionOnMoveTest.class,
    MergePermissionOnMoveTest.class,

    // Warnings tests for move folders 
    InheritWarningPermissionOnMoveTest.class,
    MergeWarningPermissionOnMoveTest.class,

    MWB682Test.class,
    MWB905Test.class,
    MWB1030Test.class, 
    MWB1119Test.class

})
public final class FolderTestSuite {


}
