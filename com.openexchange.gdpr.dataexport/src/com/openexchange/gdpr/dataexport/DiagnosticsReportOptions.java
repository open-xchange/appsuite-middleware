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

/**
 * {@link DiagnosticsReportOptions} - Options for diagnostic report for a GDPR data export run.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class DiagnosticsReportOptions {

    /**
     * Creates a new builder instance.
     *
     * @return The new builde rinstance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * The builder for an instance of <code>DiagnosticsReportOptions</code>.
     */
    public static class Builder {

        private boolean addDiagnosticsReport;
        private boolean considerPermissionDeniedErrors;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
            addDiagnosticsReport = false;
            considerPermissionDeniedErrors = false;
        }

        /**
         * Sets whether a diagnostics report is supposed to be added at all.
         *
         * @param addDiagnosticsReport <code>true</code> to create such a diagnostics report; otherwise <code>false</code>
         */
        public Builder withAddDiagnosticsReport(boolean addDiagnosticsReport) {
            this.addDiagnosticsReport = addDiagnosticsReport;
            return this;
        }

        /**
         * Specifies whether "permission denied" errors should be added to diagnostics report or not.
         *
         * @param considerPermissionDeniedErrors <code>true</code> to add "permission denied" errors; otherwise <code>false</code>
         */
        public Builder withConsiderPermissionDeniedErrors(boolean considerPermissionDeniedErrors) {
            this.considerPermissionDeniedErrors = considerPermissionDeniedErrors;
            return this;
        }

        /**
         * Builds the resulting instance of <code>DiagnosticsReportOptions</code> from this builder's arguments.
         *
         * @return The instance of <code>DiagnosticsReportOptions</code>
         */
        public DiagnosticsReportOptions build() {
            return new DiagnosticsReportOptions(addDiagnosticsReport, considerPermissionDeniedErrors);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final boolean addDiagnosticsReport;
    private final boolean considerPermissionDeniedErrors;
    private int hash;

    /**
     * Initializes a new {@link DiagnosticsReportOptions}.
     *
     * @param addDiagnosticsReport <code>true</code> to create such a diagnostics report; otherwise <code>false</code>
     * @param considerPermissionDeniedErrors <code>true</code> to add "permission denied" errors; otherwise <code>false</code>
     */
    DiagnosticsReportOptions(boolean addDiagnosticsReport, boolean considerPermissionDeniedErrors) {
        super();
        this.addDiagnosticsReport = addDiagnosticsReport;
        this.considerPermissionDeniedErrors = considerPermissionDeniedErrors;
        hash = 0;
    }

    /**
     * Checks whether a diagnostics report is supposed to be added at all.
     *
     * @return <code>true</code> to add "permission denied" errors; otherwise <code>false</code>
     */
    public boolean isAddDiagnosticsReport() {
        return addDiagnosticsReport;
    }

    /**
     * Checks whether "permission denied" errors should be added to diagnostics report or not.
     *
     * @return <code>true</code> to add "permission denied" errors; otherwise <code>false</code>
     */
    public boolean isConsiderPermissionDeniedErrors() {
        return considerPermissionDeniedErrors;
    }

    @Override
    public int hashCode() {
        int h = hash; // not thread-safe by intention
        if (h == 0) {
            int prime = 31;
            h = 1;
            h = prime * h + (addDiagnosticsReport ? 1231 : 1237);
            h = prime * h + (considerPermissionDeniedErrors ? 1231 : 1237);
            this.hash = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DiagnosticsReportOptions)) {
            return false;
        }
        DiagnosticsReportOptions other = (DiagnosticsReportOptions) obj;
        if (addDiagnosticsReport != other.addDiagnosticsReport) {
            return false;
        }
        if (considerPermissionDeniedErrors != other.considerPermissionDeniedErrors) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append("[addDiagnosticsReport=").append(addDiagnosticsReport);
        sb.append(", considerPermissionDeniedErrors=").append(considerPermissionDeniedErrors);
        sb.append(']');
        return sb.toString();
    }

}
