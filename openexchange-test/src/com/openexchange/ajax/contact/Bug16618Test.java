/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.contact;

import static org.junit.Assert.assertNotNull;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.contact.action.ListRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.container.Contact;
import com.openexchange.test.common.contact.Data;

/**
 * {@link Bug16618Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug16618Test extends AbstractAJAXSession {

    private Contact contact;

    private int contactId;

    private int folderId;

    public Bug16618Test() {
        super();
    }

    private Contact createContactWithImage() throws Exception {
        final Contact contact = new Contact();
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

        folderId = getClient().getValues().getPrivateContactFolder();
        contact.setParentFolderID(folderId);

        contact.setImage1(Data.image);

        final InsertRequest insertContactReq = new InsertRequest(contact);
        final InsertResponse insertContactResp = getClient().execute(insertContactReq);
        insertContactResp.fillObject(contact);

        contactId = contact.getObjectID();

        return contact;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        contact = createContactWithImage();
    }

    @Test
    public void testContactImageWithAllRequest() throws Throwable {
        /*
         * Check presence of image URL via action=list
         */
        {
            final ListRequest listRequest = new ListRequest(new ListIDs(folderId, contact.getObjectID()), new int[] { Contact.OBJECT_ID, Contact.IMAGE1_URL, Contact.LAST_MODIFIED });
            final CommonListResponse response = getClient().execute(listRequest);
            final int objectIdPos = response.getColumnPos(Contact.OBJECT_ID);
            final int imageURLPos = response.getColumnPos(Contact.IMAGE1_URL);
            for (final Object[] objA : response) {
                if (contactId == Integer.parseInt(String.class.cast(objA[objectIdPos]))) {
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
            final CommonAllResponse allResponse = getClient().execute(allRequest);

            final int objectIdPos = allResponse.getColumnPos(Contact.OBJECT_ID);
            final int imageURLPos = allResponse.getColumnPos(Contact.IMAGE1_URL);
            final int lastModifiedPos = allResponse.getColumnPos(Contact.LAST_MODIFIED);

            for (final Object[] temp : allResponse) {
                final int tempObjectId = Integer.parseInt(String.class.cast(temp[objectIdPos]));
                if (tempObjectId == contactId) {
                    contact.setLastModified(new Date(((Long) temp[lastModifiedPos]).longValue()));
                    final String imageURL = (String) temp[imageURLPos];
                    assertNotNull(imageURL);
                }
            }
        }
    }

}
