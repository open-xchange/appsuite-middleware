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
import java.util.HashMap;
import java.util.Map;
import com.openexchange.groupware.container.Contact;


/**
 * {@link DefaultContactComparator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class DefaultContactComparator implements Comparator<Contact>{

    private Map<Integer, Comparator<Contact>> SPECIAL_COMPARATORS = new HashMap<Integer, Comparator<Contact>>() {{
        put(Contact.SPECIAL_SORTING, new ContactComparator());
        put(Contact.USE_COUNT_GLOBAL_FIRST, new UseCountGlobalFirstComparator());
    }};
    
    private int field;
    private boolean descending;

    public DefaultContactComparator(int field, String orderDir) {
        this.field = field;
        if(orderDir != null) {
            this.descending = orderDir.equalsIgnoreCase("DESC");
        }
    }

    public int compare(Contact o1, Contact o2) {
        if(field <= 0) {
            return 1;
        }
        int rating;
        if(SPECIAL_COMPARATORS.containsKey(field)) {
            rating = SPECIAL_COMPARATORS.get(field).compare(o1, o2);
        } else {
            Object v1 = o1.get(field);
            Object v2 = o2.get(field);
            rating = internalCompare(v1, v2);
        }
        if(descending) {
            rating = -rating;
        }
        return  rating;
    }
    
    private int internalCompare(Object v1, Object v2) {
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
