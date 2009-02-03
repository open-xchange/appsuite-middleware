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

package com.openexchange.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.search.internal.terms.EqualsTerm;
import com.openexchange.search.internal.terms.GreaterThanTerm;
import com.openexchange.search.internal.terms.LessThanTerm;

/**
 * {@link SingleSearchTerm} - A single search term; e.g. <i>equals</i>.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SingleSearchTerm implements SearchTerm<Operand<?>> {

    private static interface InstanceCreator extends Serializable {

        public SingleSearchTerm newInstance();

    }

    /**
     * The search term's operation enumeration.
     */
    public static enum SingleOperation implements Operation {
        /**
         * Equals comparison
         */
        EQUALS("equals", 2, new InstanceCreator() {

            private static final long serialVersionUID = -7337346107116884060L;

            public SingleSearchTerm newInstance() {
                return new EqualsTerm();
            }
        }),
        /**
         * Less-than comparison
         */
        LESS_THAN("lt", 2, new InstanceCreator() {

            private static final long serialVersionUID = 3045641432242061311L;

            public SingleSearchTerm newInstance() {
                return new LessThanTerm();
            }
        }),
        /**
         * Greater-than comparison
         */
        GREATER_THAN("gt", 2, new InstanceCreator() {

            private static final long serialVersionUID = 8960232001776390636L;

            public SingleSearchTerm newInstance() {
                return new GreaterThanTerm();
            }
        });

        private final String str;

        private final InstanceCreator creator;

        private final int maxOperands;

        private SingleOperation(final String str, final int maxOperands, final InstanceCreator creator) {
            this.str = str;
            this.maxOperands = maxOperands;
            this.creator = creator;
        }

        public String getOperation() {
            return str;
        }

        public boolean equalsOperation(final String other) {
            return str.equalsIgnoreCase(other);
        }

        /**
         * Gets a new single search term for this operation.
         * 
         * @return A new single search term for this operation.
         */
        public SingleSearchTerm newInstance() {
            return creator.newInstance();
        }

        /**
         * Gets the max. number of operands.
         * 
         * @return The max. number of operands.
         */
        public int getMaxOperands() {
            return maxOperands;
        }

        private static final transient Map<String, SingleOperation> map;

        static {
            final SingleOperation[] values = SingleOperation.values();
            final Map<String, SingleOperation> m = new HashMap<String, SingleOperation>(values.length);
            for (final SingleOperation singleOperation : values) {
                m.put(singleOperation.str, singleOperation);
            }
            map = java.util.Collections.unmodifiableMap(m);
        }

        /**
         * Gets the single operation corresponding to specified operation string.
         * 
         * @param operation The operation string.
         * @return The operation corresponding to specified operation string or <code>null</code>.
         */
        public static SingleOperation getSingleOperation(final String operation) {
            final SingleOperation retval = map.get(operation);
            if (null != retval) {
                return retval;
            }
            final SingleOperation[] values = SingleOperation.values();
            for (final SingleOperation value : values) {
                if (value.equalsOperation(operation)) {
                    return value;
                }
            }
            return null;
        }

        /**
         * Checks if specified operation string is a single operation.
         * 
         * @param operation The operation string
         * @return <code>true</code> if specified operation string is a single operation; otherwise <code>false</code>
         */
        public static boolean containsOperation(final String operation) {
            return (null != getSingleOperation(operation));
        }
    }

    /**
     * The default capacity.
     */
    protected static final int DEFAULT_CAPACITY = 4;

    /**
     * The single operation.
     */
    protected final SingleOperation operation;

    /**
     * The operands.
     */
    protected final List<Operand<?>> operands;

    /**
     * Initializes a new {@link SingleSearchTerm} with default capacity (4).
     * 
     * @param operation The operation
     */
    protected SingleSearchTerm(final SingleOperation operation) {
        this(operation, DEFAULT_CAPACITY);
    }

    /**
     * Initializes a new {@link SingleSearchTerm}.
     * 
     * @param operation The operation
     * @param initialCapacity The initial capacity
     */
    protected SingleSearchTerm(final SingleOperation operation, final int initialCapacity) {
        super();
        this.operation = operation;
        operands = new ArrayList<Operand<?>>(initialCapacity);
    }

    public Operand<?>[] getOperands() {
        return operands.toArray(new Operand<?>[operands.size()]);
    }

    public Operation getOperation() {
        return operation;
    }

    /**
     * Adds specified operand to this search term.
     * 
     * @param operand The operand to add.
     */
    public void addOperand(final Operand<?> operand) {
        operands.add(operand);
    }

    @Override
    public String toString() {
        return new StringBuilder(operation.getOperation()).append(':').append(operands).toString();
    }
}
