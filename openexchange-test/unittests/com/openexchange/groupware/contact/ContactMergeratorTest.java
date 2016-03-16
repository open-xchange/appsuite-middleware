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

import java.util.LinkedList;
import java.util.List;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import junit.framework.TestCase;


/**
 * {@link ContactMergeratorTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ContactMergeratorTest extends TestCase {


    private SearchIterator<Contact> contacts(int...ids) {
        List<Contact> retval = new LinkedList<Contact>();
        for(int id : ids) {
            Contact contact = new Contact();
            contact.setObjectID(id);
            retval.add(contact);
        }
        return new SearchIteratorAdapter<Contact>(retval.iterator());
    }


    public void testSortIntegerColumn() throws Exception {
        SearchIterator<Contact> c1 = contactsWithIDs(11, 101, 1001);
        SearchIterator<Contact> c2 = contactsWithIDs(21,22,23);
        SearchIterator<Contact> c3 = contactsWithIDs(31,32,33);
        SearchIterator<Contact> c4 = contactsWithIDs();

        ContactMergerator merged = new ContactMergerator(new IDComparator(), c1, c2, c3, c4);
        assertIDs(merged, 11,21,22,23,31,32,33,101,1001);
    }

    private void assertIDs(ContactMergerator merged, int...ids) throws Exception {
        int index = 0;
        while(merged.hasNext()) {
            Contact c = merged.next();
            int currentId = ids[index++];
            assertEquals("Unexpected element at index: "+(index-1), currentId, c.getObjectID());
        }
        assertEquals(ids.length, index);
    }

    private SearchIterator<Contact> contactsWithIDs(int...ids) {
        List<Contact> retval = new LinkedList<Contact>();
        for(int id : ids) {
            Contact contact = new Contact();
            contact.setObjectID(id);
            retval.add(contact);
        }
        return new SearchIteratorAdapter<Contact>(retval.iterator());
    }
}
