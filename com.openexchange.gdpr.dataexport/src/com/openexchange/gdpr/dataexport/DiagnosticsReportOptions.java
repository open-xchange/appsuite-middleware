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
