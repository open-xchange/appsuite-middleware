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

import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.contact.action.DeleteRequest;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.groupware.container.Contact;


/**
 * {@link DeleteMultipleContactsTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class DeleteMultipleContactsTest extends AbstractContactTest {
    
    private Contact c1, c2, c3;
    private boolean needCleanup = true;

    /**
     * Initializes a new {@link DeleteMultipleContactsTest}.
     * @param name
     */
    public DeleteMultipleContactsTest(String name) {
        super(name);
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        c1 = new Contact();
        c1.setGivenName("Test 1");
        c1.setSurName("User");
        c1.setDisplayName("Test 1 User");
        c1.setEmail1("testuser1@example.org");
        c1.setParentFolderID(client.getValues().getPrivateContactFolder());
        c1.setCreationDate(new Date());
        
        c2 = new Contact();
        c2.setGivenName("Test 2");
        c2.setSurName("User");
        c2.setDisplayName("Test 2 User");
        c2.setEmail1("testuser2@example.org");
        c2.setParentFolderID(client.getValues().getPrivateContactFolder());
        c2.setCreationDate(new Date());
        
        c3 = new Contact();
        c3.setGivenName("Test 3");
        c3.setSurName("User");
        c3.setDisplayName("Test 3 User");
        c3.setEmail1("testuser3@example.org");
        c3.setParentFolderID(client.getValues().getPrivateContactFolder());
        c3.setCreationDate(new Date());
        
        InsertRequest in1 = new InsertRequest(c1);
        InsertResponse res1 = client.execute(in1);
        res1.fillObject(c1);
        
        InsertRequest in2 = new InsertRequest(c2);
        InsertResponse res2 = client.execute(in2);
        res2.fillObject(c2);
        
        InsertRequest in3 = new InsertRequest(c3);
        InsertResponse res3 = client.execute(in3);
        res3.fillObject(c3);
    }
    
    @Test
    public void testDeleteMultipleContacts() throws Exception {
        int[] cids = new int[] {c1.getObjectID(), c2.getObjectID(), c3.getObjectID()};
        DeleteRequest delReq = new DeleteRequest(client.getValues().getPrivateContactFolder(), cids, new Date());
        CommonDeleteResponse delRes = client.execute(delReq);
        assertFalse("Delete of multiple contacts failed: " + delRes.getErrorMessage(), delRes.hasError());
        needCleanup = false;
    }
    
    @Override
    public void tearDown() throws Exception {
        if (needCleanup) {
            try {
                DeleteRequest delReq = new DeleteRequest(c1);
                client.execute(delReq);
                delReq = new DeleteRequest(c2);
                client.execute(delReq);
                delReq = new DeleteRequest(c3);
                client.execute(delReq);
            } catch (Exception e) {

            }
        }
        super.tearDown();
    }

}
