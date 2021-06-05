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

package com.openexchange.mail;

import static com.openexchange.mail.utils.StorageUtility.UNLIMITED_QUOTA;

/**
 * {@link Quota}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Quota {

    /**
     * {@link Type} - The quota resource type
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    public static enum Type {
        /**
         * STORAGE resource: rfc822.size of contained messages
         */
        STORAGE("STORAGE"),
        /**
         * MESSAGE resource: number of messages
         */
        MESSAGE("MESSAGE");

        private final String typeStr;

        private Type(String typeStr) {
            this.typeStr = typeStr;
        }

        @Override
        public String toString() {
            return typeStr;
        }

        /**
         * Gets constant for unlimited quota for this resource type
         *
         * @return The constant for unlimited quota for this resource type
         */
        public Quota getUnlimited() {
            return new Quota(Quota.UNLIMITED, Quota.UNLIMITED, this);
        }
    }

    /**
     * Gets constant for unlimited quota for specified resource type
     * <p>
     * This is a convenience method that invokes {@link Type#getUnlimited()} for specified instance of {@link Type}.
     *
     * @param type The resource type
     * @return The constant for unlimited quota for specified resource type
     */
    public static Quota getUnlimitedQuota(Type type) {
        return type.getUnlimited();
    }

    /**
     * Gets constants for unlimited quota for specified resource types
     * <p>
     * This is a convenience method that invokes {@link Type#getUnlimited()} for each type in given array of {@link Type}.
     *
     * @param types The resource types
     * @return The constants for unlimited quota for specified resource types
     */
    public static Quota[] getUnlimitedQuotas(Type[] types) {
        final Quota[] quotas = new Quota[types.length];
        for (int i = 0; i < quotas.length; i++) {
            quotas[i] = types[i].getUnlimited();
        }
        return quotas;
    }

    /**
     * Constant which indicates unlimited quota
     *
     * @value <code>-1</code>
     */
    public static final int UNLIMITED = UNLIMITED_QUOTA;

    /**
     * The quota's limit
     */
    private final long limit;

    /**
     * The quota's usage
     */
    private final long usage;

    /**
     * The quota's resource type
     */
    private final Type type;

    /**
     * Initializes a new {@link Quota}
     *
     * @param limit The quota's limit. Storage value is expected to be kilobytes.
     * @param usage The quota's usage. Storage value is expected to be kilobytes.
     * @param type The quota's resource type to which this quota limitation applies
     */
    public Quota(long limit, long usage, Type type) {
        super();
        this.limit = limit;
        this.usage = usage;
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (limit ^ (limit >>> 32));
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + (int) (usage ^ (usage >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Quota)) {
            return false;
        }
        final Quota other = (Quota) obj;
        if (limit != other.limit) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (usage != other.usage) {
            return false;
        }
        return true;
    }

    /**
     * Gets the limit
     *
     * @return The limit. Storage value is returned as kilobytes.
     */
    public long getLimit() {
        return limit;
    }

    /**
     * Gets the usage
     *
     * @return The usage. Storage value is returned as kilobytes.
     */
    public long getUsage() {
        return usage;
    }

    /**
     * Gets the storage limit in bytes
     *
     * @return the limit
     * @throws IllegalStateException if type is {@link Type#MESSAGE}
     */
    public long getLimitBytes() {
        if (type == Type.MESSAGE) {
            throw new IllegalStateException("Quota.getLimitBytes() must only be called for quota type " + Type.STORAGE.name());
        }

        if (limit <= 0) {
            return limit;
        }

        return limit << 10;
    }

    /**
     * Gets the storage usage in bytes
     *
     * @return the usage
     * @throws IllegalStateException if type is {@link Type#MESSAGE}
     */
    public long getUsageBytes() {
        if (type == Type.MESSAGE) {
            throw new IllegalStateException("Quota.getUsageBytes() must only be called for quota type " + Type.STORAGE.name());
        }

        if (usage <= 0) {
            return usage;
        }

        return usage << 10;
    }

    /**
     * Gets the quota's resource type to which this quota limitation applies
     *
     * @return The quota's resource type to which this quota limitation applies
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns a newly created array of <code>long</code> from this quota's limit and usage values. Quota's limit is at index <code>0</code>
     * and its usage is located at index <code>1</code>. Storage values are in kilobytes.
     *
     * @return An array of <code>long</code> from this quota's limit and usage.
     */
    public long[] toLongArray() {
        return new long[] { limit, usage };
    }

    @Override
    public String toString() {
        String unit = type == Type.STORAGE ? " kB" : "";
        return String.format("Quota type=%s, limit=%d%s, usage=%d%s", type.name(), limit, unit, usage, unit);
    }
}
