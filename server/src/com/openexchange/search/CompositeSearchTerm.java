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
import com.openexchange.search.internal.terms.AndTerm;
import com.openexchange.search.internal.terms.NonTerm;
import com.openexchange.search.internal.terms.OrTerm;

/**
 * {@link CompositeSearchTerm} - Represents a compounded search term; e.g. <i>&lt;term1&gt;</i>&nbsp;<code>OR</code>
 * &nbsp;<i>&lt;term2&gt;</i>.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CompositeSearchTerm implements SearchTerm<SearchTerm<?>> {

    private static interface InstanceCreator extends Serializable {

        public CompositeSearchTerm newInstance();

    }

    /**
     * The composite operation enumeration.
     */
    public static enum CompositeOperation implements Operation {
        /**
         * The <i><code>AND</code></i> composite type.
         */
        AND("and", Integer.MAX_VALUE, new InstanceCreator() {

            private static final long serialVersionUID = -2839503961447478423L;

            public CompositeSearchTerm newInstance() {
                return new AndTerm();
            }
        }),
        /**
         * The <i><code>OR</code></i> composite type.
         */
        OR("or", Integer.MAX_VALUE, new InstanceCreator() {

            private static final long serialVersionUID = 8612089760772780923L;

            public CompositeSearchTerm newInstance() {
                return new OrTerm();
            }
        }),
        /**
         * The <i><code>NON</code></i> composite type.
         */
        NON("non", 1, new InstanceCreator() {

            private static final long serialVersionUID = 5131782739497011902L;

            public CompositeSearchTerm newInstance() {
                return new NonTerm();
            }
        });

        private final String str;

        private final InstanceCreator creator;

        private final int maxTerms;

        private CompositeOperation(final String str, final int maxTerms, final InstanceCreator creator) {
            this.str = str;
            this.creator = creator;
            this.maxTerms = maxTerms;
        }

        public String getOperation() {
            return str;
        }

        public boolean equalsOperation(final String other) {
            return str.equalsIgnoreCase(other);
        }

        /**
         * Gets a new composite search term for this operation.
         * 
         * @return A new composite search term for this operation.
         */
        public CompositeSearchTerm newInstance() {
            return creator.newInstance();
        }

        /**
         * Gets the max. number of search terms operands.
         * 
         * @return The max. number of search terms operands.
         */
        public int getMaxTerms() {
            return maxTerms;
        }

        private static final transient Map<String, CompositeOperation> map;

        static {
            final CompositeOperation[] values = CompositeOperation.values();
            final Map<String, CompositeOperation> m = new HashMap<String, CompositeOperation>(values.length);
            for (final CompositeOperation singleOperation : values) {
                m.put(singleOperation.str, singleOperation);
            }
            map = java.util.Collections.unmodifiableMap(m);
        }

        /**
         * Gets the composite operation corresponding to specified operation string.
         * 
         * @param operation The operation string.
         * @return The operation corresponding to specified operation string or <code>null</code>.
         */
        public static CompositeOperation getCompositeOperation(final String operation) {
            final CompositeOperation retval = map.get(operation);
            if (null != retval) {
                return retval;
            }
            final CompositeOperation[] values = CompositeOperation.values();
            for (final CompositeOperation value : values) {
                if (value.equalsOperation(operation)) {
                    return value;
                }
            }
            return null;
        }

        /**
         * Checks if specified operation string is a composite operation.
         * 
         * @param operation The operation string
         * @return <code>true</code> if specified operation string is a composite operation; otherwise <code>false</code>
         */
        public static boolean containsOperation(final String operation) {
            return (null != getCompositeOperation(operation));
        }
    }

    /**
     * The default capacity.
     */
    protected static final int DEFAULT_CAPACITY = 4;

    /**
     * The composite operation.
     */
    protected final CompositeOperation operation;

    /**
     * The search term operands.
     */
    protected final List<SearchTerm<?>> operands;

    /**
     * Initializes a new {@link CompositeSearchTerm} with default capacity (4).
     * 
     * @param operation The composite operation.
     */
    protected CompositeSearchTerm(final CompositeOperation operation) {
        this(operation, DEFAULT_CAPACITY);
    }

    /**
     * Initializes a new {@link CompositeSearchTerm}.
     * 
     * @param operation The composite operation.
     * @param initialCapacity The initial capacity of the list of operands
     */
    protected CompositeSearchTerm(final CompositeOperation operation, final int initialCapacity) {
        super();
        this.operation = operation;
        operands = new ArrayList<SearchTerm<?>>(initialCapacity);
    }

    public SearchTerm<?>[] getOperands() {
        return operands.toArray(new SingleSearchTerm[operands.size()]);
    }

    public Operation getOperation() {
        return operation;
    }

    /**
     * Adds specified search term.
     * 
     * @param searchTerm The search term to add.
     */
    public void addSearchTerm(final SearchTerm<?> searchTerm) {
        operands.add(searchTerm);
    }

    @Override
    public String toString() {
        return new StringBuilder(operation.getOperation()).append(':').append(operands).toString();
    }
}
