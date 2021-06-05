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

package com.openexchange.contact.provider.test.impl.utils;

import java.util.Base64;
import java.util.Date;
import com.openexchange.groupware.container.Contact;

/**
 * {@link TestContactBuilder} - A simple builder for building test {@link Contacts}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v8.0.0
 */
public class TestContactBuilder {

    private static int idCounter = 0;
    private final int id;
    private String givenName;
    private String surName;
    private String email;
    private byte[] image;
    private String imageContentType;
    private String folderId;

    /**
     * Initializes a new {@link TestContactBuilder}.
     */
    public TestContactBuilder() {
        id = idCounter++;
    }

    /**
     * Sets the folderId
     *
     * @param folderId The ID of the folder
     * @return this
     */
    public TestContactBuilder setFolderId(String folderId) {
        this.folderId = folderId;
        return this;
    }

    /**
     * Sets the "given name" of the contact
     *
     * @param givenName The "given name"
     * @return this
     */
    public TestContactBuilder setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    /**
     * Sets the "surname" of the contact
     *
     * @param surName The "surname"
     * @return this
     */
    public TestContactBuilder setSurname(String surName) {
        this.surName = surName;
        return this;
    }

    /**
     * Sets the "email" of the contact
     *
     * @param email The "email"
     * @return this
     */
    public TestContactBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Sets the "image1" of the contact
     *
     * @param imageData The "image"
     * @return this
     */
    public TestContactBuilder setImage(byte[] imageData) {
        this.image = imageData;
        return this;
    }

    /**
     * Sets the "image1" of the contact
     *
     * @param imageData The "image" as base64 encoded string
     * @return this
     */
    public TestContactBuilder setImageString(String base64imageData) {
        return setImage(Base64.getDecoder().decode(base64imageData));
    }

    /**
     * Sets the "image content type" of the contact's image1
     *
     * @param contentType The "content type"
     * @return this
     */
    public TestContactBuilder setImageContentType(String contentType) {
        this.imageContentType = contentType;
        return this;
    }

    /**
     * Builds and gets the actual contact
     *
     * @return The contact built
     */
    public Contact getContact() {
        Contact c = new Contact();
        c.setId(Integer.toString(id));
        c.setObjectID(id);
        c.setFolderId(folderId);
        c.setCreationDate(new Date());
        c.setLastModified(new Date());
        c.setGivenName(givenName);
        c.setSurName(surName);
        c.setEmail1(email);
        if (image != null) {
            c.setImage1(image);
            c.setImageLastModified(new Date());
            c.setNumberOfImages(1);
        }
        if (imageContentType != null) {
            c.setImageContentType(imageContentType);
        }
        return c;
    }
}
