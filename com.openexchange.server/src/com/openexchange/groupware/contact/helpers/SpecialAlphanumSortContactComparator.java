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
        this(new AlphanumComparator(), Order.ASCENDING);
    }

    /**
     * Initializes a new {@link SpecialAlphanumSortContactComparator}.
     *
     * @param locale The locale
     */
    public SpecialAlphanumSortContactComparator(final Locale locale) {
        this(new AlphanumComparator(locale), Order.ASCENDING);
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
        String sortName1 = contact1.getSortName();
        String sortName2 = contact2.getSortName();
        int compared;
        if (null == sortName1) {
            compared = null == sortName2 ? 0 : -1;
        } else if (null == sortName2) {
            compared = 1;
        } else {
            compared = stringComparator.compare(sortName1, sortName2);
        }
        return inverse * compared;
    }

}
