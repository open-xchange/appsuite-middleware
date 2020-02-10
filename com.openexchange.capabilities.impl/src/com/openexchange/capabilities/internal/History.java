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
