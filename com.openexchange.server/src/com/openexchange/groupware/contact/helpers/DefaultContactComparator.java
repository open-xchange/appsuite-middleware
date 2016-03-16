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

package com.openexchange.groupware.contact.helpers;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.Comparator;
import java.util.Locale;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;


/**
 * {@link DefaultContactComparator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class DefaultContactComparator implements Comparator<Contact>{

    private static interface ComparatorProvider {

        Comparator<Contact> getComparator(Locale locale);
    }

    private static final ComparatorProvider USE_COUNT_COMPARATOR_PROVIDER = new ComparatorProvider() {

        private final UseCountGlobalFirstComparator comparator = new UseCountGlobalFirstComparator();

        @Override
        public Comparator<Contact> getComparator(final Locale locale) {
            return comparator;
        }
    };

    private static final ComparatorProvider SPECIAL_COMPARATOR_PROVIDER = new ComparatorProvider() {

        @Override
        public Comparator<Contact> getComparator(final Locale locale) {
            return new SpecialAlphanumSortContactComparator(locale);
        }
    };

    private static final TIntObjectHashMap<ComparatorProvider> SPECIAL_COMPARATORS = new TIntObjectHashMap<ComparatorProvider>() {{
        put(Contact.SPECIAL_SORTING, SPECIAL_COMPARATOR_PROVIDER);
        put(Contact.USE_COUNT_GLOBAL_FIRST, USE_COUNT_COMPARATOR_PROVIDER);
    }};

    private final int field;
    private final Order order;
    private final Locale locale;

    public DefaultContactComparator(final int field, final Order order, final Locale locale) {
        super();
        this.field = field;
        this.order = order;

        this.locale = null == locale ? Locale.US : locale;
    }

    @Override
    public int compare(final Contact o1, final Contact o2) {
        if (field <= 0 || Order.NO_ORDER.equals(order)) {
            return 0;
        }
        final ComparatorProvider provider = SPECIAL_COMPARATORS.get(field);
        if (null != provider) {
            final Comparator<Contact> specialComparator = provider.getComparator(locale);
            return Order.DESCENDING.equals(order) ? -1 * specialComparator.compare(o1, o2) : specialComparator.compare(o1, o2);
        }
        final Object v1 = o1.get(field);
        final Object v2 = o2.get(field);
        return Order.DESCENDING.equals(order) ? -1 * internalCompare(v1, v2) : internalCompare(v1, v2);
    }

    private int internalCompare(final Object v1, final Object v2) {
        if(v1 == v2) {
            return 0;
        }
        if(v1 == null && v2 != null) {
            return -1;
        }
        if(v2 == null) {
            return 1;
        }
        if(Comparable.class.isInstance(v1)) {
            return ((Comparable)v1).compareTo(v2);
        }
        throw new UnsupportedOperationException("Don't know how to compare two values of class "+v1.getClass().getName());
    }

}
