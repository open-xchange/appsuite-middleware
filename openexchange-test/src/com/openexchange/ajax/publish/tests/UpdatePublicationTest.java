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
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.publish.actions.NewPublicationRequest;
import com.openexchange.ajax.publish.actions.NewPublicationResponse;
import com.openexchange.ajax.publish.actions.UpdatePublicationRequest;
import com.openexchange.ajax.publish.actions.UpdatePublicationResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.publish.Publication;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;


/**
 * {@link UpdatePublicationTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class UpdatePublicationTest extends AbstractPublicationTest {

    public UpdatePublicationTest(String name) {
        super(name);
    }

    public void testShouldUpdateExistingPublication() throws OXException, IOException, SAXException, JSONException, OXException, OXException{
        final Contact contact = createDefaultContactFolderWithOneContact();
        String folderID = String.valueOf(contact.getParentFolderID() );
        String module = "contacts";

        SimPublicationTargetDiscoveryService discovery = new SimPublicationTargetDiscoveryService();
        pubMgr.setPublicationTargetDiscoveryService(discovery);

        Publication orignal = generatePublication(module, folderID, discovery );
        NewPublicationRequest newReq = new NewPublicationRequest(orignal);
        NewPublicationResponse newResp = getClient().execute(newReq);
        assertFalse("Should contain no error after creating", newResp.hasError());
        orignal.setId(newResp.getId());

        Publication update = generatePublication(module, folderID, discovery);

        update.setId(newResp.getId());
        UpdatePublicationRequest updReq = new UpdatePublicationRequest(update);
        UpdatePublicationResponse updResp = getClient().execute(updReq);

        assertFalse("Should contain no error after updating", updResp.hasError());
        assertEquals("Should return 1 in case of success", I(1), updResp.getData());
    }

    public void testShouldBeAbleToUpdateExistingPublicationsSiteName() throws OXException, IOException, SAXException, JSONException, OXException, OXException{
        SimPublicationTargetDiscoveryService discovery = new SimPublicationTargetDiscoveryService();

        final Contact contact = createDefaultContactFolderWithOneContact();
        String folderID = String.valueOf(contact.getParentFolderID() );

        Publication pub1 = generatePublication("contacts", folderID, discovery );
        pub1.getConfiguration().put("siteName","oldName");
        pubMgr.setPublicationTargetDiscoveryService( discovery );

        pubMgr.newAction(pub1);
        assertFalse("Should contain no error after creating", pubMgr.getLastResponse().hasError());

        Publication pub2 = pubMgr.getAction(pub1.getId());
        assertEquals("Should have set the siteName", "oldName", pub2.getConfiguration().get("siteName"));

        Publication pub3 = generatePublication("contacts", folderID, discovery);
        pub3.setId(pub1.getId());
        pub3.getConfiguration().put("siteName", "newName");
        pubMgr.updateAction(pub3);

        UpdatePublicationResponse updResp = (UpdatePublicationResponse) pubMgr.getLastResponse();
        assertFalse("Should contain no error after updating", updResp.hasError());
        assertTrue("Should return 1 in case of success", updResp.wasSuccessful());

        Publication pub4 = pubMgr.getAction(pub3.getId());
        assertEquals("Should have updated the siteName", "newName", pub4.getConfiguration().get("siteName"));
    }


    /*
    public void testUpdatingTargetShouldCallForNewConfiguration(){
        fail("Implement me!");
    }

    public void testUpdatingWithActualChange(){
        fail("Implement me!");
    }
    */

    /*
    public void testUpdatingNonExistentPublicationShouldFail() throws OXException, IOException, SAXException, JSONException{
        final Contact contact = createDefaultContactFolderWithOneContact();
        String folderID = String.valueOf(contact.getParentFolderID() );
        String module = "contacts";

        Publication update = generatePublication(module, folderID);
        update.setId(Integer.MAX_VALUE);
        UpdatePublicationRequest updReq = new UpdatePublicationRequest(update);
        UpdatePublicationResponse updResp = getClient().execute(updReq);

        assertTrue("Should contain error when updating non-existent publication ", updResp.hasError());
        assertFalse("Should not contain the error code for 'unknown error'" , "PUBH-0001".equals(updResp.getException().getErrorCode()));
    }
    */

}
