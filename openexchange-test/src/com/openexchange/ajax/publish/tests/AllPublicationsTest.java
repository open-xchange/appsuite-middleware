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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.publish.tests;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.publish.actions.AllPublicationsRequest;
import com.openexchange.ajax.publish.actions.AllPublicationsResponse;
import com.openexchange.ajax.publish.actions.NewPublicationRequest;
import com.openexchange.ajax.publish.actions.NewPublicationResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;
import com.openexchange.publish.json.PublicationJSONException;
import com.openexchange.tools.servlet.AjaxException;


/**
 * {@link AllPublicationsTest}
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class AllPublicationsTest extends AbstractPublicationTest {
    
    public AllPublicationsTest(String name) {
        super(name);
    }
    
    public void testShouldNotFindNonExistingPublication() throws AjaxException, IOException, SAXException, JSONException{
        AJAXClient myClient = getClient();
        
        FolderObject testFolder = generateFolder("pubsub", FolderObject.CONTACT);
        getFolderManager().insertFolderOnServer( testFolder );
        
        AllPublicationsRequest req = new AllPublicationsRequest(String.valueOf(testFolder.getObjectID()), Integer.MAX_VALUE, "calendar", new LinkedList<String>());
        
        AllPublicationsResponse res = myClient.execute(req);
        JSONArray data = (JSONArray) res.getData();
        assertEquals("Array should be empty", I(0), I(data.length()));
    }

    public void testShouldFindOneFreshlyCreatedPublication() throws AjaxException, IOException, SAXException, JSONException, PublicationException, PublicationJSONException{
        Contact contact = createDefaultContactFolderWithOneContact();
        String folderID = String.valueOf(contact.getParentFolderID() );
        String module = "contacts";
        
        // publish
        SimPublicationTargetDiscoveryService discovery = new SimPublicationTargetDiscoveryService();

        Publication expected = generatePublication(module, folderID, discovery);
        expected.setDisplayName("This will be changed");
        NewPublicationRequest newReq = new NewPublicationRequest(expected);
        AJAXClient myClient = getClient();
        NewPublicationResponse newResp = myClient.execute(newReq);
        assertFalse("Precondition: Should be able to create a publication", newResp.hasError());
        expected.setId(newResp.getId());

        //retrieve publications
        AllPublicationsRequest req = new AllPublicationsRequest(folderID, expected.getId(), module, Arrays.asList(new String[]{"id","entity", "entityModule", "displayName", "target"}));
        AllPublicationsResponse resp = getClient().execute(req);
        assertFalse("Should work", resp.hasError());
        assertEquals("Should have exactly one result", 1, resp.getAll().size());
        
        JSONArray actual = resp.getAll().get(0);
        assertEquals("Should have same publication ID", expected.getId(), actual.getInt(0));
        assertEquals(expected.getEntityId(), actual.getJSONObject(1).get("folder"));
        assertEquals("Should have same module", expected.getModule(), actual.getString(2));
        assertFalse("Should change display name", expected.getDisplayName().equals(actual.getString(3)));
        assertEquals("Should have same target ID", expected.getTarget().getId(), actual.getString(4));
    }
}
