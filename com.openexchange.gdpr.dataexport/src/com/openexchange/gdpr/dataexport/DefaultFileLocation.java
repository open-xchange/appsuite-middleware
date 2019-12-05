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

package com.openexchange.gdpr.dataexport;

import java.util.UUID;

/**
 * {@link DefaultFileLocation}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DefaultFileLocation implements FileLocation {

    /**
     * Creates a new builder for an instance of <code>DefaultFileLocation</code>.
     *
     * @return The builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>DefaultFileLocation</code> */
    public static class Builder {

        private long size;
        private int number;
        private String fileStorageLocation;
        private UUID taskId;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
            size = -1;
        }

        /**
         * Sets the size
         *
         * @param size The size to set
         * @return This builder
         */
        public Builder withSize(long size) {
            this.size = size;
            return this;
        }

        /**
         * Sets the number
         *
         * @param number The number to set
         * @return This builder
         */
        public Builder withNumber(int number) {
            this.number = number;
            return this;
        }

        /**
         * Sets the fileStorageLocation
         *
         * @param fileStorageLocation The fileStorageLocation to set
         * @return This builder
         */
        public Builder withFileStorageLocation(String fileStorageLocation) {
            this.fileStorageLocation = fileStorageLocation;
            return this;
        }

        /**
         * Sets the task identifier
         *
         * @param taskId The task identifier to set
         * @return This builder
         */
        public Builder withTaskId(UUID taskId) {
            this.taskId = taskId;
            return this;
        }

        /**
         * Creates the instance of <code>DefaultFileLocation</code> from this builder's arguments
         *
         * @return The <code>DefaultFileLocation</code> instance
         */
        public DefaultFileLocation build() {
            return new DefaultFileLocation(fileStorageLocation, number, size, taskId);
        }

    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final String fileStorageLocation;
    private final int number;
    private final long size;
    private final UUID taskId;

    /**
     * Initializes a new {@link DefaultFileLocation}.
     *
     * @param fileStorageLocation The file storage location
     * @param number The number
     * @param size The size
     * @param taskId The task identifier
     */
    DefaultFileLocation(String fileStorageLocation, int number, long size, UUID taskId) {
        super();
        this.fileStorageLocation = fileStorageLocation;
        this.number = number;
        this.size = size;
        this.taskId = taskId;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getFileStorageLocation() {
        return fileStorageLocation;
    }

    @Override
    public UUID getTaskId() {
        return taskId;
    }

}
