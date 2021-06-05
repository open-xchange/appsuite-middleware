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
import com.openexchange.search.internal.terms.EqualsTerm;
import com.openexchange.search.internal.terms.GreaterOrEqualTerm;
import com.openexchange.search.internal.terms.GreaterThanTerm;
import com.openexchange.search.internal.terms.IsNullTerm;
import com.openexchange.search.internal.terms.LessOrEqualTerm;
import com.openexchange.search.internal.terms.LessThanTerm;
import com.openexchange.search.internal.terms.NotEqualTerm;

/**
 * {@link SingleSearchTerm} - A single search term; e.g. <i>equals</i>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SingleSearchTerm implements SearchTerm<Operand<?>> {

    private static interface InstanceCreator extends Serializable {

        SingleSearchTerm newInstance();
    }

    /**
     * The search term's operation enumeration.
     */
    public static enum SingleOperation implements Operation {
        /**
         * Equals comparison
         */
    	EQUALS("=", 2, "=", OperationPosition.BETWEEN, "%s=%s", new InstanceCreator() {

            private static final long serialVersionUID = -7337346107116884060L;

            @Override
            public SingleSearchTerm newInstance() {
                return new EqualsTerm();
            }
        }),
        /**
         * Less-than comparison
         */
        LESS_THAN("<", 2, "<", OperationPosition.BETWEEN, "&(%1$s<=%2$s)(!(%1$s=%2$s))", new InstanceCreator() {

            private static final long serialVersionUID = 3045641432242061311L;

            @Override
            public SingleSearchTerm newInstance() {
                return new LessThanTerm();
            }
        }),
        /**
         * Greater-than comparison
         */
        GREATER_THAN(">", 2, ">", OperationPosition.BETWEEN, "&(%1$s>=%2$s)(!(%1$s=%2$s))", new InstanceCreator() {

            private static final long serialVersionUID = 8960232001776390636L;

            @Override
            public SingleSearchTerm newInstance() {
                return new GreaterThanTerm();
            }
        }),
        /**
         * Not-equal comparison
         */
        NOT_EQUALS("<>", 2, "<>", OperationPosition.BETWEEN, "!(%s=%s)", new InstanceCreator() {

            private static final long serialVersionUID = 2354286763646087233L;

            @Override
            public SingleSearchTerm newInstance() {
                return new NotEqualTerm();
            }
        }),
        /**
         * Greater-than or equal comparison
         */
        GREATER_OR_EQUAL(">=", 2, ">=", OperationPosition.BETWEEN, "%s>=%s", new InstanceCreator() {

            private static final long serialVersionUID = 1820374998914432375L;

            @Override
            public SingleSearchTerm newInstance() {
                return new GreaterOrEqualTerm();
            }
        }),
        /**
         * Less-than or equal comparison
         */
        LESS_OR_EQUAL("<=", 2, "<=", OperationPosition.BETWEEN, "%s<=%s", new InstanceCreator() {

            private static final long serialVersionUID = 5173216283440032636L;

            @Override
            public SingleSearchTerm newInstance() {
                return new LessOrEqualTerm();
            }
        }),
        /**
         * is null check
         */
        ISNULL("isNull", 1, "IS NULL", OperationPosition.AFTER, "!(%s=*)", new InstanceCreator() {

            private static final long serialVersionUID = 1341911248645665193L;

            @Override
            public SingleSearchTerm newInstance() {
                return new IsNullTerm();
            }
        })
        ;

        private final String str;

        private final String sql;

        private String ldap;

        private final InstanceCreator creator;

        private final int maxOperands;

		private OperationPosition sqlPos;

		private OperationPosition ldapPos;


        private SingleOperation(final String str, final int maxOperands, final String sql, final OperationPosition sqlPos, final String ldap, final InstanceCreator creator) {
            this.str = str;
            this.maxOperands = maxOperands;
            this.creator = creator;
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

        @Override
        public OperationPosition getSqlPosition() {
        	return sqlPos;
        }

        private static final transient Map<String, SingleOperation> map;

        static {
            SingleOperation[] values = SingleOperation.values();
            Map<String, SingleOperation> m = new HashMap<String, SingleOperation>(values.length);
            for (SingleOperation singleOperation : values) {
                m.put(singleOperation.str, singleOperation);
            }
            map = ImmutableMap.copyOf(m);
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

		@Override
        public String getSqlRepresentation() {
			return sql;
		}

		@Override
        public String getLdapRepresentation() {
			return ldap;
		}

		public com.openexchange.search.SearchTerm.OperationPosition getLdapPosition() {
			return ldapPos;
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
    public SingleSearchTerm(final SingleOperation operation) {
        this(operation, DEFAULT_CAPACITY);
    }

    /**
     * Initializes a new {@link SingleSearchTerm}.
     *
     * @param operation The operation
     * @param initialCapacity The initial capacity
     */
    public SingleSearchTerm(final SingleOperation operation, final int initialCapacity) {
        super();
        this.operation = operation;
        operands = new ArrayList<Operand<?>>(initialCapacity);
    }

    @Override
    public Operand<?>[] getOperands() {
        return operands.toArray(new Operand<?>[operands.size()]);
    }

    @Override
    public SingleOperation getOperation() {
        return operation;
    }

    /**
     * Adds specified operand to this search term.
     *
     * @param operand The operand to add.
     * @return A self reference
     */
    public SingleSearchTerm addOperand(final Operand<?> operand) {
        operands.add(operand);
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder(operation.getOperation()).append(':').append(operands).toString();
    }
}
