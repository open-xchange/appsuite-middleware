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

package com.openexchange.ajax.contact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Comparator;
import java.util.UUID;
import org.junit.Test;
import com.davekoelle.AlphanumComparator;
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

    public Bug35059Test() {
        super();
    }

    @Test
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
        contact = cotm.newAction(contact);
        /*
         * get distribution list again
         */
        GetResponse response = getClient().execute(new GetRequest(contact, getClient().getValues().getTimeZone()));
        contact = response.getContact();
        DistributionListEntryObject[] list = contact.getDistributionList();
        assertNotNull("No distribution list", list);
        assertEquals("Wrong number of members", distributionList.length, list.length);
        /*
         * check sort order
         */
        AlphanumComparator alphanumComparator = new AlphanumComparator(getClient().getValues().getLocale());
        Comparator<DistributionListEntryObject> comparator = new Comparator<DistributionListEntryObject>() {

            @Override
            public int compare(DistributionListEntryObject o1, DistributionListEntryObject o2) {
                Contact contact1 = new Contact();
                contact1.setDisplayName(o1.getDisplayname());
                contact1.setEmail1(o1.getEmailaddress());
                String sortName1 = contact1.getSortName();
                Contact contact2 = new Contact();
                contact2.setDisplayName(o2.getDisplayname());
                contact2.setEmail1(o2.getEmailaddress());
                String sortName2 = contact2.getSortName();
                return alphanumComparator.compare(sortName1, sortName2);
            }
        };
        for (int i = 0; i < list.length - 1; i++) {
            DistributionListEntryObject smallerMember = list[i];
            DistributionListEntryObject biggerMember = list[i + 1];
            assertTrue("Wrong sort order: " + smallerMember + " appears before " + biggerMember, 0 >= comparator.compare(smallerMember, biggerMember));
        }
    }

}
