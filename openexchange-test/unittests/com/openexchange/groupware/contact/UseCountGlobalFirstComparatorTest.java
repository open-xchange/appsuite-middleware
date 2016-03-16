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

package com.openexchange.groupware.contact;

import com.openexchange.groupware.contact.helpers.UseCountGlobalFirstComparator;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import junit.framework.TestCase;


/**
 * {@link UseCountGlobalFirstComparatorTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class UseCountGlobalFirstComparatorTest extends TestCase {

    public void testGlobalUserFolderBeforeRegularFolder() {
        Contact inGlobalFolder = new Contact();
        inGlobalFolder.setObjectID(1);
        inGlobalFolder.setParentFolderID(FolderObject.SYSTEM_LDAP_FOLDER_ID);
        inGlobalFolder.setUseCount(0);

        Contact notInGlobalFolder = new Contact();
        notInGlobalFolder.setObjectID(2);
        notInGlobalFolder.setParentFolderID(23);
        notInGlobalFolder.setUseCount(200000);

        assertBigger(inGlobalFolder, notInGlobalFolder);
    }

    public void testGlobalUserFoldersByUseCount() {
        Contact lowUseCount = new Contact();
        lowUseCount.setObjectID(1);
        lowUseCount.setParentFolderID(FolderObject.SYSTEM_LDAP_FOLDER_ID);
        lowUseCount.setUseCount(0);

        Contact highUseCount = new Contact();
        highUseCount.setObjectID(2);
        highUseCount.setParentFolderID(FolderObject.SYSTEM_LDAP_FOLDER_ID);
        highUseCount.setUseCount(200000);

        assertBigger(highUseCount, lowUseCount);
    }

    public void testRegularFoldersByUseCount() {
        Contact lowUseCount = new Contact();
        lowUseCount.setObjectID(1);
        lowUseCount.setParentFolderID(23);
        lowUseCount.setUseCount(0);

        Contact highUseCount = new Contact();
        highUseCount.setObjectID(2);
        highUseCount.setParentFolderID(23);
        highUseCount.setUseCount(200000);

        assertBigger(highUseCount, lowUseCount);
    }


    private void assertBigger(Contact c1, Contact c2) {
        assertTrue("c1 was lower or equal than c2", 0 < new UseCountGlobalFirstComparator().compare(c1, c2));
    }
}
