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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import junit.framework.Test;
import junit.framework.TestSuite;
import com.openexchange.ajax.share.tests.AddGuestPermissionTest;
import com.openexchange.ajax.share.tests.AggregateSharesTest;
import com.openexchange.ajax.share.tests.AllTest;
import com.openexchange.ajax.share.tests.AnonymousGuestPasswordTest;
import com.openexchange.ajax.share.tests.CreateSubfolderTest;
import com.openexchange.ajax.share.tests.CreateWithGuestPermissionTest;
import com.openexchange.ajax.share.tests.DeleteTest;
import com.openexchange.ajax.share.tests.DownloadHandlerTest;
import com.openexchange.ajax.share.tests.ExpiredSharesTest;
import com.openexchange.ajax.share.tests.FileStorageTransactionTest;
import com.openexchange.ajax.share.tests.FolderTransactionTest;
import com.openexchange.ajax.share.tests.GetALinkTest;
import com.openexchange.ajax.share.tests.GuestContactTest;
import com.openexchange.ajax.share.tests.GuestPasswordTest;
import com.openexchange.ajax.share.tests.InviteTest;
import com.openexchange.ajax.share.tests.ParallelGuestSessionsTest;
import com.openexchange.ajax.share.tests.QuotaTest;
import com.openexchange.ajax.share.tests.RemoveGuestPermissionTest;

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
        tests.addTestSuite(AllTest.class);
        tests.addTestSuite(DeleteTest.class);
        tests.addTestSuite(FolderTransactionTest.class);
        tests.addTestSuite(AggregateSharesTest.class);
        tests.addTestSuite(InviteTest.class);
        tests.addTestSuite(FileStorageTransactionTest.class);
        tests.addTestSuite(GuestContactTest.class);
        tests.addTestSuite(AnonymousGuestPasswordTest.class);
        tests.addTestSuite(GuestPasswordTest.class);
        tests.addTestSuite(GetALinkTest.class);
        tests.addTestSuite(ParallelGuestSessionsTest.class);
        tests.addTestSuite(QuotaTest.class);
        tests.addTestSuite(DownloadHandlerTest.class);
        return tests;
    }

}
