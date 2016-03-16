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
