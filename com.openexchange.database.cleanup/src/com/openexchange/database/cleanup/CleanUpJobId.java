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

package com.openexchange.database.cleanup;

import java.util.Objects;
import com.openexchange.java.Strings;

/**
 * {@link CleanUpJobId} - The identifier for a clean-up job.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class CleanUpJobId {

    /** The max. length for a clean-up job identifier */
    public static final int MAX_LENGTH = 191;

    /**
     * Creates a new clean-up identifier.
     *
     * @param identifier The job identifier
     * @return The new instance
     */
    public static CleanUpJobId newInstanceFor(String identifier) {
        return new CleanUpJobId(identifier);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final String identifier;
    private int hash = 0;

    /**
     * Initializes a new {@link CleanUpJobId}.
     *
     * @param identifier The job identifier
     */
    private CleanUpJobId(String identifier) {
        super();
        if (Strings.isEmpty(identifier)) {
            throw new IllegalArgumentException("Identifier must not be null or empty");
        }
        if (identifier.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Identifier length must not exceed " + MAX_LENGTH + " characters");
        }
        if (Strings.isNotAscii(identifier)) {
            throw new IllegalArgumentException("Identifier is required to only consist of ASCII characters");
        }
        this.identifier = identifier;
    }

    /**
     * Gets the job identifier.
     *
     * @return The job identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash == 0) {
            hash = Objects.hash(identifier);
            this.hash = hash;
        }
        return hash;
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
        return Objects.equals(identifier, ((CleanUpJobId) obj).identifier);
    }

    @Override
    public String toString() {
        return identifier;
    }

}
