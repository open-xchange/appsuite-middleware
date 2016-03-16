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

import static com.openexchange.java.Autoboxing.I;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.contact.action.ContactUpdatesResponse;
import com.openexchange.ajax.contact.action.DeleteRequest;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.contact.action.UpdatesRequest;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;

/**
 * {@link Bug13960Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug13960Test extends AbstractAJAXSession {

    private static final int[] COLUMNS = { Contact.OBJECT_ID, Contact.DEFAULT_ADDRESS, Contact.FILE_AS, Contact.NUMBER_OF_IMAGES };

    private AJAXClient client;
    private TimeZone timeZone;
    private Contact contact;

    public Bug13960Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        timeZone = client.getValues().getTimeZone();
        contact = new Contact();
        contact.setParentFolderID(client.getValues().getPrivateContactFolder());
        InsertResponse response = client.execute(new InsertRequest(contact));
        response.fillObject(contact);
    }

    @Override
    protected void tearDown() throws Exception {
        client.execute(new DeleteRequest(contact));
        super.tearDown();
    }

    public void testJSONValues() throws Throwable {
        {
            GetRequest request = new GetRequest(contact, timeZone);
            GetResponse response = client.execute(request);
            JSONObject json = (JSONObject) response.getData();
            assertFalse("'Default address' should not be contained if not set.", json.has(ContactFields.DEFAULT_ADDRESS));
            assertFalse("'File as should' not be contained if not set.", json.has(ContactFields.FILE_AS));
            assertTrue("'Number of images' should be contained always.", json.has(ContactFields.NUMBER_OF_IMAGES));
            assertEquals("'Number of images' should be zero.", 0, json.getInt(ContactFields.NUMBER_OF_IMAGES));
        }
        {
            UpdatesRequest request = new UpdatesRequest(contact.getParentFolderID(), COLUMNS, 0, Order.ASCENDING, new Date(contact.getLastModified().getTime() - 1));
            ContactUpdatesResponse response = client.execute(request);
            int row = 0;
            while (row < response.size()) {
                if (response.getValue(row, Contact.OBJECT_ID).equals(I(contact.getObjectID()))) {
                    break;
                }
                row++;
            }
            JSONArray array = ((JSONArray) response.getData()).getJSONArray(row);
            int defaultAddressPos = response.getColumnPos(Contact.DEFAULT_ADDRESS);
            assertEquals("Default address should not be contained if not set.", JSONObject.NULL, array.get(defaultAddressPos));
            int fileAsPos = response.getColumnPos(Contact.FILE_AS);
            assertEquals(JSONObject.NULL, array.get(fileAsPos));
            int numberOfImagesPos = response.getColumnPos(Contact.NUMBER_OF_IMAGES);
            assertEquals("'Number of images' should be zero.", 0, array.getInt(numberOfImagesPos));
        }
    }
}
