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
import java.util.Optional;

/**
 * {@link Operation} - An operation applied to a capability set.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class Operation implements Serializable, Comparable<Operation> {

    private static final long serialVersionUID = -7756821897342237613L;

    /** The operation types */
    public static enum Type {

        /** Certain capability has been added */
        ADDED("added", "Listed capabilities were added to the set of user's capabilities"),
        /** Certain capability was tried being added, but such a capability was already existent */
        NOT_ADDED("not-added", "Listed capabilities were tried being added to the set of user's capabilities, but were already contained"),
        /** Certain capability has been removed */
        REMOVED("removed", "Listed capabilities were removed from the set of user's capabilities"),
        /** Certain capability was tried being removed, but was <b>not</b> existent */
        NOT_REMOVED("not-removed", "Listed capabilities were tried being removed from the set of user's capabilities, but were not contained"),
        ;

        private final String identifier;
        private final String description;

        /**
         * Initializes a new {@link Type}.
         *
         * @param identifier The type identifier
         * @param description The type's description
         */
        private Type(String identifier, String description) {
            this.identifier = identifier;
            this.description = description;
        }

        /**
         * Gets the identifier
         *
         * @return The identifier
         */
        public String getIdentifier() {
            return identifier;
        }

        /**
         * Gets the description
         *
         * @return The description
         */
        public String getDescription() {
            return description;
        }
    }

    /**
     * Creates a new operation that reflects an addition of a capability to a capability set.
     *
     * @param capabilityId The capability identifier
     * @param optionalReason The optional reason string
     * @return The newly created operation
     */
    public static Operation addingOperation(String capabilityId, Optional<String> optionalReason) {
        return new Operation(capabilityId, Type.ADDED, optionalReason);
    }

    /**
     * Creates a new operation that reflects a failed addition of a capability to a capability set.
     *
     * @param capabilityId The capability identifier
     * @param optionalReason The optional reason string
     * @return The newly created operation
     */
    public static Operation noopAddingOperation(String capabilityId, Optional<String> optionalReason) {
        return new Operation(capabilityId, Type.NOT_ADDED, optionalReason);
    }

    /**
     * Creates a new operation that reflects a removal of a capability from a capability set.
     *
     * @param capabilityId The capability identifier
     * @param optionalReason The optional reason string
     * @return The newly created operation
     */
    public static Operation removingOperation(String capabilityId, Optional<String> optionalReason) {
        return new Operation(capabilityId, Type.REMOVED, optionalReason);
    }

    /**
     * Creates a new operation that reflects a failed removal of a capability from a capability set.
     *
     * @param capabilityId The capability identifier
     * @param optionalReason The optional reason string
     * @return The newly created operation
     */
    public static Operation noopRemovingOperation(String capabilityId, Optional<String> optionalReason) {
        return new Operation(capabilityId, Type.NOT_REMOVED, optionalReason);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final String capabilityId;
    private final Type type;
    private final String optionalReason;

    /**
     * Initializes a new {@link Operation}.
     *
     * @param capabilityId The capability identifier
     * @param type The operation type
     * @param optionalReason The optional reason string
     */
    private Operation(String capabilityId, Type type, Optional<String> optionalReason) {
        super();
        this.capabilityId = capabilityId;
        this.type = type;
        this.optionalReason = optionalReason.orElse(null);

    }

    /**
     * Gets the optional reason text.
     *
     * @return The optional reason text
     */
    public Optional<String> getOptionalReason() {
        return Optional.ofNullable(optionalReason);
    }

    /**
     * Gets the capability identifier
     *
     * @return The capability identifier
     */
    public String getCapabilityId() {
        return capabilityId;
    }

    /**
     * Gets the operation type
     *
     * @return The operation type
     */
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        if (optionalReason == null) {
            return capabilityId;
        }

        StringBuilder builder = new StringBuilder(capabilityId);
        builder.append(" (").append(optionalReason).append(')');
        return builder.toString();
    }

    @Override
    public int compareTo(Operation o) {
        return capabilityId.compareTo(o.capabilityId);
    }

}
