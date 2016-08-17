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

package com.openexchange.ajax.share;

import com.openexchange.ajax.share.bugs.Bug40369Test;
import com.openexchange.ajax.share.bugs.Bug40527Test;
import com.openexchange.ajax.share.bugs.Bug40548Test;
import com.openexchange.ajax.share.bugs.Bug40561Test;
import com.openexchange.ajax.share.bugs.Bug40596Test;
import com.openexchange.ajax.share.bugs.Bug40627Test;
import com.openexchange.ajax.share.bugs.Bug40722Test;
import com.openexchange.ajax.share.bugs.Bug40826Test;
import com.openexchange.ajax.share.bugs.Bug40993Test;
import com.openexchange.ajax.share.bugs.Bug41184Test;
import com.openexchange.ajax.share.bugs.Bug41287Test;
import com.openexchange.ajax.share.bugs.Bug41537Test;
import com.openexchange.ajax.share.bugs.Bug41622Test;
import com.openexchange.ajax.share.bugs.Bug43270Test;
import com.openexchange.ajax.share.bugs.Bug44962Test;
import com.openexchange.ajax.share.tests.AddGuestPermissionTest;
import com.openexchange.ajax.share.tests.AddGuestUserToGroupTest;
import com.openexchange.ajax.share.tests.AggregateSharesTest;
import com.openexchange.ajax.share.tests.AnonymousGuestPasswordTest;
import com.openexchange.ajax.share.tests.AnonymousGuestTest;
import com.openexchange.ajax.share.tests.ConvertToInternalPermissionTest;
import com.openexchange.ajax.share.tests.CopySharedFilesPermissionRemovalTest;
import com.openexchange.ajax.share.tests.CopySharedFilesVersionsRemovalTest;
import com.openexchange.ajax.share.tests.CreateSubfolderTest;
import com.openexchange.ajax.share.tests.CreateWithGuestPermissionTest;
import com.openexchange.ajax.share.tests.DownloadHandlerTest;
import com.openexchange.ajax.share.tests.EmptyGuestPasswordTest;
import com.openexchange.ajax.share.tests.ExpiredSharesTest;
import com.openexchange.ajax.share.tests.FileStorageTransactionTest;
import com.openexchange.ajax.share.tests.FolderItemCountTest;
import com.openexchange.ajax.share.tests.FolderTransactionTest;
import com.openexchange.ajax.share.tests.GetALinkTest;
import com.openexchange.ajax.share.tests.GuestAutologinTest;
import com.openexchange.ajax.share.tests.GuestContactTest;
import com.openexchange.ajax.share.tests.GuestPasswordTest;
import com.openexchange.ajax.share.tests.LinkUpdateTest;
import com.openexchange.ajax.share.tests.ListFileSharesTest;
import com.openexchange.ajax.share.tests.ListFolderSharesTest;
import com.openexchange.ajax.share.tests.LoginScreenTest;
import com.openexchange.ajax.share.tests.MailNotificationTest;
import com.openexchange.ajax.share.tests.NotifyFileSharesTest;
import com.openexchange.ajax.share.tests.NotifyFolderSharesTest;
import com.openexchange.ajax.share.tests.ParallelGuestSessionsTest;
import com.openexchange.ajax.share.tests.PasswordResetServletTest;
import com.openexchange.ajax.share.tests.QuotaTest;
import com.openexchange.ajax.share.tests.RemoveGuestPermissionTest;
import com.openexchange.ajax.share.tests.ResolveLegacyLinkTest;
import com.openexchange.ajax.share.tests.SharedFilesFolderTest;
import com.openexchange.ajax.share.tests.ShowSharedFilesFolderTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * {@link ShareAJAXSuite}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ShareAJAXSuite extends TestSuite {

    /**
     * Gets all share tests in a suite.
     *
     * @return The test suite.
     */
    public static Test suite() {
        TestSuite tests = new TestSuite(ShareAJAXSuite.class.getName());
        tests.addTestSuite(CreateWithGuestPermissionTest.class);
        tests.addTestSuite(AddGuestPermissionTest.class);
        tests.addTestSuite(RemoveGuestPermissionTest.class);
        tests.addTestSuite(ExpiredSharesTest.class);
        tests.addTestSuite(CreateSubfolderTest.class);
        tests.addTestSuite(FolderTransactionTest.class);
        tests.addTestSuite(AggregateSharesTest.class);
        tests.addTestSuite(FileStorageTransactionTest.class);
        tests.addTestSuite(GuestContactTest.class);
        tests.addTestSuite(AnonymousGuestPasswordTest.class);
        tests.addTestSuite(GuestPasswordTest.class);
        tests.addTestSuite(GetALinkTest.class);
        tests.addTestSuite(ParallelGuestSessionsTest.class);
        tests.addTestSuite(QuotaTest.class);
        tests.addTestSuite(DownloadHandlerTest.class);
        tests.addTestSuite(MailNotificationTest.class);
        tests.addTestSuite(GuestAutologinTest.class);
        tests.addTestSuite(ConvertToInternalPermissionTest.class);
        tests.addTestSuite(EmptyGuestPasswordTest.class);
        tests.addTestSuite(LoginScreenTest.class);
        tests.addTestSuite(FolderItemCountTest.class);
        tests.addTestSuite(ShowSharedFilesFolderTest.class);
        tests.addTestSuite(AnonymousGuestTest.class);
        tests.addTestSuite(LinkUpdateTest.class);
        tests.addTestSuite(ListFileSharesTest.class);
        tests.addTestSuite(ListFolderSharesTest.class);
        tests.addTestSuite(NotifyFolderSharesTest.class);
        tests.addTestSuite(NotifyFileSharesTest.class);
        tests.addTestSuite(SharedFilesFolderTest.class);
        tests.addTestSuite(CopySharedFilesPermissionRemovalTest.class);
        tests.addTestSuite(CopySharedFilesVersionsRemovalTest.class);
        tests.addTestSuite(Bug40369Test.class);
        tests.addTestSuite(Bug40548Test.class);
        tests.addTestSuite(Bug40596Test.class);
        tests.addTestSuite(Bug40627Test.class);
        tests.addTestSuite(Bug40561Test.class);
        tests.addTestSuite(Bug40527Test.class);
        tests.addTestSuite(Bug40722Test.class);
        tests.addTestSuite(Bug40826Test.class);
        tests.addTestSuite(AddGuestUserToGroupTest.class);
        tests.addTestSuite(PasswordResetServletTest.class);
        tests.addTestSuite(Bug40993Test.class);
        tests.addTestSuite(Bug41184Test.class);
        tests.addTestSuite(Bug41287Test.class);
        tests.addTestSuite(Bug41537Test.class);
        tests.addTestSuite(ResolveLegacyLinkTest.class);
        tests.addTestSuite(Bug41622Test.class);
        tests.addTestSuite(Bug43270Test.class);
        tests.addTestSuite(Bug44962Test.class);
        return tests;
    }

}
