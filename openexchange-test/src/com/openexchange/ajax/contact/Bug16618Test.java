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

import java.util.Date;
import java.util.TimeZone;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.contact.action.DeleteRequest;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.contact.action.ListRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.contact.Data;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug16618Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug16618Test extends AbstractAJAXSession {

    private AJAXClient client;

    private TimeZone tz;

    private Contact contact;

    private int contextId;

    private int contactId;

    private int folderId;

    public Bug16618Test(final String name) {
        super(name);
    }

    private Contact createContactWithImage() throws Exception {
        final Contact contact = new Contact();
        contextId = client.getValues().getContextId();
        // contact.setContextId(contextId);
        contact.setTitle("Herr");
        contact.setSurName("Abba");
        contact.setGivenName("Baab");
        contact.setDisplayName("Baab Abba");
        contact.setStreetBusiness("Franz-Meier Weg 17");
        contact.setCityBusiness("Test Stadt");
        contact.setStateBusiness("NRW");
        contact.setCountryBusiness("Deutschland");
        contact.setTelephoneBusiness1("+49112233445566");
        contact.setCompany("Internal Test AG");
        contact.setEmail1("baab.abba@open-foobar.com");

        folderId = client.getValues().getPrivateContactFolder();
        contact.setParentFolderID(folderId);

        contact.setImage1(Data.image);

        final InsertRequest insertContactReq = new InsertRequest(contact);
        final InsertResponse insertContactResp = client.execute(insertContactReq);
        insertContactResp.fillObject(contact);

        contactId = contact.getObjectID();

        return contact;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = client.getValues().getTimeZone();
        contact = createContactWithImage();
    }

    @Override
    protected void tearDown() throws Exception {
        client.execute(new DeleteRequest(contact));
        super.tearDown();
    }

    public void testContactImageWithAllRequest() throws Throwable {
        /*
         * Check presence of image URL via action=list
         */
        {
            final ListRequest listRequest =
                new ListRequest(new ListIDs(folderId, contact.getObjectID()), new int[] { Contact.OBJECT_ID, Contact.IMAGE1_URL, Contact.LAST_MODIFIED });
            final CommonListResponse response = client.execute(listRequest);
            final int objectIdPos = response.getColumnPos(Contact.OBJECT_ID);
            final int imageURLPos = response.getColumnPos(Contact.IMAGE1_URL);
            for (final Object[] objA : response) {
                if (contactId == ((Integer) objA[objectIdPos]).intValue()) {
                    final String imageURL = (String) objA[imageURLPos];
                    assertNotNull(imageURL);
                }
            }
        }
        /*
         * Check presence of image URL via action=all
         */
        {
            final AllRequest allRequest = new AllRequest(folderId, new int[] { Contact.OBJECT_ID, Contact.IMAGE1_URL, Contact.LAST_MODIFIED });
            final CommonAllResponse allResponse = client.execute(allRequest);

            final int objectIdPos = allResponse.getColumnPos(Contact.OBJECT_ID);
            final int imageURLPos = allResponse.getColumnPos(Contact.IMAGE1_URL);
            final int lastModifiedPos = allResponse.getColumnPos(Contact.LAST_MODIFIED);

            for (final Object[] temp : allResponse) {
                final int tempObjectId = ((Integer) temp[objectIdPos]).intValue();
                if (tempObjectId == contactId) {
                    contact.setLastModified(new Date(((Long) temp[lastModifiedPos]).longValue()));
                    final String imageURL = (String) temp[imageURLPos];
                    assertNotNull(imageURL);
                }
            }
        }
    }

}
