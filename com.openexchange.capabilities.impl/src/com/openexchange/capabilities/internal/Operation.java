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
    private final Optional<String> optionalReason;

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
        this.optionalReason = optionalReason;

    }

    /**
     * Gets the optional reason text.
     *
     * @return The optional reason text
     */
    public Optional<String> getOptionalReason() {
        return optionalReason;
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
        if (!optionalReason.isPresent()) {
            return capabilityId;
        }

        StringBuilder builder = new StringBuilder(capabilityId);
        builder.append(" (").append(optionalReason.get()).append(')');
        return builder.toString();
    }

    @Override
    public int compareTo(Operation o) {
        return capabilityId.compareTo(o.capabilityId);
    }

}
