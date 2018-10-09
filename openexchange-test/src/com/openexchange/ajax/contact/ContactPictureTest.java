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

import static com.openexchange.java.Autoboxing.L;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
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
    public void testUserFallbackPicture() throws Exception {
        assertThatPictureIsMissing(apiClient.getUserId().toString(), null, null);
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

    private byte[] getContactPicture(String userId, String contactId, String mail) throws ApiException {
        return getContactPicture(userId, contactId, mail, contactFolderId);
    }

    private byte[] getContactPicture(String userId, String contactId, String mail, String contactFolderId) throws ApiException {
        return contactsApi.getContactPicture(getSessionId(), userId, contactId, contactFolderId, mail, null, null, null, null, null, null, null, null, null);
    }
    
    private void assertThatPictureIsMissing(String userId, String contactId, String mail) throws ApiException {
        assertThatPictureIsMissing(userId, contactId, mail, contactFolderId);
    }
    
    private void assertThatPictureIsMissing(String userId, String contactId, String mail, String contactFolderId) throws ApiException, IllegalStateException {
        try {
            getContactPicture(userId, contactId, mail, contactFolderId);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                return;
            }
            throw e;
        }
        throw new IllegalStateException("Client should have received a 404");
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
        contactsApi.deleteContacts(getSessionId(), L(Long.MAX_VALUE), Collections.singletonList(element));
    }
}
