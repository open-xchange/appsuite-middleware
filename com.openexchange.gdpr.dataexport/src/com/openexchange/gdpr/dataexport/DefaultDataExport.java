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

import java.util.Date;
import java.util.List;
import java.util.Optional;
import com.google.common.collect.ImmutableList;


/**
 * {@link DefaultDataExport}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DefaultDataExport implements DataExport {

    /**
     * Creates a new builder for an instance of <code>DefaultDataExport</code>.
     *
     * @return The builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>DefaultDataExport</code> */
    public static class Builder {

        private DataExportTask task;
        private List<DataExportResultFile> resultFiles;
        private Date availableUntil;

        /**
         * Sets the task
         *
         * @param task The task to set
         * @return This builder
         */
        public Builder withTask(DataExportTask task) {
            this.task = task;
            return this;
        }

        /**
         * Sets the date until the data export is available
         *
         * @param availableUntil The until date
         * @return This builder
         */
        public Builder withAvailableUntil(Date availableUntil) {
            this.availableUntil = availableUntil;
            return this;
        }

        /**
         * Sets the result files
         *
         * @param resultFiles The result files to set
         * @return This builder
         */
        public Builder withResultFiles(List<DataExportResultFile> resultFiles) {
            this.resultFiles = resultFiles;
            return this;
        }

        /**
         * Creates the instance of <code>DefaultDataExport</code> from this builder's arguments
         *
         * @return The <code>DefaultDataExport</code> instance
         */
        public DefaultDataExport build() {
            return new DefaultDataExport(resultFiles, task, availableUntil);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final List<DataExportResultFile> resultFiles;
    private final DataExportTask task;
    private final Date availableUntil;

    /**
     * Initializes a new {@link DefaultDataExport}.
     *
     * @param resultFiles The result files
     * @param task The task
     * @param availableUntil The until date
     */
    DefaultDataExport(List<DataExportResultFile> resultFiles, DataExportTask task, Date availableUntil) {
        super();
        this.availableUntil = availableUntil;
        this.resultFiles = resultFiles == null ? null : ImmutableList.copyOf(resultFiles);
        this.task = task;
    }

    @Override
    public Optional<Date> getAvailableUntil() {
        return Optional.ofNullable(availableUntil);
    }

    @Override
    public DataExportTask getTask() {
        return task;
    }

    @Override
    public Optional<List<DataExportResultFile>> getResultFiles() {
        return Optional.ofNullable(resultFiles);
    }

}
