/*
 * The Alphanum Algorithm is an improved sorting algorithm for strings
 * containing numbers.  Instead of sorting numbers in ASCII order like
 * a standard sort, this algorithm sorts numbers in numeric order.
 *
 * The Alphanum Algorithm is discussed at http://www.DaveKoelle.com
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package com.openexchange.groupware.contact.helpers;

import java.util.Comparator;
import java.util.Locale;
import com.davekoelle.AlphanumComparator;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;

/**
 * {@link SpecialAlphanumSortContactComparator} - Sorts with respect to {@link Contact#SPECIAL_SORTING}. Considering given names, too, if
 * last names are equal.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SpecialAlphanumSortContactComparator implements Comparator<Contact> {

    /**
     * The string comparator used to compare non-null strings.
     */
    private final Comparator<String> stringComparator;

    /**
     * Whether to inverse comparison result.
     */
    private final int inverse;

    /**
     * Initializes a new {@link SpecialAlphanumSortContactComparator}.
     */
    public SpecialAlphanumSortContactComparator() {
        super();
        /*
         * The glorious alphanum comparator used to compare non-null strings.
         */
        stringComparator = new AlphanumComparator();
        inverse = 1;
    }

    /**
     * Initializes a new {@link SpecialAlphanumSortContactComparator}.
     *
     * @param locale The locale
     */
    public SpecialAlphanumSortContactComparator(final Locale locale) {
        super();
        /*
         * The glorious alphanum comparator used to compare non-null strings.
         */
        stringComparator = new AlphanumComparator(locale);
        inverse = 1;
    }

    /**
     * Initializes a new {@link SpecialAlphanumSortContactComparator}.
     *
     * @param stringComparator The string comparator
     * @param sortOrder The sort order
     */
    public SpecialAlphanumSortContactComparator(final Comparator<String> stringComparator, final Order sortOrder) {
        super();
        this.stringComparator = stringComparator;
        this.inverse = sortOrder == Order.DESCENDING ? -1 : 1;
    }

    @Override
    public int compare(final Contact contact1, final Contact contact2) {
        final int nonNullField1 = detectFirstNonNullField(contact1);
        final int nonNullField2 = detectFirstNonNullField(contact2);
        final int compared =
            stringComparator.compare(
                0 == nonNullField1 ? "" : contact1.get(nonNullField1).toString(),
                0 == nonNullField2 ? "" : contact2.get(nonNullField2).toString());
        if (0 == compared && isLastName(nonNullField1) && isLastName(nonNullField2)) {
            /*
             * Both last names are equal. Consider first name, too (if non-null)
             */
            return inverse * compareGivenName(contact1, contact2);
        }
        return inverse * compared;
    }

    /**
     * Checks if specified field is either YOMI last name or surname.
     *
     * @param field The field to check
     * @return <code>true</code> if specified field is either YOMI last name or surname; otherwise <code>false</code>
     */
    private static boolean isLastName(final int field) {
        return Contact.YOMI_LAST_NAME == field || Contact.SUR_NAME == field;
    }

    /**
     * Compares the given names of specified contacts; preferring YOMI first names.
     *
     * @param contact1 The first contact
     * @param contact2 The second contact
     * @return The comparison result
     */
    private int compareGivenName(final Contact contact1, final Contact contact2) {
        final String givenName1 =
            contact1.containsYomiFirstName() ? contact1.getYomiFirstName() : (contact1.containsGivenName() ? contact1.getGivenName() : "");
        final String givenName2 =
            contact2.containsYomiFirstName() ? contact2.getYomiFirstName() : (contact2.containsGivenName() ? contact2.getGivenName() : "");
        return stringComparator.compare(givenName1, givenName2);
    }

    /**
     * Gets the field number for the first non-<code>null</code> value in specified contact following this order:
     * <ol>
     * <li>YOMI last name</li>
     * <li>surname</li>
     * <li>display name</li>
     * <li>YOMI company</li>
     * <li>company</li>
     * <li>email1</li>
     * <li>email2</li>
     * </ol>
     *
     * @param contact The contact
     * @return The field number for first non-<code>null</code> field or <code>0</code> if each value of the sequence was <code>null</code>
     */
    private static int detectFirstNonNullField(final Contact contact) {
        final int retval;
        if (contact.containsYomiLastName()) {
            retval = Contact.YOMI_LAST_NAME;
        } else if (contact.containsSurName()) {
            retval = Contact.SUR_NAME;
        } else if (contact.containsDisplayName()) {
            retval = Contact.DISPLAY_NAME;
        } else if (contact.containsYomiCompany()) {
            retval = Contact.YOMI_COMPANY;
        } else if (contact.containsCompany()) {
            retval = Contact.COMPANY;
        } else if (contact.containsEmail1()) {
            retval = Contact.EMAIL1;
        } else if (contact.containsEmail2()) {
            retval = Contact.EMAIL2;
        } else {
            retval = 0; // Neutral
        }
        return retval;
    }

}
