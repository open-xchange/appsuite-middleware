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

package com.openexchange.contact.storage.rdb.fields;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import com.openexchange.groupware.contact.helpers.ContactField;

/**
 * {@link Fields} - Provides constants and utility methods to create SQL statements.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Fields {

    /**
     * A set of all contact fields as used by the 'prg_contacts' database table.
     */
    //TODO: might be better to list the fields explicitly
    public static final EnumSet<ContactField> CONTACT_DATABASE = EnumSet.complementOf(EnumSet.of(ContactField.IMAGE1_URL,
        ContactField.IMAGE1_CONTENT_TYPE, ContactField.IMAGE_LAST_MODIFIED, ContactField.IMAGE1, ContactField.DISTRIBUTIONLIST,
        ContactField.LAST_MODIFIED_OF_NEWEST_ATTACHMENT, ContactField.LAST_MODIFIED_UTC, ContactField.SORT_NAME));

    /**
     * A set of all contact fields as used by the 'del_contacts' database table.
     */
    public static final EnumSet<ContactField> DEL_CONTACT_DATABASE = EnumSet.of(ContactField.CONTEXTID, ContactField.FOLDER_ID,
        ContactField.OBJECT_ID, ContactField.INTERNAL_USERID, ContactField.UID, ContactField.FILENAME, ContactField.LAST_MODIFIED,
        ContactField.CREATION_DATE, ContactField.CREATED_BY, ContactField.MODIFIED_BY, ContactField.PRIVATE_FLAG);

    /**
     * An array of all contact fields as used by the contacts database table.
     */
    public static final ContactField[] CONTACT_DATABASE_ARRAY =
        sort(CONTACT_DATABASE.toArray(new ContactField[CONTACT_DATABASE.size()]));

    /**
     * A set of all contact fields that are only set once during creation and never change afterwards.
     */
    public static final EnumSet<ContactField> READONLY_CONTACT_DATABASE = EnumSet.of(ContactField.OBJECT_ID, ContactField.CREATED_BY,
        ContactField.CREATION_DATE, ContactField.CONTEXTID, ContactField.UID);

    /**
     * A set of all contact fields as used by the images database table.
     */
    public static final EnumSet<ContactField> IMAGE_DATABASE = EnumSet.of(ContactField.OBJECT_ID, ContactField.IMAGE1,
        ContactField.IMAGE_LAST_MODIFIED, ContactField.IMAGE1_CONTENT_TYPE, ContactField.CONTEXTID);

    /**
     * A set of all additional contact fields as used by the images database table.
     */
    public static final EnumSet<ContactField> IMAGE_DATABASE_ADDITIONAL = EnumSet.of(ContactField.IMAGE1,
        ContactField.IMAGE_LAST_MODIFIED, ContactField.IMAGE1_CONTENT_TYPE);

    /**
     * An array of all contact fields as used by the images database table.
     */
    public static final ContactField[] IMAGE_DATABASE_ARRAY =
        IMAGE_DATABASE.toArray(new ContactField[IMAGE_DATABASE.size()]);

    /**
     * A set of all contact fields as used by the distribution list database table.
     */
    public static final EnumSet<DistListMemberField> DISTLIST_DATABASE = EnumSet.allOf(DistListMemberField.class);

    /**
     * An array of all contact fields as used by the distribution list database table.
     */
    public static final DistListMemberField[] DISTLIST_DATABASE_ARRAY =
        DISTLIST_DATABASE.toArray(new DistListMemberField[DISTLIST_DATABASE.size()]);

    /**
     * A set of all contact fields that are relevant for contacts referenced by the distribution list database table.
     */
    public static final EnumSet<ContactField> DISTLIST_DATABASE_RELEVANT = EnumSet.of(ContactField.OBJECT_ID, ContactField.FOLDER_ID,
        ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3,  ContactField.DISPLAY_NAME, ContactField.SUR_NAME,
        ContactField.GIVEN_NAME);

    /**
     * Sorts the supplied contact fields in an order appropriate for inserting / updating the backed columns in the database.
     *
     * @param fields The fields to sort
     * @return The array for convenience
     */
    public static ContactField[] sort(ContactField[] fields) {
        Arrays.sort(fields, new Comparator<ContactField>() {

            @Override
            public int compare(ContactField field1, ContactField field2) {
                if (field1 == field2) {
                    return 0;
                } else if (null == field1 || ContactField.DISPLAY_NAME.equals(field1)) {
                    return 1;
                } else if (null == field2 || ContactField.DISPLAY_NAME.equals(field2)) {
                    return -1;
                } else {
                    return field1.compareTo(field2);
                }
            }
        });
        return fields;
    }


    private Fields() {
        // prevent instantiation
    }

}
