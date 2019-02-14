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
