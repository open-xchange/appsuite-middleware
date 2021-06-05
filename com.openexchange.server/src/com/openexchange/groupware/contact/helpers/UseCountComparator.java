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

package com.openexchange.groupware.contact.helpers;

import java.util.Comparator;
import java.util.Locale;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class UseCountComparator implements Comparator<Contact> {

    /** The fall-back use-count comparator */
    public static final UseCountComparator FALLBACK_USE_COUNT_COMPARATOR = new UseCountComparator((Comparator<Contact>) null);

    private final Comparator<Contact> contactComparator;

    /**
     * Initializes a new {@link UseCountComparator}.
     *
     * @param locale The locale to use
     */
    public UseCountComparator(final Locale locale) {
        this(new SpecialAlphanumSortContactComparator(locale));
    }

    /**
     * Initializes a new {@link UseCountComparator}.
     *
     * @param comp The comparator to use if both - use-count and folder - are equal
     */
    public UseCountComparator(final Comparator<Contact> comp) {
        super();
        this.contactComparator = comp;
    }

    @Override
    public int compare(final Contact o1, final Contact o2) {
        int comp = o2.getUseCount() - o1.getUseCount();
        if (0 != comp) {
            return comp;
        }
        if (o1.getParentFolderID() != o2.getParentFolderID()) {
            if (o1.getParentFolderID() == FolderObject.SYSTEM_LDAP_FOLDER_ID) {
                return -1;
            }
            if (o2.getParentFolderID() == FolderObject.SYSTEM_LDAP_FOLDER_ID) {
                return 1;
            }
        }
        if (null != contactComparator) {
            return contactComparator.compare(o1, o2);
        }
        return o2.getDisplayName().compareTo(o1.getDisplayName());
    }

}
