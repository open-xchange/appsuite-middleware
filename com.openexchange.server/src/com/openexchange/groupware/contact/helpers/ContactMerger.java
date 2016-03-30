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

import java.util.LinkedList;
import java.util.List;
import com.openexchange.groupware.container.Contact;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ContactMerger {

    private boolean overwrite = false;

    public ContactMerger(boolean overwrite) {
        setOverwrite(overwrite);
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * Merges the first object with the second one and returns a merged one. In case of conflicts: If <i>overwrite</i> is set, values from
     * the second object take precedence, otherwise the values from the first do.
     *
     * @param c1 first object to be merged. This one will be changed afterwards.
     * @param c2 second object to be merged. This one will not be changed at all.
     */
    public Contact merge(Contact c1, Contact c2) {
        Contact clone = c1.clone();

        for (ContactField field : ContactField.values()) {
        	if (field.isVirtual()) {
        		continue;
        	}
            int number = field.getNumber();
            if (c2.contains(number)) {
                if (overwrite || !c1.contains(number)) {
                    clone.set(number, c2.get(number));
                }
            }
        }

        return clone;
    }

    public List<Contact> merge(List<Contact> list1, List<Contact> list2) {
        if (list1.size() != list2.size()) {
            throw new IllegalArgumentException("Both lists to be merged must be same length, but are: " + list1.size() + "/" + list2.size());
        }

        LinkedList<Contact> merged = new LinkedList<Contact>();
        for (int i = 0, length = list1.size(); i < length; i++) {
            merged.add(merge(list1.get(i), list2.get(i)));
        }

        return merged;
    }
}
