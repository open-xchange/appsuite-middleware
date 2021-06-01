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

import static com.openexchange.java.Autoboxing.L;
import java.util.Collections;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.dav.carddav.Photos;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ContactData;
import com.openexchange.testing.httpclient.models.ContactListElement;

/**
 *
 * {@link ContactPictureTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class ContactPictureTest extends AbstractApiClientContactTest {

    private final static String IMAGE_TYPE = "image/png";

    private final static byte IMAGE[] = Photos.PNG_100x100;

    private ContactData contactObj;

    // ---------------------------------------------------------------------------------------------

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        contactObj = createContactObject("ContactPictureTest");
        setImage(contactObj);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testUserFallbackPicture() {
        assertThatPictureIsMissing(String.valueOf(testUser.getUserId()), null, null);
    }

    @Test
    public void testFallbackContactPicture() throws Exception {
        removeImage();
        final String contactId = createContact(contactObj);

        assertThatPictureIsMissing(null, contactId, null);
    }

    @Test
    public void testGetContactPicture() throws Exception {
        final String contactId = createContact(contactObj);

        byte[] contactPicture = getContactPicture(null, contactId, null);
        assertImage(contactPicture);
    }

    @Test
    public void testGetFallbackPictureByWrongMail() throws Exception {
        createContact(contactObj);

        assertThatPictureIsMissing(null, null, "foobar@asd.com");
    }

    @Test
    public void testGetContactPictureByMail() throws Exception {
        String mail = "picture@example.org";
        contactObj.setEmail1(mail);
        createContact(contactObj);
        byte[] contactPicture = getContactPicture(null, null, mail);
        assertImage(contactPicture);
    }

    @Test
    public void testUpdateContact() throws Exception {
        final String contactId = createContact(contactObj);

        byte[] contactPicture = getContactPicture(null, contactId, null);
        assertImage(contactPicture);

        contactObj.setId(contactId);

        contactObj.setTelephoneBusiness2("+48112233445566");
        updateContact(contactObj, contactFolderId);

        contactPicture = getContactPicture(null, contactId, null);
        assertImage(contactPicture);

    }

    @Test
    public void testUpdateContactMail() throws Exception {
        String mail = "testUpdateContactMail@example.org";
        contactObj.setEmail1(mail);
        String contactId = createContact(contactObj);

        byte[] contactPicture = getContactPicture(null, null, mail);
        assertImage(contactPicture);

        mail = "testUpdateContactMail2@example.org";
        contactObj.setEmail1(mail);
        contactObj.setId(contactId);
        updateContact(contactObj, contactFolderId);

        contactPicture = getContactPicture(null, null, mail);
        assertImage(contactPicture);
    }

    @Test
    public void testAddContactMail() throws Exception {
        String mail = "testUpdateContactMail@example.org";
        contactObj.setEmail1(mail);
        String contactId = createContact(contactObj);

        byte[] contactPicture = getContactPicture(null, null, mail);
        assertImage(contactPicture);

        String mail2 = "testUpdateContactMail2@example.org";
        contactObj.setEmail2(mail2);
        contactObj.setId(contactId);
        updateContact(contactObj, contactFolderId);

        contactPicture = getContactPicture(null, null, mail2);
        assertImage(contactPicture);
    }

    @Test
    public void testUpdateContactPicture() throws Exception {
        final String contactId = createContact(contactObj);

        byte[] contactPicture = getContactPicture(null, contactId, null);
        assertImage(contactPicture);

        contactObj.setId(contactId);

        setImage(contactObj, Photos.PNG_200x200);
        updateContact(contactObj, contactFolderId);

        contactPicture = getContactPicture(null, contactId, null);
        assertImage(contactPicture, Photos.PNG_200x200);
    }

    @Test
    public void testDeleteContactPicture() throws Exception {
        final String contactId = createContact(contactObj);

        byte[] contactPicture = getContactPicture(null, contactId, null);
        assertImage(contactPicture);

        contactObj.setId(contactId);

        removeImage();
        updateContact(contactObj, contactFolderId);

        assertThatPictureIsMissing(null, contactId, null);
    }

    @Test
    public void testDeleteContact() throws Exception {
        final String contactId = createContact(contactObj);
        byte[] contactPicture = getContactPicture(null, contactId, null);
        assertImage(contactPicture);

        deleteContact(contactId);

        assertThatPictureIsMissing(null, contactId, null);
    }

    // ---------------------------------------------------------------------------------------------

    private byte[] getContactPicture(String userID, String contactId, String mail) throws ApiException {
        return getContactPicture(userID, contactId, mail, contactFolderId);
    }

    private byte[] getContactPicture(String userID, String contactId, String mail, String contactFolderID) throws ApiException {
        return contactsApi.getContactPicture(userID, null, contactId, contactFolderID, mail, null, null, null, null, null, null, null, null, null);
    }

    private void assertThatPictureIsMissing(String userID, String contactId, String mail) {
        assertThatPictureIsMissing(userID, contactId, mail, contactFolderId);
    }

    private void assertThatPictureIsMissing(String userID, String contactId, String mail, String contactFolderID) throws IllegalStateException {
        try {
            getContactPicture(userID, contactId, mail, contactFolderID);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                return;
            }
            Assert.fail("Expected a 404 error but received a " + e.getCode() + " with message: " + e.getMessage());
        }
        Assert.fail("Client should have received a 404");
    }

    private void setImage(final ContactData contactObj) {
        setImage(contactObj, IMAGE);
    }

    private void setImage(final ContactData contactObj, byte[] image) {
        contactObj.setImage1(Base64.encodeBase64String(image));
        contactObj.setImage1ContentType(IMAGE_TYPE);
    }

    /**
     * Delete contact picture.
     * <code>null</code> will not be sent by API client, so we must set it to an empty string
     */
    private void removeImage() {
        contactObj.setImage1("");
        contactObj.setImage1Url("");
        contactObj.setImage1ContentType("");
    }

    private void assertImage(byte[] actual) {
        assertImage(actual, IMAGE);
    }

    private void assertImage(byte[] actual, byte[] expected) {
        Assert.assertNotNull("Response should not be null.", actual);
        Assert.assertEquals("The image length is different.", expected.length, actual.length);
        Assert.assertArrayEquals("The image content is different.", expected, actual);
    }

    private void deleteContact(final String contactId) throws ApiException {
        ContactListElement element = new ContactListElement();
        element.setFolder(contactFolderId);
        element.setId(contactId);
        contactsApi.deleteContacts(L(Long.MAX_VALUE), Collections.singletonList(element));
    }
}
