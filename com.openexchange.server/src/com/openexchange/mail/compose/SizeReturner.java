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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;

/**
 * {@link SizeReturner} - Provides the size for multiple attachments.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class SizeReturner {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>SizeReturner</code> */
    public static class Builder {

        private long size;
        private List<DataProvider> dataProviders;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
        }

        /**
         * Sets the size
         *
         * @param size The size to set
         * @return This builder
         */
        public Builder withSize(long size) {
            this.size = size < 0 ? 0 : size;
            return this;
        }

        /**
         * Adds specified size
         *
         * @param sizeToAdd The size to add
         * @return This builder
         */
        public Builder addSize(long sizeToAdd) {
            this.size += sizeToAdd < 0 ? 0 : sizeToAdd;
            return this;
        }

        /**
         * Adds given data provider for an attachment not specifying a concrete size in meta-data
         *
         * @param dataProvider The data provider to add
         * @return This builder
         */
        public Builder addDataProvider(DataProvider dataProvider) {
            if (null != dataProvider) {
                List<DataProvider> dataProviders = this.dataProviders;
                if (null == dataProviders) {
                    dataProviders = new ArrayList<>(4);
                    this.dataProviders = dataProviders;
                }
                dataProviders.add(dataProvider);
            }
            return this;
        }

        /**
         * Builds the resulting instance of <code>SizeReturner</code> from this builder's arguments.
         *
         * @return The <code>SizeReturner</code> instance
         */
        public SizeReturner build() {
            return new SizeReturner(size, dataProviders);
        }
    }

    private static final SizeReturner SIZE_RETURNER_0 = new SizeReturner(0, null);

    /**
     * Creates a size returner for specified total size.
     *
     * @param totalSize The total size
     * @return The size returner
     */
    public static SizeReturner sizeReturnerFor(long totalSize) {
        return totalSize <= 0 ? SIZE_RETURNER_0 : new SizeReturner(totalSize, null);
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private final long size;
    private final List<DataProvider> dataProviders;

    /**
     * Initializes a new {@link SizeReturner}.
     *
     * @param size The counted size so far
     * @param dataProviders The list of data providers for attachments not specifying a concrete size in meta-data
     */
    SizeReturner(long size, List<DataProvider> dataProviders) {
        super();
        this.size = size;
        this.dataProviders = dataProviders == null ? Collections.emptyList() : dataProviders;
    }

    /**
     * Gets the total size.
     *
     * @return The total size
     * @throws OXException If size cannot be returned; e.g. due to an I/O error
     */
    public long getTotalSize() throws OXException {
        if (dataProviders.isEmpty()) {
            return size;
        }

        try {
            long totalSize = size;
            for (DataProvider dataProvider : dataProviders) {
                totalSize += Streams.countInputStream(dataProvider.getData());
            }
            return totalSize;
        } catch (IOException e) {
            throw CompositionSpaceErrorCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the size of those attachments specifying a concrete size in meta-data
     *
     * @return The size
     */
    public long getSize() {
        return size;
    }

    /**
     * Checks if this size returner has any data provider
     *
     * @return <code>true</code> if there is any data provider; otherwise <code>false</code>
     */
    public boolean hasDataProviders() {
        return !dataProviders.isEmpty();
    }

    /**
     * Gets the data providers for those attachments <b>not</b> specifying a concrete size in meta-data
     *
     * @return The data providers
     */
    public List<DataProvider> getDataProviders() {
        return dataProviders;
    }

}
