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

package com.openexchange.capabilities.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * {@link History} - A set of operations, which were applied to a capability set.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class History implements Serializable {

    private static final long serialVersionUID = 1254265250025909179L;

    /** The operations map */
    private final transient List<Operation> operations;

    /**
     * Initializes a new {@link History}.
     */
    public History() {
        super();
        operations = new ArrayList<>();
    }

    /**
     * Adds specified operation to this history.
     *
     * @param operation The operation to add
     * @return <code>true</code> if an existent operation associated with the same capability has been replaced; otherwise <code>false</code>
     */
    public synchronized void addOperation(Operation operation) {
        operations.add(operation);
    }

    /**
     * Gets a mapping of operations grouped by type. Each listing is chronologically sorted.
     *
     * @return The operation mapping
     */
    public synchronized Map<Operation.Type, List<Operation>> getGroupedOperations() {
        Map<Operation.Type, List<Operation>> map = new TreeMap<>();

        for (Operation operation : this.operations) {
            List<Operation> ops = map.get(operation.getType());
            if (ops == null) {
                ops = new ArrayList<>();
                map.put(operation.getType(), ops);
            }
            ops.add(operation);
        }

        return map;
    }

    /**
     * Gets a chronological listing of this history's operations.
     *
     * @return The operations
     */
    public synchronized List<Operation> getOperations() {
        return new ArrayList<>(operations);
    }

    /**
     * Checks if this history contains operations.
     *
     * @return <tt>true</tt> if this history contains no operations; otherwise <code>false</code>
     */
    public synchronized boolean isEmpty() {
        return this.operations.isEmpty();
    }

    @Override
    public synchronized String toString() {
        return operations.toString();
    }

}
