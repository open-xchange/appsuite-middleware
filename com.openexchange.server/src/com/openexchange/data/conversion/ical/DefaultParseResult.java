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

package com.openexchange.data.conversion.ical;

import java.util.Collections;
import java.util.List;


/**
 * {@link DefaultParseResult} - Default parse results.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class DefaultParseResult<T> implements ParseResult<T> {

    /**
     * Creates an empty parse result.
     *
     * @return An empty parse result
     */
    public static <T> DefaultParseResult<T> emptyParseResult() {
        return new DefaultParseResult<T>(Collections.<T> emptyList(), null);
    }

    /**
     * Creates a parse result for specified collection.
     *
     * @param objects The objects to wrap
     * @return The parse result for given collection
     */
    public static <T> DefaultParseResult<T> parseResultFor(List<T> objects) {
        return new DefaultParseResult<T>(objects, null);
    }

    /**
     * Creates a new builder instance
     *
     * @return The new builder instance
     */
    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    /** The builder for an instance of <code>DefaultParseResult</code> */
    public static class Builder<T> {

        private List<T> importedObjects;
        private TruncationInfo truncationInfo;

        Builder() {
            super();
        }

        public Builder<T> importedObjects(List<T> importedObjects) {
            this.importedObjects = importedObjects;
            return this;
        }

        public Builder<T> truncationInfo(TruncationInfo truncationInfo) {
            this.truncationInfo = truncationInfo;
            return this;
        }

        /** Builds the <code>DefaultParseResult</code> instance from this builder's arguments */
        public DefaultParseResult<T> build() {
            return new DefaultParseResult<>(importedObjects, truncationInfo);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private final List<T> importedObjects;
    private final TruncationInfo truncationInfo;

    /**
     * Initializes a new {@link DefaultParseResult}.
     */
    DefaultParseResult(List<T> importedObjects, TruncationInfo truncationInfo) {
        super();
        this.importedObjects = null == importedObjects ? Collections.<T> emptyList() : importedObjects;
        this.truncationInfo = truncationInfo;
    }

    @Override
    public List<T> getImportedObjects() {
        return importedObjects;
    }

    @Override
    public TruncationInfo getTruncationInfo() {
        return truncationInfo;
    }

}
