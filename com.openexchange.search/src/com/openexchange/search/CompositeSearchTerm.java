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

package com.openexchange.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.search.internal.terms.AndTerm;
import com.openexchange.search.internal.terms.NotTerm;
import com.openexchange.search.internal.terms.OrTerm;

/**
 * {@link CompositeSearchTerm} - Represents a compounded search term; e.g. <i>&lt;term1&gt;</i>&nbsp;<code>OR</code>
 * &nbsp;<i>&lt;term2&gt;</i>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CompositeSearchTerm implements SearchTerm<SearchTerm<?>> {

	static final int PRETTY_BIG_NUMBER = Integer.MAX_VALUE / 2; //needed since someone used MAX_VALUE instead and then did a MAX_VALUE+1 comparison. Note: This is also wrong. Since this is going to become SQL code, it should reflect the allowed maximum length of a query...

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
        AND("and", PRETTY_BIG_NUMBER, "AND", OperationPosition.BETWEEN, "&%s", new InstanceCreator() {

            private static final long serialVersionUID = -2839503961447478423L;

            @Override
            public CompositeSearchTerm newInstance() {
                return new AndTerm();
            }
        }),
        /**
         * The <i><code>OR</code></i> composite type.
         */
        OR("or", PRETTY_BIG_NUMBER, "OR", OperationPosition.BETWEEN, "|%s", new InstanceCreator() {

            private static final long serialVersionUID = 8612089760772780923L;

            @Override
            public CompositeSearchTerm newInstance() {
                return new OrTerm();
            }
        }),
        /**
         * The <i><code>NOT</code></i> composite type.
         */
        NOT("not", 1, "!", OperationPosition.BEFORE, "!%s", new InstanceCreator() {

            private static final long serialVersionUID = 5131782739497011902L;

            @Override
            public CompositeSearchTerm newInstance() {
                return new NotTerm();
            }
        });

        private final String str;

        private final String sql;

        private String ldap;

        private final InstanceCreator creator;

        private final int maxTerms;

		private OperationPosition sqlPos;

        private CompositeOperation(final String str, final int maxTerms, final String sql, final OperationPosition sqlPos, final String ldap, final InstanceCreator creator) {
            this.str = str;
            this.creator = creator;
            this.maxTerms = maxTerms;
            this.sql = sql;
            this.ldap = ldap;
            this.sqlPos = sqlPos;
        }

        @Override
        public String getOperation() {
            return str;
        }

        @Override
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

        @Override
        public OperationPosition getSqlPosition() {
        	return sqlPos;
        }

        private static final transient Map<String, CompositeOperation> map;

        static {
            CompositeOperation[] values = CompositeOperation.values();
            Map<String, CompositeOperation> m = new HashMap<String, CompositeOperation>(values.length);
            for (CompositeOperation singleOperation : values) {
                m.put(singleOperation.str, singleOperation);
            }
            map = ImmutableMap.copyOf(m);
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

		@Override
        public String getSqlRepresentation() {
			return sql;
		}

		@Override
        public String getLdapRepresentation() {
			return ldap;
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
    public CompositeSearchTerm(final CompositeOperation operation) {
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

    @Override
    public SearchTerm<?>[] getOperands() {
        return operands.toArray(new SearchTerm[operands.size()]);
    }

    @Override
    public Operation getOperation() {
        return operation;
    }

    /**
     * Adds specified search term.
     *
     * @param searchTerm The search term to add.
     * @return A self reference
     */
    public CompositeSearchTerm addSearchTerm(final SearchTerm<?> searchTerm) {
        operands.add(searchTerm);
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder(operation.getOperation()).append(':').append(operands).toString();
    }
}
