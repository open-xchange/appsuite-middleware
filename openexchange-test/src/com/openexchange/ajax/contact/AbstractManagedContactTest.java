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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;

public abstract class AbstractManagedContactTest extends AbstractAJAXSession {

    protected int folderID;
    protected int secondFolderID;
    
    protected String folderName1;
    protected String folderName2;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        UserValues values = getClient().getValues();
        this.folderName1 = ("ManagedContactTest_" + (new Date().getTime()));
        FolderObject folder = ftm.generatePublicFolder(folderName1, Module.CONTACTS.getFolderConstant(), values.getPrivateContactFolder(), values.getUserId());
        folder = ftm.insertFolderOnServer(folder);
        folderID = folder.getObjectID();
        
        this.folderName2 = ("ManagedContactTest_2" + (new Date().getTime()));
        folder = ftm.generatePublicFolder(folderName2, Module.CONTACTS.getFolderConstant(), values.getPrivateContactFolder(), values.getUserId());
        folder = ftm.insertFolderOnServer(folder);
        secondFolderID = folder.getObjectID();
    }

    protected Contact generateContact(String lastname) {
        return this.generateContact(lastname, folderID);
    }
    
    protected Contact generateContact(String lastname, int folderId) {
        Contact contact = new Contact();
        contact.setSurName(lastname);
        contact.setGivenName("Given name");
        contact.setDisplayName(contact.getSurName() + ", " + contact.getGivenName());
        contact.setParentFolderID(folderId);
        return contact;
    }

    protected Contact generateContact() {
        return generateContact("Surname");
    }
    
    protected void assertFileName(HttpResponse httpResp, String expectedFileName) {
        Header[] headers = httpResp.getHeaders("Content-Disposition");
        for (Header header : headers) {
            assertNotNull(header.getValue());
            assertTrue(header.getValue().contains(expectedFileName));
        }
    }    
    
    protected JSONObject addRequestIds(int folderId, int objectId) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", objectId);
        json.put("folder_id", folderId);
        return json;
    }   
}
