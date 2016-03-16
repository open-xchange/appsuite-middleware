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

import static com.openexchange.groupware.calendar.TimeTools.D;
import java.util.Locale;
import junit.framework.TestCase;
import com.openexchange.groupware.contact.helpers.DefaultContactComparator;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.Order;

public class DefaultContactComparatorTest extends TestCase {

    private Contact c1;
    private Contact c2;

    @Override
    public void setUp() {
        c1 = new Contact();
        c2 = new Contact();

        c1.setObjectID(10);
        c2.setObjectID(11);
    }

    public void testIntegerField() {

        assertBigger(c2, c1, Contact.OBJECT_ID);
    }

    public void testStringField() {
        c1.setGivenName("Alpha");
        c2.setGivenName("Beta");

        assertBigger(c2, c1, Contact.GIVEN_NAME);
    }

    public void testDateField() {
        c1.setBirthday(D("03.03.1981 00:00"));
        c2.setBirthday(D("04.01.1981 00:00"));

        assertBigger(c1, c2, Contact.BIRTHDAY);
    }

    public void testDesc() {
        assertBigger(c1, c2, Contact.OBJECT_ID, Order.DESCENDING);
    }

    public void testSpecial() {

        c1.setGivenName("Anne123");
        c2.setSurName("Anne2000");

        assertBigger(c2, c1, Contact.SPECIAL_SORTING);
    }

    public void testUseCountGlobalFirst() {
        c1.setUseCount(0);
        c1.setParentFolderID(FolderObject.SYSTEM_LDAP_FOLDER_ID);

        c2.setUseCount(23);
        c2.setParentFolderID(23);

        assertBigger(c1, c2, Contact.USE_COUNT_GLOBAL_FIRST);
    }



    private void assertBigger(final Contact c1, final Contact c2, final int field) {
        assertBigger(c1, c2, field, Order.ASCENDING);
    }

    private void assertBigger(final Contact c1, final Contact c2, final int field, final Order order) {
        assertTrue("c1 was lower or equal than c2", 0 < new DefaultContactComparator(field, order, Locale.US).compare(c1, c2));
    }

}
