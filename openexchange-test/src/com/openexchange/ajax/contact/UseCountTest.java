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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import java.util.UUID;
import org.json.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.contact.action.AutocompleteRequest;
import com.openexchange.ajax.framework.CommonSearchResponse;
import com.openexchange.ajax.jslob.actions.SetRequest;
import com.openexchange.ajax.mail.MailTestManager;
import com.openexchange.ajax.mail.TestMail;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.test.ContactTestManager;

/**
 * {@link UseCountTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class UseCountTest extends ContactTest {

    private MailTestManager mtm;
    private String address;
    private int folderId;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        FolderObject folder = ftm.generatePrivateFolder("useCountTest" + UUID.randomUUID().toString(), Module.CONTACTS.getFolderConstant(), getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId());
        folder = ftm.insertFolderOnServer(folder);
        folderId = folder.getObjectID();
        Contact c1 = ContactTestManager.generateContact(folder.getObjectID(), "UseCount");
        c1.setEmail1("useCount1@ox.invalid");
        c1 = cotm.newAction(c1);
        Contact c2 = ContactTestManager.generateContact(folder.getObjectID(), "UseCount");
        c2.setEmail1("useCount2@ox.invalid");
        c2 = cotm.newAction(c2);

        SetRequest req = new SetRequest("io.ox/mail", "{\"contactCollectOnMailTransport\": true}", true);
        getClient().execute(req);

        mtm = new MailTestManager(getClient());
        address = c2.getEmail1();
        mtm.send(new TestMail(getClient().getValues().getDefaultAddress(), address, "Test", "text/plain", "Test"));
    }

    @After
    public void tearDown() throws Exception {
        try {
            mtm.cleanUp();
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testUseCount() throws Exception {
        AutocompleteRequest req = new AutocompleteRequest("UseCount", false, String.valueOf(folderId), CONTACT_FIELDS, false);
        CommonSearchResponse resp = getClient().execute(req);
        assertFalse(resp.hasError());
        JSONArray json = (JSONArray) resp.getData();
        assertNotNull(json);
        assertEquals(2, json.length());
        Contact[] contacts = jsonArray2ContactArray(json, CONTACT_FIELDS);
        assertEquals(address, contacts[0].getEmail1());
    }

}
