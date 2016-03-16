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
import java.util.Date;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.publish.actions.GetPublicationRequest;
import com.openexchange.ajax.publish.actions.GetPublicationResponse;
import com.openexchange.ajax.publish.actions.NewPublicationRequest;
import com.openexchange.ajax.publish.actions.NewPublicationResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.publish.Publication;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;


/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CreatePublicationTest extends AbstractPublicationTest {

    public CreatePublicationTest(String name) {
        super(name);
    }

    public void testOnePublicationOfOneContactFolderShouldNotBeAHassle() throws OXException, IOException, SAXException, JSONException, OXException, OXException{
        //create contact folder
        FolderObject folder = fMgr.generatePublicFolder("publishedContacts"+new Date().getTime(), FolderObject.CONTACT, getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId());
        fMgr.insertFolderOnServer( folder );

        //fill contact folder
        Contact contact = generateContact("Herbert", "Meier");
        contact.setParentFolderID(folder.getObjectID());
        cMgr.newAction(contact);

        //publish
        SimPublicationTargetDiscoveryService discovery = new SimPublicationTargetDiscoveryService();

        Publication expected = generatePublication("contacts", String.valueOf(folder.getObjectID() ), discovery );
        NewPublicationRequest newReq = new NewPublicationRequest(expected);
        AJAXClient myClient = getClient();
        NewPublicationResponse newResp = myClient.execute(newReq);
        expected.setId( newResp.getId() );

        //verify
        GetPublicationRequest getReq = new GetPublicationRequest( expected.getId() );
        GetPublicationResponse getResp = myClient.execute(getReq);
        Publication actual = getResp.getPublication(discovery);

        assertEquals("Should return the same folder as sent to the server", expected.getEntityId(), actual.getEntityId());
        assertEquals("Should return the same module as sent to the server", expected.getModule(), actual.getModule());
        assertEquals("Should return the same user as sent to the server", expected.getUserId(), actual.getUserId());
        assertEquals("Should return the same target id as sent to the server", expected.getTarget().getId(), actual.getTarget().getId());
        assertEquals("Should be enabled by default", true, actual.isEnabled());

    }

    public void testOnePublicationOfOneContactFolderWithoutAContactShouldNotBeAHassle() throws OXException, IOException, SAXException, JSONException, OXException, OXException{
        //create contact folder
        FolderObject folder = fMgr.generatePublicFolder("publishedContacts"+new Date().getTime(), FolderObject.CONTACT, getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId());
        fMgr.insertFolderOnServer( folder );

        //fill contact folder
        Contact contact = generateContact("Herbert", "Meier");
        contact.setParentFolderID(folder.getObjectID());
        cMgr.newAction(contact);

        //publish
        SimPublicationTargetDiscoveryService discovery = new SimPublicationTargetDiscoveryService();

        Publication expected = generatePublication("contacts", String.valueOf(folder.getObjectID() ), discovery );
        pubMgr.setPublicationTargetDiscoveryService(discovery);
        pubMgr.newAction(expected);

        //verify
        Publication actual = pubMgr.getAction(expected.getId());

        assertEquals("Should return the same folder as sent to the server", expected.getEntityId(), actual.getEntityId());
        assertEquals("Should return the same module as sent to the server", expected.getModule(), actual.getModule());
        assertEquals("Should return the same user as sent to the server", expected.getUserId(), actual.getUserId());
        assertEquals("Should return the same target id as sent to the server", expected.getTarget().getId(), actual.getTarget().getId());
    }

}
