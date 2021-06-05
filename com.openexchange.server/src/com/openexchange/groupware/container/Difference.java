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

package com.openexchange.groupware.container;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Difference}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Difference {

    public static final int COMMON = -1;

    private final List<Object> added;
    private final List<Object> removed;
    private final List<Change> changed;
    private int field;

    /**
     * Initializes a new {@link Difference}.
     */
    public Difference() {
        this(COMMON);
    }

    /**
     * Initializes a new {@link Difference}.
     *
     * @param field The field identifier
     */
    public Difference(int field) {
        super();
        added = new ArrayList<Object>();
        removed = new ArrayList<Object>();
        changed = new ArrayList<Change>();
        this.field = field;
    }

    public List<Object> getAdded() {
        return added;
    }

    public List<Object> getRemoved() {
        return removed;
    }

    public List<Change> getChanged() {
        return changed;
    }

    public int getField() {
        return field;
    }

    public void setField(int field) {
        this.field = field;
    }
}
