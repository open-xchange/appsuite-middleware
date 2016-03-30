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

package com.openexchange.ajax.subscribe.test;

import java.io.IOException;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.subscribe.actions.DeleteSubscriptionRequest;
import com.openexchange.ajax.subscribe.actions.DeleteSubscriptionResponse;
import com.openexchange.ajax.subscribe.actions.GetSubscriptionRequest;
import com.openexchange.ajax.subscribe.actions.GetSubscriptionResponse;
import com.openexchange.ajax.subscribe.actions.NewSubscriptionRequest;
import com.openexchange.ajax.subscribe.actions.NewSubscriptionResponse;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Autoboxing;
import com.openexchange.subscribe.Subscription;


/**
 * {@link DeleteSubscriptionTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class DeleteSubscriptionTest extends AbstractSubscriptionTest {

    public DeleteSubscriptionTest(String name) {
        super(name);
    }

    public void testDeleteOMXFSubscriptionShouldAlwaysWork() throws OXException, IOException, SAXException, JSONException{
        //setup
        FolderObject folder = getFolderManager().generatePublicFolder("subscriptionTest", FolderObject.CONTACT, getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId());
        getFolderManager().insertFolderOnServer(folder);

        DynamicFormDescription form = generateFormDescription();
        Subscription expected = generateOXMFSubscription(form);
        expected.setFolderId( String.valueOf( folder.getObjectID() ) );

       //new request
        NewSubscriptionRequest newReq = new NewSubscriptionRequest(expected, form);
        NewSubscriptionResponse newResp = getClient().execute(newReq);

        assertFalse("Should succeed creating the subscription", newResp.hasError());
        expected.setId( newResp.getId() );

        //delete subcription
        DeleteSubscriptionRequest delReq = new DeleteSubscriptionRequest( expected.getId());
        DeleteSubscriptionResponse delResp = getClient().execute(delReq);
        assertFalse("Should succeed deleting the subscription", delResp.hasError());

        //verify absense via get request
        GetSubscriptionRequest getReq = new GetSubscriptionRequest( newResp.getId() );
        GetSubscriptionResponse getResp = getClient().execute(getReq);

        assertTrue("Should fail trying to get subcription afte deletion", getResp.hasError());
        assertEquals("Should return 1 in case of success", Autoboxing.I(1), delResp.getData());
    }
}
