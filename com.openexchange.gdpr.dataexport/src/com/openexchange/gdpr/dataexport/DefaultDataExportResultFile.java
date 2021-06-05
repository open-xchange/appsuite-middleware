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
 * {@link DefaultDataExportResultFile}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DefaultDataExportResultFile implements DataExportResultFile {

    /**
     * Creates a new builder for an instance of <code>DefaultDataExportResultFile</code>.
     *
     * @return The builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>DefaultDataExportResultFile</code> */
    public static class Builder {

        private UUID taskId;
        private String contentType;
        private int number;
        private String fileName;
        private String fileStorageLocation;
        private long size;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
            contentType = "application/zip";
            size = -1L;
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
         * Sets the file name
         *
         * @param fileName The file name to set
         * @return This builder
         */
        public Builder withFileName(String fileName) {
            this.fileName = fileName;
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
        * Sets the content type, which is <code>"application/zip"</code> by default.
        *
        * @param contentType The content type to set
        * @return This builder
        */
       public Builder withContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * Creates the instance of <code>DefaultDataExportResultFile</code> from this builder's arguments
         *
         * @return The <code>DefaultDataExportResultFile</code> instance
         */
        public DefaultDataExportResultFile build() {
            return new DefaultDataExportResultFile(fileName, number, contentType, fileStorageLocation, size, taskId);
        }

    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    private final UUID taskId;
    private final String contentType;
    private final int number;
    private final String fileName;
    private final String fileStorageLocation;
    private final long size;

    /**
     * Initializes a new {@link DefaultDataExportResultFile}.
     *
     * @param taskId The task identifier
     * @param contentType The content type
     * @param number The package number
     * @param fileName The file name
     */
    DefaultDataExportResultFile(String fileName, int number, String contentType, String fileStorageLocation, long size, UUID taskId) {
        super();
        this.fileStorageLocation = fileStorageLocation;
        this.size = size;
        this.taskId = taskId;
        this.contentType = contentType;
        this.fileName = fileName;
        this.number = number;
    }

    @Override
    public String getFileStorageLocation() {
        return fileStorageLocation;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public UUID getTaskId() {
        return taskId;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

}
