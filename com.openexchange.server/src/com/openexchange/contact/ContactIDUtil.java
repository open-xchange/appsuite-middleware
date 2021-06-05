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

package com.openexchange.contact;

import static com.openexchange.java.Autoboxing.i;
import com.openexchange.groupware.container.Contact;

/**
 * {@link ContactIDUtil} - Utility class to create ContactIDs
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public final class ContactIDUtil {

    /**
     * Creates a new {@link ContactID} from the specified {@link Contact}
     *
     * @param contact The {@link Contact} object
     * @return The new {@link ContactID}
     */
    public static ContactID createContactID(Contact contact) {
        return createContactID(contact.getParentFolderID(), contact.getObjectID());
    }

    /**
     * Creates a new {@link ContactID} by using the object identifier
     * of the specified {@link Contact} and the specified folderId
     * 
     * @param folderId The folderId
     * @param contact The {@link Contact} object
     * @return The new {@link ContactID}
     */
    public static ContactID createContactID(String folderId, Contact contact) {
        return createContactID(folderId, contact.getObjectID());
    }

    /**
     * Creates a new {@link ContactID} by using the object identifier
     * of the specified {@link Contact} and the specified folderId
     * 
     * @param folderId The folderId
     * @param contact The {@link Contact} object
     * @return The new {@link ContactID}
     */
    public static ContactID createContactID(int folderId, Contact contact) {
        return createContactID(folderId, contact.getObjectID());
    }

    /**
     * Creates a new {@link ContactID} by using the folder identifier
     * of the specified {@link Contact} and the specified object identifier
     * 
     * @param contact The {@link Contact} object
     * @param objectId The objectId
     * @return The new {@link ContactID}
     */
    public static ContactID createContactID(Contact contact, String objectId) {
        return createContactID(contact.getParentFolderID(), objectId);
    }

    /**
     * Creates a new {@link ContactID} by using the folder identifier
     * of the specified {@link Contact} and the specified object identifier.
     * 
     * @param contact The {@link Contact} object
     * @param objectId The objectId
     * @return The new {@link ContactID}
     */
    public static ContactID createContactID(Contact contact, int objectId) {
        return createContactID(contact.getParentFolderID(), objectId);
    }

    /**
     * Creates a new {@link ContactID} from the specified folder and object identifiers
     *
     * @param folderId The folder identifier
     * @param objectId The object identifier
     * @return The new {@link ContactID}
     */
    public static ContactID createContactID(String folderId, String objectId) {
        return new ContactID(folderId, objectId);
    }

    /**
     * Creates a new {@link ContactID} from the specified folder and object identifiers
     *
     * @param folderId The folder identifier
     * @param objectId The object identifier
     * @return The new {@link ContactID}
     */
    public static ContactID createContactID(Integer folderId, Integer objectId) {
        return new ContactID(Integer.toString(i(folderId)), Integer.toString(i(objectId)));
    }

    /**
     * Creates a new {@link ContactID} from the specified folder and object identifiers
     *
     * @param folderId The folder identifier
     * @param objectId The object identifier
     * @return The new {@link ContactID}
     */
    public static ContactID createContactID(int folderId, int objectId) {
        return new ContactID(Integer.toString(folderId), Integer.toString(objectId));
    }

    /**
     * Creates a new {@link ContactID} from the specified folder and object identifiers
     *
     * @param folderId The folder identifier
     * @param objectId The object identifier
     * @return The new {@link ContactID}
     */
    public static ContactID createContactID(String folderId, int objectId) {
        return new ContactID(folderId, Integer.toString(objectId));
    }

    /**
     * Creates a new {@link ContactID} from the specified folder and object identifiers
     *
     * @param folderId The folder identifier
     * @param objectId The object identifier
     * @return The new {@link ContactID}
     */
    public static ContactID createContactID(int folderId, String objectId) {
        return new ContactID(Integer.toString(folderId), objectId);
    }

}
