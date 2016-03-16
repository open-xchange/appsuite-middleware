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

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.publish.actions.DeletePublicationRequest;
import com.openexchange.ajax.publish.actions.GetPublicationRequest;
import com.openexchange.ajax.publish.actions.GetPublicationResponse;
import com.openexchange.ajax.publish.actions.NewPublicationRequest;
import com.openexchange.ajax.publish.actions.NewPublicationResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.publish.Publication;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;

/**
 * {@link DeletePublicationTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class DeletePublicationTest extends AbstractPublicationTest {

    public DeletePublicationTest(String name) {
        super(name);
    }

    public void testDeletingAFolderDeletesThePublication() throws OXException, IOException, SAXException, JSONException, InterruptedException {
        Contact contact = createDefaultContactFolderWithOneContact();

        // publish
        SimPublicationTargetDiscoveryService discovery = new SimPublicationTargetDiscoveryService();

        Publication expected = generatePublication("contacts", String.valueOf(contact.getParentFolderID()), discovery);
        NewPublicationRequest newReq = new NewPublicationRequest(expected);
        AJAXClient myClient = getClient();
        NewPublicationResponse newResp = myClient.execute(newReq);
        assertFalse("Precondition: Should be able to create a publication", newResp.hasError());
        expected.setId(newResp.getId());

        // delete folder of publication
        getFolderManager().deleteFolderOnServer(contact.getParentFolderID(), new Date(Long.MAX_VALUE));
        Thread.sleep(1000); //asynchronous delete event needs time to hit

        // verify deletion of publication
        GetPublicationRequest getReq = new GetPublicationRequest(expected.getId());
        GetPublicationResponse getResp = myClient.execute(getReq);
        assertTrue("Reading a publication of a deleted folder should not work", getResp.hasError());
    }

    public void testDeletionOfPublicationShouldWork() throws OXException, IOException, SAXException, JSONException {
        Contact contact = createDefaultContactFolderWithOneContact();

        // publish
        SimPublicationTargetDiscoveryService discovery = new SimPublicationTargetDiscoveryService();

        Publication expected = generatePublication("contacts", String.valueOf(contact.getParentFolderID()), discovery);
        NewPublicationRequest newReq = new NewPublicationRequest(expected);
        AJAXClient myClient = getClient();
        NewPublicationResponse newResp = myClient.execute(newReq);
        expected.setId(newResp.getId());

        // delete publication
        DeletePublicationRequest delReq = new DeletePublicationRequest( expected.getId() );
        GetPublicationResponse delResp = myClient.execute(delReq);
        assertFalse("Deletion should produce no errors", delResp.hasError());

        // verify deletion of publication
        GetPublicationRequest getReq = new GetPublicationRequest(expected.getId());
        GetPublicationResponse getResp = myClient.execute(getReq);
        assertTrue("Reading deleted publication should produce exception", getResp.hasError());
    }

    public void testDeletionOfNonExistingPublicationShouldFail() throws OXException, IOException, SAXException, JSONException {
        // delete publication
        pubMgr.setFailOnError(false); // We'll provoke an error on purpose
        pubMgr.deleteAction(Arrays.asList(Integer.valueOf(Integer.MAX_VALUE)));
        assertTrue("Deletion of non-existing publication should produce errors", pubMgr.getLastResponse().hasError());
    }
}
