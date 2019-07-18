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
 *    trademarks of the OX Software GmbH. group of companies.
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
        return knownArgument == null ? null : getArgument(knownArgument.getName());
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
