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

package com.openexchange.ajax.publish.tests;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.publish.actions.ListPublicationsRequest;
import com.openexchange.ajax.publish.actions.ListPublicationsResponse;
import com.openexchange.ajax.publish.actions.NewPublicationRequest;
import com.openexchange.ajax.publish.actions.NewPublicationResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.publish.Publication;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;


/**
 * {@link ListPublicationsTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ListPublicationsTest extends AbstractPublicationTest {

    public ListPublicationsTest(String name) {
        super(name);
    }

    public void testListExistingPublication() throws OXException, IOException, SAXException, JSONException, OXException, OXException{
        final Contact contact = createDefaultContactFolderWithOneContact();
        String folderID = String.valueOf(contact.getParentFolderID() );
        String module = "contacts";

        SimPublicationTargetDiscoveryService discovery = new SimPublicationTargetDiscoveryService();
        pubMgr.setPublicationTargetDiscoveryService(discovery);

        Publication expected = generatePublication(module, folderID, discovery );
        expected.setDisplayName("This will be changed");
        NewPublicationRequest newReq = new NewPublicationRequest(expected);
        NewPublicationResponse newResp = getClient().execute(newReq);
        assertFalse("Precondition: Should be able to create a publication", newResp.hasError());
        expected.setId(newResp.getId());

        ListPublicationsRequest listReq = new ListPublicationsRequest(
            Arrays.asList(I(expected.getId())),
            Arrays.asList("id","entity", "entityModule", "displayName", "target"));
        ListPublicationsResponse listResp = getClient().execute(listReq);

        assertEquals("Should only find one element", 1, listResp.getList().size());

        JSONArray actual = listResp.getList().get(0);
        assertEquals("Should have same publication ID", expected.getId(), actual.getInt(0));
        assertEquals(expected.getEntityId(), actual.getJSONObject(1).get("folder"));
        assertEquals("Should have same module", expected.getModule(), actual.getString(2));
        assertFalse("Should change display name", expected.getDisplayName().equals(actual.getString(3)));
        assertEquals("Should have same target ID", expected.getTarget().getId(), actual.getString(4));
    }

    public void testListExistingPublicationOfEmptyFolder() throws OXException, IOException, SAXException, JSONException, OXException, OXException{
        final FolderObject contact = createDefaultContactFolder();
        String folderID = String.valueOf(contact.getObjectID() );
        String module = "contacts";

        SimPublicationTargetDiscoveryService discovery = new SimPublicationTargetDiscoveryService();
        pubMgr.setPublicationTargetDiscoveryService(discovery);

        Publication expected = generatePublication(module, folderID , discovery);
        expected.setDisplayName("This will be changed");

        pubMgr.newAction(expected);
        NewPublicationResponse newResp = (NewPublicationResponse) pubMgr.getLastResponse();
        assertFalse("Precondition: Should be able to create a publication", newResp.hasError());
        expected.setId(newResp.getId());

        pubMgr.listAction(
            Arrays.asList(I(expected.getId())),
            Arrays.asList("id","entity", "entityModule", "displayName", "target"));
        ListPublicationsResponse listResp = (ListPublicationsResponse) pubMgr.getLastResponse();

        assertEquals("Should only find one element", 1, listResp.getList().size());

        JSONArray actual = listResp.getList().get(0);
        assertEquals("Should have same publication ID", expected.getId(), actual.getInt(0));
        assertEquals(expected.getEntityId(), actual.getJSONObject(1).get("folder"));
        assertEquals("Should have same module", expected.getModule(), actual.getString(2));
        assertFalse("Should change display name", expected.getDisplayName().equals(actual.getString(3)));
        assertEquals("Should have same target ID", expected.getTarget().getId(), actual.getString(4));
    }
}
