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

/**
 * {@link SpecialAlphanumSortContactComparator} - Sorts with respect to {@link Contact#SPECIAL_SORTING}. Considering given names, too, if
 * last names are equal.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SpecialAlphanumSortContactComparator implements Comparator<Contact> {

    private final Comparator<String> delegate;
    private final Locale locale;

    /**
     * Initializes a new {@link SpecialAlphanumSortContactComparator} with default locale {@link Locale#US}.
     */
    public SpecialAlphanumSortContactComparator() {
        this(Locale.US);
    }

    /**
     * Initializes a new {@link SpecialAlphanumSortContactComparator}.
     *
     * @param locale The locale
     */
    public SpecialAlphanumSortContactComparator(Locale locale) {
        super();
        Locale localeToUse = null == locale ? Locale.US : locale;
        this.delegate = new AlphanumComparator(localeToUse);
        this.locale = localeToUse;
    }

    @Override
    public int compare(Contact contact1, Contact contact2) {
        String sortName1 = contact1.getSortName(locale);
        String sortName2 = contact2.getSortName(locale);
        if (null == sortName1) {
            return null == sortName2 ? 0 : -1;
        }
        if (null == sortName2) {
            return 1;
        }
        return delegate.compare(sortName1, sortName2);
    }

}
