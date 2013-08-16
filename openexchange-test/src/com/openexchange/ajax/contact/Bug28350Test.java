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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.contact.action.DeleteRequest;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;


/**
 * {@link Bug28350Test}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class Bug28350Test extends ContactTest {
    
    private AJAXClient client;
    private Contact dList;

    /**
     * Initializes a new {@link Bug28350Test}.
     * @param name
     */
    public Bug28350Test(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = new AJAXClient(User.User1);
        dList = new Contact();
        dList.setDisplayName("Bug 28350 DList");
        dList.setParentFolderID(client.getValues().getPrivateContactFolder());
        DistributionListEntryObject dlistObject = new DistributionListEntryObject();
        dlistObject.setEmailaddress("bug28350@invalid.bug");
        dList.setDistributionList(new DistributionListEntryObject[] { dlistObject });
        InsertRequest insertRequest = new InsertRequest(dList);
        InsertResponse insertResponse = client.execute(insertRequest);
        insertResponse.fillObject(dList);
    }
    
    @Override
    protected void tearDown() throws Exception {
        GetRequest getRequest = new GetRequest(dList, client.getValues().getTimeZone());
        GetResponse getResponse = client.execute(getRequest);
        Contact toDelete = getResponse.getContact();
        if (null != toDelete) {
            DeleteRequest deleteRequest = new DeleteRequest(toDelete);
            client.execute(deleteRequest);
        }
    }
    
    public void testBug28350() throws Exception {
        Contact duplicate = dList.clone();
        InsertRequest insertRequest = new InsertRequest(duplicate, false);
        InsertResponse insertResponse = client.execute(insertRequest);
        assertTrue("Inserting duplicate dlist names should throw an exception", insertResponse.hasError());
        assertEquals("Incorrect error code", insertResponse.getException().getErrorCode(), "CON-0178");
    }

}
