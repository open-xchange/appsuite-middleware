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

package com.openexchange.ajax.share;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.ajax.share.bugs.Bug40369Test;
import com.openexchange.ajax.share.bugs.Bug40527Test;
import com.openexchange.ajax.share.bugs.Bug40548Test;
import com.openexchange.ajax.share.bugs.Bug40561Test;
import com.openexchange.ajax.share.bugs.Bug40596Test;
import com.openexchange.ajax.share.bugs.Bug40627Test;
import com.openexchange.ajax.share.bugs.Bug40722Test;
import com.openexchange.ajax.share.bugs.Bug40826Test;
import com.openexchange.ajax.share.bugs.Bug41184Test;
import com.openexchange.ajax.share.bugs.Bug41287Test;
import com.openexchange.ajax.share.bugs.Bug41537Test;
import com.openexchange.ajax.share.bugs.Bug41622Test;
import com.openexchange.ajax.share.bugs.Bug43270Test;
import com.openexchange.ajax.share.bugs.Bug44962Test;
import com.openexchange.ajax.share.bugs.Bug52843Test;
import com.openexchange.ajax.share.bugs.Bug58051Test;
import com.openexchange.ajax.share.bugs.Bug65805Test;
import com.openexchange.ajax.share.bugs.MWB933Test;
import com.openexchange.ajax.share.tests.AddGuestPermissionTest;
import com.openexchange.ajax.share.tests.AddGuestUserToGroupTest;
import com.openexchange.ajax.share.tests.AggregateSharesTest;
import com.openexchange.ajax.share.tests.AnonymousGuestFoldersTest;
import com.openexchange.ajax.share.tests.AnonymousGuestPasswordTest;
import com.openexchange.ajax.share.tests.AnonymousGuestTest;
import com.openexchange.ajax.share.tests.ConvertToInternalPermissionTest;
import com.openexchange.ajax.share.tests.CopySharedFilesPermissionRemovalTest;
import com.openexchange.ajax.share.tests.CopySharedFilesVersionsRemovalTest;
import com.openexchange.ajax.share.tests.CreateSubfolderTest;
import com.openexchange.ajax.share.tests.CreateWithGuestPermissionTest;
import com.openexchange.ajax.share.tests.DownloadHandlerTest;
import com.openexchange.ajax.share.tests.EmptyGuestPasswordTest;
import com.openexchange.ajax.share.tests.FileStorageTransactionTest;
import com.openexchange.ajax.share.tests.FolderItemCountTest;
import com.openexchange.ajax.share.tests.FolderTransactionTest;
import com.openexchange.ajax.share.tests.GetALinkTest;
import com.openexchange.ajax.share.tests.GetLinkInheritanceTest;
import com.openexchange.ajax.share.tests.GetLinkInheritanceWrongModuleTest;
import com.openexchange.ajax.share.tests.GuestAutologinTest;
import com.openexchange.ajax.share.tests.GuestContactTest;
import com.openexchange.ajax.share.tests.LinkUpdateTest;
import com.openexchange.ajax.share.tests.ListFileSharesTest;
import com.openexchange.ajax.share.tests.ListFolderSharesTest;
import com.openexchange.ajax.share.tests.LocalizedMessagesTest;
import com.openexchange.ajax.share.tests.LoginScreenTest;
import com.openexchange.ajax.share.tests.MailNotificationTest;
import com.openexchange.ajax.share.tests.NotifyFileSharesTest;
import com.openexchange.ajax.share.tests.NotifyFolderSharesTest;
import com.openexchange.ajax.share.tests.ParallelGuestSessionsTest;
import com.openexchange.ajax.share.tests.PasswordResetServletTest;
import com.openexchange.ajax.share.tests.QuotaTest;
import com.openexchange.ajax.share.tests.ResolveLegacyLinkTest;
import com.openexchange.ajax.share.tests.SharedFilesFolderTest;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * {@link ShareAJAXSuite}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    CreateWithGuestPermissionTest.class,
    AddGuestPermissionTest.class,
    CreateSubfolderTest.class,
    FolderTransactionTest.class,
    AggregateSharesTest.class,
    FileStorageTransactionTest.class,
    GuestContactTest.class,
    AnonymousGuestPasswordTest.class,
    GetALinkTest.class,
    GetLinkInheritanceTest.class,
    GetLinkInheritanceWrongModuleTest.class,
    ParallelGuestSessionsTest.class,
    QuotaTest.class,
    DownloadHandlerTest.class,
    MailNotificationTest.class,
    GuestAutologinTest.class,
    ConvertToInternalPermissionTest.class,
    EmptyGuestPasswordTest.class,
    LoginScreenTest.class,
    FolderItemCountTest.class,
    AnonymousGuestTest.class,
    LinkUpdateTest.class,
    ListFileSharesTest.class,
    ListFolderSharesTest.class,
    NotifyFolderSharesTest.class,
    NotifyFileSharesTest.class,
    SharedFilesFolderTest.class,
    CopySharedFilesPermissionRemovalTest.class,
    CopySharedFilesVersionsRemovalTest.class,
    Bug40369Test.class,
    Bug40548Test.class,
    Bug40596Test.class,
    Bug40627Test.class,
    Bug40561Test.class,
    Bug40527Test.class,
    Bug40722Test.class,
    Bug40826Test.class,
    AddGuestUserToGroupTest.class,
    PasswordResetServletTest.class,
    Bug41184Test.class,
    Bug41287Test.class,
    Bug41537Test.class,
    ResolveLegacyLinkTest.class,
    Bug41622Test.class,
    Bug43270Test.class,
    Bug44962Test.class,
    Bug52843Test.class,
    Bug58051Test.class,
    AnonymousGuestFoldersTest.class,
    Bug65805Test.class,
    LocalizedMessagesTest.class,
    MWB933Test.class,
})
public class ShareAJAXSuite  {
}
