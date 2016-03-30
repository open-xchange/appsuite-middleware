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
