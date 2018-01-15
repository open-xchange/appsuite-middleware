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

package com.openexchange.groupware.upload.impl;


/**
 * {@link MaxSize} - The maximum allowed size for an upload request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MaxSize {

    /** The constant for unlimited upload */
    public static final MaxSize UNLIMITED = new MaxSize(-1L, Source.UPLOAD_LIMIT);

    /**
     * Creates a new builder instance
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builds an instance of <code>MaxSize</code> */
    public static class Builder {

        private long maxSize;
        private Source source;

        Builder() {
            super();
            source = Source.UPLOAD_LIMIT;
        }

        /**
         * Sets the source for maximum allowed size for an upload request.
         *
         * @param source The source
         * @return This builder
         */
        public Builder withSource(Source source) {
            this.source = source;
            return this;
        }

        /**
         * Sets maximum allowed size in bytes.
         *
         * @param maxSize The maximum allowed size
         * @return This builder
         */
        public Builder withMaxSize(long maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        /**
         * Creates the instance of <code>MaxSize</code> from this builder's arguments.
         *
         * @return The instance of <code>MaxSize</code>
         */
        public MaxSize build() {
            return new MaxSize(maxSize, source);
        }
    }

    // ---------------------------------------------------------------------------------------

    /** The source for maximum allowed size for an upload request. */
    public static enum Source {
        /**
         * The configured maximum allowed size in bytes for a complete upload request.
         */
        UPLOAD_LIMIT,
        /**
         * The available space of the storage to which the binary content is supposed to be saved.
         */
        STORAGE_LIMIT,
        ;
    }

    // ---------------------------------------------------------------------------------------

    private final long maxSize;
    private final Source source;

    /**
     * Initializes a new {@link MaxSize}.
     */
    MaxSize(long maxSize, Source source) {
        super();
        this.maxSize = maxSize;
        this.source = source;
    }

    /**
     * Gets the maximum allowed size in bytes for a complete upload request.
     *
     * @return The maximum allowed size in bytes
     */
    public long getMaxSize() {
        return maxSize;
    }

    /**
     * Gets the source for maximum allowed size for an upload request.
     *
     * @return The source
     */
    public Source getSource() {
        return source;
    }

}
