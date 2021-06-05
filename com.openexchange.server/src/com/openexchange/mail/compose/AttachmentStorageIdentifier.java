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

package com.openexchange.mail.compose;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * {@link AttachmentStorageIdentifier}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class AttachmentStorageIdentifier {

    /** The enumeration for known arguments */
    public static enum KnownArgument {
        /**
         * The argument providing file storage identifier.
         */
        FILE_STORAGE_IDENTIFIER("fileStorageId"),
        ;

        private final String name;

        private KnownArgument(String name) {
            this.name = name;
        }

        /**
         * Gets the argument name
         *
         * @return The argument name
         */
        public String getName() {
            return name;
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------------

    private final String identifier;
    private final Map<String, Object> arguments;

    /**
     * Initializes a new {@link AttachmentStorageIdentifier}.
     *
     * @param identifier The identifier of the item in storage
     */
    public AttachmentStorageIdentifier(String identifier) {
        this(identifier, null);
    }

    /**
     * Initializes a new {@link AttachmentStorageIdentifier}.
     *
     * @param identifier The identifier of the item in storage
     * @param knownArgument The known argument
     * @param value The argument's value
     */
    public AttachmentStorageIdentifier(String identifier, KnownArgument knownArgument, Object value) {
        this(identifier, Collections.singletonMap(knownArgument.getName(), value));
    }

    /**
     * Initializes a new {@link AttachmentStorageIdentifier}.
     *
     * @param identifier The identifier of the item in storage
     * @param arguments The optional arguments
     */
    public AttachmentStorageIdentifier(String identifier, Map<String, Object> arguments) {
        super();
        this.identifier = identifier;
        this.arguments = arguments == null || arguments.isEmpty() ? null : arguments;
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
     * Gets the optional arguments
     *
     * @return The optional arguments
     */
    public Optional<Map<String, Object>> getArguments() {
        return Optional.ofNullable(arguments);
    }

    /**
     * Gets the argument for given name.
     *
     * @param <A> The argument type
     * @param knownArgument The argument to look-up
     * @return The argument
     */
    public <A> Optional<A> getArgument(KnownArgument knownArgument) {
        return knownArgument == null ? Optional.empty() : getArgument(knownArgument.getName());
    }

    /**
     * Gets the argument for given name.
     *
     * @param <A> The argument type
     * @param name The argument name to look-up by
     * @return The argument
     */
    public <A> Optional<A> getArgument(String name) {
        if (name == null || arguments == null) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable((A) arguments.get(name));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime * result + ((arguments == null) ? 0 : arguments.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AttachmentStorageIdentifier other = (AttachmentStorageIdentifier) obj;
        if (identifier == null) {
            if (other.identifier != null) {
                return false;
            }
        } else if (!identifier.equals(other.identifier)) {
            return false;
        }
        if (arguments == null) {
            if (other.arguments != null) {
                return false;
            }
        } else if (!arguments.equals(other.arguments)) {
            return false;
        }
        return true;
    }

}
