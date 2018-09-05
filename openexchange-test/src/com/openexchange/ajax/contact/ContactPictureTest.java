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

import java.util.Arrays;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.openexchange.dav.carddav.Photos;
import com.openexchange.testing.httpclient.models.ContactData;

/**
 *
 * {@link ContactPictureTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class ContactPictureTest extends AbstractApiClientContactTest {

    private final static String IMAGE_TYPE = "image/png";

    private final static byte IMAGE[] = Photos.PNG_100x100;

    @Override
    public void setUp() throws Exception {
        super.setUp();

    }

    @Test
    public void testFallbackContactPicture() throws Exception {
        final ContactData contactObj = new ContactData();
        
        contactObj.setLastName("Fallback");
        contactObj.setFirstName("Picture");
        contactObj.setCountryBusiness("Deutschland");
        contactObj.setEmail1(UUID.randomUUID() + "@open-xchange.com");
        contactObj.setFolderId(contactFolderId);
        contactObj.setMarkAsDistributionlist(false);
        contactObj.setDistributionList(null);

        final String contactId = createContact(contactObj);

        byte[] contactPicture = contactsApi.getContactPicture(getSessionId(), null, contactId, contactFolderId, null);
        
        Assert.assertNotNull("Response should not be null.", contactPicture);
        Assert.assertEquals(Photos.FALLBACK_PICTURE.length, contactPicture.length);
        Assert.assertTrue("Wrong image", Arrays.equals(contactPicture, Photos.FALLBACK_PICTURE));
    }

    @Test
    public void testGetContactPicture() throws Exception {
        final ContactData contactObj = createContactObject("testGetContactPicture");
        contactObj.setImage1(Base64.encodeBase64String(IMAGE));
        contactObj.setImage1ContentType(IMAGE_TYPE);
        final String contactId = createContact(contactObj);
        byte[] contactPicture = contactsApi.getContactPicture(getSessionId(), null, contactId, contactFolderId, null);
        Assert.assertNotNull("Response should not be null.", contactPicture);
        Assert.assertEquals(IMAGE.length, contactPicture.length);
        Assert.assertArrayEquals(IMAGE, contactPicture);
    }

    @Test
    public void testGetContactPictureByMail() throws Exception {
        String mail = "picture@example.org";
        final ContactData contactObj = createContactObject("testGetContactPictureByMail");
        contactObj.setImage1(Base64.encodeBase64String(IMAGE));
        contactObj.setImage1ContentType(IMAGE_TYPE);
        contactObj.setEmail1(mail);
        createContact(contactObj);
        byte[] contactPicture = contactsApi.getContactPicture(getSessionId(), null, null, contactFolderId, mail);
        Assert.assertNotNull("Response should not be null.", contactPicture);
        Assert.assertEquals(IMAGE.length, contactPicture.length);
        Assert.assertArrayEquals(IMAGE, contactPicture);
    }
}
