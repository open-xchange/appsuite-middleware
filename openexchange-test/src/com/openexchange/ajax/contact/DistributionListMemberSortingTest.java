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

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ContactData;
import com.openexchange.testing.httpclient.models.ContactResponse;
import com.openexchange.testing.httpclient.models.DistributionListMember;
import com.openexchange.testing.httpclient.models.DistributionListMember.MailFieldEnum;

/**
 * {@link DistributionListMemberSortingTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class DistributionListMemberSortingTest extends AbstractApiClientContactTest {

    private ContactData contactObj;
    private String conId;
    private String conAID;
    private String conBID;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ContactData contact2 = createContactObject("Contact");
        contact2.setFirstName("BBB");
        contact2.setLastName("BBB");
        conBID = createContact(contact2);
        contact2.setId(conBID);

        ContactData contact1 = createContactObject("Contact");
        contact1.setFirstName("AAA");
        contact1.setLastName("AAA");
        conAID = createContact(contact1);
        contact1.setId(conAID);

        contactObj = new ContactData();
        contactObj.setDisplayName("DistributionList");
        contactObj.setMarkAsDistributionlist(Boolean.TRUE);
        List<DistributionListMember> members = new ArrayList<>();
        members.add(getMemberFromContact(contact1));
        members.add(getMemberFromContact(contact2));
        contactObj.setDistributionList(members);
        contactObj.setFolderId(contactFolderId);
        conId = createContact(contactObj);
    }

    private DistributionListMember getMemberFromContact(ContactData con) {
        DistributionListMember result = new DistributionListMember();
        result.setDisplayName(con.getDisplayName());
        result.setFolderId(con.getFolderId());
        result.setId(con.getId());
        result.setMail(con.getEmail1());
        result.setMailField(MailFieldEnum.NUMBER_1);
        return result;
    }

    @Test
    public void testSorting() throws ApiException {
        List<DistributionListMember> distributionList = getDisList();

        Assert.assertEquals(conAID, distributionList.get(0).getId());
        Assert.assertNotNull("Sortname must not be null!", distributionList.get(0).getSortName());
        Assert.assertEquals(conBID, distributionList.get(1).getId());
        Assert.assertNotNull("Sortname must not be null!", distributionList.get(1).getSortName());
    }

    @Test
    public void testUpdatesDisplayname() throws Exception {
        List<DistributionListMember> distributionList = getDisList();

        DistributionListMember distributionListMemberA = distributionList.get(0);
        Assert.assertEquals(conAID, distributionListMemberA.getId());
        Assert.assertNotNull("Sortname must not be null!", distributionList.get(0).getSortName());
        Assert.assertEquals(conBID, distributionList.get(1).getId());
        Assert.assertNotNull("Sortname must not be null!", distributionList.get(1).getSortName());

        /*
         * Update contact display name, expect new display name in distribution list
         */
        ContactData delta = new ContactData();
        String displayName = "TotallyAwesomeNew Name";
        delta.setId(distributionListMemberA.getId());
        delta.setDisplayName(displayName);
        updateContact(delta, contactFolderId);
        distributionList = getDisList();

        distributionListMemberA = distributionList.get(0);
        Assert.assertEquals(conAID, distributionListMemberA.getId());
        Assert.assertEquals(displayName, distributionListMemberA.getDisplayName());
        Assert.assertNotNull("Sortname must not be null!", distributionList.get(0).getSortName());
        Assert.assertEquals(conBID, distributionList.get(1).getId());
        Assert.assertNotNull("Sortname must not be null!", distributionList.get(1).getSortName());

    }

    private List<DistributionListMember> getDisList() throws ApiException {
        ContactResponse response = contactsApi.getContact(conId, contactFolderId);
        Assert.assertNull(response.getErrorDesc(), response.getError());
        Assert.assertNotNull("Data shouldn't be null", response.getData());
        ContactData data = response.getData();
        List<DistributionListMember> distributionList = data.getDistributionList();

        Assert.assertNotNull("Missing members", distributionList);
        Assert.assertEquals("Wrong number of members.", 2, distributionList.size());

        return distributionList;
    }

}
