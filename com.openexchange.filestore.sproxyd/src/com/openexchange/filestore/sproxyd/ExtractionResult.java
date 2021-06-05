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

package com.openexchange.filestore.sproxyd;

/**
 * {@link ExtractionResult} - The result for extracting association/path information from Swift URI.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ExtractionResult {

    private final int contextId;
    private final int userId;
    private final Association association;
    private final String prefix;
    private final String rawPrefix;

    /**
     * Initializes a new {@link ExtractionResult}.
     */
    public ExtractionResult(int userId, int contextId) {
        super();
        String prefix = contextId + "/" + userId;
        this.prefix = prefix;
        this.rawPrefix = prefix;
        this.userId = userId;
        this.contextId = contextId;
        this.association = Association.CONTEXT_AND_USER;
    }

    /**
     * Initializes a new {@link ExtractionResult}.
     */
    public ExtractionResult(String prefix, String rawPrefix) {
        super();
        this.prefix = prefix;
        this.rawPrefix = rawPrefix;
        contextId = -1;
        userId = -1;
        this.association = Association.CUSTOM;
    }

    /**
     * Checks if this extraction result has a context/user association
     *
     * @return <code>true</code> for context/user association; otherwise <code>false</code>
     */
    public boolean hasContextUserAssociation() {
        return Association.CONTEXT_AND_USER == association;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier or <code>-1</code> if not set
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier or <code>-1</code> if not set
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the association
     *
     * @return The association
     */
    public Association getAssociation() {
        return association;
    }

    /**
     * Gets the prefix
     *
     * @return The prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets the raw prefix
     *
     * @return The raw prefix
     */
    public String getRawPrefix() {
        return rawPrefix;
    }

}
