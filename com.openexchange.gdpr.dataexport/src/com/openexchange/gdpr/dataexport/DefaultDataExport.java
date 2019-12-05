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
