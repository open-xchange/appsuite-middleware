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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ContactData;
import com.openexchange.testing.httpclient.models.ContactResponse;
import com.openexchange.testing.httpclient.models.DistributionListMember;

/**
 * {@link DistributionListMemberSortingTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class DistributionListMemberSortingTest extends AbstractApiClientContactTest {

    private ContactData contactObj;
    private String      conId;
    private String      conAID;
    private String      conBID;

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
        contactObj.setMarkAsDistributionlist(true);
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
        result.setMailField(new BigDecimal(1));
        return result;
    }

    @Test
    public void testSorting() throws ApiException {
        ContactResponse response = contactsApi.getContact(getSessionId(), conId, contactFolderId);
        Assert.assertNull(response.getErrorDesc(), response.getError());
        Assert.assertNotNull("Data shouldn't be null", response.getData());
        ContactData data = response.getData();
        List<DistributionListMember> distributionList = data.getDistributionList();
        Assert.assertNotNull("Missing members", distributionList);
        Assert.assertEquals("Wrong number of members.", 2, distributionList.size());

        Assert.assertEquals(conAID, distributionList.get(0).getId());
        Assert.assertNotNull("Sortname must not be null!", distributionList.get(0).getSortName());
        Assert.assertEquals(conBID, distributionList.get(1).getId());
        Assert.assertNotNull("Sortname must not be null!", distributionList.get(1).getSortName());
    }

}
