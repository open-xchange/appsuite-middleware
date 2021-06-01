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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.openexchange.groupware.container.Contact;

public class Bug18862Test extends AbstractManagedContactTest {

    /**
     * Size of the contact image in bytes, should be larger than the
     * configured <code>max_image_size</code>.
     */
    private static final int IMAGE_SIZE = 100 * 1024;

    private static final String EXPECTED_CODE = "CON-0101";

    public Bug18862Test() {
        super();
    }

    @Test
    public void testUploadTooLargeImage() {
        final Contact contact = super.generateContact();
        contact.setImage1(new byte[IMAGE_SIZE]);
        contact.setImageContentType("image/jpg");
        contact.setNumberOfImages(1);
        super.cotm.newAction(contact);
        assertNotNull("got no response", super.cotm.getLastResponse());
        assertNotNull("no exception thrown", super.cotm.getLastResponse().getException());
        assertEquals("unexpected error code", EXPECTED_CODE, super.cotm.getLastResponse().getException().getErrorCode());
    }
}
