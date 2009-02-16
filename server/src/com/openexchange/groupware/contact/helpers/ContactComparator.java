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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import com.openexchange.groupware.container.ContactObject;

/**
 * A special implementation of a comparator that uses the first not null attribute of a contact in order of surname, display name, company,
 * business email address, private email address.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ContactComparator implements Comparator<ContactObject> {

    private static final AlphanumComparator alphanumComparator = new AlphanumComparator();

    /**
     * Default constructor.
     */
    public ContactComparator() {
        super();
    }

    public int compare(final ContactObject contact1, final ContactObject contact2) {
        final String s1 = getFirstNotNull(contact1);
        final String s2 = getFirstNotNull(contact2);
        return alphanumComparator.compare(s1, s2);
    }

    private String getFirstNotNull(final ContactObject contact) {
        final String retval;
        if (contact.containsSurName()) {
            retval = contact.getSurName();
        } else if (contact.containsDisplayName()) {
            retval = contact.getDisplayName();
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
