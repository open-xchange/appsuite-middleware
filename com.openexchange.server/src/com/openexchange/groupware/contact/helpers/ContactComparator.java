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
import com.davekoelle.AlphanumComparator;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;

/**
 * A special implementation of a comparator that uses the first not null attribute of a contact in order of surname, display name, company,
 * business email address, private email address.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @deprecated Use {@link SpecialAlphanumSortContactComparator} instead!!!
 */
@Deprecated
public class ContactComparator implements Comparator<Contact> {

	private final Comparator<String> comparator;
	private final int weight;

    /**
     * Default constructor.
     *
     * @deprecated Use {@link SpecialAlphanumSortContactComparator} instead!!!
     */
    @Deprecated
    public ContactComparator() {
        super();
        this.comparator = new AlphanumComparator();
        weight = 1;
    }

    /**
     * Initializes a new {@link ContactComparator}.
     *
     * @param comp The string comparator
     * @param sortOrder The sort order
     * @deprecated Use {@link SpecialAlphanumSortContactComparator} instead!!!
     */
    @Deprecated
    public ContactComparator(final Comparator<String> comp, final Order sortOrder) {
        super();
        this.comparator = comp;
        this.weight = sortOrder == Order.DESCENDING ? -1 : 1;
    }

    @Override
    public int compare(final Contact contact1, final Contact contact2) {
        final String s1 = getFirstNotNull(contact1);
        final String s2 = getFirstNotNull(contact2);
        return weight * comparator.compare(s1, s2);
    }

    private String getFirstNotNull(final Contact contact) {
        final String retval;
        if (contact.containsYomiLastName()) {
            /*
             * Consider (yomi) given name, too
             */
            String appendix = contact.getYomiFirstName();
            if (null == appendix) {
                appendix = contact.getGivenName();
            }
            retval =
                null == appendix ? contact.getYomiLastName() : new StringBuilder(contact.getYomiLastName()).append(' ').append(appendix).toString();
        } else if (contact.containsSurName()) {
            /*-
             * Consider (yomi) given name, too
             *
             * TODO: Prefer normal given name when sorting by normal surname?
             */
            String appendix = contact.getYomiFirstName();
            if (null == appendix) {
                appendix = contact.getGivenName();
            }
            retval =
                null == appendix ? contact.getSurName() : new StringBuilder(contact.getSurName()).append(' ').append(appendix).toString();
        } else if (contact.containsDisplayName()) {
            retval = contact.getDisplayName();
        } else if (contact.containsYomiCompany()) {
        	retval = contact.getYomiCompany();
        } else if (contact.containsCompany()) {
            retval = contact.getCompany();
        } else if (contact.containsEmail1()) {
            retval = contact.getEmail1();
        } else if (contact.containsEmail2()) {
            retval = contact.getEmail2();
        } else {
            retval = "";
        }
        return retval;
    }
}
