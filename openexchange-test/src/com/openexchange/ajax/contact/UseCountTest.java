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

import java.util.Random;
import org.json.JSONArray;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.contact.action.AutocompleteRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.CommonSearchResponse;
import com.openexchange.ajax.jslob.actions.SetRequest;
import com.openexchange.ajax.mail.MailTestManager;
import com.openexchange.ajax.mail.TestMail;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.test.ContactTestManager;
import com.openexchange.test.FolderTestManager;


/**
 * {@link UseCountTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class UseCountTest extends ContactTest {

    private AJAXClient client;
    private ContactTestManager ctm;
    private FolderTestManager ftm;
    private MailTestManager mtm;
    private String address;
    private int folderId;

    /**
     * Initializes a new {@link UseCountTest}.
     * @param name
     */
    public UseCountTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = new AJAXClient(User.User1);
        ftm = new FolderTestManager(client);
        FolderObject folder = ftm.generatePrivateFolder("useCountTest", Module.CONTACTS.getFolderConstant(), client.getValues().getPrivateContactFolder(), client.getValues().getUserId());
        folder = ftm.insertFolderOnServer(folder);
        folderId = folder.getObjectID();
        ctm = new ContactTestManager(client);
        Contact c1 = ContactTestManager.generateContact(folder.getObjectID(), "UseCount");
        c1.setEmail1("useCount1@ox.invalid");
        c1 = ctm.newAction(c1);
        Contact c2 = ContactTestManager.generateContact(folder.getObjectID(), "UseCount");
        c2.setEmail1("useCount2@ox.invalid");
        c2 = ctm.newAction(c2);

        AJAXClient client = new AJAXClient(User.User1);
        SetRequest req = new SetRequest("io.ox/mail", "{\"contactCollectOnMailTransport\": true}", true);
        client.execute(req);

        mtm = new MailTestManager(client);
        address = c2.getEmail1();
        mtm.send(new TestMail(client.getValues().getDefaultAddress(), address, "Test", "text/plain", "Test"));
    }

    @Override
    public void tearDown() throws Exception {
        mtm.cleanUp();
        ctm.cleanUp();
        ftm.cleanUp();
        super.tearDown();
    }

    public void testUseCount() throws Exception {
        AJAXClient client = new AJAXClient(User.User1);
        AutocompleteRequest req = new AutocompleteRequest("UseCount", false, String.valueOf(folderId), CONTACT_FIELDS, false);
        CommonSearchResponse resp = client.execute(req);
        assertFalse(resp.hasError());
        JSONArray json = (JSONArray) resp.getData();
        assertNotNull(json);
        assertEquals(2, json.length());
        Contact[] contacts = jsonArray2ContactArray(json, CONTACT_FIELDS);
        assertEquals(address, contacts[0].getEmail1());
        client.logout();
    }

}
