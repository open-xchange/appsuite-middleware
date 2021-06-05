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
