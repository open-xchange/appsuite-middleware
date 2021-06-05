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

import java.util.Optional;
import org.json.ImmutableJSONObject;
import org.json.JSONObject;

/**
 * {@link DataExportSavepoint} - Represents a save-point when export of a certain module was paused.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportSavepoint {

    /**
     * Creates a new builder for an instance of <code>DataExportSavepoint</code>.
     *
     * @return The builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>DataExportSavepoint</code> */
    public static class Builder {

        private JSONObject savepoint;
        private DataExportDiagnosticsReport report;
        private String fileStorageLocation;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
        }

        /**
         * Adds the message
         *
         * @param message The message to add
         * @return This builder
         */
        public Builder addMessage(Message message) {
            if (message == null) {
                return this;
            }

            if (report == null) {
                report = new DataExportDiagnosticsReport(DiagnosticsReportOptions.builder().build());
            }
            report.add(message);
            return this;
        }

        /**
         * Sets the report
         *
         * @param report The report to set
         * @return This builder
         */
        public Builder withReport(DataExportDiagnosticsReport report) {
            this.report = report;
            return this;
        }

        /**
         * Sets the save-point
         *
         * @param savepoint The save-point to set
         * @return This builder
         */
        public Builder withSavepoint(JSONObject savepoint) {
            this.savepoint = savepoint;
            return this;
        }


        /**
         * Sets the file storage location
         *
         * @param fileStorageLocation The file storage location to set
         * @return This builder
         */
        public Builder withFileStorageLocation(String fileStorageLocation) {
            this.fileStorageLocation = fileStorageLocation;
            return this;
        }

        /**
         * Creates the instance of <code>DataExportSavepoint</code> from this builder's arguments
         *
         * @return The <code>DataExportSavepoint</code> instance
         */
        public DataExportSavepoint build() {
            return new DataExportSavepoint(savepoint, fileStorageLocation, report);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    private final JSONObject savepoint;
    private final DataExportDiagnosticsReport report;
    private final String fileStorageLocation;

    /**
     * Initializes a new {@link DataExportSavepoint}.
     *
     * @param savepoint The save-point
     * @param fileStorageLocation The file storage location
     * @param report The report
     */
    DataExportSavepoint(JSONObject savepoint, String fileStorageLocation, DataExportDiagnosticsReport report) {
        super();
        this.fileStorageLocation = fileStorageLocation;
        this.savepoint = savepoint == null ? null : ImmutableJSONObject.immutableFor(savepoint);
        this.report = report == null ? new DataExportDiagnosticsReport(DiagnosticsReportOptions.builder().build()) : report;
    }

    /**
     * Gets the optional report for this save-point
     *
     * @return The optional report
     */
    public Optional<DataExportDiagnosticsReport> getReport() {
        return Optional.ofNullable(report);
    }

    /**
     * Gets the optional save-point data
     *
     * @return The save-point data
     */
    public Optional<JSONObject> getSavepoint() {
        return Optional.ofNullable(savepoint);
    }

    /**
     * Gets the optional file storage location
     *
     * @return The file storage location
     */
    public Optional<String> getFileStorageLocation() {
        return Optional.ofNullable(fileStorageLocation);
    }

    @Override
    public String toString() {
        return savepoint == null ? "null" : savepoint.toString();
    }

}
