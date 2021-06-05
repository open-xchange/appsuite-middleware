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
