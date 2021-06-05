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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.FileInputStream;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.testing.httpclient.models.ContactData;

/**
 *
 * {@link ContactImageApiClientScaleTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ContactImageApiClientScaleTest extends AbstractApiClientContactTest {

    String protocol;
    String hostname;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        protocol = AJAXConfig.getProperty(AJAXConfig.Property.PROTOCOL);
        hostname = AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME);
    }

    @Test
    public void testCreateContact() throws Exception {
        FileInputStream input = null;
        try {
            final ContactData contactObj = createContactObject("testContactWithImage");
            String testDataDir = AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR);
            java.io.File file = new java.io.File(testDataDir, "oxlogo.png");
            input = new FileInputStream(file);
            final byte bigImage[] = new byte[input.available()];
            input.read(bigImage);
            contactObj.setImage1(Base64.encodeBase64String(bigImage));
            contactObj.setImage1ContentType("image/png");
            final String objectId = createContact(contactObj);

            ContactData loadContact = loadContact(objectId, contactFolderId);
            final String imageUrl = loadContact.getImage1Url();
            if (imageUrl == null) {
                fail("Contact contains no image URL.");
            }

            final byte[] b = loadImageByURL(protocol, hostname, imageUrl);
            assertTrue("Wrong or no scaling", b.length < bigImage.length);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    @Test
    public void testUpdateContact() throws Exception {
        FileInputStream input = null;
        try {
            final ContactData contactObj = createContactObject("testUpdateContactWithImageUpdate");
            final String objectId = createContact(contactObj);
            String testDataDir = AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR);
            java.io.File file = new java.io.File(testDataDir, "oxlogo.png");
            input = new FileInputStream(file);
            final byte bigImage[] = new byte[input.available()];
            input.read(bigImage);
            contactObj.setImage1(Base64.encodeBase64String(bigImage));
            contactObj.setImage1ContentType("image/png");
            contactObj.setId(objectId);
            contactObj.setFolderId(null);
            updateContact(contactObj, contactFolderId);

            ContactData loadContact = loadContact(objectId, contactFolderId);
            final String imageUrl = loadContact.getImage1Url();
            if (imageUrl == null) {
                fail("Contact contains no image URL.");
            }

            final byte[] b = loadImageByURL(protocol, hostname, imageUrl);
            assertTrue("Wrong or no scaling", b.length < bigImage.length);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

}
