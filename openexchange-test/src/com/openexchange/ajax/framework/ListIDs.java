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

package com.openexchange.ajax.framework;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ListIDs implements Iterable<ListID> {

    private final List<ListID> identifiers = new ArrayList<ListID>();

    /**
     * Default constructor.
     */
    public ListIDs() {
        super();
    }

    /**
     * Convenience constructor for one ListID
     */
    public ListIDs(ListID listID) {
        this();
        add(listID);
    }

    /**
     * Convenience constructor for folder and id as ints
     */
    public ListIDs(int folder, int id) {
        this();
        add(new ListIDInt(folder, id));
    }

    public void add(final ListID listID) {
        identifiers.add(listID);
    }

    public int size() {
        return identifiers.size();
    }

    public ListID get(final int i) {
        return identifiers.get(i);
    }

    @Override
    public Iterator<ListID> iterator() {
        return identifiers.iterator();
    }

    public static ListIDs l(final int[]... identifiers) {
        final ListIDs retval = new ListIDs();
        for (final int[] identifier : identifiers) {
            assert identifier.length == 2;
            retval.add(new ListIDInt(identifier[0], identifier[1]));
        }
        return retval;
    }
}
