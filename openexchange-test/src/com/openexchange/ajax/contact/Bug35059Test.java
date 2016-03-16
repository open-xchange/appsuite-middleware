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

package com.openexchange.ajax.contact;

import java.util.Comparator;
import java.util.UUID;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.java.util.UUIDs;

/**
 * {@link Bug35059Test}
 *
 * The sorting of the displayed birthdays in the birthday-widget seems to be wrong.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug35059Test extends AbstractManagedContactTest {

    public Bug35059Test(String name) {
        super(name);
    }

	@Override
	public void setUp() throws Exception {
	    super.setUp();
	}

    public void testSortOrder() throws Exception {
    	/*
    	 * create distribution list
    	 */
        DistributionListEntryObject[] distributionList = new DistributionListEntryObject[100];
        for (int i = 0; i < 100; i++) {
            DistributionListEntryObject member = new DistributionListEntryObject();
            member.setEmailaddress(UUIDs.getUnformattedString(UUID.randomUUID()) + "@example.com");
            if (0 == i % 2) {
                member.setDisplayname(UUIDs.getUnformattedString(UUID.randomUUID()));
            }
            distributionList[i] = member;
        }
        Contact contact = super.generateContact("List");
        contact.setDistributionList(distributionList);
        contact = manager.newAction(contact);
    	/*
    	 * get distribution list again
    	 */
        GetResponse response = client.execute(new GetRequest(contact, client.getValues().getTimeZone()));
        contact = response.getContact();
        DistributionListEntryObject[] list = contact.getDistributionList();
        assertNotNull("No distribution list", list);
        assertEquals("Wrong number of members", distributionList.length, list.length);
        /*
         * check sort order
         */
        Comparator<DistributionListEntryObject> comparator = new Comparator<DistributionListEntryObject>() {

            @Override
            public int compare(DistributionListEntryObject o1, DistributionListEntryObject o2) {
                String name1 = null == o1.getDisplayname() ? o1.getEmailaddress() : o1.getDisplayname() + o1.getEmailaddress();
                String name2 = null == o2.getDisplayname() ? o2.getEmailaddress() : o2.getDisplayname() + o2.getEmailaddress();
                return name1.compareTo(name2);
            }
        };
        for (int i = 0; i < list.length - 1; i++) {
            DistributionListEntryObject smallerMember = list[i];
            DistributionListEntryObject biggerMember = list[i + 1];
            assertTrue("Wrong sort order: " + smallerMember + " appears before " + biggerMember, 0 >= comparator.compare(smallerMember, biggerMember));
        }
    }

}
