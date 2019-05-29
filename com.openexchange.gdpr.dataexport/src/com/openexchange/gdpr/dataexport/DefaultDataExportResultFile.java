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
