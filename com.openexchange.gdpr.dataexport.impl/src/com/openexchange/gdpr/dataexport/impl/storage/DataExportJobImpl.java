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

package com.openexchange.gdpr.dataexport.impl.storage;

import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.DataExportJob;
import com.openexchange.gdpr.dataexport.DataExportWorkItem;

/**
 * The job implementation.
 */
public class DataExportJobImpl implements DataExportJob {

    private final DataExportTask task;
    private final AbstractDataExportSql<?> sql;

    /**
     * Initializes a new {@link DataExportTaskJobImplementation}.
     *
     * @param task The task
     * @param sql The database access
     */
    DataExportJobImpl(DataExportTask task, AbstractDataExportSql<?> sql) {
        this.task = task;
        this.sql = sql;
    }

    @Override
    public Optional<DataExportWorkItem> getNextDataExportWorkItem() throws OXException {
        return Optional.ofNullable(sql.getNextWorkItem(task.getId(), task.getUserId(), task.getContextId()));
    }

    @Override
    public DataExportTask getDataExportTask() {
        return task;
    }
}