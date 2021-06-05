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

package com.openexchange.mail.compose;

import com.openexchange.java.Strings;

/**
 * {@link AttachmentOrigin} - The origin of an attachment.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public enum AttachmentOrigin {
    /**
     * The attachment has been uploaded.
     */
    UPLOAD("upload"),
    /**
     * The attachment is taken over from original mail (e.g. on forward).
     */
    MAIL("mail"),
    /**
     * The attachment is taken over from Drive.
     */
    DRIVE("drive"),
    /**
     * The attachment is a vCard generated from an existent contact.
     */
    CONTACT("contact"),
    /**
     * The attachment is user's vCard
     */
    VCARD("vcard")

    ;

    private final String identifier;

    private AttachmentOrigin(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the attachment origin for given identifier.
     *
     * @param origin The origin's identifier
     * @return The associated attachment origin instance or <code>null</code> if there is no such attachment origin
     */
    public static AttachmentOrigin getOriginFor(String origin) {
        if (Strings.isEmpty(origin)) {
            return null;
        }

        for (AttachmentOrigin o : AttachmentOrigin.values()) {
            if (origin.equalsIgnoreCase(o.identifier)) {
                return o;
            }
        }
        return null;
    }
}
